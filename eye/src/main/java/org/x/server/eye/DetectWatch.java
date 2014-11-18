package org.x.server.eye;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.eye.DetectSubject.Message;
import org.x.server.eye.DetectSubject.Observer;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class DetectWatch extends WebSocketServlet {

	private static final long serialVersionUID = -2012068452342201975L;
	private static final Logger logger = LoggerFactory.getLogger(DetectWatch.class);
	private static DetectAddress detectAddress = new DetectAddress();

	public static enum DetectType {

		CATEGORY("category"), SEARCH("search");

		private static final Map<String, DetectType> types = new ConcurrentHashMap<String, DetectWatch.DetectType>();

		static {
			for (DetectType detectType : DetectType.values())
				types.put(detectType.content, detectType);
		}

		private String content;

		private DetectType(String content) {
			this.content = content;
		}

		public static DetectType getType(String type) {
			return types.get(type);
		}

	}

	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		return new DetectMessageInbound();
	}

	private class DetectMessageInbound extends MessageInbound implements Observer {

		@Override
		protected void onBinaryMessage(ByteBuffer message) throws IOException {
		}

		@Override
		protected void onTextMessage(CharBuffer message) throws IOException {
			if (message == null)
				return;
			BasicDBObject msg = (BasicDBObject) JSON.parse(message.toString());
			if (msg == null)
				return;
			executeDetectAddress(msg);
		}

		private void executeDetectAddress(BasicDBObject msg) {
			switch (DetectType.getType(msg.getString("action"))) {
			case CATEGORY:
				detectAddress.executeDefaultCategory(3, 3);
				break;
			case SEARCH:
				detectAddress.executeDefaultSearch(3, 3);
				break;
			default:
				logger.warn(msg.getString("action"));
				break;
			}
		}

		public void execute(Message message) {
			if (getWsOutbound() != null)
				writeMessage(message);
		}

		private void writeMessage(Message message) {
			try {
				getWsOutbound().writeTextMessage(CharBuffer.wrap(message.toString().toCharArray()));
				getWsOutbound().flush();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		@Override
		protected void onOpen(WsOutbound outbound) {
			detectAddress.registerObserver(this);
			super.onOpen(outbound);
		}

		@Override
		protected void onClose(int status) {
			detectAddress.removeObserver(this);
			super.onClose(status);
		}

	}

}
