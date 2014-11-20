package org.z.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.interfaces.ModuleBroadcastIntf;
import org.z.global.connect.ZeroSocket;
import org.z.global.connect.ZeroSocketFactory;
import org.z.global.dict.Global;
import org.z.global.dict.Global.MessageSource;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.ModuleZeromqIntf;
import org.z.global.util.ZeromqUtil;
import org.z.global.zk.ServerDict.NodeType;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class ModuleBroadcast implements ModuleBroadcastIntf, Runnable {
	protected static Logger logger = LoggerFactory.getLogger(ModuleBroadcast.class);
	private String id = null;
	private ModuleZeromqIntf mq = null;
	private Socket publishSocket = null;
	private ZMQ.Socket subscribeSocket = null;
	private ZMQ.Socket pushSocket = null;
	private ZMQ.Socket innerSocket = null;
	private boolean isAlive = false;
	private NodeType nodeType = null;
	private String publishAddr = null;
	private Thread t = null;

	@Override
	public void afterCreate(Object[] params) {
		nodeType = params.length >= 1 ? NodeType.valueOf(params[0].toString()) : NodeType.app;
	}

	@Override
	public boolean init(boolean isReload) {
		this.mq = (ModuleZeromqIntf) ModuleFactory.moduleInstanceBy("mq");
		if (this.mq == null) {
			logger.error("ModuleZeromq must start first.");
			return false;
		}
		id = this.mq.name() + "@Broadcast";
		pushSocket = this.mq.ctx().createSocket(ZMQ.PUSH);
		pushSocket.setIdentity((this.id).getBytes());
		pushSocket.setSndHWM(1000000);
		pushSocket.bind("inproc://broadcast-" + nodeType.name() + "-push");

		String pubId = id + "-pub";
		publishSocket = this.mq.ctx().createSocket(ZMQ.PUB);
		publishSocket.setIdentity(pubId.getBytes());
		pushSocket.setSndHWM(1000000);
		switch (nodeType) {
		case router:
			publishSocket.bind("tcp://" + Global.ServerIP + ":" + Global.MQRouterPublishPort);
			break;
		case app:
			publishSocket.bind("tcp://" + Global.ServerIP + ":" + Global.MQAppPublishPort);
			break;
		}

		String subId = id + "-sub";
		subscribeSocket = this.mq.ctx().createSocket(ZMQ.SUB);
		subscribeSocket.setRcvHWM(1000000);
		subscribeSocket.setIdentity(subId.getBytes());

		String innerId = this.id + "-inner";
		// for dispatch message to publish.
		innerSocket = this.mq.ctx().createSocket(ZMQ.DEALER);
		innerSocket.setIdentity(innerId.getBytes());
		innerSocket.bind("inproc://broadcast-" + nodeType.name() + "-inner");
		return this.mq != null;
	}

	@Override
	public ModuleZeromqIntf mq() {
		return this.mq;
	}

	public void disconnect(String addr) {
		try {
			subscribeSocket.disconnect(addr);
		} catch (Exception e) {
			logger.error("disconnect", e);
		}
	}

	public void connect(String addr) {
		try {
			this.publishAddr = addr;
			subscribeSocket.connect(publishAddr);
			subscribeSocket.subscribe("yiqihi".getBytes());
			logger.info("Subscribe connect to ip [{}]", new Object[] { Const.BroadCastIP });
		} catch (Exception e) {
			logger.error("connect", e);
		}
	}

	@Override
	public void reconnect() {
		String addr = null;
		switch (this.nodeType) {
		case app:
			addr = "tcp://" + this.mq().getRouterIP() + ":" + Global.MQRouterPublishPort;
			break;
		case router:
			if (Const.BroadCastIP.equalsIgnoreCase(Global.ServerIP)) {
				return;
			}
			addr = "tcp://" + Const.BroadCastIP + ":" + Global.MQRouterPublishPort;
			break;
		}
		if (publishAddr != null && !publishAddr.equalsIgnoreCase(addr)) {
			this.disconnect(publishAddr);
		}
		this.connect(addr);
	}

	@Override
	public void start(boolean isReload) {
		isAlive = true;
		this.reconnect();
		t = new Thread(this);
		t.start();
	}

	@Override
	public void stop() {
		if (isAlive == false) {
			return;
		}
		logger.error("ModuleBroadcast Stop");
		isAlive = false;
		try {
			t.join();
			this.mq.ctx().destroySocket(subscribeSocket);
			this.mq.ctx().destroySocket(publishSocket);
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
		return id;
	}

	@Override
	public void send(ZMsg msg) {
		ZeroSocket oSocket = null;
		String uri = "inproc://broadcast-" + nodeType.name() + "-inner";
		try {
			oSocket = ZeroSocketFactory.get(uri, this.mq.ctx(), ZMQ.DEALER);
			if (oSocket == null || oSocket.isAlive == false) {
				return;
			}
			msg.send(oSocket.socket);
		} catch (Exception e) {
			oSocket.isAlive = false;
			logger.error("post", e);
		} finally {
			ZeromqUtil.free(msg);
			ZeroSocketFactory.ret(uri, oSocket);
		}
	}

	public void handleSubscribe(ZMsg msg) {
		if (msg == null || msg.size() < 3) {
			return;
		}
		switch (nodeType) {
		case app:
			ZFrame c = msg.pop();
			String content = c.toString();
			c.destroy();
			if (!content.equalsIgnoreCase("yiqihi")) {
				return;
			}
			msg.addFirst(String.valueOf(MessageSource.fromApp.ordinal()));
			msg.send(pushSocket, false);
			break;
		case router:
			msg.send(publishSocket, false);
			break;
		}
	}

	public void handleInner(ZMsg msg) {
		if (msg == null || msg.size() < 3) {
			return;
		}
		msg.addFirst("yiqihi");
		msg.send(publishSocket, false);
	}

	@Override
	public void run() {
		ZMQ.Poller items = this.mq.ctx().getContext().poller(2);
		items.register(subscribeSocket, ZMQ.Poller.POLLIN);
		items.register(innerSocket, ZMQ.Poller.POLLIN);
		while (isAlive && t.isInterrupted() == false) {
			if (items.poll(Const.HEARTBEAT_INTERVAL) < 1) {
				continue;
			}
			ZMsg msg = null;
			try {
				if (items.pollin(0)) {
					msg = ZMsg.recvMsg(subscribeSocket);
					handleSubscribe(msg);
				} else if (items.pollin(1)) {
					msg = ZMsg.recvMsg(innerSocket);
					handleInner(msg);
				}
			} catch (Exception e) {
				isAlive = false;
				logger.error("run", e);
				break;
			} finally {
				msg.destroy();
			}
		}
		stop();
	}
	
}
