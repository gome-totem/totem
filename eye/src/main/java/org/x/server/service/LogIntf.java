package org.x.server.service;

import com.mongodb.BasicDBObject;

public interface LogIntf {
	
	public void printLog(BasicDBObject logMsg);

	public void writeLog(BasicDBObject logMsg);
}
