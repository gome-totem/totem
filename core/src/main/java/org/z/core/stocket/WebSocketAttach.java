package org.z.core.stocket;

import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;

public class WebSocketAttach {

	public ClientHandshake handshake = null;
	public Draft draft = null;
	public String content = null;

	public WebSocketAttach(ClientHandshake handshake, Draft draft) {
		this.handshake = handshake;
		this.draft = draft;
	}

	public WebSocketAttach(String content) {
		this.content = content;
	}

}
