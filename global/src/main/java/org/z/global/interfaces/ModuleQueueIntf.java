package org.z.global.interfaces;

import com.mongodb.BasicDBObject;

public interface ModuleQueueIntf extends ModuleIntf {

	public void put(BasicDBObject event);

}
