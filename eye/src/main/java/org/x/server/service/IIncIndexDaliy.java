package org.x.server.service;

import com.mongodb.BasicDBObject;

public interface IIncIndexDaliy {

	public BasicDBObject findOneInMongo(String indexlogs2, String id) ;

	public  BasicDBObject dealDaliyInMongo(String collName, int dateIndex);
}
