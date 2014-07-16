package org.z.store.redis;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class RedisSocket {
	public boolean isAlive = true;
	public String id = null;
	protected String uri = null;
	public Jedis instance = null;
	protected static Logger logger = LoggerFactory.getLogger(RedisSocket.class);

	public RedisSocket(String uri) {
		this.uri = uri;
	}

	public void init() {
		try {
			instance = new Jedis(uri);
			instance.connect();
			isAlive = true;
		} catch (Exception e) {
			logger.error("init", e);
			isAlive = false;
		}
	}

	public String get(String key) {
		return this.instance.get(key);
	}

	public long setnx(String key, String value) {
		return this.instance.setnx(key, value);
	}

	public String getSet(String key, String value) {
		return this.instance.getSet(key, value);
	}

	public String set(String prefix, String key, String content) {
		return this.set(prefix + "@" + key, content);
	}
	public Boolean exists(String key) {
		return this.instance.exists(key);
	}

	public Set<String> keys(String pattern) {
		return this.instance.keys(pattern);
	}
	public void delete(String key) {
		this.instance.del(key);
	}

	public String set(String key, String content) {
		return this.instance.set(key, content);
	}
	public void expire(String key, int seconds) {
		this.instance.expire(key, seconds);
	}

	public void destroy() {
		this.close();
	}

	public void close() {
		if (instance == null)
			return;
		try {
			this.instance.disconnect();
		} catch (Exception e) {
		}
	}

}
