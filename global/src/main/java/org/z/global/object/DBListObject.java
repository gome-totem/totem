package org.z.global.object;

import java.util.ArrayList;

import com.mongodb.BasicDBObject;

public class DBListObject {

	public ArrayList<BasicDBObject> value = null;
	public String key = null;
	public long timestamp = 0;

	public ArrayList<BasicDBObject> getValue() {
		return this.value;
	}

}
