package org.z.store.totem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.store.intf.StoreDB;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.mongodb.BasicDBObject;

public class RedisPool implements StoreDB {
	protected static Logger logger = LoggerFactory.getLogger(RedisPool.class);
	public ShardedJedisPool pool = null;
	private JedisPoolConfig cfg;

	public static RedisPool build() {
		return new RedisPool();
	}

	public void init(BasicDBObject reids) {
		cfg = new JedisPoolConfig();
		cfg.setMaxWaitMillis(10000);
		this.getDB(reids);
	}

	@Override
	public void getDB(BasicDBObject obj) {

		String ips = obj.getString("ip");
		String dbName = obj.getString("dbname");
		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
			for (String ip : ips.split(",")) {
			shards.add(new JedisShardInfo(ip));
			logger.info("RedisPool register id=[{}] IP[{}]", new Object[] { dbName, ip });
		}
		pool = new ShardedJedisPool(cfg, shards);
	}

	public String get(String key) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.get(key);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void expire(String key, int seconds) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.expire(key, seconds);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String hget(String key, String field) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.hget(key, field);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Map<String, String> hgetAll(String key) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.hgetAll(key);
		} finally {
			pool.returnResource(jedis);
		}
	}
	
	public Long lpush(String key, String value) {
		ShardedJedis jedis = pool.getResource();
		if(jedis == null)
			return null;
		try {
			return jedis.lpush(key, value);
		}
		finally {
			pool.returnResource(jedis);
		}
	}
	
	public Long rpush(String key, String value) {
		ShardedJedis jedis = pool.getResource();
		if(jedis == null)
			return null;
		try {
			return jedis.rpush(key, value);
		}
		finally {
			pool.returnResource(jedis);
		}
	}
	
	public String rpop(String key) {
		ShardedJedis jedis = pool.getResource();
		if(jedis == null)
			return null;
		try {
			return jedis.rpop(key);
		}
		finally {
			pool.returnResource(jedis);
		}
	}
	
	public Long lrem(String key, int count, String value) {
		ShardedJedis jedis = pool.getResource();
		if(jedis == null)
			return null;
		try {
			return jedis.lrem(key, count, value);
		}
		finally {
			pool.returnResource(jedis);
		}
	}
	
	public String lpop(String key) {
		ShardedJedis jedis = pool.getResource();
		if(jedis == null){
			return null;
		}
		try {
			return jedis.lpop(key);
		}
		finally {
			pool.returnResource(jedis);
		}
	}

	public List<String> lrange(String key, int start, int end) {
		ShardedJedis jedis = pool.getResource();
		List<String> list = null;
		if (jedis == null)
			return null;
		try {
			list = jedis.lrange(key, start, end);
		} finally {
			pool.returnResource(jedis);
		}
		return list;
	}

	public long llen(String key) {
		ShardedJedis jedis = pool.getResource();
		Long len = (long) 0 ;
		if (jedis == null)
			return 0;
		try {
			 len= jedis.llen(key);
		} finally {
			pool.returnResource(jedis);
		}
		return len;
	}

	public String set(String key, String value) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.set(key, value);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public String hmset(String key, Map<String, String> hash) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return null;
		try {
			return jedis.hmset(key, hash);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Long hset(String key, String field, String value) {
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
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.del(key);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void hdelete(String key, String fields) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.hdel(key, fields);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void zadd(String key, double score, String member) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.zadd(key, score, member);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public void zincrby(String key, double score, String member) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.zincrby(key, score, member);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public Set<String> zrevrange(String key, long start, long end) {
		ShardedJedis jedis = pool.getResource();
		Set<String> set = null;
		if (jedis == null)
			return null;
		try {
			set = jedis.zrevrange(key, start, end);
		} finally {
			pool.returnResource(jedis);
		}
		return set;
	}

	public void incrBy(String key, long integer) {
		ShardedJedis jedis = pool.getResource();
		if (jedis == null)
			return;
		try {
			jedis.incrBy(key, integer);
		} finally {
			pool.returnResource(jedis);
		}
	}

	public boolean hexists(String key, String field) {
		ShardedJedis jedis = pool.getResource();
		boolean exists = false;
		if (jedis == null)
			return exists;
		try {
			exists = jedis.hexists(key, field);
		} finally {
			pool.returnResource(jedis);
		}
		return exists;
	}

	public boolean exists(String key) {
		ShardedJedis jedis = pool.getResource();
		boolean exists = false;
		if (jedis == null)
			return exists;
		try {
			exists = jedis.exists(key);
		} finally {
			pool.returnResource(jedis);
		}
		return exists;
	}

	public static void main(String[] args) {
		Jedis jedis = new Jedis("10.58.50.107", 6379);
		/*
		jedis.zadd("test1", 2, "2");
		jedis.zadd("test1", 3, "3");
		jedis.zadd("test1", 4, "4");
		jedis.zadd("test1", 5, "5");
		jedis.zadd("test1", 6, "6");
		jedis.zincrby("test1", 10, "2");
		System.out.println(jedis.zrevrange("test1", 0, 6));
		*/

		jedis.lpush("key", "v1");
		jedis.lpush("key1", "v2");
		jedis.lpush("key", "v2");
		jedis.lpush("key2", "v1");
		jedis.lpush("key", "v3");
		jedis.lpush("key", "v3");

		List<String> v = jedis.lrange("qss", 0, jedis.llen("qss"));
		Iterator<String> list = v.iterator();
		while(list.hasNext()){
			System.out.println(list.next());
		}
		
		
		
	}

}

