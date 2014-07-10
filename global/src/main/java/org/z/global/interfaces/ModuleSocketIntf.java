package org.z.global.interfaces;

import java.util.ArrayList;
import org.z.global.factory.SocketSession;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public interface ModuleSocketIntf extends ModuleIntf {

	public BasicDBObject relayFromMQ(String message);

	public BasicDBObject checkSession(DBObject oReq);

	public BasicDBObject encryptSocket(String password, BasicDBObject oReq);

	public BasicDBObject encryptSocket(String password, String content);

	public BasicDBObject decryptSocket(String password, String content);

	public SocketHandlerIntf socketHandler();

	public ArrayList<SocketSession> userBy(long userId);

	public boolean sendMessage(SocketSession session, BasicDBObject oMsg);

	public boolean readOnline(String server, long userId);

}
