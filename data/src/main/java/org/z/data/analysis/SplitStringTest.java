package org.z.data.analysis;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.x.segment.dict.Dictionary;
import org.x.segment.parse.ChineseSplit;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class SplitStringTest {

	@SuppressWarnings({ "unused", "deprecation" })
	private static List<DBObject> getBasiceObjectList() throws UnknownHostException, MongoException {
		Dictionary.init();
		Mongo mongo = new Mongo("10.57.40.224", 27017);
		DB db = mongo.getDB("gome");
		DBCollection key = db.getCollection("keywords");
		List<DBObject> list = key.find(null, new BasicDBObject("item", true)).toArray();
		return list;
	}

	public static void main(String[] args) throws UnknownHostException, MongoException {
		Dictionary.init();
		System.out.println(ChineseSplit.toShortString("abc.efg"));
		String ss = "亮剑618";
		List<BasicDBObject> list = new ArrayList<BasicDBObject>();
		list.add(new BasicDBObject("item", ss));
		Test test1 = new Test(list, true);
		test1.start();
	}
}
