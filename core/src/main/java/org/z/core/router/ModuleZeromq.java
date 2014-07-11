package org.z.core.router;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleZeromq.MessageSource;
import org.z.core.common.ModuleEvent;
import org.z.global.connect.ZeroSocket;
import org.z.global.connect.ZeroSocketFactory;
import org.z.global.dict.CompressMode;
import org.z.global.dict.Device;
import org.z.global.dict.Global;
import org.z.global.dict.Global.SocketDataType;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Const;
import org.z.global.interfaces.ModuleZeromqIntf;
import org.z.global.interfaces.SecurityIntf;
import org.z.global.interfaces.SocketEvent;
import org.z.global.util.StringUtil;
import org.z.global.util.ZeromqUtil;
import org.z.global.util.ZooUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeAction;
import org.z.global.zk.ServerDict.NodeType;
import org.z.global.zk.ServerNodes;
import org.z.global.zk.ZooKeeperWatchIntf;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


public class ModuleZeromq implements ModuleZeromqIntf, Runnable, ZooKeeperWatchIntf {

	protected ZContext ctx = null;
	protected ZMQ.Socket bindSocket = null;
	private ZMQ.Socket innerSocket = null;
	private ZMQ.Socket pushSocket = null;
	protected String name = null;
	protected boolean verbose = false;
	private boolean isAlive = false;
	protected BasicDBObject serverNode = new BasicDBObject();
	protected ServerNodes appServers = new ServerNodes();
	protected AtomicLong jobNo = new AtomicLong(0);
	protected Thread t = null;
	public ModuleEvent event = null;
	protected SecurityIntf security = null;
	protected HashSet<String> tags = new HashSet<String>();
	protected static Logger logger = LoggerFactory.getLogger(ModuleZeromq.class);

	public HashSet<String> tags() {
		tags.addAll(appServers.tags());
		return tags;
	}

	@Override
	public void afterCreate(Object[] params) {
	}

	@Override
	public boolean init(boolean isReload) {
		if (StringUtil.isEmpty(Const.SecurityClassName) == false) {
			try {
				Class<?> c = Class.forName(Const.SecurityClassName);
				security = (SecurityIntf) c.newInstance();
				logger.info("init SecurityClass[{}]", new Object[] { Const.SecurityClassName });
				tags.add(Global.DevelopMode ? "dict@" + Global.DevelopName : "dict");
			} catch (Exception e) {
				logger.error("init SecurityClass[{}] Fail.", new Object[] { Const.SecurityClassName });
			}
		}
		tags.add(Global.DevelopMode ? "server@" + Global.DevelopName : "server");
		name = Global.ServerName + "@Router";
		this.verbose = Global.DevelopMode;
		serverNode.append("developMode", Global.DevelopMode);
		serverNode.append("name", name);
		serverNode.append("id", name + StringUtil.currentTime());
		serverNode.append("ip", Global.ServerIP);
		serverNode.append("port", Global.MQRouterPort);
		serverNode.append("tags", tags());
		serverNode.append("appServerCount", 0);
		BasicDBList roles = new BasicDBList();
		serverNode.append("role", roles);
		if (Global.ServerIP.equalsIgnoreCase(Const.BroadCastIP)) {
			roles.add("broadcast");
		}
		ServerDict.self.appListener.addWather(this);
		event = new ModuleEvent();
		return true;
	}

	@Override
	public void start(boolean isReload) {
		isAlive = true;
		this.ctx = new ZContext();
		this.createSocket(isReload);
		t = new Thread(this);
		t.start();
	}

	private void createSocket(boolean isReload) {
		if (isReload == false) {
			bindSocket = ctx.createSocket(ZMQ.ROUTER);
			bindSocket.setIdentity(name.getBytes());
			if (Global.DevelopMode) {
				bindSocket.bind("tcp://*:" + Global.MQRouterPort);
			} else {
				bindSocket.bind("tcp://" + Global.ServerIP + ":" + Global.MQRouterPort);
			}
			pushSocket = ctx.createSocket(ZMQ.PUSH);
			pushSocket.setIdentity((this.name + "-push").getBytes());
			pushSocket.setSndHWM(10000000);
			pushSocket.bind("inproc://router-push");

			innerSocket = ctx.createSocket(ZMQ.DEALER);
			innerSocket.setIdentity((this.name + "-inner").getBytes());
			innerSocket.bind("inproc://router-inner");
		}
		registerServer();
	}

	public void registerServer() {
		ZooUtil.setPath(ServerDict.self.zoo(), "/routers/" + this.getId(), serverNode.toString(), CreateMode.EPHEMERAL);
		logger.info("Router  [{}] is active at  ServerIP={} & Port={} & PublisherPort={}", new Object[] { this.getId(), serverNode.getString("ip"), Global.MQRouterPort, Global.MQRouterPublishPort });
	}

	@Override
	public String getId() {
		return this.serverNode.getString("id");
	}

	@Override
	public String getRouterIP() {
		return this.serverNode.getString("ip");
	}

	@Override
	public String name() {
		return this.name;
	}

	protected void addAppServer(BasicDBObject oReq) {
		appServers.add(oReq);
		serverNode.append("appServerCount", serverNode.getInt("appServerCount") + 1);
		serverNode.append("tags", tags());
		ZooUtil.setPath(ServerDict.self.zoo(), "/routers/" + serverNode.getString("id"), serverNode.toString(), CreateMode.EPHEMERAL);
		logger.info("Register AppServer [{}] &Tag={} at Router=[{}] & TotalAppServerCount=[{}]",
				new Object[] { oReq.getString("id"), oReq.getString("tags"), this.getId(), serverNode.getInt("appServerCount") });
	}

	protected boolean hasAppServer(String id) {
		return appServers.contains(id);
	}

	protected void removeAppServer(String id) {
		if (appServers.remove(id) == false) {
			return;
		}
		int appCount = serverNode.getInt("appServerCount") - 1;
		appCount = appCount < 0 ? 0 : appCount;
		serverNode.append("appServerCount", appCount);
		serverNode.append("tags", tags());
		ZooUtil.setPath(ServerDict.self.zoo(), "/routers/" + serverNode.getString("id"), serverNode.toString(), CreateMode.EPHEMERAL);
		logger.info("Remove AppServer [{}] &TotalAppServerCount=[{}]", new Object[] { id, serverNode.getInt("appServerCount") });
	}

	protected void sendToClient(Socket instance, String clientAddr, MessageVersion version, String msgTag, BasicDBObject o) {
		this.sendToClient(instance, clientAddr, version, msgTag, StringUtil.toBytes(o.toString()));
	}

	protected void sendToClient(Socket instance, String clientAddr, MessageVersion version, String msgTag, byte[] content) {
		CompressMode mode = null;
		byte[] compressed = content;
		if (content.length > Global.DataPacketLimit) {
			mode = CompressMode.SNAPPY;
			compressed = StringUtil.compress(content);
		}
		if (compressed == content) {
			mode = CompressMode.NONE;
		}
		sendToClient(instance, clientAddr, version, msgTag, mode, compressed);
	}

	protected void sendToClient(Socket instance, String clientAddr, MessageVersion version, String msgTag, CompressMode mode, byte[] content) {
		if (MessageVersion.SOCKET == version) {
			SocketEvent event = this.event.removeSocket(clientAddr);
			if (event != null) {
				event.onMessage(SocketDataType.bytes, content, true);
			}
		} else {
			ZMsg msg = new ZMsg();
			msg.add(clientAddr);
			msg.add(ZeromqUtil.EMPTY);
			msg.add(ZeromqUtil.getBytes(msgTag));
			msg.add(mode.getData());
			msg.add(content);
			msg.send(instance, true);
		}
	}

	@Override
	public String allocateJobNo() {
		if (jobNo.get() >= Long.MAX_VALUE) {
			jobNo.set(0);
		}
		return String.valueOf(jobNo.addAndGet(1));
	}

	protected BasicDBObject hashAppServer(String tag) {
		return appServers.hashServer(NodeType.app, tag, null,null);
	}

	protected void fireEvent(ZFrame messageVersion, ZFrame messageTag, ZFrame sender, String content) {
		if (MessageVersion.SOCKET.ordinal() != Integer.parseInt(messageVersion.toString())) {
			return;
		}
		BasicDBObject oMsg = new BasicDBObject().append("@router", this.getId()).append("@app", content).append("msgTag", messageTag.toString());
		SocketEvent event = this.event.getSocket(sender.toString());
		if (event != null) {
			event.onMessage(SocketDataType.json, oMsg, true);
		}
	}

	protected void sendToAppServer(String serverId, ZFrame device, ZFrame service, ZFrame serviceIndex, ZFrame scope, ZFrame messageType, ZFrame messageVersion, ZFrame messageId, ZFrame messageTag,
			ZFrame messageTo, ZFrame timestamp, ZFrame sender, ZFrame compress, ZMsg m) {
		ZMsg msg = m.duplicate();
		msg.addFirst(compress.getData());
		msg.addFirst(timestamp.getData());
		msg.addFirst(messageTo.getData());
		msg.addFirst(messageTag.getData());
		msg.addFirst(messageId.getData());
		msg.addFirst(messageVersion.getData());
		msg.addFirst(messageType.getData());
		msg.addFirst(scope.getData());
		msg.addFirst(serviceIndex.getData());
		msg.addFirst(service.getData());
		msg.addFirst(device.getData());
		msg.addFirst(ZeromqUtil.EMPTY);
		msg.addFirst(sender.getData());
		msg.addFirst(serverId);
		msg.send(bindSocket, true);
	}

	public void createJobByAPI(SocketEvent event) {
		this.event.addSocket(event);
		ZeroSocket oSocket = null;
		String uri = "inproc://jobs-router-push";
		ZMsg msg = new ZMsg();
		try {
			oSocket = ZeroSocketFactory.get(uri, this.ctx, ZMQ.DEALER);
			if (oSocket == null || oSocket.isAlive == false) {
				return;
			}
			this.allocateJobNo();
			msg.add(String.valueOf(MessageSource.fromSocket.ordinal()));
			msg.add(Device.values()[event.device()].getData());
			msg.add(event.serviceName());
			msg.add(String.valueOf(event.serviceIndex()));
			msg.add(MessageScope.values()[event.messageScope()].getData());
			msg.add(MessageType.values()[event.messageType()].getData());
			msg.add(MessageVersion.values()[event.messageVersion()].getData());
			msg.add(event.messageId());
			msg.add(event.messageTag());
			msg.add(event.messageTo());
			msg.add(String.valueOf(event.timestamp()));
			msg.add(CompressMode.values()[event.compressMode()].getData());
			msg.add(event.content());
			msg.wrap(new ZFrame(String.valueOf(event.id())));
			msg.send(oSocket.socket);
		} catch (Exception e) {
			oSocket.isAlive = false;
			logger.error("createJobByAPI", e);
		} finally {
			ZeromqUtil.free(msg);
			ZeroSocketFactory.ret(uri, oSocket);
		}
	}

	private void createJob() {
		ZMsg msg = ZMsg.recvMsg(bindSocket);
		if (msg == null || msg.size() < 3) {
			msg.destroy();
			return;
		}
		msg.addFirst(String.valueOf(MessageSource.fromRouter.ordinal()));
		msg.send(pushSocket, false);
		msg.destroy();
	}

	public void dispatchJobResponse() {
		ZMsg msg = ZMsg.recvMsg(innerSocket);
		ZFrame c = msg.pop();
		if (c.getData().length == 0) {
			c.destroy();
			c = msg.pop();
		}
		int index = Integer.parseInt(c.toString());
		c.destroy();
		MessageSource type = MessageSource.values()[index];
		switch (type) {
		case fromRouter:
			break;
		case fromApp:
			msg.send(this.bindSocket);
			break;
		default:
			msg.send(this.bindSocket);
			break;
		}
		msg.destroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		ZMQ.Poller items = ctx.getContext().poller(2);
		items.register(bindSocket, ZMQ.Poller.POLLIN);
		items.register(innerSocket, ZMQ.Poller.POLLIN);
		try {
			logger.info("Router [{}]   is Runing with  Tag {}.", new Object[] { this.getId(), this.tags() });
			while (isAlive && !t.isInterrupted()) {
				if (items.poll(Const.HEARTBEAT_INTERVAL) < 1) {
					continue;
				}
				if (items.pollin(0)) {
					createJob();
				}
				if (items.pollin(1)) {
					dispatchJobResponse();
				}
			}
		} catch (Exception e) {
			isAlive = false;
			logger.error("run", e);
		}
		logger.info("Router [{}]   is Stopped with  Tag {}.", new Object[] { this.getId(), this.tags });
		stop();
	}

	@Override
	public void stop() {
		if (isAlive == false) {
			return;
		}
		logger.error("Router [{}] is stop at  ServerIP={}", new Object[] { this.getId(), serverNode.getString("ip") });
		isAlive = false;
		try {
			t.join();
			ctx.destroy();
		} catch (Exception e) {
			logger.error("stop", e);
		}
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public void reconnect() {

	}

	@Override
	public ZContext ctx() {
		return this.ctx;
	}

	@Override
	public void zooNodeChange(NodeType type, NodeAction action, String id, String ip) {
		if (type == NodeType.app && action == NodeAction.delete) {
			this.removeAppServer(id);
		}
	}


	public static void main(String[] args) {
	}

}
