package org.z.store.beanstalk;

import java.util.HashMap;
import java.util.ArrayList;

public class Client {

	protected float timeout_secs;
	protected int handle;
	protected int port;
	protected String host;

	static {
		System.loadLibrary("client");
	}

	public native void Init(String host, int port, float timeout_secs);

	public native boolean ping();

	public native boolean use(String tube);

	public native boolean watch(String tube);

	public native boolean ignore(String tube);

	public native long put(String body, long priority, long delay, long ttr);

	public native long put(byte[] body, long priority, long delay, long ttr);

	public native boolean del(long id);

	public native boolean del(Job job);

	public native boolean reserve(Job job);

	public native boolean reserve(Job job, long timeout);

	public native boolean release(Job job, long priority, long delay);

	public native boolean release(long id, long priority, long delay);

	public native boolean bury(Job job, long priority);

	public native boolean bury(long id, long priority);

	public native boolean touch(Job job);

	public native boolean touch(long id);

	public native boolean peek(Job job, long id);

	public native boolean peek_ready(Job job);

	public native boolean peek_delayed(Job job);

	public native boolean peek_buried(Job job);

	public native boolean kick(int bound);

	public native void connect(String host, int port, float secs);

	public native void reconnect();

	public native boolean disconnect();

	public native String list_tube_used();

	public native HashMap<String, String> stats();

	public native HashMap<String, String> stats_job(long id);

	public native HashMap<String, String> stats_tube(String name);

	public native ArrayList<String> list_tubes();

	public native ArrayList<String> list_tubes_watched();
}
