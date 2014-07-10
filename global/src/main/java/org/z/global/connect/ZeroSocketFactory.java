package org.z.global.connect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObject;
import nf.fr.eraasoft.pool.impl.AbstractPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global;
import org.z.global.zk.ServerDict.NodeType;
import org.zeromq.ZContext;

public class ZeroSocketFactory {

	private static class SocketPoolableFactory implements PoolableObject<ZeroSocket> {

		protected String uri = null;
		protected int socketMode = 0;
		protected ZContext ctx = null;

		public SocketPoolableFactory(String uri, ZContext ctx, int socketMode) {
			this.uri = uri;
			this.ctx = ctx;
			this.socketMode = socketMode;
		}

		@Override
		public void activate(ZeroSocket arg0) throws PoolException {
		}

		@Override
		public void destroy(ZeroSocket arg0) {
			ZeroSocketFactory.logger.info("Socket[{}] destory.", new Object[] { arg0.uri });
			arg0.destroy();
		}

		@Override
		public ZeroSocket make() throws PoolException {
			return ZeroSocketFactory.create(uri, ctx, socketMode);
		}

		@Override
		public void passivate(ZeroSocket arg0) {
		}

		@Override
		public boolean validate(ZeroSocket arg0) {
			return arg0.isAlive;
		}
	}

	static class watchTask extends Thread {
		private ConcurrentHashMap<String, ObjectPool<ZeroSocket>> poolByUri = null;
		public watchTask(ConcurrentHashMap<String, ObjectPool<ZeroSocket>> poolByUri) {
			this.poolByUri = poolByUri;
		}

		@Override
		public void run() {
			if(poolByUri.size()==0)return;
			for (String key : poolByUri.keySet()) {
					ObjectPool<ZeroSocket> pool=poolByUri.get(key);
				try {
					if (pool.getObj().isAlive) {

					}
				} catch (PoolException e) {
					e.printStackTrace();
				}
				logger.info("Uri=[{}] SocketPool=[{}]", new Object[] { key, pool.toString() });
			}
		}

	}

	protected static ConcurrentHashMap<String, ObjectPool<ZeroSocket>> poolByUri = new ConcurrentHashMap<String, ObjectPool<ZeroSocket>>();
	protected static Logger logger = LoggerFactory.getLogger(ZeroSocketFactory.class);
	protected static long intlong = System.currentTimeMillis() + (60 * 1000 * 2);
	private static AtomicLong log = new AtomicLong();

	public static void clear(String ip, NodeType type) {
		AbstractPool<ZeroSocket> pool = null;
		String uri = "tcp://" + ip + ":";
		if (type == NodeType.router) {
			uri += Global.MQRouterPort;
		} else if (type == NodeType.app) {
			uri += Global.MQAppServerPort;
		} else {
			return;
		}
		ObjectPool<ZeroSocket> p = poolByUri.get(uri);
		if (p != null) {
			pool = (AbstractPool<ZeroSocket>) p;
			pool.clear();
			logger.info("clear pool[{}]", new Object[] { uri });
		}
	}

	public static ZeroSocket get(String uri, ZContext ctx, int socketMode) {
		log.addAndGet(1);
		if (log.get() > 100) {
			log.set(0);
			Thread watchTask = new watchTask(poolByUri);
			watchTask.start();
			logger.info("ObjectPool size[{}]", new Object[] { poolByUri.size() });
		}
		ObjectPool<ZeroSocket> pool = poolByUri.get(uri);
		if (pool == null) {
			PoolSettings<ZeroSocket> setting = new PoolSettings<ZeroSocket>(new SocketPoolableFactory(uri, ctx, socketMode)).validateWhenReturn(true);
			setting.min(0).max(0).maxIdle(30000).validateWhenReturn(true);
			pool = setting.pool();
			poolByUri.put(uri, pool);
		}
		try {
			return pool.getObj();
		} catch (PoolException e) {
			logger.error("get", e);
			return null;
		}
	}

	public static void ret(String uri, ZeroSocket obj) {
		ObjectPool<ZeroSocket> pool = poolByUri.get(uri);
		if (pool == null) {
			return;
		}
		pool.returnObj(obj);
	}

	public static ZeroSocket create(String uri, ZContext ctx, int socketMode) {
		ZeroSocket socket = new ZeroSocket(uri, ctx, socketMode);
		socket.init();
		return socket;
	}

}
