package org.x.server.eye;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.z.global.util.StringUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class WebSocketSession extends MessageInbound {

	private static Log log = LogFactory.getLog(WebSocketSession.class);
	protected long timestamp = 0;
	protected String id = StringUtil.createUniqueID();
	protected Eye eye = null;
	protected List<String> filters = new ArrayList<String>();

	public WebSocketSession(Eye eye) {
		this.eye = eye;
	}

	protected void send(BasicDBObject oMsg) {
		if ("log".equalsIgnoreCase(oMsg.getString("msgType"))) {
			String id = oMsg.getString("serverType") + "_" + oMsg.getString("ip");
			if (filters.contains(id) == false) {
				return;
			}
		}
		WsOutbound outbound = this.getWsOutbound();
		try {
			if (outbound != null) {
				outbound.writeTextMessage(CharBuffer.wrap(oMsg.toString().toCharArray()));
				outbound.flush();
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	@Override
	protected void onTextMessage(CharBuffer requestInfo) throws IOException {
		if (requestInfo == null) {
			return;
		}
		BasicDBObject oMsg = (BasicDBObject) JSON.parse(requestInfo.toString());
		String action = oMsg.getString("action");
		if (action.equalsIgnoreCase("open")) {
			this.timestamp = System.currentTimeMillis();
			String type = oMsg.getString("type");
			String ip = oMsg.getString("ip");
			WebSocketConnect connect = WebSocketConnect.connect(ip, type);
			connect.addSession(this);
			filters.add(type + "_" + ip);
		} else if (action.equalsIgnoreCase("close")) {
			String type = oMsg.getString("type");
			String ip = oMsg.getString("ip");
			filters.remove(type + "_" + ip);
			WebSocketConnect connect = WebSocketConnect.by(ip, type);
			if (connect == null) {
				return;
			}
			connect.removeSession(this);
		} else if (action.equalsIgnoreCase("hello")) {
			if (log.isInfoEnabled()) {
				log.info("hello from ");
			}
		}
	}

	@Override
	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
	}

	@Override
	protected void onClose(int status) {
		Eye.sessions.remove(this);
		super.onClose(status);
	}

	@Override
	protected void onOpen(WsOutbound outbound) {
		super.onOpen(outbound);
	}

}
