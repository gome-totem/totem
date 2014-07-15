package org.z.store.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

public class RedisFactory {

	private static class RedisPoolableFactory implements PoolableObject<RedisSocket> {

		protected String ip = null;

		public RedisPoolableFactory(String ip) {
			this.ip = ip;
		}

		@Override
		public void activate(RedisSocket arg0) throws PoolException {
		}

		@Override
		public void destroy(RedisSocket arg0) {
			RedisFactory.logger.info("Redis Connect[{}] destory.", new Object[] { arg0.hashCode() });
			arg0.destroy();
		}

		@Override
		public RedisSocket make() throws PoolException {
			return RedisFactory.create(ip);
		}

		@Override
		public void passivate(RedisSocket arg0) {
		}

		@Override
		public boolean validate(RedisSocket arg0) {
			return arg0.isAlive;
		}

	}

	protected static ConcurrentHashMap<String, ObjectPool<RedisSocket>> poolByUri = new ConcurrentHashMap<String, ObjectPool<RedisSocket>>();
	protected static ZContext context = new ZContext();
	protected static AtomicInteger allocateId = new AtomicInteger(1);
	protected static Logger logger = LoggerFactory.getLogger(RedisFactory.class);

	public static RedisSocket get(String ip) {
		ObjectPool<RedisSocket> pool = poolByUri.get(ip);
		if (pool == null) {
			PoolSettings<RedisSocket> setting = new PoolSettings<RedisSocket>(new RedisPoolableFactory(ip)).validateWhenReturn(true);
			setting.min(0).max(0).maxIdle(30000).validateWhenReturn(true);
			pool = setting.pool();
			poolByUri.put(ip, pool);
		}
		try {
			return pool.getObj();
		} catch (PoolException e) {
			logger.error("get", e);
			return null;
		}
	}

	public static void ret(String uri, RedisSocket obj) {
		ObjectPool<RedisSocket> pool = poolByUri.get(uri);
		if (pool == null) {
			return;
		}
		pool.returnObj(obj);
	}

	public static RedisSocket create(String ip) {
		RedisSocket client = new RedisSocket(ip);
		client.init();
		return client;
	}

}
