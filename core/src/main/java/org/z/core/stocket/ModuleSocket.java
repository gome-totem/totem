package org.z.core.stocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.NamedThreadFactory;
import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.framing.Framedata;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.MessageBox;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Device;
import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.SocketConnectType;
import org.z.global.dict.Global.SocketDataType;
import org.z.global.environment.Config;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.factory.SocketSession;
import org.z.global.interfaces.ModuleSocketIntf;
import org.z.global.interfaces.SocketEvent;
import org.z.global.interfaces.SocketHandlerIntf;
import org.z.global.interfaces.WebSocketEvent;
import org.z.global.ip.IPSeeker;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.EncryptUtil;
import org.z.global.util.StringUtil;
import org.z.global.util.TextUtil;
import org.z.global.zk.ServiceName;
import org.z.store.beanstalk.Client;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataSet;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;


public class ModuleSocket implements ModuleSocketIntf {
	protected static Logger logger = LoggerFactory.getLogger(ModuleSocket.class);
	public static final int STREAM_IDLE_SECONDS = 90;
	public static final int WEBSOCKET_IDLE_SECONDS = 300;
	public static final int HTTP_IDLE_SECONDS = 60;
	private ServerBootstrap bootstrap = null;
	private boolean alive = false;
	protected int port = 0;
	protected boolean isAppServer = false;
	protected org.z.core.router.ModuleZeromq moduleRouter = null;
	protected org.z.core.app.ModuleZeromq moduleApp = null;
	public List<Draft> known_drafts = new ArrayList<Draft>();
	protected ExecutorService threadPool = null;
	protected int MaxSocketThreadCount = 30000;
	protected AtomicLong sessionCounter = new AtomicLong(0);
	public ConcurrentHashMap<Integer, SocketSession> channels = new ConcurrentHashMap<Integer, SocketSession>();
	public ConcurrentHashMap<Long, ArrayList<SocketSession>> users = new ConcurrentHashMap<Long, ArrayList<SocketSession>>();
	public ConcurrentHashMap<Long, ConcurrentHashMap<String, BasicDBObject>> userPeers = new ConcurrentHashMap<Long, ConcurrentHashMap<String, BasicDBObject>>();
	public Client smsClient = null;
	protected String smsKey = Config.rock().getItem("Sms-Key", "");
	protected String smsPassword = Config.rock().getItem("Sms-Password", "");
	public HashedWheelTimer timer = new HashedWheelTimer(10, TimeUnit.SECONDS);
	public SocketHandlerIntf socketHandler = null;
	protected BasicDBList defaultGroups = new BasicDBList();

	@Override
	public void afterCreate(Object[] params) {
		this.port = (Integer) params[0];
		this.isAppServer = this.port == Global.SocketAppPort;
		defaultGroups.add("最近联系");
		defaultGroups.add("订单确认中");
		defaultGroups.add("已经下单");
		defaultGroups.add("正在行程中");
		defaultGroups.add("活跃用户群");
		defaultGroups.add("潜在用户群");
	}

	@Override
	public boolean init(boolean isReload) {
		if (isReload == false) {
			known_drafts.add(new Draft_17());
			known_drafts.add(new Draft_10());
			known_drafts.add(new Draft_76());
			known_drafts.add(new Draft_75());
			ThreadFactory tf = new NamedThreadFactory("SocketPool");
			threadPool = new ThreadPoolExecutor(20, MaxSocketThreadCount, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), tf);
			try {
//				smsClient = new Client(smsKey, smsPassword);
			} catch (Exception e) {
				logger.info("init", e);
				return false;
			}
		}
		if (isAppServer) {
			moduleApp = (org.z.core.app.ModuleZeromq) ModuleFactory.moduleInstanceBy("mq");
			if (moduleApp == null) {
				logger.error("can't find mq module loaded in AppServer,please launch first.");
				return false;
			}
		} else {
			moduleRouter = (org.z.core.router.ModuleZeromq) ModuleFactory.moduleInstanceBy("mq");
			if (moduleRouter == null) {
				logger.error("can't find mq module loaded in Router,please launch first.");
				return false;
			}
		}
		ThreadFactory serverBossTF = new NamedThreadFactory("ModuleSocket-BOSS-");
		ThreadFactory serverWorkerTF = new NamedThreadFactory("ModuleSocket-WORKER-");
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(serverBossTF),
				Executors.newCachedThreadPool(serverWorkerTF)));
		final ModuleSocket self = this;
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = new DefaultChannelPipeline();
				pipeline.addLast("0", new ProtocolMultiplexerDecoder(self));
				return pipeline;
			}
		});
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		return bootstrap != null;
	}

	public void execute(Runnable command) {
		threadPool.execute(command);
	}

	@Override
	public void start(boolean isReload) {
		alive = true;
		try {
			Class<?> c = Class.forName(Const.SocketHandlerClassName);
			socketHandler = (SocketHandlerIntf) c.newInstance();
			socketHandler.afterCreate(this);
			logger.info("init SocketHandler ClassName[{}] success.", new Object[] { Const.SocketHandlerClassName });
		} catch (Exception e) {
			logger.error("init DesktopSocket[{}] fail.", new Object[] { Const.SocketHandlerClassName });
		}
		Channel acceptor = bootstrap.bind(new InetSocketAddress(port));
		if (port == Global.SocketRouterPort) {
			logger.info("Router Socket Server {} at [{}]", new Object[] { acceptor.isBound() ? "listening " : "fail", port });
		} else {
			logger.info("App Socket Server {} at [{}]", new Object[] { acceptor.isBound() ? "listening " : "fail", port });
		}
	}

	public void sendBye(ConcurrentHashMap<String, BasicDBObject> peers, long userId) {
		if (peers == null)
			return;
		BasicDBObject oSocketMsg = new BasicDBObject();
		oSocketMsg.append("action", "bye");
		oSocketMsg.append("userId", userId);
		oSocketMsg.append("offlineUserId", userId);
		for (Iterator<Entry<String, BasicDBObject>> i = peers.entrySet().iterator(); i.hasNext();) {
			Entry<String, BasicDBObject> entry = i.next();
			BasicDBObject oRecord = entry.getValue();
			oSocketMsg.append("toUserId", oRecord.getLong("userId"));
			oSocketMsg.append("toUserName", oRecord.getString("userName"));
			ZeroConnect.sendSocket(Global.DevelopMode == true ? "localhost" : oSocketMsg.getString("appserver"), oSocketMsg);
		}
	}

	public void removeChannel(Channel channel) {
		SocketSession session = channels.remove(channel.getId());
		if (session == null || session.userId == 0)
			return;
		ArrayList<SocketSession> sessions = users.get(session.userId);
		if (sessions == null)
			return;
		sessions.remove(session);
		if (sessions.size() == 0) {
			users.remove(session.userId);
			ConcurrentHashMap<String, BasicDBObject> peers = userPeers.remove(session.userId);
			sendBye(peers, session.userId);
		}
	}

	@Override
	public void stop() {
		alive = false;
		bootstrap.releaseExternalResources();
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public String getId() {
		return String.valueOf(bootstrap.hashCode());
	}

	@Override
	public BasicDBObject encryptSocket(String password, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("data", TextUtil.easyEncrypt(password, oReq.toString()));
		return oResult;
	}

	@Override
	public BasicDBObject encryptSocket(String password, String content) {
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("data", TextUtil.easyEncrypt(password, content));
		return oResult;
	}

	@Override
	public BasicDBObject decryptSocket(String password, String content) {
		content = TextUtil.easyDecrypt(password, content);
		content = EncryptUtil.decodeBASE64(content);
		try {
			return (BasicDBObject) JSON.parse(content);
		} catch (Exception e) {
			return null;
		}
	}

	public SocketHandlerIntf socketHandler() {
		return this.socketHandler;
	}

	@Override
	public boolean readOnline(String server, long userId) {
		if (Global.ServerName.equalsIgnoreCase(server)) {
			ArrayList<SocketSession> sessions = userBy(userId);
			return sessions != null && sessions.size() > 0;
		} else {
			BasicDBObject oSocketMsg = new BasicDBObject().append("action", "readOnline").append("userId", userId);
			BasicDBObject oResult = (BasicDBObject) ZeroConnect.sendSocket(Global.DevelopMode == true ? "localhost" : server, oSocketMsg);
			return oResult != null && oResult.getBoolean("online", false);
		}
	}

	public BasicDBObject dispatchSocket(BasicDBObject oSocket, ConcurrentHashMap<String, BasicDBObject> peer, BasicDBObject oSocketMsg) {
		peer.put(oSocket.getString("userId"), oSocket);
		oSocketMsg.append("toUserId", oSocket.getLong("userId"));
		oSocketMsg.append("toUserName", oSocket.getString("userName"));
		BasicDBObject oResult = null;
		if (Global.ServerName.equalsIgnoreCase(oSocket.getString("appserver"))) {
			ArrayList<SocketSession> sessions = userBy(oSocket.getLong("userId"));
			logger.info("userId[{}] has SessionsCount[{}]=",
					new String[] { oSocket.getString("userId"), String.valueOf(sessions == null ? 0 : sessions.size()) });
			int count = 0;
			for (int t = 0; sessions != null && t < sessions.size(); t++) {
				SocketSession s = sessions.get(t);
				count += sendMessage(s, oSocketMsg) ? 1 : 0;
			}
			oResult = DBObjectUtil.selectBy(oSocket, "userId", "userName").append("online", count != 0);
		} else {
			oResult = (BasicDBObject) ZeroConnect.sendSocket(Global.DevelopMode == true ? "localhost" : oSocket.getString("appserver"), oSocketMsg);
			oResult = DBObjectUtil.selectBy(oSocket, "userId", "userName").append("online", oResult.getBoolean("xeach", false));
		}
		return oResult;
	}

	public BasicDBList dispatch(String action, SocketSession session, BasicDBList sockets, BasicDBObject oMsgHeader, BasicDBObject oMsg) {
		String msgId = oMsgHeader.getString("_id");
		long customerId = oMsgHeader.getLong("customerId");
		BasicDBList oResults = new BasicDBList();
		BasicDBObject oSocketMsg = new BasicDBObject();
		oSocketMsg.putAll((BSONObject) oMsg);
		oSocketMsg.append("action", action);
		oSocketMsg.append("msgId", msgId);
		oSocketMsg.append("userId", session.userId);
		oSocketMsg.append("userName", session.userName);
		oSocketMsg.append("userServer", session.userServer);
		oSocketMsg.append("fromIP", session.ip);
		oSocketMsg.append("fromCity", IPSeeker.getCityByIP(session.ip));
		ConcurrentHashMap<String, BasicDBObject> peer = userPeers.get(session.userId);
		if (peer == null) {
			peer = new ConcurrentHashMap<String, BasicDBObject>();
			userPeers.put(session.userId, peer);
		}
		HashSet<String> socketUsers = new HashSet<String>();
		BasicDBObject oSocket = null;
		BasicDBObject oResult = null;
		int serviceCount = 0;
		for (int i = 0; i < sockets.size(); i++) {
			oSocket = (BasicDBObject) sockets.get(i);
			socketUsers.add(oSocket.getString("userId"));
			if (oSocket.getLong("userId") == session.userId) {
				continue;
			}
			try {
				oResult = dispatchSocket(oSocket, peer, oSocketMsg);
			} catch (Exception e) {
				logger.error("dispatch", e);
				continue;
			}
			boolean online = oResult.getBoolean("online", false);
			if (online == false) {
				MessageBox.unReadMsg(oSocket.getString("appserver"), oSocket.getLong("userId"), msgId, session.userId, System.currentTimeMillis(),
						oMsg.getString("content"));
			} else if (oSocket.getLong("userId") <= 100 && oSocket.getLong("userId") != customerId) {
				serviceCount++;
			}
			oResults.add(oResult);
		}
		if (serviceCount == 0) {
			String sql = "select userId,userName,appserver from user_socket where userId>=6 and userId<=100 and online=1 order by serviceCount limit 1";
			oSocket = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
			if (oSocket != null && socketUsers.contains(oSocket.getString("userId")) == false) {
				try {
					oSocket.append("r", 3);
					oSocket.append("w", 1);
					oSocket.append("state", true);
					if (addUserSocket(session, peer, msgId, sockets, oSocket)) {
						oResult = dispatchSocket(oSocket, peer, oSocketMsg);
						oResults.add(oResult);
					}
				} catch (Exception e) {
					logger.error("sendSocketBy", e);
				}
			}
		}
		return oResults;
	}

	public boolean addUserSocket(SocketSession session, ConcurrentHashMap<String, BasicDBObject> peer, String msgId, BasicDBList sockets, BasicDBObject oSocket) {
		BasicDBObject oMsgHeader = (BasicDBObject) RedisPool.use().getDBObject(msgId);
		if (oMsgHeader == null) {
			oMsgHeader = MessageBox.readHeader(session.userServer, session.userId, msgId);
		}
		if (oMsgHeader == null) {
			logger.error("can't  find msgHeader=" + msgId);
			return false;
		}
		String boxServer = oMsgHeader.getString("boxServer");
		long boxId = oMsgHeader.getLong("boxId");
		BasicDBObject oResult = null;
		oMsgHeader.removeField("_id");
		oMsgHeader.append("action", "hi");
		oMsgHeader.append("msgId", msgId);
		oMsgHeader.append("userId", oMsgHeader.getLong("customerId"));
		oMsgHeader.append("userName", oMsgHeader.getString("customerName"));
		try {
			oResult = dispatchSocket(oSocket, peer, oMsgHeader);
		} catch (Exception e) {
			logger.error("addUserSocket", e);
			return false;
		}
		if (oResult.getBoolean("online", false) == false) {
			logger.info("addUserSocket dispatchBy[{}] fail.", new String[] { oSocket.toString() });
			return false;
		}
		MessageBox.writeHeader(oSocket.getString("appserver"), oSocket.getLong("userId"), boxServer, boxId, msgId, oMsgHeader.getLong("customerId"),
				oMsgHeader.getString("customerName"), oMsgHeader.getLong("bizUserId"), oMsgHeader.getInt("catalog"), oMsgHeader.getInt("bizType"),
				oMsgHeader.getString("bizTitle"), oMsgHeader.getString("bizName"), oMsgHeader.getLong("shortId"));
		sockets.add(oSocket);
		MessageBox.updateSockets(boxServer, boxId, msgId, sockets);
		BasicDBObject oContact = socketHandler.readContact(oMsgHeader.getLong("customerId"));
		socketHandler.writeContact(oSocket.getString("appserver"), oSocket.getLong("userId"), oContact);
		return true;
	}

	public BasicDBObject readActivityBy(int catalog, int bizType, long shortId) {
		BizType type = BizType.values()[bizType];
		String sql = null;
		switch (type) {
		case car:
			sql = "select user_id as userId,user_name as userName from activity_car where short_id=?";
			break;
		case require:
			sql = "select user_id as userId,user_name as userName from activity_require where short_id=?";
			break;
		case trip:
			sql = "select user_id as userId,user_name as userName from activity_trip where short_id=?";
			break;
		default:
			return null;
		}
		BasicDBObject oResult = DataSet.queryDBObject(Const.defaultDictMysql, Const.defaultDictMysqlDB, sql, new String[] { String.valueOf(shortId) }, 0, 0);
		return oResult;
	}

	public BasicDBObject readContactBy(SocketSession session, long timestamp) {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select groups from user_contact where userId=?",
				new Long[] { session.userId }, 0, 0);
		String content = rows.length == 0 ? "" : rows[0][0];
		BasicDBObject oResult = new BasicDBObject();
		if (StringUtil.isEmpty(content)) {
			oResult.append("groups", this.defaultGroups);
		} else {
			oResult.append("groups", JSON.parse(content));
		}
		BasicDBObject oReq = new BasicDBObject().append("timestamp", new BasicDBObject().append("$gt", timestamp));
		BasicDBList items = DataCollection.findAll(session.userServer, Const.defaultMongoDB, "contact_" + session.userId, oReq, new BasicDBObject());
		oResult.append("items", items);
		return oResult;
	}

	public SocketSession newSession(Channel channel, SocketConnectType type) {
		SocketSession session = new SocketSession(channel, type);
		channels.put(channel.getId(), session);
		logger.info("create Session[{}] & SessionCount [{}]", new Object[] { session.channelId, channels.size() });
		return session;
	}

	public BasicDBObject checkSession(DBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		SocketSession session = this.channelBy(Integer.parseInt(oReq.get("session").toString()));
		if (session != null) {
			oResult.append("xeach", true);
		}
		return oResult;
	}

	public SocketSession channelBy(int id) {
		return channels.get(id);
	}

	@Override
	public ArrayList<SocketSession> userBy(long userId) {
		return users.get(userId);
	}

	public void addUserSession(SocketSession session) {
		if (session.userId == 0) {
			return;
		}
		ArrayList<SocketSession> sessions = users.get(session.userId);
		if (sessions == null) {
			sessions = new ArrayList<SocketSession>();
			users.put(session.userId, sessions);
		}
		if (sessions.indexOf(session) < 0) {
			sessions.add(session);
		}
	}

	protected boolean checkRangeTime(String dizTime, BasicDBObject oResult) {
		if (!StringUtil.isEmpty(dizTime)) {
			String[] dizTimes = dizTime.split("\\-");
			if (dizTimes.length == 2) {
				java.util.Calendar c = java.util.Calendar.getInstance();
				c.setTimeInMillis(System.currentTimeMillis());
				int hour = c.get(java.util.Calendar.HOUR_OF_DAY);
				if (StringUtils.isNumeric(dizTimes[0])) {
					oResult.append("limitTimeStart", Integer.parseInt(dizTimes[0]));
				}
				if (StringUtils.isNumeric(dizTimes[1])) {
					oResult.append("limitTimeStop", Integer.parseInt(dizTimes[1]));
				}
				if (oResult.containsField("limitTimeStart") && oResult.containsField("limitTimeStop")
						&& ((hour < oResult.getInt("limitTimeStart")) || (hour >= oResult.getInt("limitTimeStop")))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean sendMessage(SocketSession toSession, BasicDBObject oReq) {
		if (toSession.type == SocketConnectType.websocket) {
			return sendWebSocketMsg(toSession, oReq);
		} else {
			return sendSocketMsg(toSession, oReq);
		}
	}

	public boolean sendWebSocketMsg(SocketSession toSession, BasicDBObject oReq) {
		if (toSession == null || toSession.channel.isConnected() == false) {
			return false;
		}
		Draft d = (Draft) toSession.draft;
		List<Framedata> items = d.createFrames(oReq.toString(), true);
		for (Framedata b : items) {
			toSession.channel.write(d.createBinaryFrame(b));
		}
		return true;
	}

	public boolean sendSocketMsg(SocketSession toSession, BasicDBObject oReq) {
		if (toSession == null || toSession.channel.isConnected() == false) {
			return false;
		}
		String content = encryptSocket(toSession.authKey, oReq.toString()).toString();
		SocketResponse record = new SocketResponse(SocketDataType.string, content, false);
		toSession.channel.write(record);
		return true;
	}

	public BasicDBObject relayFromMQ(String message) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		BasicDBObject oMsg = (BasicDBObject) JSON.parse(message);
		String action = oMsg.getString("action");
		ArrayList<SocketSession> sessions = null;
		if (action.equalsIgnoreCase("readOnline")) {
			sessions = userBy(oMsg.getLong("userId"));
			return oResult.append("online", sessions != null && sessions.size() > 0);
		}
		sessions = userBy(oMsg.getLong("toUserId"));
		if (sessions == null) {
			return oResult.append("xeach", false).append("message", oMsg.getString("toUserId") + " offline.");
		}
		int onlineCount = 0;
		for (Iterator<SocketSession> t = sessions.iterator(); t.hasNext();) {
			SocketSession s = t.next();
			switch (s.type) {
			case websocket:
			case http:
				DBObjectUtil.deleteFields(oMsg, "mobile", "qq", "msn", "birthday");
				long timestamp = System.currentTimeMillis();
				oMsg.append("ago", StringUtil.timeDiff(timestamp));
				oMsg.append("timestamp", StringUtil.formatDateTime(timestamp));
				onlineCount += this.sendWebSocketMsg(s, oMsg) ? 1 : 0;
				break;
			default:
				oMsg.removeField("toUserId");
				oMsg.removeField("toUserName");
				onlineCount += this.sendSocketMsg(s, oMsg) ? 1 : 0;
				break;
			}
		}
		return oResult.append("xeach", onlineCount >= 1);
	}

	public void handle(SocketEvent event) {
		if (event.serviceIndex() == ServiceName.Desktop.ordinal()) {
			event.session().device = Device.DESKTOP;
			socketHandler.onData(event);
			return;
		}
		if (isAppServer) {
			moduleApp.createJobByAPI(event);
		} else {
			moduleRouter.createJobByAPI(event);
		}
	}

	public void registerWebSocketEvent(WebSocketEvent event) {
		if (isAppServer) {
			moduleApp.event.registerWebSocket(event);
		} else {
			moduleRouter.event.registerWebSocket(event);
		}
	}

	public void removeWebSocketEvent(long id) {
		if (isAppServer) {
			moduleApp.event.removeWebSocket(id);
		} else {
			moduleRouter.event.removeWebSocket(id);
		}
	}

	public BasicDBObject joinTalk(SocketSession session, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("action", "joinTalk").append("xeach", false);
		if (oReq.containsField("userName") == false || oReq.containsField("msgId") == false) {
			return oResult.append("message", "非法请求");
		}
		String msgId = oReq.getString("msgId");
		BasicDBObject oMsgHeader = MessageBox.readHeader(session.userServer, session.userId, msgId);
		if (oMsgHeader == null) {
			return oResult.append("message", "数据读取失败");
		}
		String boxServer = oMsgHeader.getString("boxServer");
		long boxId = Long.parseLong(oMsgHeader.getString("boxId"));
		BasicDBObject record = MessageBox.readSocketRecord(session.userId, boxServer, boxId, msgId);
		if (record == null) {
			return oResult.append("message", "用户队列读取失败");
		}
		BasicDBList oSockets = MessageBox.doFilterSockets(record, session.userId);
		if (oSockets.size() == 0) {
			return oResult.append("message", "过滤用户队列为空");
		}
		String userName = oReq.getString("userName");
		String sql = "select user_id as userId,server as appserver from user where user_name=?";
		String[][] rows = DataSet.query(Const.defaultDictMysql, Const.defaultDictMysqlDB, sql, new String[] { userName }, 0, 0);
		if (rows == null || rows.length == 0)
			return oResult.append("message", "用户名不存在");
		String userId = rows[0][0];
		String appServer = rows[0][1];
		BasicDBObject oSocket = null;
		for (int i = 0; i < oSockets.size(); i++) {
			oSocket = (BasicDBObject) oSockets.get(i);
			if (userId.equalsIgnoreCase(oSocket.getString("userId"))) {
				return oResult.append("message", "用户已经在列表中");
			}
		}
		oSocket = new BasicDBObject().append("userId", Long.parseLong(userId)).append("userName", userName).append("appserver", appServer).append("r", 1)
				.append("w", 3).append("state", true);
		oSockets.add(oSocket);
		MessageBox.writeHeader(oSocket.getString("appserver"), oSocket.getLong("userId"), oMsgHeader.getString("boxServer"), oMsgHeader.getLong("boxId"),
				msgId, oMsgHeader.getLong("customerId"), oMsgHeader.getString("customerName"), oMsgHeader.getLong("bizUserId"), oMsgHeader.getInt("catalog"),
				oMsgHeader.getInt("bizType"), oMsgHeader.getString("bizTitle"), oMsgHeader.getString("bizName"), oMsgHeader.getLong("shortId"));
		MessageBox.updateSockets(boxServer, boxId, msgId, oSockets);
		return oResult.append("xeach", true);
	}

	public BasicDBObject quitTalk(SocketSession session, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("action", "quitTalk").append("xeach", false);
		if (oReq.containsField("msgId") == false) {
			return oResult.append("message", "非法请求");
		}
		String msgId = oReq.getString("msgId");
		BasicDBObject oMsgHeader = MessageBox.readHeader(session.userServer, session.userId, msgId);
		if (oMsgHeader == null) {
			return oResult.append("message", "数据读取失败");
		}
		String boxServer = oMsgHeader.getString("boxServer");
		long boxId = Long.parseLong(oMsgHeader.getString("boxId"));
		BasicDBObject record = MessageBox.readSocketRecord(session.userId, boxServer, boxId, msgId);
		if (record == null) {
			return oResult.append("message", "用户队列读取失败");
		}
		BasicDBList oSockets = MessageBox.doFilterSockets(record, session.userId);
		if (oSockets.size() == 0) {
			return oResult.append("message", "过滤用户队列为空");
		}
		BasicDBObject oSocket = null;
		int index = -1;
		int serviceCount = 0;
		for (int i = 0; i < oSockets.size(); i++) {
			oSocket = (BasicDBObject) oSockets.get(i);
			if (Integer.parseInt(oSocket.getString("userId")) <= 100) {
				serviceCount++;
			}
			if (session.userId == oSocket.getLong("userId")) {
				index = i;
			}
		}
		if (index < 0) {
			return oResult.append("message", "不在对话列表中");
		}
		if ((serviceCount == 1) && (session.userId <= 100)) {
			return oResult.append("message", "对话必须存在一个客服人员");
		}
		oSockets.remove(index);
		MessageBox.removeHeader(session.userServer, session.userId, msgId);
		MessageBox.updateSockets(boxServer, boxId, msgId, oSockets);
		return oResult.append("xeach", true).append("userId", oReq.getString("userId"));
	}

	public BasicDBObject readMsgBody(SocketSession session, BasicDBObject oReq) {
		BasicDBObject oError = new BasicDBObject().append("action", "readMsgBody").append("xeach", false).append("message", "请求数据错误.");
		int scrollMode = oReq.getInt("scrollMode", 1);
		oError.append("userId", oReq.getString("userId")).append("scrollMode", scrollMode);
		if (oReq.containsField("msgId") == false || oReq.containsField("boxServer") == false || oReq.containsField("boxId") == false) {
			return oError;
		}
		String msgId = oReq.getString("msgId");
		String boxServer = oReq.getString("boxServer");
		long boxId = Long.parseLong(oReq.getString("boxId"));
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(msgId));
		BasicDBObject oReturn = new BasicDBObject().append("boxServer", 1).append("boxId", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(session.userServer, Const.defaultMongoDB, "msgheader_" + session.userId, qField, oReturn);
		if (one == null || one.getString("boxServer").equalsIgnoreCase(boxServer) == false || one.getLong("boxId") != boxId) {
			oError.append("message", "数据非法");
			return oError;
		}
		BasicDBObject record = MessageBox.readSocketRecord(session.userId, boxServer, boxId, msgId, "msgCount", "timestamp");
		if (record == null) {
			oError.append("message", "数据非法");
			return oError;
		}
		long timestamp = oReq.getLong("timestamp", 0);
		if (scrollMode == 1 && record.getLong("timestamp", 0) <= timestamp) {
			return oError.append("message", "没有新数据");
		}
		MessageBox.clearMsgHeader(session.userServer, session.userId, msgId);
		int level = record.getInt("r" + session.userId);
		BasicDBObject oResult = MessageBox.readMessages(boxServer, boxId, msgId, session.userId, level, scrollMode, timestamp);
		oResult.append("action", "readMsgBody");
		oResult.append("userId", oReq.getString("userId"));
		oResult.append("scrollMode", scrollMode);
		return oResult;
	}

	public BasicDBList readContactState(SocketSession session) {
		BasicDBList results = new BasicDBList();
		BasicDBList items = DataCollection.findAll(session.userServer, Const.defaultMongoDB, "contact_" + session.userId, new BasicDBObject());
		for (int i = 0; items != null && i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			String userId = oItem.getString("userId");
			String content = RedisPool.use().get(userId);
			BasicDBObject oResult = new BasicDBObject().append("userId", userId);
			results.add(oResult);
			if (StringUtil.isEmpty(content)) {
				oResult.append("online", false);
				continue;
			}
			oItem = (BasicDBObject) JSON.parse(content);
			oResult.append("online", oItem.getBoolean("online", false));
		}
		return results;
	}

	public void updateUserOnline(SocketSession session, boolean online) {
		if (session.userId == 0)
			return;
		BasicDBObject oSession = new BasicDBObject();
		oSession.append("online", online);
		oSession.append("userId", session.userId);
		oSession.append("userName", session.userName);
		oSession.append("server", session.userServer);
		oSession.append("callServer", session.callServer);
		RedisPool.use().set(String.valueOf(session.userId), oSession.toString());
		logger.info("server[{}] & userId=[{}] & userName=[{}] {}", new Object[] { session.userServer, session.userId, session.userName,
				online ? "online" : "offline" });
	}

	public void removeSession(SocketSession session, Channel channel) {
		if (channel != session.channel) {
			logger.info("session-channel Id[{}] not match channel Id[{}]", new Object[] { session.channel.getId(), channel.getId() });
		}
		if (session.device == Device.DESKTOP) {
			socketHandler.doLogout(session);
		} else {
			updateUserOnline(session, false);
		}
		removeChannel(channel);
		session.clear();
	}

	public static void main(String[] args) {
		String content = "{\"clientKey\":\"XIAOMING-PC@10.57.31.178\",\"action\":\"login\",\"userName\":\"\",\"phoneNumber\":\"3\",\"version\":\"1.76\",\"phonePassword\":\"jglyxyzs\",\"captcha\":\"x2kewc\"}";
		BasicDBObject oReq = (BasicDBObject) JSON.parse(content);
		System.out.println(oReq.toString());
	}
}
