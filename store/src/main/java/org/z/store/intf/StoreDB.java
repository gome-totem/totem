package org.z.store.intf;

import com.mongodb.BasicDBObject;

public interface StoreDB {

	public void init(BasicDBObject mongos);

	public void getDB(BasicDBObject obj);

}
