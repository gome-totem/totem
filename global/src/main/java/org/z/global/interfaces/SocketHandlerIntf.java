package org.z.global.interfaces;


import org.z.global.factory.SocketSession;

import com.mongodb.BasicDBObject;

public interface SocketHandlerIntf {

	public boolean onData(SocketEvent event);

	public void afterCreate(Object object);

	public void doLogout(SocketSession session);

	public void writeContact(String server, long userId, BasicDBObject oContact);

	public BasicDBObject readContact(long userId);

}
