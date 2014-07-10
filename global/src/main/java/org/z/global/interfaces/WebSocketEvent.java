package org.z.global.interfaces;

import org.z.global.object.LogMessage;



public interface WebSocketEvent {

	public void onMessage(LogMessage message);

	public long getId();

	public int getLevel();

}
