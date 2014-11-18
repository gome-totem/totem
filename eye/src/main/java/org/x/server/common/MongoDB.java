package org.x.server.common;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global;
import org.z.store.mongdb.DataResult;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

public class MongoDB {

	protected static Logger logger = LoggerFactory.getLogger(MongoDB.class);

	private static Hashtable<String, DB> mongoDBs = new Hashtable<String, DB>();
	private static Hashtable<String, MongoClient> mongoClients = new Hashtable<String, MongoClient>();
	private static Builder builder = null;
	private static MongoClientOptions mongoClientOptions = null;

	static {
		builder = MongoClientOptions.builder();
		builder.autoConnectRetry(true);
		builder.connectionsPerHost(100);
		builder.threadsAllowedToBlockForConnectionMultiplier(50);
		builder.maxWaitTime(10000);
		builder.connectTimeout(3000);
		mongoClientOptions = builder.build();
	}

	public static DBCollection getCollection(String serverName, String dbName, String collName) {
		DB db = getMongoDB(serverName, dbName);
		if (db == null) {
			return null;
		}
		return db.getCollection(collName);
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

	public static synchronized DB getMongoDB(String serverName, String dbName) {
		String key = serverName + "@" + dbName;
		DB db = mongoDBs.get(key);
		if (db != null && db.getStats().ok()) {
			return db;
		}
		MongoClient mongoClient = getClient(serverName);
		if (mongoClient == null) {
			logger.error("no mongo is running in sever [{}], port [{}]", new Object[] { serverName, Global.MongoPort });
			return null;
		}
		db = mongoClient.getDB(dbName);
		if (db != null) {
			mongoDBs.put(key, db);
		}
		return db;
	}

	public static synchronized MongoClient getClient(String serverName) {
		return getClient(serverName, Global.MongoPort);
	}

	public static synchronized MongoClient getClient(String serverName, int port) {
		MongoClient mongoClient = mongoClients.get(serverName);
		if (mongoClient != null) {
			return mongoClient;
		}
		try {
			mongoClient = new MongoClient(new ServerAddress(serverName, port), mongoClientOptions);
		} catch (UnknownHostException e) {
			logger.error("no mongo running in sever [{}], port [{}]", new Object[] { serverName, port });
			e.printStackTrace();
		}
		if (mongoClient != null) {
			mongoClients.put(serverName, mongoClient);
		}
		return mongoClient;
	}

	public static ObjectId insert(String serverName, String dbName, String collName, DBObject oField) {
		DBCollection collection = getCollection(serverName, dbName, collName);
		if (collection == null) {
			return null;
		}
		collection.insert(oField);
		return (ObjectId) oField.get("_id");
	}

	public static void insertBatch(String serverName, String dbName, String collName, BasicDBList list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0, j = list.size(); i < j; i++) {
			DBObject dbObject = (DBObject) list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.insert(listDB);
	}

	public static void insertBatch(String serverName, String dbName, String collName, List<BasicDBList> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0, j = list.size(); i < j; i++) {
			DBObject dbObject = (DBObject) list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.insert(listDB);
	}

	public static void insertBatchDB(String serverName, String dbName, String collName, List<DBObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.insert(list);
	}

	public static void dropCollection(String serverName, String dbName, String collName) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.drop();
	}

	public static Set<String> getCollectionNames(String serverName, String dbName) {
		DB db = getMongoDB(serverName, dbName);
		return db.getCollectionNames();
	}

	public static void remove(String serverName, String dbName, String collName, DBObject oField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.remove(oField);
	}

	public static void removeBatch(String serverName, String dbName, String collName, List<DBObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		for (int i = 0, j = list.size(); i < j; i++) {
			table.remove(list.get(i));
		}
	}

	public static void removeBatch(String serverName, String dbName, String collName, BasicDBList list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		for (int i = 0, j = list.size(); i < j; i++) {
			table.remove((DBObject) list.get(i));
		}
	}

	public static DBObject findById(String serverName, String dbName, String collName, String id) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		return table.findOne(new ObjectId(id));
	}

	public static long getCount(String serverName, String dbName, String collName, DBObject qField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return 0;
		}
		return table.getCount(qField);
	}

	public static DataResult find(String serverName, String dbName, String collName, DBObject qField, DBObject sortField, int offset, int count) {
		return find(serverName, dbName, collName, qField, sortField, null, offset, count);
	}

	public static DataResult find(String serverName, String dbName, String collName, DBObject qField, DBObject sortField) {
		return find(serverName, dbName, collName, qField, sortField, null, 0, 0);
	}

	public static DBCursor findAll(String serverName, String dbName, String collName) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = table.find();
		return cur;
	}

	public static DBCursor find(String serverName, String dbName, String collName, DBObject sortField, int option) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = table.find().sort(sortField).addOption(option);
		return cur;
	}

	public static DBCursor find(String serverName, String dbName, String collName, DBObject qField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = table.find(qField);
		return cur;
	}

	public static DBCursor find(String serverName, String dbName, String collName, DBObject qField, DBObject sortField, int option) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = null;
		if (qField != null && sortField != null) {
			cur = table.find(qField).sort(sortField).addOption(option);
		} else if (qField != null) {
			cur = table.find(qField).addOption(option);
		} else if (sortField != null) {
			cur = table.find().sort(sortField).addOption(option);
		} else {
			cur = table.find().addOption(option);
		}
		return cur;
	}

	public static DataResult find(String serverName, String dbName, String collName, DBObject qField, DBObject sortField, DBObject returnField, int offset, int count) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = null;
		DataResult result = new DataResult();
		try {
			cur = table.find(qField, returnField);
			if (sortField != null) {
				cur.sort(sortField);
			}
			if (cur == null) {
				return result;
			}
			result.totalCount = cur.count();
			if (count == 0) {
				count = result.totalCount;
			}
			offset = offset < 0 ? 0 : offset;
			cur.skip(offset);
			cur.limit(count);
			if (count > 0) {
				result.list.addAll(cur.toArray());
			}
		} finally {
			cur.close();
		}
		return result;
	}

	public static DataResult findByRandom(String serverName, String dbName, String collName, DBObject qField, DBObject sortField, DBObject returnField, int count) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = null;
		DataResult result = new DataResult();
		try {
			cur = table.find(qField, returnField);
			if (sortField != null) {
				cur.sort(sortField);
			}
			if (cur == null) {
				return result;
			}
			result.totalCount = cur.count();
			if (count == 0) {
				count = result.totalCount;
			}
			if (result.totalCount == 0) {
				return result;
			}
			int offset = 0;
			if (result.totalCount > count) {
				offset = RandomUtils.nextInt(result.totalCount - count);
			}
			if (offset > 0) {
				cur.skip(offset);
			}
			cur.limit(count);
			if (count > 0) {
				result.list.addAll(cur.toArray());
			}
		} finally {
			cur.close();
		}
		return result;
	}

	public static DBObject findOne(String serverName, String dbName, String collName, DBObject qField, DBObject returnField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		return table.findOne(qField, returnField);
	}

	public static DBObject findOne(String serverName, String dbName, String collName, DBObject qField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		return table.findOne(qField);
	}

	public static void findAndModify(String serverName, String dbName, String collName, BasicDBObject query, BasicDBObject update) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.findAndModify(query, update);
	}

	public static List<DBObject> findAll(String serverName, String dbName, String collName, DBObject returnField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		List<DBObject> list = new ArrayList<DBObject>();
		if (table == null) {
			return list;
		}
		DBCursor cur = null;
		try {
			cur = table.find(new BasicDBObject(), returnField);
			if (cur == null) {
				return list;
			}
			while (cur.hasNext()) {
				DBObject obj = cur.next();
				list.add(obj);
			}
		} finally {
			cur.close();
		}
		return list;
	}

	public static List<DBObject> findAll(String serverName, String dbName, String collName, DBObject qField, DBObject returnField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		List<DBObject> list = new ArrayList<DBObject>();
		if (table == null) {
			return list;
		}
		DBCursor cur = null;
		try {
			cur = table.find(qField, returnField);
			if (cur == null) {
				return list;
			}
			while (cur.hasNext()) {
				DBObject obj = cur.next();
				list.add(obj);
			}
		} finally {
			cur.close();
		}
		return list;
	}

	public static List<DBObject> findPage(String serverName, String dbName, String collName, DBObject returnField, int skip, int limit) {
		DBCollection table = getCollection(serverName, dbName, collName);
		List<DBObject> list = new ArrayList<DBObject>();
		if (table == null) {
			return list;
		}
		DBCursor cur = null;
		try {
			cur = table.find(new BasicDBObject(), returnField).skip(skip).limit(limit);
			if (cur == null) {
				return list;
			}
			for (; cur.hasNext();) {
				DBObject obj = cur.next();
				list.add(obj);
			}
		} finally {
			cur.close();
		}
		return list;
	}

	public static List<DBObject> findPage(String serverName, String dbName, String collName, int skip, int limit) {
		DBCollection table = getCollection(serverName, dbName, collName);
		List<DBObject> list = new ArrayList<DBObject>();
		if (table == null) {
			return list;
		}
		DBCursor cur = null;
		try {
			cur = table.find().skip(skip).limit(limit);
			if (cur == null) {
				return list;
			}
			for (; cur.hasNext();) {
				DBObject obj = cur.next();
				list.add(obj);
			}
		} finally {
			cur.close();
		}
		return list;
	}

	public static int selectCount(String serverName, String dbName, String collName, DBObject qField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return 0;
		}
		DBCursor cur = table.find(qField, null);
		int result = cur.count();
		cur.close();
		return result;
	}

	public static boolean update(String serverName, String dbName, String collName, DBObject qField, DBObject oField, boolean upsert) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return false;
		}
		WriteResult wr = table.update(qField, oField, upsert, false);
		return wr.getLastError().isEmpty() == true;
	}

	public static boolean update(String serverName, String dbName, String collName, DBObject qField, DBObject oField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return false;
		}
		WriteResult wr = table.update(qField, oField);
		return wr.getLastError().isEmpty() == true;
	}

	public static void updateMulti(String serverName, String dbName, String collName, DBObject qField, DBObject oField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.updateMulti(qField, oField);
	}

	public static CommandResult runCommand(String serverName, String dbName, BasicDBObject cmd) {
		DB db = getMongoDB(serverName, dbName);
		if (db == null) {
			return null;
		}
		return db.command(cmd);
	}

	public static void createIndex(String serverName, String dbName, String collName, BasicDBObject keys) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.ensureIndex(keys);
	}

	public static void dropIndex(String serverName, String dbName, String collName, DBObject keys) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.dropIndex(keys);
	}

	public static void createIndex(String serverName, String dbName, String collName, String indexName, BasicDBObject keys, final boolean unique) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.ensureIndex(keys, indexName, unique);
	}

	public static void main(String args[]) {
	}

}
