package org.z.core.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global.CodeType;
import org.z.global.environment.Const;
import org.z.global.lock.LockHandlerIntf;
import org.z.global.lock.Locks;
import org.z.global.util.ShortUtil;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;
import org.z.store.mongdb.DataCollection;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBObject;

public class ShortId {

	protected static final Logger logger = LoggerFactory.getLogger(ShortId.class);
	protected static ConcurrentHashMap<String, String> caches = new ConcurrentHashMap<String, String>();

	protected static class LockShort implements LockHandlerIntf {

		public String id = null;
		public String name = null;
		public int index = 0;

		public LockShort(int index, String name) {
			this.index = index;
			this.name = name;
		}

		@Override
		public void execute() {
			int size = ShortUtil.getCodeSize(name, 3);
			String first = StringUtil.zeroString(size);
			String lastId = RedisPool.use("dict").get("last" + name);
			if (StringUtil.isEmpty(lastId)) {
				lastId = first;
			}
			lastId = ShortUtil.execute(lastId);
			id = index + lastId;
			String content = RedisPool.use("dict").get(id);
			while (StringUtil.isEmpty(content) == false) {
				lastId = ShortUtil.execute(lastId);
				id = index + lastId;
				content = RedisPool.use("dict").get(id);
			}
			RedisPool.use("dict").set("last" + name, lastId);
		}
	}

	public static String create(FacetInfo info, String value) {
		String key = info.name + value;
		String content = caches.get(key);
		if (StringUtil.isEmpty(content) == false) {
			return content;
		}
		content = RedisPool.use("dict").get(key);
		if (StringUtil.isEmpty(content) == false) {
			caches.put(key, content);
			return content;
		}
		String id = null;
		boolean find = false;
		int size = ShortUtil.getCodeSize(info.name, 3);
		for (int i = 0; i < 4; i++) {
			id = info.id + ShortUtil.execute(value, size, i);
			content = RedisPool.use("dict").get(id);
			if (StringUtil.isEmpty(content) == true) {
				find = true;
				break;
			}
		}
		if (find == false) {
			String lockPath = "/locks/short" + info.name;
			LockShort lock = new LockShort(info.id, info.name);
			Locks locks = new Locks(ServerDict.self.zoo(), lockPath, lock);
			try {
				locks.doWork(1, TimeUnit.MINUTES);
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
			id = lock.id;
		}
		caches.put(key, id);
		RedisPool.use("dict").set(key, id);
		RedisPool.use("dict").set(id, value);
		BasicDBObject oRecord = new BasicDBObject().append("id", id).append("type", info.name).append("value", value);
		DataCollection.insert(Const.defaultDictMongo, Const.defaultDictMongoDB, "shorts", oRecord);
		return id;
	}

	public static void main(String[] args) {
		int mode = 0;
		for (int i = 0; i < 100; i++) {
			switch (mode) {
			case 0:
				String key = "city" + i;
				String id = RedisPool.use("dict").get(key);
				RedisPool.use("dict").delete(key);
				RedisPool.use("dict").delete(id);
				break;
			case 1:
				ShortId.create(FacetInfo.byId.get(CodeType.location.ordinal()), String.valueOf(i));
				break;
			}
		}
		if (mode == 0) {
			RedisPool.use("dict").delete("lastcity");
		}
	}

}
