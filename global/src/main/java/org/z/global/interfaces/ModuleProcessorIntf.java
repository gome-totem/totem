package org.z.global.interfaces;


import org.z.global.dict.MessageScope;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public interface ModuleProcessorIntf extends ModuleIntf {

	public BasicDBObject execute(String sender, int deviceIndex, int serviceIndex, MessageScope scope, int messageType, int messageVersion, String messageId, String messageTag, String messageTo,
			long messageTime, DBObject oReq);

}
