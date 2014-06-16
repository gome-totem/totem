package org.z.global.util;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import com.mongodb.BasicDBObject;

/**
 * @author xiaoming@yundiz.com
 * @version 创建时间：2013-4-5 上午9:38:21
 */

public class JVMUtil {

	public static BasicDBObject readRuntime() {
		Runtime runtime = Runtime.getRuntime();
		int size = 1024 * 1024;
		long maxMemory = Math.round((float) runtime.maxMemory() / size);
		long allocatedMemory = Math.round((float) runtime.totalMemory() / size);
		long freeMemory = Math.round((float) runtime .freeMemory() / size) + maxMemory - allocatedMemory;
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("maxMemory", maxMemory);
		oResult.append("allocatedMemory", allocatedMemory);
		oResult.append("freeMemory", freeMemory);
		final Thread[] allThreads = getAllThreads();
		oResult.append("totalThread", allThreads.length);
		int n = 0;
		for (Thread thread : allThreads)
			if (thread.getState() == State.BLOCKED)
				n++;
		oResult.append("blockedThread", n);
		return oResult;
	}

	public static ThreadGroup getRootThreadGroup() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		ThreadGroup ptg;
		while ((ptg = tg.getParent()) != null)
			tg = ptg;
		return tg;
	}

	public static Thread[] getAllThreads() {
		final ThreadGroup root = getRootThreadGroup();
		final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
		int nAlloc = thbean.getThreadCount();
		int n = 0;
		Thread[] threads;
		do {
			nAlloc *= 2;
			threads = new Thread[nAlloc];
			n = root.enumerate(threads, true);
		} while (n == nAlloc);
		return java.util.Arrays.copyOf(threads, n);
	}

	public static Thread[] getAllDaemonThreads() {
		final Thread[] allThreads = getAllThreads();
		final Thread[] daemons = new Thread[allThreads.length];
		int nDaemon = 0;
		for (Thread thread : allThreads)
			if (thread.isDaemon())
				daemons[nDaemon++] = thread;
		return java.util.Arrays.copyOf(daemons, nDaemon);
	}

	public static Thread[] getAllBlockedThreads() {
		final Thread[] allThreads = getAllThreads();
		final Thread[] blocks = new Thread[allThreads.length];
		int n = 0;
		for (Thread thread : allThreads)
			if (thread.getState() == State.BLOCKED)
				blocks[n++] = thread;
		return java.util.Arrays.copyOf(blocks, n);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
