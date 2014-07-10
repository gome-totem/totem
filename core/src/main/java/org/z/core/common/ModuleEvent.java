package org.z.core.common;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.z.global.interfaces.SocketEvent;
import org.z.global.interfaces.WebSocketEvent;
import org.z.global.object.LogMessage;

public class ModuleEvent {
	protected ConcurrentHashMap<String, SocketEvent> socketEvents = new ConcurrentHashMap<String, SocketEvent>();
	protected ConcurrentHashMap<Long, WebSocketEvent> webSocketEvents = new ConcurrentHashMap<Long, WebSocketEvent>();

	public void notifyWebSocket(LogMessage message) {
		if (webSocketEvents.size() == 0)
			return;
		for (Iterator<Entry<Long, WebSocketEvent>> i = webSocketEvents.entrySet().iterator(); i.hasNext();) {
			Entry<Long, WebSocketEvent> entry = i.next();
			WebSocketEvent event = entry.getValue();
			if (event.getLevel() < 0) {
				continue;
			}
			event.onMessage(message);
		}
	}

	public void registerWebSocket(WebSocketEvent event) {
		webSocketEvents.put(event.getId(), event);
	}

	public void removeWebSocket(long id) {
		webSocketEvents.remove(id);
	}

	public SocketEvent removeSocket(String id) {
		SocketEvent event = this.socketEvents.remove(id);
		return event;
	}

	public SocketEvent getSocket(String id) {
		SocketEvent event = this.socketEvents.get(id);
		return event;
	}

	public void addSocket(SocketEvent event) {
		socketEvents.put(event.id(), event);
	}
}
