package org.z.store.totem;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.store.intf.StoreDB;
import org.z.store.mongdb.MongoDB;
import org.z.store.redis.RedisPool;
import org.z.store.totem.StoreBuilder.StoreAction;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.mongodb.BasicDBObject;

public class TotemDB {
	public static enum StoreGroup {
		DictGroup() {
			@Override
			public RedisPool Redis() {
				return TotemDB.DictCluster.getInstance(RedisPool.class);
			}
			@Override
			public MongoDB Mongo() {
				return TotemDB.DictCluster.getInstance(MongoDB.class);
			}
			@Override
			public StoreDB Mysql() {
				return null;
			}
		},
		LOGGroup() {
			@Override
			public StoreDB Redis() {
				return TotemDB.LogCluster.getInstance(RedisPool.class);
			}
			@Override
			public StoreDB Mongo() {
				return TotemDB.LogCluster.getInstance(MongoDB.class);
			}
			@Override
			public StoreDB Mysql() {
				return null;
			}
		},
		DataGroup() {
			@Override
			public StoreDB Redis() {
				return TotemDB.DataCluster.getInstance(RedisPool.class);
			}
			@Override
			public StoreDB Mongo() {
				return TotemDB.DataCluster.getInstance(MongoDB.class);
			}
			@Override
			public StoreDB Mysql() {
				return null;
			}
		};
		public abstract StoreDB Redis();

		public abstract StoreDB Mongo();

		public abstract StoreDB Mysql();
	}
	protected static Logger logger = LoggerFactory.getLogger(TotemDB.class);
	private ConcurrentMap<String, String> settings = null;
	private static ClassToInstanceMap<StoreDB> DataCluster = MutableClassToInstanceMap.create();
	private static ClassToInstanceMap<StoreDB> DictCluster = MutableClassToInstanceMap.create();
	private static ClassToInstanceMap<StoreDB> LogCluster = MutableClassToInstanceMap.create();
	public TotemDB(ConcurrentMap<String, String> settings) {
		this.settings = settings;
	}

	public void initDBFactory() {
		if (!checkConfig()) {
			logger.info("TotemDB init  is fail");
			return;
		}
		BasicDBObject oServer =null ;//ServerDict.self.serverBy(settings.get(StoreAction.usr.toString()));
		this.readDBInfo(oServer);
	}

	public void readDBInfo(BasicDBObject oServer) {
		for (String key : oServer.keySet()) {
				initDB(key, oServer);
		}
	}

	private void initDB(String key ,BasicDBObject obj) {
		// switch (key) {
		// case "dict":
		// MongoDB dictMongo = MongoDB.build();
		// dictMongo.init(obj);
		// DictCluster.put(MongoDB.class, dictMongo);
		// RedisPool dictRedis = RedisPool.build();
		// dictRedis.init(obj);
		// DictCluster.put(RedisPool.class, dictRedis);
		// break;
		// case "data":
		// MongoDB dataMongo = MongoDB.build();
		// dataMongo.init(obj);
		// DataCluster.put(MongoDB.class, dataMongo);
		// RedisPool dataRedis = RedisPool.build();
		// dataRedis.init(obj);
		// DataCluster.put(RedisPool.class, dataRedis);
		// break;
		// default:
		// MongoDB logMongo = MongoDB.build();
		// logMongo.init(obj);
		// LogCluster.put(MongoDB.class, logMongo);
		// RedisPool logRedis = RedisPool.build();
		// logRedis.init(obj);
		// LogCluster.put(RedisPool.class, logRedis);
		// break;
		// }
	}
	private boolean checkConfig() {
		if (!settings.containsKey(StoreAction.usr)) {
			logger.info("usr is null");
			return false;
		}
//		if (ServerDict.self.serverBy(settings.get(StoreAction.usr)) == null) {
//			logger.info("usrinfo is null");
//			return false;
//		}
		return true;
	}

	public static void main(String[] args) {
		StoreGroup.DataGroup.Redis();
	}

}
