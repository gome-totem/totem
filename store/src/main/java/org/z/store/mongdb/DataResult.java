package org.z.store.mongdb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class DataResult {
	public BasicDBList list = new BasicDBList();
	public int totalCount = 0;

	public BasicDBObject toObject() {
		BasicDBObject result = new BasicDBObject();
		result.append("totalCount", totalCount);
		result.append("items", list);
		return result;
	}

	public BasicDBList getList() {
		return list;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void normalize() {
		for (int i = 0; i < list.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) list.get(i);
			oItem.append("id", oItem.getString("_id"));
			oItem.removeField("_id");
		}
	}

}
