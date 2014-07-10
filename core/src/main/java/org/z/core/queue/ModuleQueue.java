package org.z.core.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.z.global.dict.Global.ModuleMessageType;
import org.z.global.dict.LoggerDict;
import org.z.global.dict.LoggerDict.LogType;
import org.z.global.interfaces.ModuleQueueIntf;

import com.mongodb.BasicDBObject;

public class ModuleQueue implements ModuleQueueIntf, Runnable {
	private String id = "queue";
	protected static Logger logger = LoggerDict.getLogger(LogType.SYSLOG);
	private boolean isAlive = false;
	private Thread t = null;
	public ArrayBlockingQueue<BasicDBObject> queue = new ArrayBlockingQueue<BasicDBObject>(1000);
	@SuppressWarnings("unused")
	private ExecutorService threadPool = null;

	@Override
	public void put(BasicDBObject msg) {
			queue.add(msg);
	}

	@Override
	public void run() {
		while (!t.isInterrupted() && isAlive) {
			try {
				int s = queue.size();
				if (s == 0)
					continue;
				BasicDBObject msg = queue.poll();
				int type = msg.getInt("type");
				ModuleMessageType msgType = ModuleMessageType.values()[type];
				switch (msgType) {
				case dragon:
					break;
				}
			} catch (Exception e) {
				logger.error("EventQueue[{}]", new Object[] { e.toString() });
			}
		}
		stop();
	}

	@Override
	public void afterCreate(Object[] params) {

	}

	@Override
	public boolean init(boolean isReload) {
		threadPool = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
		return true;
	}

	@Override
	public void start(boolean isReload) {
		isAlive = true;
		t = new Thread(this);
		t.start();
		logger.info("Module EventQueue start success.");
	}

	@Override
	public void stop() {
		logger.error("Module EventQueue Shutdown");
		isAlive = false;
		try {
			t.join();
		} catch (Exception e) {
			logger.error("stop", e);
		}
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public String getId() {
		return id;
	}

}
