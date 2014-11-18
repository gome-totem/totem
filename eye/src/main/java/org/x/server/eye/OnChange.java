package org.x.server.eye;

import com.mongodb.BasicDBObject;

public interface OnChange {

	public void execute(BasicDBObject oMsg);

}
