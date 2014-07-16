package org.z.store.mongdb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.environment.Const;
import org.z.global.object.SecurityObject;
import org.z.global.zk.ServerDict;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

@SuppressWarnings("deprecation")
public class DataCollection {

	private static Hashtable<String, DB> mongoDBs = new Hashtable<String, DB>();
	private static Hashtable<String, MongoClient> mongos = new Hashtable<String, MongoClient>();
	protected static Logger logger = LoggerFactory.getLogger(DataCollection.class);
	private static boolean auth = false;
	public static MongoClientOptions mongoClientOptions = null;
	public static Builder builder = null;
	static {
		builder = MongoClientOptions.builder();
		builder.autoConnectRetry(true);
		builder.connectionsPerHost(100);
		builder.threadsAllowedToBlockForConnectionMultiplier(50);
		builder.maxWaitTime(10000);
		builder.connectTimeout(3000);
		mongoClientOptions = builder.build();
	}

	public static synchronized DB getMongoDB(String serverName, String dbName) {
		String key = serverName + "@" + dbName;
		try {
			DB db = mongoDBs.get(key);
			if (db != null && db.getStats().ok()) {
				return db;
			}
			MongoClient mongo = mongos.get(serverName);
			BasicDBObject oServer = ServerDict.self.serverBy(serverName);
			if (mongo == null) {
				if (oServer == null) {
					logger.error("**serverName=" + serverName + " not exist in DictServer");
					return db;
				}
				mongo = new MongoClient(new ServerAddress(oServer.getString("ip"), Global.MongoPort), mongoClientOptions);
				if (mongo.getConnector().isOpen()) {
					mongos.put(serverName, mongo);
				}
			}
			db = mongo.getDB(dbName);
			BasicDBObject oMongo = (BasicDBObject) oServer.get("mongo");
			if (oMongo != null) {
				String userName = oMongo.getString("user");
				String password = oMongo.getString("password");
				if (oServer.getBoolean("encrypt", false) == true) {
					String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.MongoUser, SecurityMode.Undo, userName),
							SecurityObject.create(SecurityType.MongoPassword, SecurityMode.Undo, password));
					userName = values[0];
					password = values[1];
				}
				auth = db.authenticate(userName, password.toCharArray());
				if (!auth) {
					logger.info("mongo@server[" + oServer.getString("ip") + "~" + serverName + "@" + dbName + "] auth fail.");
				} else {
					logger.info("mongo@server[" + oServer.getString("ip") + "~" + serverName + "@" + dbName + "] auth success.");
					mongoDBs.put(key, db);
				}
			}
			return db;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static synchronized DB getMongoDB(String serverIP, String dbName, String userName, String password) {
		String key = serverIP + "@" + dbName;
		try {
			DB db = mongoDBs.get(key);
			if (db != null && db.getStats().ok()) {
				return db;
			}
			MongoClient mongo = mongos.get(serverIP);
			if (mongo == null) {
				mongo = new MongoClient(new ServerAddress(serverIP, Global.MongoPort), mongoClientOptions);
				if (mongo.getConnector().isOpen()) {
					mongos.put(serverIP, mongo);
				}
			}
			db = mongo.getDB(dbName);
			auth = db.authenticate(userName, password.toCharArray());
			if (!auth) {
				logger.info("mongo@server[" + serverIP + "@" + dbName + "] auth fail.");
			} else {
				logger.info("mongo@server[" + serverIP + "@" + dbName + "] auth success.");
				mongoDBs.put(key, db);
			}
			return db;
		} catch (Exception e) {
			return null;
		}
	}

	public static DBCollection getCollection(String serverName, String dbName, String collName) {
		DB db = getMongoDB(serverName, dbName);
		return db.getCollection(collName);
	}

	public static Set<String> getCollections(String serverName, String dbName) {
		DB db = getMongoDB(serverName, dbName);
		return db.getCollectionNames();
	}

	public static void dropCollection(String serverName, String dbName, String collName) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.drop();
	}

	public static ObjectId insert(String serverName, String dbName, String collName, DBObject oField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		table.insert(oField);
		return (ObjectId) oField.get("_id");
	}

	public static void insertBatch(String serverName, String dbName, String collName, List<BasicDBObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0; i < list.size(); i++) {
			DBObject dbObject = list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.insert(listDB);
	}

	public static void insertBatch(String serverName, String dbName, String collName, BasicDBList list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0; i < list.size(); i++) {
			DBObject dbObject = (DBObject) list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.insert(listDB);
	}

	/**
	 *
	 * @param dbName
	 * @param collName
	 * @param qField
	 * @param oField
	 *
	 *
	 *            DEMO1 ------------------
	 *            c.save(BasicDBObjectBuilder.start().add("id", 1).add("x",
	 *            true).get()); update(new BasicDBObject("id", 1), new
	 *            BasicDBObject("$set", new BasicDBObject("x", 5.5))); DEMO2
	 *            --------------------------------------------------------------
	 *            -------- BasicDBObjectBuilder.start().push("a").add("x", 1)
	 *            数据记录为{ "_id" : ObjectId("4b868f7136499121ba3c2a1d"), "a" : {
	 *            "x" : 1 } }
	 *
	 *            DEMO3 正则表达式
	 *            ----------------------------------------------------
	 *            ------------------ c.insert(new BasicDBObject("x", "a"));
	 *            c.insert(new BasicDBObject("x", "A")); assertEquals(2,
	 *            c.find(new BasicDBObject("x",
	 *            Pattern.compile("a",Pattern.CASE_INSENSITIVE))).itcount())
	 */
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

	public static void findAndModify(String serverName, String dbName, String collName, DBObject qField, DBObject oField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.findAndModify(qField, oField);
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
		for (int i = 0; i < list.size(); i++) {
			table.remove(list.get(i));
		}
	}

	public static long getCollectionCount(String serverName, String dbName, String collName) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return 0;
		}
		return table.getCount();
	}

	public static long getCount(String serverName, String dbName, String collName, DBObject qField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return 0;
		}
		return table.getCount(qField);
	}

	public static DBObject findById(String serverName, String dbName, String collName, String id) {
		ObjectId objectId = new ObjectId(id);
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		return table.findOne(objectId);
	}

	public static DataResult find(String serverName, String dbName, String collName, DBObject qField, DBObject sortField, int offset, int count) {
		return find(serverName, dbName, collName, qField, sortField, null, offset, count);
	}

	public static DataResult find(String serverName, String dbName, String collName, DBObject qField, DBObject sortField) {
		return find(serverName, dbName, collName, qField, sortField, null, 0, 0);
	}

	/**
	 *
	 * @param dbName
	 * @param collName
	 * @param qField
	 * @param returnField定义返回的数据格式
	 * @return
	 */
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

	public static DBObject findOne(String serverName, String dbName, String collName, DBObject qField, DBObject returnField, DBObject orderBy) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		return table.findOne(qField, returnField, orderBy);
	}

	public static DBObject findOne(String serverName, String dbName, String collName, DBObject qField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return null;
		}
		return table.findOne(qField);
	}

	public static long findCount(String serverName, String dbName, String collName) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return 0;
		}
		return table.count();
	}

	public static BasicDBList findAll(String serverName, String dbName, String collName, DBObject returnField) {
		return findAll(serverName, dbName, collName, new BasicDBObject(), returnField);
	}

	public static BasicDBList findAll(String serverName, String dbName, String collName, DBObject reqField, DBObject returnField) {
		return findAll(serverName, dbName, collName, reqField, returnField, null);
	}

	public static BasicDBList findAll(String serverName, String dbName, String collName, DBObject reqField, DBObject returnField, DBObject sortField) {
		DBCollection table = getCollection(serverName, dbName, collName);
		BasicDBList list = new BasicDBList();
		if (table == null) {
			return list;
		}
		DBCursor cur = null;
		try {
			cur = table.find(reqField, returnField);
			if (sortField != null) {
				cur.sort(sortField);
			}
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

	/**
	 * @param dbName
	 * @param collName
	 * @param keys
	 *            {age:1, name:-1} 1=增 -1=降 BasicDBObjectBuilder
	 *            .start().append("x", 1).append("y", 1).get() 注意1和-1必须是整数
	 * @param unique
	 */
	public static void createIndex(String serverName, String dbName, String collName, String indexName, BasicDBObject keys, final boolean unique) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.ensureIndex(keys, indexName, unique);
	}

	public static void dropIndex(String serverName, String dbName, String collName, DBObject keys) {
		DBCollection table = getCollection(serverName, dbName, collName);
		if (table == null) {
			return;
		}
		table.dropIndex(keys);
	}

	public static CommandResult runCommand(String serverName, String dbName, BasicDBObject cmd) {
		DB db = getMongoDB(serverName, dbName);
		if (db == null) {
			return null;
		}
		return db.command(cmd);
	}

	protected static CommandResult findAndModify(String serverName, String dbName, String collName, BasicDBObject qField, BasicDBObject oUpdate) {
		return findAndModify(serverName, dbName, collName, qField, oUpdate);
	}

	protected static CommandResult findAndModify(String serverName, String dbName, String collName, BasicDBObject qField, BasicDBObject oUpdate, BasicDBObject returnFields) {
		BasicDBObject cmd = new BasicDBObject().append("findandmodify", collName).append("query", qField).append("update", oUpdate);
		if (returnFields != null) {
			cmd.append("fields", returnFields);
		}
		return runCommand(serverName, dbName, cmd);
	}

	public static void main(String[] args) {
		int mode = 13;
		String dbName = null;
		String collName = null;
		switch (mode) {
		case -1:
			System.out.println(DataCollection.findAll(Const.defaultMongoServer, Const.defaultMongoDB, "conversations", BasicDBObjectBuilder.start().append("userId", 1).get()));
			break;
		case 1:
			DataCollection.dropIndex(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("x", 1).get());
			DataCollection.dropIndex(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("y", 1).get());
			break;
		case 2:
			long currentTime = System.currentTimeMillis();
			DBObject oField = DataCollection.findOne(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("x", "1001").append("y", "1001").get());
			System.out.println("used:" + (System.currentTimeMillis() - currentTime));
			break;
		case 3:
			oField = DataCollection.findById(Const.defaultMongoServer, dbName, collName, "4b85d947e2c3c2421af79009");
			System.out.println("findById:" + oField.toString());
			oField = DataCollection.findOne(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("x", "1").get());
			System.out.println("Match:" + oField.toString());
			break;
		case 4:
			DataCollection.createIndex(Const.defaultMysqlServer, dbName, collName, "keyIdx", new BasicDBObject().append("mail", 1), true);
			DataCollection.createIndex(Const.defaultMysqlServer, dbName, collName, "tagIndex", new BasicDBObject().append("tags", 1), true);
			break;
		case 5:
			DataCollection.update(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("x", "2").get(), BasicDBObjectBuilder.start().append("z", "2").get(), true);

			break;
		case 6:
			DataCollection.updateMulti(Const.defaultMongoServer, dbName, collName, new BasicDBObject(), (DBObject) JSON.parse("{'jason':'fff'}"));
			break;
		case 7:
			BasicDBList list = DataCollection.findAll(Const.defaultMongoServer, dbName, collName, null);
			for (int i = 0; i < list.size(); i++) {
				System.out.println("List[" + i + "]" + list.get(i).toString());
			}
			break;
		case 8:
			DataCollection.find(Const.defaultMongoServer, Const.defaultMongoDB, "conversation_replys", BasicDBObjectBuilder.start().add("_id", "keyId").get(),
					BasicDBObjectBuilder.start().add("_id", "timestamp").get(), BasicDBObjectBuilder.start().add("_id", "keyId").get(), 0, 0);
			break;
		case 9:
			int price = 60;
			BasicDBObject oUpdate = new BasicDBObject("$set", new BasicDBObject().append("price", price));
			CommandResult result = DataCollection.findAndModify(Const.defaultMongoServer, Const.defaultMongoDB, "test",
					new BasicDBObject("day", 2).append("fromCity", "北京2").append("price", new BasicDBObject("$gt", price)), oUpdate);
			System.out.println(result.toString());
			break;
		/* 更新数组 */
		case 12:
			// DataCollection.update(dbName, collName, o1, BasicDBObjectBuilder
			// .start().push("$unset").append("list", "1").get());
			DataCollection.update(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("z", "2").get(),
					BasicDBObjectBuilder.start().push("$push").append("list", "3").get(), true);
			DataCollection.update(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("z", "2").get(), BasicDBObjectBuilder.start().push("$pop").append("list", 1).get(),
					true);
			DataCollection.update(Const.defaultMongoServer, dbName, collName, BasicDBObjectBuilder.start().append("z", "2").get(),
					BasicDBObjectBuilder.start().push("$pull").append("list", "2").get(), true);

			break;
		/* 删除所有的COLLECTION */
		case 13:
			String[] servers = new String[] { "dict", "x1", "x2" };
			for (int i = 0; i < servers.length; i++) {
				String serverName = servers[i];
				Set<String> names = DataCollection.getCollections(serverName, Const.defaultMongoDB);
				for (Iterator<String> t = names.iterator(); t.hasNext();) {
					String name_ = t.next();
					if (name_.startsWith("msg") || name_.startsWith("contact_")) {
						System.out.println("drop:" + name_);
						DataCollection.dropCollection(servers[i], Const.defaultMongoDB, name_);
					}
				}
			}
			break;
		}
		System.out.println("done!");
	}
}
