package org.z.core.stocket;

import java.util.List;

import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.Framedata;
import org.jboss.netty.channel.Channel;
import org.z.global.factory.SocketSession;
import org.z.global.interfaces.WebSocketEvent;
import org.z.global.object.LogMessage;

public class WebSocketEventImpl implements WebSocketEvent {
	protected long id = 0;
	protected int level = 0;
	protected SocketSession session = null;

	public WebSocketEventImpl(SocketSession session, int level) {
		this.session = session;
		this.id = session.channelId;
		this.level = level;
	}

	@Override
	public void onMessage(LogMessage message) {
		Channel channel = session.channel;
		if (channel.isConnected() == false)
			return;
		Draft d = (Draft) this.session.draft;
		List<Framedata> items = d.createFrames(message.toString(), true);
		for (Framedata b : items) {
			channel.write(d.createBinaryFrame(b));
		}
	}

	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public int getLevel() {
		return this.level;
	}

}
