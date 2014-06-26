package org.z.store.mongdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.store.intf.StoreDB;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class MongoDB implements StoreDB {

	protected static Logger logger = LoggerFactory.getLogger(MongoDB.class);
	private boolean auth = false;
	private MongoClientOptions mongoClientOptions = null;
	private DB db;

	public static MongoDB build() {
		return new MongoDB();
	}

	@SuppressWarnings("deprecation")
	public void init(BasicDBObject obj) {
		Builder builder = MongoClientOptions.builder();
		builder.autoConnectRetry(true);
		builder.connectionsPerHost(100);
		builder.threadsAllowedToBlockForConnectionMultiplier(50);
		builder.maxWaitTime(10000);
		builder.connectTimeout(3000);
		mongoClientOptions = builder.build();
		getDB(obj);
	}

	@SuppressWarnings({ "deprecation" })
	public void getDB(BasicDBObject obj) {
		try {
			String ip =obj.getString("ip");
			String dbName = obj.getString("dbName");
			MongoClient mongo = new MongoClient(new ServerAddress(ip, 19753), mongoClientOptions);
			db = mongo.getDB(dbName);
			BasicDBObject oMongopass = (BasicDBObject) obj.get("pass");
			if (oMongopass != null) {
				auth = db.authenticate(oMongopass.getString("user"), oMongopass.getString("password").toCharArray());
				if (!auth) {
//					logger.info("mongo@server[" + ip + "~" + dbName + "] auth fail.");
				} else {
//					logger.info("mongo@server[" + ip + "~" + dbName + "] auth success.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public DBCollection getCollection(String collName) {
		return db.getCollection(collName);
	}

	public Set<String> getCollections() {
		return db.getCollectionNames();
	}

	public void dropCollection(String collName) {
		DBCollection table = getCollection(collName);
		if (table == null) {
			return;
		}
		table.drop();
	}

	public ObjectId insert(String collName, DBObject oField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		table.insert(oField);
		return (ObjectId) oField.get("_id");
	}

	public void insertBatch(String collName, List<BasicDBObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0; i < list.size(); i++) {
			DBObject dbObject = list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		table.insert(listDB);
	}

	public void insertBatchDB(String collName, List<DBObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0; i < list.size(); i++) {
			DBObject dbObject = list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		table.insert(listDB);
	}

	public void insertBatch(String collName, BasicDBList list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0; i < list.size(); i++) {
			DBObject dbObject = (DBObject) list.get(i);
			listDB.add(dbObject);
		}
		DBCollection table = this.getCollection(collName);
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
	@SuppressWarnings("deprecation")
	public boolean update(String collName, DBObject qField, DBObject oField, boolean upsert) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return false;
		}
		WriteResult wr = table.update(qField, oField, upsert, false);
		return wr.getLastError().isEmpty() == true;
	}

	@SuppressWarnings("deprecation")
	public boolean update(String collName, DBObject qField, DBObject oField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return false;
		}
		WriteResult wr = table.update(qField, oField);
		return wr.getLastError().isEmpty() == true;
	}

	public void updateMulti(String collName, DBObject qField, DBObject oField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		table.updateMulti(qField, oField);
	}

	public void remove(String collName, DBObject oField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		table.remove(oField);
	}

	public void removeBatch(String collName, List<DBObject> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			table.remove(list.get(i));
		}
	}

	public long getCollectionCount(String collName) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return 0;
		}
		return table.getCount();
	}

	public long getCount(String collName, DBObject qField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return 0;
		}
		return table.getCount(qField);
	}

	public DBObject findById(String collName, String id) {
		ObjectId objectId = new ObjectId(id);
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		return table.findOne(objectId);
	}

	public DataResult find(String collName, DBObject qField, DBObject sortField, int offset, int count) {
		return find(collName, qField, sortField, null, offset, count);
	}

	public DataResult find(String collName, DBObject qField, DBObject sortField) {
		return find(collName, qField, sortField, null, 0, 0);
	}

	public DataResult find(String collName, DBObject qField, DBObject sortField, DBObject returnField) {
		return find(collName, qField, null, returnField, 0, 0);
	}

	public DBCursor find(String collName, DBObject sortField, int option) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = table.find().sort(sortField).addOption(option);
		return cur;
	}

	public DBCursor find(String collName, DBObject qField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = table.find(qField);
		return cur;
	}

	public DBCursor findAll(String collName) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		DBCursor cur = table.find();
		return cur;
	}

	public DBCursor find(String collName, DBObject qField, DBObject sortField, int option) {
		DBCollection table = this.getCollection(collName);
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

	/**
	 * 
	 * @param dbName
	 * @param collName
	 * @param qField
	 * @param returnField定义返回的数据格式
	 * @return
	 */
	public DataResult find(String collName, DBObject qField, DBObject sortField, DBObject returnField, int offset, int count) {
		DBCollection table = this.getCollection(collName);
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

	public DataResult findByRandom(String collName, DBObject qField, DBObject sortField, DBObject returnField, int count) {
		DBCollection table = this.getCollection(collName);
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

	public DBObject findOne(String collName, DBObject qField, DBObject returnField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		return table.findOne(qField, returnField);
	}

	public DBObject findOne(String collName, DBObject qField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		return table.findOne(qField);
	}

	public void findAndModify(String collName, BasicDBObject query, BasicDBObject update) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		table.findAndModify(query, update);
	}

	public long findCount(String collName) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return 0;
		}
		return table.count();
	}

	public List<DBObject> findAll(String collName, DBObject returnField) {
		DBCollection table = this.getCollection(collName);
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
			for (; cur.hasNext();) {
				DBObject obj = cur.next();
				list.add(obj);
			}
		} finally {
			cur.close();
		}
		return list;
	}

	public List<DBObject> findAll(String collName, DBObject qField, DBObject returnField) {
		DBCollection table = this.getCollection(collName);
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
			for (; cur.hasNext();) {
				DBObject obj = cur.next();
				list.add(obj);
			}
		} finally {
			cur.close();
		}
		return list;
	}

	public List<DBObject> findPage(String collName, DBObject returnField, int skip, int limit) {
		DBCollection table = this.getCollection(collName);
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

	public List<DBObject> findPage(String collName, int skip, int limit) {
		DBCollection table = this.getCollection(collName);
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

	public int selectCount(String collName, DBObject qField) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return 0;
		}
		DBCursor cur = table.find(qField, null);
		int result = cur.count();
		cur.close();
		return result;
	}

	/**
	 * 
	 * @param dbName
	 * @param collName
	 * @param keys
	 *            {age:1, name:-1} 1=增 -1=降 BasicDBObjectBuilder
	 *            .start().append("x", 1).append("y", 1).get() 注意1和-1必须是整数
	 * @param unique
	 */


	public void dropIndex(String collName, DBObject keys) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return;
		}
		table.dropIndex(keys);
	}

	public CommandResult runCommand(String dbName, BasicDBObject cmd) {
		if (db == null) {
			return null;
		}
		return db.command(cmd);
	}

	public MapReduceOutput mapReduce(String collName, String map, String reduce, DBObject query) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		MapReduceCommand cmd = new MapReduceCommand(table, map, reduce, null, MapReduceCommand.OutputType.INLINE, query);
		return table.mapReduce(cmd);
	}

	public MapReduceOutput mapReduce(String collName, String map, String reduce, String outputCollection, OutputType outputType, DBObject query) {
		DBCollection table = this.getCollection(collName);
		if (table == null) {
			return null;
		}
		if (outputType == null) {
			outputType = MapReduceCommand.OutputType.REPLACE;
		}
		MapReduceCommand cmd = new MapReduceCommand(table, map, reduce, outputCollection, outputType, query);
		return table.mapReduce(cmd);
	}

	public static void main(String[] args) {
		int mode = -2;
		String collName = null;
		MongoDB mongo = MongoDB.build();
		switch (mode) {
		case -1:
			System.out.println(mongo.findAll("conversations", BasicDBObjectBuilder.start().append("userId", 1).get()));
			break;
		case 1:
			mongo.dropIndex(collName, BasicDBObjectBuilder.start().append("x", 1).get());
			mongo.dropIndex(collName, BasicDBObjectBuilder.start().append("y", 1).get());
			break;
		case 2:
			long currentTime = System.currentTimeMillis();
			DBObject oField = mongo.findOne(collName, BasicDBObjectBuilder.start().append("x", "1001").append("y", "1001").get());
			System.out.println("used:" + (System.currentTimeMillis() - currentTime));
			break;
		case 3:
			oField = mongo.findById(collName, "4b85d947e2c3c2421af79009");
			System.out.println("findById:" + oField.toString());
			oField = mongo.findOne(collName, BasicDBObjectBuilder.start().append("x", "1").get());
			System.out.println("Match:" + oField.toString());
			break;
		case 5:
			mongo.update(collName, BasicDBObjectBuilder.start().append("x", "2").get(), BasicDBObjectBuilder.start().append("z", "2").get(), true);

			break;
		case 6:
			mongo.updateMulti(collName, new BasicDBObject(), (DBObject) JSON.parse("{'jason':'fff'}"));
			break;
		case 7:
			List<DBObject> list = mongo.findAll(collName, null);
			for (int i = 0; i < list.size(); i++) {
				System.out.println("List[" + i + "]" + list.get(i).toString());
			}
			break;
		case 8:
			mongo.find("conversation_replys", BasicDBObjectBuilder.start().add("_id", "keyId").get(), BasicDBObjectBuilder.start().add("_id", "timestamp").get(),
					BasicDBObjectBuilder.start().add("_id", "keyId").get(), 0, 0);
			break;
		case 9:
			int price = 60;
			BasicDBObject oUpdate = new BasicDBObject("$set", new BasicDBObject().append("price", price));
			mongo.findAndModify("test", new BasicDBObject("day", 2).append("fromCity", "北京2").append("price", new BasicDBObject("$gt", price)), oUpdate);
			break;
		/* 更新数组 */
		case 12:
			// DataCollection.update(dbName, collName, o1, BasicDBObjectBuilder
			// .start().push("$unset").append("list", "1").get());
			mongo.update(collName, BasicDBObjectBuilder.start().append("z", "2").get(), BasicDBObjectBuilder.start().push("$push").append("list", "3").get(), true);
			mongo.update(collName, BasicDBObjectBuilder.start().append("z", "2").get(), BasicDBObjectBuilder.start().push("$pop").append("list", 1).get(), true);
			mongo.update(collName, BasicDBObjectBuilder.start().append("z", "2").get(), BasicDBObjectBuilder.start().push("$pull").append("list", "2").get(), true);

			break;
		/* 删除所有的COLLECTION */
		case 13:
			Set<String> names = mongo.getCollections();
			for (Iterator<String> t = names.iterator(); t.hasNext();) {
				String name_ = t.next();
				if (name_.startsWith("F_")) {
					System.out.println("drop:" + name_);
					mongo.dropCollection(name_);
				}
			}
			names = mongo.getCollections();
			for (Iterator<String> t = names.iterator(); t.hasNext();) {
				String name_ = t.next();
				if (name_.startsWith("F_")) {
					System.out.println("drop:" + name_);
					mongo.dropCollection(name_);
				}
			}
			break;
		}
		System.out.println("done!");
	}


}
