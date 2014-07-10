package org.z.global.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class ZeroSocket {
	public boolean isAlive = true;
	public String id = null;
	protected ZContext context = null;
	protected String uri = null;
	public Socket socket = null;
	protected boolean isCreated = false;
	protected int socketMode = 0;
	protected static Logger logger = LoggerFactory.getLogger(ZeroSocket.class);

	public ZeroSocket(String uri, ZContext ctx, int socketMode) {
		this.uri = uri;
		this.id = StringUtil.createSenderId();
		this.socketMode = socketMode;
		this.isCreated = ctx == null;
		this.context = ctx;
	}

	public void init() {
		try {
			if (this.context == null) {
				context = new ZContext();
			}
			socket = context.createSocket(this.socketMode);
			socket.setReceiveTimeOut(Const.AppConnectTimeout * 1000);
			socket.setSendTimeOut(Const.AppConnectTimeout * 1000);
			socket.setIdentity(StringUtil.toBytes(id));
			socket.connect(uri);
			isAlive = true;
		} catch (Exception e) {
			logger.error("SocketCount=" + context.getSockets().size(), e);
			isAlive = false;
		}
	}

	public void destroy() {
		isAlive = false;
		if (context == null) {
			return;
		}
		try {
			if (this.isCreated) {
				this.context.destroy();
			} else {
				this.context.destroySocket(socket);
			}
		} catch (Exception e) {
			logger.error("socketClient uri[{}] destory error[{}].", new String[] { uri, e.getLocalizedMessage() });
		}
	}

}
