package org.x.server.tools;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

public class MongoTools {

	//mongo链接集和
	private static ConcurrentHashMap<String, DBCollection> mongoConns = new ConcurrentHashMap<String, DBCollection>(); 
	
	/**
	 * 获取mongo链接
	 * @param host 数据库服务器IP
	 * @param port  数据库端口号
	 * @param dbname  数据库名
	 * @param collectionName  集和名
	 * @param uname  用户名
	 * @param upwd   密码
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static DBCollection getMongoConn(String host,int port,String dbname,String collectionName,String uname,String upwd){
		DBCollection conn = null;
		String connKey = collectionName + "_" + dbname + "@"+host;
		if(mongoConns.get(connKey)!=null){
			return mongoConns.get(connKey);
		}
		
		MongoClient mc = null;
		try {
			mc = new MongoClient(host, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DB db = mc.getDB(dbname);
		char[] passwd = upwd.toCharArray();
		db.authenticateCommand(uname, passwd);
		conn = db.getCollection(collectionName);
		
		mongoConns.put(connKey, conn);
		
		return conn;
	}
	
	public static synchronized DB getAuthMongoDB(String ip, int port,String dbName,boolean bAuth,String authUser,String authPassword) {
			DB db = null;
			try {
				Mongo m = new Mongo(ip,port);
				db = m.getDB(dbName);
				if(bAuth)
					db.authenticate(authUser, authPassword.toCharArray());	
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return db;
	}
	
	/**
	 * 查询Mongo库
	 * @param conn 数据库链接
	 * @param searchQuery 查询对象
	 * @return 查询结果BasicDBList
	 */
	public static BasicDBList query(DBCollection conn,BasicDBObject searchQuery,int n){
		BasicDBList resultList = new BasicDBList();
		
		DBCursor cursor = conn.find(searchQuery).limit(n);
		while(cursor.hasNext()){
			DBObject dbo = cursor.next();
			resultList.add(dbo);
		}
		return resultList;
	}
	
	
	/**
	 * 查询Mongo库
	 * @param conn 数据库链接
	 * @param searchQuery 查询对象
	 * @return 查询结果BasicDBList
	 */
	public static BasicDBList query(DBCollection conn,BasicDBObject searchQuery,BasicDBObject sortQuery,int pageNo,int pageSize){
		BasicDBList resultList = new BasicDBList();
		
		DBCursor cursor = conn.find(searchQuery).sort(sortQuery).skip((pageNo-1)*pageSize).limit(pageSize);
		while(cursor.hasNext()){
			DBObject dbo = cursor.next();
			resultList.add(dbo);
		}
		return resultList;
	}
	
	public static void main(String [] args){
		DBCollection conn = MongoTools.getMongoConn("10.58.50.55", 19753, "logs", "searchlogs", "gome", "totem");
////		BasicDBObject oid = new BasicDBObject();
////		oid.append("oid", "51d40f91e4b067cf96f9e2cc");
//		BasicDBObject query = new BasicDBObject();
//		query.append("ResultCount", new BasicDBObject().append("$eq", 0));
////		System.out.println(query);
//		@SuppressWarnings("unused")
//		BasicDBList slogs = MongoTools.query(conn,query,10);
		
//		System.out.println(ServerDict.self.apps());
	}
	
	
}
