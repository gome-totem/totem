package com.x.server.test;

import java.net.UnknownHostException;

import org.z.global.util.StringUtil;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class TestMongo {

	@SuppressWarnings("deprecation")
	public static void main(String args[]) throws UnknownHostException,
			MongoException {

		Mongo mongo = new Mongo("10.58.50.106", 19753);
		DB db = mongo.getDB("logs");
		char[] passwd = "totem".toCharArray();
		db.authenticateCommand("gome", passwd);
		DBCollection collection = db.getCollection("indexlog");
		DBCursor cur = collection.find();
			 
		int key = 1 ;
		switch (key) {
		case 1:
			while (cur.hasNext()) {
				DBObject obj = cur.next();
				String productId= (String)obj.get("id") ;
				if(!StringUtil.isEmpty(productId) && "A0003375405".equals(productId)){
					System.out.println(obj);
//						StringUtil.writeToFile(obj.toString(), "/workspace/1.text", "utf-8");
				}
			}
			break;

		case 2:
			break ;
		default:
			break;
		}
		
			
			

		System.out.println("done!");
		
	}
}
