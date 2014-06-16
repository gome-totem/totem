package org.z.global.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import java.util.concurrent.TimeUnit;

public class Locks {
	protected final InterProcessMutex lock;
	protected final LockHandlerIntf handler;
	protected final String lockPath;

	public Locks(CuratorFramework client, String lockPath, LockHandlerIntf handler) {
		this.handler = handler;
		this.lockPath = lockPath;
		lock = new InterProcessMutex(client, lockPath);
	}

	public void doWork(long time, TimeUnit unit) throws Exception {
		if (!lock.acquire(time, unit)) {
			throw new IllegalStateException(this.lockPath + " could not acquire the lock");
		}
		try {
			this.handler.execute();
		} finally {
			lock.release();
		}
	}
}
