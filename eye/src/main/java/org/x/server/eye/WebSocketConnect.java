package org.x.server.eye;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * connect router& appserver
 * 
 * @author root
 * 
 */
public class WebSocketConnect extends WebSocketClient {

	private final Log log = LogFactory.getLog(getClass());
	private Logger logger = LoggerFactory.getLogger(WebSocketConnect.class);
	private ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<String, WebSocketSession>();
	private EventQueue queue = null;
	private String serverType;
	private String serverIp;

	public WebSocketConnect(URI serverURI, String serverType, String serverIp) {
		super(serverURI);
		this.serverType = serverType;
		this.serverIp = serverIp;
	}

	public void attach(EventQueue queue) {
		this.queue = queue;
	}

	public void addSession(WebSocketSession session) {
		sessions.put(session.id, session);
	}

	public void removeSession(WebSocketSession session) {
		sessions.remove(session.id);
		if (sessions.size() == 0) {
			this.forceClose();
		}
	}

	public void checkExpire() {
		for (Iterator<Entry<String, WebSocketSession>> i = sessions.entrySet().iterator(); i.hasNext();) {
			Entry<String, WebSocketSession> entry = i.next();
			WebSocketSession session = entry.getValue();
			if (System.currentTimeMillis() - session.timestamp >= 10 * 60000) {
				removeSession(session);
			}
		}
	}

	public void forceClose() {
		this.close();
		Eye.connects.remove(this.uri.toString());
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		BasicDBObject oReq = new BasicDBObject().append("action", "registerListener");
		this.send(oReq.toString());
	}

	@Override
	public void onMessage(String message) {
		BasicDBObject oMsg = (BasicDBObject) JSON.parse(message);
		oMsg.append("serverType", serverType);
		oMsg.append("ip", serverIp);
		oMsg.append("msgType", "log");
		queue.add(oMsg);
		logger.info("Message of search log {}", new Object[]{oMsg});
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		Eye.connects.remove(this.uri.toString());
	}

	@Override
	public void onError(Exception ex) {
		log.error(ex.getMessage());
	}

	public static String uriBy(String ip, String serverType) {
		int port = 0;
		if (serverType.equalsIgnoreCase("router")) {
			port = Global.SocketRouterPort;
		}
		if (serverType.equalsIgnoreCase("appserver")) {
			port = Global.SocketAppPort;
		}
		String uri = "ws://" + ip + ":" + port;
		return uri;
	}

	public static WebSocketConnect by(String ip, String serverType) {
		String uri = uriBy(ip, serverType);
		WebSocketConnect instance = Eye.connects.get(uri);
		return instance;
	}

	public static synchronized WebSocketConnect connect(String ip, String serverType) {
		String uri = uriBy(ip, serverType);
		WebSocketConnect instance = Eye.connects.get(uri);
		if (instance != null) {
			return instance;
		}
		try {
			instance = new WebSocketConnect(new URI(uri), serverType, ip);
			instance.connect();
			instance.attach(Eye.instance.queue);
			Eye.connects.put(uri, instance);
		} catch (Exception e) {
		}
		return instance;
	}

	public static void main(String[] args) throws URISyntaxException, InterruptedException {
	}

}
