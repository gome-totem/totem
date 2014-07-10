package org.z.core.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleZeromq.MessageSource;
import org.z.core.queue.ModuleMsg;
import org.z.global.connect.ZeroSocket;
import org.z.global.connect.ZeroSocketFactory;
import org.z.global.dict.CompressMode;
import org.z.global.dict.Device;
import org.z.global.dict.Global;
import org.z.global.dict.Global.CodeType;
import org.z.global.dict.Global.ModuleMessageType;
import org.z.global.dict.Global.SocketDataType;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Config;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.OnModuleMsgIntf;
import org.z.global.interfaces.SocketEvent;
import org.z.global.util.JVMUtil;
import org.z.global.util.StringUtil;
import org.z.global.util.ZeromqUtil;
import org.z.global.util.ZooUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServiceName;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ModuleJobPull implements ModuleIntf {
	private int threadCount = 0;
	protected ModuleZeromq mq = null;
	protected static Logger logger = LoggerFactory.getLogger(JobPull.class);

	public class JobPull implements Runnable {
		protected ModuleJobPull module;
		protected ZMQ.Socket socketPullApp = null;
		protected ZMQ.Socket socketPullBroadcast = null;
		private boolean alive = true;
		private int index = 0;
		private Thread t = null;

		public JobPull(ModuleJobPull module, int index) {
			this.module = module;
			this.index = index;
			createSocket();
			logger.info("App-JobPull Thread[{}] Start.", new Object[] { this.index });
		}

		public void createSocket() {
			socketPullApp = this.module.mq.ctx.createSocket(ZMQ.PULL);
			socketPullApp.setIdentity(StringUtil.toBytes("jobs-push@" + this.index));
			socketPullApp.connect("inproc://app-push");
			socketPullBroadcast = this.module.mq.ctx.createSocket(ZMQ.PULL);
			socketPullBroadcast.setIdentity(StringUtil.toBytes("broadcast-pull@" + this.index));
			socketPullBroadcast.connect("inproc://broadcast-app-push");
		}

		public void attach(Thread t) {
			this.t = t;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			ZMQ.Poller items = this.module.mq.ctx.getContext().poller(2);
			items.register(socketPullApp, ZMQ.Poller.POLLIN);
			items.register(socketPullBroadcast, ZMQ.Poller.POLLIN);
			while (alive && !t.isInterrupted()) {
				ZMsg msg = null;
				if (items.poll(Const.HEARTBEAT_INTERVAL) < 1) {
					continue;
				}
				if (items.pollin(0)) {
					msg = ZMsg.recvMsg(socketPullApp);
				} else if (items.pollin(1)) {
					msg = ZMsg.recvMsg(socketPullBroadcast);
				}
				ZFrame c = msg.pop();
				final MessageSource messageFrom = MessageSource.values()[Integer.parseInt(c.toString())];
				c.destroy();
				ZFrame sender = msg.pop();
				ZFrame empty = sender.size() == 0 ? null : msg.pop();
				ZFrame device = msg.pop();
				ZFrame serviceName = msg.pop();
				ZFrame serviceIndex = msg.pop();
				ZFrame scope = msg.pop();
				ZFrame messageType = msg.pop();
				ZFrame messageVersion = msg.pop();
				MessageVersion version = MessageVersion.values()[Integer.parseInt(messageVersion.toString())];
				ZFrame messageId = msg.pop();
				ZFrame messageTag = msg.pop();
				ZFrame messageTo = msg.pop();
				ZFrame timestamp = msg.pop();
				ZFrame compress = msg.pop();
				BasicDBObject oResult;
				ServiceName service = ServiceName.values()[Integer.parseInt(serviceIndex.toString())];
				MessageType msgType = MessageType.values()[Integer.parseInt(messageType.toString())];
				try {
					c = msg.pop();
					byte[] bytes = c.getData();
					c.destroy();
					if (Integer.parseInt(compress.toString()) == CompressMode.SNAPPY.ordinal()) {
						bytes = StringUtil.uncompress(bytes);
					}
					String content = StringUtil.toString(bytes);
					switch (service) {
					case Server:
						switch (msgType) {
						case REGISTER:
							if (!StringUtil.isEmpty(content)) {
								mq.registerServer(content);
							}
							break;
						case OFFLINE:
							switchTo(false, StringUtil.toString(sender.getData()), version, messageTag);
							break;
						case ONLINE:
							switchTo(true, StringUtil.toString(sender.getData()), version, messageTag);
							break;
						case READ:
							String msgTag = StringUtil.toString(messageTag.getData());
							if (msgTag.equalsIgnoreCase("runtime")) {
								content = JVMUtil.readRuntime().append("jobCount", Integer.parseInt(this.module.mq.allocateJobNo())).toString();
							}
							sendJob(packClientMsg(StringUtil.toString(sender.getData()), version, messageTag.getData(), content));
							break;
						case UPDATE:
							changeRouter(StringUtil.toString(sender.getData()), version, messageTag.getData(), content);
							break;
						case SOCKET:
							handleSocket(StringUtil.toString(sender.getData()), version, messageTag.getData(), content);
							break;
						case FACET:
							handleFacet(StringUtil.toString(sender.getData()), version, messageTag, content);
							break;
						default:
							logger.info("not handle for [{}]", new String[] { content });
							break;
						}
						break;
					default:
						oResult = new BasicDBObject().append("server", this.module.mq.getId());
						oResult.append("@from", messageFrom.name()).append("@thread", this.index);
						MessageScope msgScope = MessageScope.values()[Integer.parseInt(scope.toString())];
						DBObject oReq = (DBObject) JSON.parse(content);
						BasicDBObject oResponse = ModuleFactory.processor().execute(StringUtil.toString(sender.getData()), Integer.parseInt(device.toString()),
								Integer.parseInt(serviceIndex.toString()), msgScope, Integer.parseInt(messageType.toString()),
								Integer.parseInt(messageVersion.toString()), StringUtil.toString(messageId.getData()), StringUtil.toString(messageTag.getData()), StringUtil.toString(messageTo.getData()),
								Long.parseLong(timestamp.toString()), oReq);
						oResult.append("@time", System.currentTimeMillis() - Long.parseLong(timestamp.toString()));
						oResult.append("state", true);
						oResult.append("response", oResponse);
						switch (messageFrom) {
						case fromRouter:
							sendJob(packRouterMsg(Device.values()[Integer.parseInt(device.toString())], ServiceName.Server, msgScope, MessageType.REPLY,
									version, StringUtil.toString(messageId.getData()), StringUtil.toString(messageTag.getData()), StringUtil.toString(messageTo.getData()), Long.parseLong(timestamp.toString()),
									StringUtil.toBytes(oResult.toString()), StringUtil.toString(sender.getData())));
							break;
						case fromApp:
							sendJob(packClientMsg(StringUtil.toString(sender.getData()), version, messageTag.getData(), oResult.toString()));
							break;
						case fromSocket:
							this.module.sendSocketEvent(StringUtil.toString(sender.getData()), oResult);
							break;
						}
						break;
					}
				} catch (Exception e) {
					logger.error("JobPull", e);
				}
				ZeromqUtil.free(empty);
				ZeromqUtil.free(sender);
				ZeromqUtil.free(device);
				ZeromqUtil.free(serviceName);
				ZeromqUtil.free(serviceIndex);
				ZeromqUtil.free(scope);
				ZeromqUtil.free(messageType);
				ZeromqUtil.free(messageVersion);
				ZeromqUtil.free(messageTag);
				ZeromqUtil.free(timestamp);
				ZeromqUtil.free(compress);
				ZeromqUtil.free(msg);
			}
			stop();
		}

		public void stop() {
			alive = false;
			try {
				t.join();
				if (socketPullApp != null) {
					this.module.mq.ctx.destroySocket(socketPullApp);
				}
				if (socketPullBroadcast != null) {
					this.module.mq.ctx.destroySocket(socketPullBroadcast);
				}
			} catch (Exception e) {
				logger.error("stop", e);
			}

		}
	}

	@Override
	public boolean init(boolean isReload) {
		mq = (ModuleZeromq) ModuleFactory.moduleInstanceBy("mq");
		if (mq == null) {
			logger.error("ModuleZeromq not start.");
			return false;
		}
		return true;
	}

	@Override
	public void afterCreate(Object[] params) {
		threadCount = Integer.parseInt(params[0].toString());
	}

	@Override
	public void start(boolean isReload) {
		int count = Math.max(Config.rock().getIntItem("AppProcessThreadCount", "1"), threadCount);
		for (int i = 0; i < count; i++) {
			JobPull task = new JobPull(this, i + 1);
			Thread t = new Thread(task);
			task.attach(t);
			t.start();
		}
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public String getId() {
		return "app-jobpull";
	}

	private ZMsg packClientMsg(String clientAddr, MessageVersion version, byte[] msgTag, String content) {
		byte[] bytes = StringUtil.toBytes(content);
		return packClientMsg(clientAddr, version, msgTag, bytes);
	}

	private ZMsg packClientMsg(String clientAddr, MessageVersion version, byte[] msgTag, byte[] bytes) {
		ZMsg msg = new ZMsg();
		msg.addFirst(String.valueOf(MessageSource.fromApp.ordinal()));
		msg.add(clientAddr);
		msg.add(ZeromqUtil.EMPTY);
		msg.add(msgTag);
		CompressMode compress = null;
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
		return msg;
	}

	private ZMsg packRouterMsg(Device device, ServiceName service, MessageScope scope, MessageType type, MessageVersion version, String msgId, String msgTag,
			String msgTo, long timestamp, byte[] bytes, String address) {
		ZMsg msg = new ZMsg();
		msg.add(String.valueOf(MessageSource.fromRouter.ordinal()));
		msg.add(ZeromqUtil.EMPTY);
		msg.add(device.getData());
		msg.add(service.getData());
		msg.add(String.valueOf(service.ordinal()));
		msg.add(scope.getData());
		msg.add(type.getData());
		msg.add(version.getData());
		msg.add(ZeromqUtil.getBytes(msgId));
		msg.add(ZeromqUtil.getBytes(msgTag));
		msg.add(ZeromqUtil.getBytes(msgTo));
		msg.add(String.valueOf(timestamp));
		CompressMode compress = null;
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
		msg.add(StringUtil.toBytes(address));
		return msg;
	}

	private void changeRouter(String clientAddr, MessageVersion version, byte[] msgTag, String content) {
		packRouterMsg(Device.SERVER, ServiceName.Server, MessageScope.ROUTER, MessageType.DISCONNECT, MessageVersion.MQ, this.mq.getId(), "change", "TO",
				System.currentTimeMillis(), ZeromqUtil.EMPTY, this.mq.getId());
		sendJob(packRouterMsg(Device.SERVER, ServiceName.Server, MessageScope.ROUTER, MessageType.DISCONNECT, MessageVersion.MQ, "ID", msgTag.toString(), "TO",
				System.currentTimeMillis(), ZeromqUtil.EMPTY, this.mq.getId()));
		this.mq.stop();
		this.mq.routerNode = ServerDict.self.routerBy(content);
		this.mq.start(true);
		ModuleIntf subscribe = ModuleFactory.moduleInstanceBy("subscribe");
		subscribe.stop();
		subscribe.start(true);
		byte[] bytes = StringUtil.toBytes("{state:" + this.mq.isAlive() + "}");
		sendJob(this.packClientMsg(clientAddr, version, msgTag, bytes));
	}

	private void handleSocket(String clientAddr, MessageVersion version, byte[] msgTag, String content) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		if (ModuleFactory.socket() == null) {
			oResult.append("message", "AppServer[" + Global.ServerName + "] doesn't load ModuleSocket.");
		} else {
			oResult = ModuleFactory.socket().relayFromMQ(content);
		}
		byte[] bytes = StringUtil.toBytes(oResult.toString());
		logger.info("handleSocket[{}] && clientAddr=[{}]", new String[] { oResult.toString(), clientAddr });
		sendJob(this.packClientMsg(clientAddr, version, msgTag, bytes));
	}

	protected void switchTo(boolean online, String sender, MessageVersion version, ZFrame msgTag) {
		boolean exist = ZooUtil.exists(ServerDict.self.zoo(), "/appservers/" + this.mq.getId());
		String name = online ? "online" : "offline";
		if (exist != online) {
			if (online == true) {
				this.mq.restart();
				sendJob(packClientMsg(sender, version, msgTag.getData(), "{\"" + name + "\":true}"));
			} else {
				sendJob(packRouterMsg(Device.SERVER, ServiceName.Server, MessageScope.ROUTER, MessageType.DISCONNECT, MessageVersion.MQ, "ID",
						msgTag.toString(), "TO", System.currentTimeMillis(), ZeromqUtil.EMPTY, this.mq.getId()));
				sendJob(packClientMsg(sender, version, msgTag.getData(), "{\"" + name + "\":true}"));
				this.mq.serverNode.removeField("routerId");
				ZooUtil.delete(ServerDict.self.zoo(), "/appservers/" + this.mq.getId());
			}
		}
		logger.info("AppServer  [{}] is [{}]  at  ServerIP={} & Port={} & PublisherPort={}",
				new Object[] { this.mq.getId(), name, this.mq.serverNode.getString("ip"), Global.MQRouterPort, Global.MQRouterPublishPort });
	}


	public void sendJob(ZMsg msg) {
		ZeroSocket oSocket = null;
		String uri = "inproc://app-inner";
		try {
			oSocket = ZeroSocketFactory.get(uri, this.mq.ctx, ZMQ.DEALER);
			if (oSocket == null || oSocket.isAlive == false) {
				return;
			}
			msg.send(oSocket.socket);
		} catch (Exception e) {
			oSocket.isAlive = false;
			logger.error("sendJob", e);
		} finally {
			ZeromqUtil.free(msg);
			ZeroSocketFactory.ret(uri, oSocket);
		}
	}
	protected void handleFacet(String sender, MessageVersion version, ZFrame msgTag, String content) {
		BasicDBObject oRecord = (BasicDBObject) JSON.parse(content);
		String name = new String(msgTag.getData());
		CodeType type = CodeType.valueOf(name);
		OnModuleMsgIntf msg = (OnModuleMsgIntf) ModuleFactory.moduleInstanceBy("appdict");
		boolean b = msg.handleMsg(new ModuleMsg(ModuleMessageType.facetchange, name, type.ordinal(), oRecord));
		sendJob(packClientMsg(sender, version, msgTag.getData(), "{\"" + name + "\":" + b + "}"));
	}

	public void sendSocketEvent(String senderId, Object oResult) {
		SocketEvent event = this.mq.event.removeSocket(senderId);
		if (event == null || oResult == null) {
			return;
		}
		if (oResult instanceof String) {
			event.onMessage(SocketDataType.string, oResult, true);
		} else if (oResult instanceof BasicDBObject) {
			event.onMessage(SocketDataType.json, oResult, true);
		}
	}

}
