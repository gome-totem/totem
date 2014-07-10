package org.z.core.app;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.ModuleEvent;
import org.z.global.connect.ZeroConnect;
import org.z.global.connect.ZeroSocket;
import org.z.global.connect.ZeroSocketFactory;
import org.z.global.dict.CompressMode;
import org.z.global.dict.Device;
import org.z.global.dict.Global;
import org.z.global.dict.Global.HashMode;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.SocketEvent;
import org.z.global.util.StringUtil;
import org.z.global.util.ZeromqUtil;
import org.z.global.util.ZooUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeAction;
import org.z.global.zk.ServerDict.NodeType;
import org.z.global.zk.ServiceName;
import org.z.global.zk.ZooKeeperWatchIntf;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ModuleZeromq implements ModuleIntf, Runnable, ZooKeeperWatchIntf {
	public static enum MessageSource {
		fromRouter, fromApp, fromSocket, fromSelf
	};
	protected String name = null;
	private ZMQ.Socket connSocket = null;
	private ZMQ.Socket bindSocket = null;
	private ZMQ.Socket pushSocket = null;
	private ZMQ.Socket jobSocket = null;
	protected boolean verbose = false;
	protected Thread t = null;
	protected BasicDBObject routerNode = null;
	protected BasicDBObject serverNode = new BasicDBObject();
	public ModuleEvent event = null;
	private boolean isAlive = false;
	protected ZContext ctx = new ZContext();
	private AtomicLong jobNo = new AtomicLong(0);
	protected static Logger logger = LoggerFactory.getLogger(ModuleZeromq.class);

	@Override
	public void afterCreate(Object[] params) {
	}

	@Override
	public boolean init(boolean isReload) {
		if (isReload == false) {
			this.event = new ModuleEvent();
			name = Global.ServerName + "@App";
			serverNode.append("id", name + StringUtil.currentTime());
			ServerDict.self.routerListener.addWather(this);
		}
		this.verbose = Global.DevelopMode;
		routerNode = ServerDict.self.hashServer(NodeType.router, String.valueOf(System.currentTimeMillis()));
		while (routerNode != null) {
			BasicDBObject oRouter = (BasicDBObject) ZeroConnect.readServerId(HashMode.router, routerNode.getString("ip"));
			if (oRouter == null || !oRouter.getString("id").equalsIgnoreCase(routerNode.getString("id"))) {
				ServerDict.self.removeRouter(routerNode.getString("id"));
				routerNode = ServerDict.self.hashServer(NodeType.router, String.valueOf(System.currentTimeMillis()));
				continue;
			} else {
				break;
			}
		}
		if (routerNode == null) {
			logger.error("AppServer [{}] can't find Router.", new String[] { Global.ServerName });
			return false;
		}
		return true;
	}

	public void createSockets(boolean isReload) {
		if (isReload == false) {
			bindSocket = ctx.createSocket(ZMQ.ROUTER);
			bindSocket.setIdentity((this.name + "@BIND").getBytes());
			bindSocket.bind("tcp://" + Global.ServerIP + ":" + Global.MQAppServerPort);

			pushSocket = ctx.createSocket(ZMQ.PUSH);
			pushSocket.setIdentity((this.name + "@JOB").getBytes());
			pushSocket.setSndHWM(1000000);
			pushSocket.bind("inproc://jobs-push");

			jobSocket = ctx.createSocket(ZMQ.DEALER);
			jobSocket.setIdentity((this.name + "@DISPATCH").getBytes());
			jobSocket.bind("inproc://jobs-app");
		}
		connSocket = ctx.createSocket(ZMQ.DEALER);
		if (routerNode != null) {
			connSocket.setIdentity(this.getId().getBytes());
			connSocket.connect("tcp://" + routerNode.getString("ip") + ":" + Global.MQRouterPort);
			this.registerServer(null);
		}
	}

	public void registerServer(String routerId) {
		if (StringUtil.isEmpty(routerId) == false && !routerId.equalsIgnoreCase(routerNode.getString("id"))) {
			logger.info("AppServer [{}] is crash ,because connected RouterId={}not match retured RouterId[{}] .\n", new Object[] { routerNode.getString("id"), routerId });
			System.exit(-1);
		}
		if (routerId == null) {
			serverNode.append("tags", ModuleFactory.loadedModules);
			serverNode.append("port", Global.MQAppServerPort);
			serverNode.append("ip", Global.ServerIP);
			serverNode.append("routerId", routerNode.getString("id"));
			BasicDBList roles = new BasicDBList();
			serverNode.append("role", roles);
			if (Global.ServerIP.equalsIgnoreCase(Const.ApiAppServer)) {
				roles.add("api");
			}
			registerToRouter();
			logger.info("AppServer [{}] is connecting to Router={} ", new Object[] { this.getId(), routerNode.getString("id") });
		} else {
			ZooUtil.setPath(ServerDict.self.zoo(), "/appservers/" + this.getId(), serverNode.toString(), CreateMode.EPHEMERAL);
			logger.info("AppServer [{}] is connected to Router={} and listening Port [{}].\n", new Object[] { this.getId(), routerNode.getString("id"), Global.MQAppServerPort });
		}
	}

	protected String allocateJobNo() {
		if (jobNo.get() >= Long.MAX_VALUE) {
			jobNo.set(0);
		}
		return String.valueOf(jobNo.addAndGet(1));
	}

	private void registerToRouter() {
		ZMsg msg = new ZMsg();
		msg.add("");
		msg.add(Device.SERVER.getData());
		msg.add(ServiceName.Server.getData());
		msg.add(String.valueOf(ServiceName.Server.ordinal()));
		msg.add(MessageScope.ROUTER.getData());
		msg.add(MessageType.REGISTER.getData());
		msg.add(MessageVersion.MQ.getData());
		msg.add(ZeromqUtil.MsgId);
		msg.add(ZeromqUtil.MsgTag);
		msg.add(ZeromqUtil.MsgTo);
		msg.add(String.valueOf(System.currentTimeMillis()));
		CompressMode compress = null;
		byte[] bytes = StringUtil.toBytes(serverNode.toString());
		byte[] compressed = bytes;
		if (bytes.length > Global.DataPacketLimit) {
			compress = CompressMode.SNAPPY;
			compressed = StringUtil.compress(bytes);
		}
		if (compressed == bytes) {
			compress = CompressMode.NONE;
		}
		msg.add(compress.getData());
		msg.add(compressed);
		msg.send(connSocket, true);
	}

	private void createJobByConnect() {
		ZMsg job = ZMsg.recvMsg(connSocket);
		if (job == null || job.size() < 3) {
			job.destroy();
			return;
		}
		job.addFirst(String.valueOf(MessageSource.fromRouter.ordinal()));
		job.send(pushSocket, false);
		job.destroy();
	}

	private void createJobByBind() {
		ZMsg job = ZMsg.recvMsg(bindSocket);
		if (job == null || job.size() < 3) {
			job.destroy();
			return;
		}
		job.addFirst(String.valueOf(MessageSource.fromApp.ordinal()));
		job.add(this.allocateJobNo());
		job.send(pushSocket, false);
		job.destroy();
	}

	public void createJobByAPI(SocketEvent event) {
		this.event.addSocket(event);
		ZMsg msg = new ZMsg();
		try {
			msg.addFirst(String.valueOf(MessageSource.fromSocket.ordinal()));
			msg.add(String.valueOf(event.id()));
			msg.add(ZeromqUtil.EMPTY);
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
			msg.add(this.allocateJobNo());
			msg.send(this.pushSocket);
		} finally {
			ZeromqUtil.free(msg);
		}
	}

	public void dispatchJobMsg() {
		ZMsg msg = ZMsg.recvMsg(jobSocket);
		ZFrame tag = msg.pop();
		if (tag.getData().length == 0) {
			tag.destroy();
			tag = msg.pop();
		}
		int index = Integer.parseInt(tag.toString());
		tag.destroy();
		MessageSource type = MessageSource.values()[index];
		switch (type) {
		case fromRouter:
			msg.send(connSocket, false);
			break;
		case fromApp:
			msg.send(bindSocket, false);
			break;
		case fromSocket:
			break;
		case fromSelf:
			changeRouter(msg);
			break;
		}
		msg.destroy();
	}

	private void changeRouter(ZMsg msg) {
		String clientAddr = msg.popString();
		System.out.println(clientAddr);
	}

	@Override
	public void start(boolean isReload) {
		isAlive = true;
		createSockets(isReload);
		t = new Thread(this);
		t.start();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void run() {
		ZMQ.Poller items = ctx.getContext().poller(3);
		items.register(bindSocket, ZMQ.Poller.POLLIN);
		items.register(connSocket, ZMQ.Poller.POLLIN);
		items.register(jobSocket, ZMQ.Poller.POLLIN);
		try {
			logger.info("AppServer [{}]   is Runing with  Tag {}.", new Object[] { this.getId(), ModuleFactory.loadedModules });
			while (isAlive && !t.isInterrupted()) {
				if (items.poll(Const.HEARTBEAT_INTERVAL) < 1) {
					continue;
				}
				if (items.pollin(0)) {
					createJobByBind();
				}
				if (items.pollin(1)) {
					createJobByConnect();
				}
				if (items.pollin(2)) {
					dispatchJobMsg();
				}
			}
		} catch (Exception e) {
			isAlive = false;
			logger.error("run", e);
		}
		logger.info("AppServer [{}]   is Stopped with  Tag {}.", new Object[] { this.getId(), ModuleFactory.loadedModules });
	}

	@Override
	public void stop() {
		if (isAlive == false) {
			return;
		}
		this.jobNo.set(0);
		ZooUtil.delete(ServerDict.self.zoo(), "/appservers/" + this.getId());
		logger.error("AppServer [{}] is stopping at  ServerIP={}", new Object[] { this.getId(), Global.ServerIP });
		isAlive = false;
		try {
			t.join();
			ctx.destroySocket(connSocket);
		} catch (Exception e) {
			logger.error("stop", e);
		}
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public String getId() {
		return serverNode.getString("id");
	}

	public void dispatch(ServiceName service, MessageScope msgScope, MessageType msgType, String msgTag, MessageVersion version, long timestamp, String content) {
		ZMsg msg = new ZMsg();
		msg.add(Device.SERVER.getData());
		msg.add(service.getData());
		msg.add(String.valueOf(service.ordinal()));
		msg.add(msgScope.getData());
		msg.add(msgType.getData());
		msg.add(version.getData());
		msg.add(ZeromqUtil.getBytes(StringUtil.createUniqueID()));
		msg.add(ZeromqUtil.getBytes(msgTag));
		msg.add(ZeromqUtil.MsgTo);
		msg.add(String.valueOf(timestamp));
		byte[] bytes = StringUtil.toBytes(content);
		if (bytes.length >= Global.DataPacketLimit) {
			bytes = StringUtil.compress(bytes);
			msg.add(CompressMode.SNAPPY.getData());
		} else {
			msg.add(CompressMode.NONE.getData());
		}
		msg.add(bytes);
		BasicDBList servers = null;
		if (msgScope == MessageScope.ALLROUTER) {
			servers = ServerDict.self.routers();
		} else {
			servers = ServerDict.self.apps();
		}
		if (servers == null) {
			logger.error("dispatch fail,servers is null");
			return;
		}
		for (int i = 0; i < servers.size(); i++) {
			BasicDBObject server = (BasicDBObject) servers.get(i);
			ZeroSocket oSocket = null;
			String uri = "tcp://" + server.getString("ip") + ":";
			switch (msgScope) {
			case ALLROUTER:
			case BROADCAST:
			case ROUTER:
				uri += Global.MQRouterPort;
				break;
			case ALLAPP:
			case APP:
				uri += Global.MQAppServerPort;
				break;
			}
			String returnTag = "";
			oSocket = ZeroSocketFactory.get(uri, null, ZMQ.REQ);
			ZMsg returnMsg = null;
			try {
				try {
					msg.send(oSocket.socket, false);
//					logger.info("dispatch uri[{}] &  id[{}] & msgTag[{}]", new Object[] { uri, oSocket.id, msgTag });
					returnMsg = ZMsg.recvMsg(oSocket.socket);
					if (!returnMsg.isEmpty() && returnMsg.getFirst().hasData()) {
						ZFrame v = returnMsg.pop();
						returnTag = StringUtil.toString(v.getData());
						v.destroy();

						v = returnMsg.pop();
						v.destroy();

						v = returnMsg.pop();
						v.destroy();
						if (returnTag.equalsIgnoreCase(msgTag) == false) {
							logger.warn("ReturnTag=[{}] not match[{}]", new Object[] { returnTag, msgTag });
						}
					}
				} finally {
					ZeromqUtil.free(returnMsg);
					ZeroSocketFactory.ret(uri, oSocket);
				}
				if (!returnTag.equalsIgnoreCase(msgTag)) {
					logger.warn(" MsgTag={} not match ReturnTag={},MsgType=[{}] & ServiceName=[{}]\n Require Content=[{}]", new Object[] { msgTag, returnTag, msgTag, service.name(), content });
				}
			} catch (Exception e) {
				oSocket.isAlive = false;
				logger.error("dispatch[{}],exception:[{}]", new Object[] { uri, e.getLocalizedMessage() });
			}
		}
		ZeromqUtil.free(msg);
	}

	public void dispatchBroadCast(ServiceName service, MessageType msgType, String msgTag, String content) {
		ZMsg msg = new ZMsg();
		msg.add(Device.SERVER.getData());
		msg.add(service.getData());
		msg.add(String.valueOf(service.ordinal()));
		msg.add(MessageScope.BROADCAST.getData());
		msg.add(msgType.getData());
		msg.add(MessageVersion.MQ.getData());
		msg.add(ZeromqUtil.getBytes(StringUtil.createUniqueID()));
		msg.add(ZeromqUtil.getBytes(msgTag));
		msg.add(ZeromqUtil.MsgTo);
		msg.add(String.valueOf(System.currentTimeMillis()));
		byte[] bytes = StringUtil.toBytes(content);
		if (bytes.length >= Global.DataPacketLimit) {
			bytes = StringUtil.compress(bytes);
			msg.add(CompressMode.SNAPPY.getData());
		} else {
			msg.add(CompressMode.NONE.getData());
		}
		msg.add(bytes);
		ZeroSocket oSocket = null;
		String uri = "tcp://" + Global.BroadCastIP + ":" + Global.MQRouterPort;
		oSocket = ZeroSocketFactory.get(uri, null, ZMQ.REQ);
		ZMsg returnMsg = null;
		try {
			msg.send(oSocket.socket, false);
			logger.info("dispatch uri[{}] &  id[{}] & msgTag[{}]", new Object[] { uri, oSocket.id, msgTag });
			returnMsg = ZMsg.recvMsg(oSocket.socket);
			if (!returnMsg.isEmpty() && returnMsg.getFirst().hasData()) {
				ZFrame v = returnMsg.pop();
				v.destroy();
				v = returnMsg.pop();
				v.destroy();
				v = returnMsg.pop();
				v.destroy();
			}
		} catch (Exception e) {
			oSocket.isAlive = false;
			logger.error("dispatch[{}],exception:[{}]", new Object[] { uri, e.getLocalizedMessage() });
		} finally {
			ZeromqUtil.free(returnMsg);
			ZeromqUtil.free(msg);
			ZeroSocketFactory.ret(uri, oSocket);
		}
	}

	public void restart() {
		this.stop();
		if (this.init(true) == false) {
			logger.error("AppServer crash.");
			System.exit(-1);
		}
		this.start(true);
		ModuleIntf subscribe = ModuleFactory.moduleInstanceBy("subscribe");
		subscribe.stop();
		subscribe.start(true);
	}

	@Override
	public void zooNodeChange(NodeType type, NodeAction action, String id, String ip) {
		if (this.isAlive == false) {
			return;
		}
		try {
			logger.info("nodeType=[{}] Action=[{}] id=[{}]", new Object[] { type.name(), action.name(), id });
			if (type == NodeType.router && action == NodeAction.delete && this.routerNode.getString("id").equalsIgnoreCase(id)) {
				this.restart();
			}
		} catch (Exception e) {
			logger.info("zooNodeChange", e);
		}

	}

	@Override
	public void reconnect() {
		registerServer(null);
	}

	public static void main(String[] args) {
		int mode = 1;
		switch (mode) {
		case 0:
			ModuleZeromq mq = new ModuleZeromq();
			BasicDBObject oReq = new BasicDBObject();
			oReq.append("sku", "zzx");
			mq.dispatch(ServiceName.ProductIndex, MessageScope.ALLROUTER, MessageType.UPDATE, "product", MessageVersion.MQ, System.currentTimeMillis(), oReq.toString());
			break;

		}
		System.out.println("done!");
	}


}
