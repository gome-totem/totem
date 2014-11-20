package org.z.store.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

class ServerIpCompare implements Comparator<JedisShardInfo> {
	@Override
	public int compare(JedisShardInfo o1, JedisShardInfo o2) {
		return o1.getHost().compareTo(o2.getHost());
	}
}

public class RedisPool {
	protected Logger logger = LoggerFactory.getLogger(RedisPool.class);
	protected boolean state = true;
	public List<JedisShardInfo> shards = null;
	public ShardedJedisPool pool = null;
	protected String name = null;
	public static HashMap<String, RedisPool> instances = new HashMap<String, RedisPool>();

	public static RedisPool use() {
		return use("default");
	}

	public static RedisPool use(String name) {
		RedisPool instance = instances.get(name);
		if (instance != null) {
			return instance;
		}
		RedisPool pool = new RedisPool();
		if (pool.init(name) == true) {
			instances.put(name, pool);
		}
		return pool;
	}

	public RedisPool() {
		shards = new ArrayList<JedisShardInfo>();
	}

	public boolean init(String id) {
		state = false;
		this.name = id;
		BasicDBObject oPool = ServerDict.self.redisPoolBy(id);
		if (oPool == null) {
			return state;
		}
		BasicDBList servers = (BasicDBList) oPool.get("servers");
		for (Object s : servers) {
			String server = String.valueOf(s);
			String ip = ServerDict.self.serverIPBy(server);
			if (StringUtil.isEmpty(ip))
				continue;
			shards.add(new JedisShardInfo(ip));
			logger.info("RedisPool register Name=[{}] IP[{}]", new Object[] { name, server + "@" + ip });
		}
		state = shards.size() != 0;
		if (state == false) {
			return state;
		}
		JedisPoolConfig cfg = new JedisPoolConfig();
		cfg.setMaxIdle(1000 * 60);
		Collections.sort(shards, new ServerIpCompare());
		pool = new ShardedJedisPool(cfg, shards);
		state = true;
		return state;
	}

	public String get(String key) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return null;
		}
		if (shards == null || shards.size() == 0) {
			return null;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.get(key);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String hget(String key, String field) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return null;
		}
		if (shards == null || shards.size() == 0) {
			return null;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.hget(key, field);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Map<String, String> hget(String key) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return null;
		}
		if (shards == null || shards.size() == 0) {
			return null;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.hgetAll(key);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public long hdel(String key, String[] fields) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return 0;
		}
		if (shards == null || shards.size() == 0) {
			return 0;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return 0;
		try {
			return jedis.hdel(key, fields);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public boolean hexists(String key, String field) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return false;
		}
		if (shards == null || shards.size() == 0) {
			return false;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return false;
		try {

			return jedis.hexists(key, field);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public long incrBy(String key, long integer) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return 0;
		}
		if (shards == null || shards.size() == 0) {
			return 0;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return 0;
		try {
			return jedis.incrBy(key, integer);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public long decrBy(String key, long integer) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return 0;
		}
		if (shards == null || shards.size() == 0) {
			return 0;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return 0;
		try {

			return jedis.decrBy(key, integer);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long hset(String key, String field, String value) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return null;
		}
		if (shards == null || shards.size() == 0) {
			return null;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.hset(key, field, value);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void delete(String key) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return;
		}
		if (shards == null || shards.size() == 0) {
			return;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.del(key);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public DBObject getDBObject(String key) {
		String content = get(key);
		if (StringUtil.isEmpty(content)) {
			return null;
		}
		return (DBObject) JSON.parse(content);
	}

	public String set(String key, String value) {
		if (state == false) {
			logger.info("RedisPool[" + name + "] is not ready.");
			return null;
		}
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.set(key, value);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public static void main(String[] args) {
		int mode = 0;
		switch (mode) {
		case 0:
			for (int i = 0; i < 100; i++) {
				System.out.println("index=" + i);
				RedisPool.use().set(String.valueOf(i), "value=" + i);
			}
			break;
		case 1:
			
//			RedisPool

			break;
		}
		System.out.println("done!");
	}
}
