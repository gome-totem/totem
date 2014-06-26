package org.z.store.beanstalk;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.xerial.snappy.Snappy;
import org.z.store.beanstalk.BeansConnectFactory.Builder;

public class BeansConnect {

//	protected static Logger logger = LoggerFactory.getLogger(BeansConnect.class);

	private Client client;

	public BeansConnect(Builder builder) {

		String use = builder.getUse();
		String watch = builder.getWatch();

		if (use == null && watch == null) {
			throw new NullPointerException("beanstalk client use or watch all null.");
		}

		client = new Client();
		client.Init(builder.getServer(), builder.getPort(), builder.getTimeout_secs());

		if (use != null && watch != null) {
//			logger.warn("beanstalk client use and watch at the same time");
		}

		if (use != null) {
			client.use(use);
		}

		if (watch != null) {
			client.watch(watch);
		}

	}

	public boolean ping() {
		return client.ping();
	}

	public boolean use(String tube) {
		return client.use(tube);
	}

	public boolean watch(String tube) {
		return client.watch(tube);
	}

	public boolean ignore(String tube) {
		return client.ignore(tube);
	}

	public long put(byte[] body, long priority, long delay, long ttr) {
		return client.put(body, priority, delay, ttr);
	}

	public long put(String body, long priority, long delay, long ttr) {
		byte[]  date =null;
		try {
			date =Snappy.compress(body.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client.put(date, priority, delay, ttr);
	}

	public boolean del(long id) {
		return client.del(id);
	}

	public boolean del(Job job) {
		return client.del(job);
	}

	public boolean reserve(Job job) {
		return client.reserve(job);
	}

	public boolean reserve(Job job, long timeout) {
		return client.reserve(job, timeout);
	}

	public boolean release(Job job, long priority, long delay) {
		return client.release(job, priority, delay);
	}

	public boolean release(long id, long priority, long delay) {
		return client.release(id, priority, delay);
	}

	public boolean bury(Job job, long priority) {
		return client.bury(job, priority);
	}

	public boolean bury(long id, long priority) {
		return client.bury(id, priority);
	}

	public boolean touch(Job job) {
		return client.touch(job);
	}

	public boolean touch(long id) {
		return client.touch(id);
	}

	public boolean peek(Job job, long id) {
		return client.peek(job, id);
	}

	public boolean peek_ready(Job job) {
		return client.peek_ready(job);
	}

	public boolean peek_delayed(Job job) {
		return client.peek_delayed(job);
	}

	public boolean peek_buried(Job job) {
		return client.peek_buried(job);
	}

	public boolean kick(int bound) {
		return client.kick(bound);
	}

	public void connect(String host, int port, float secs) {
		client.connect(host, port, secs);
	}

	public void reconnect() {
		client.reconnect();
	}

	public boolean disconnect() {
		return client.disconnect();
	}

	public String list_tube_used() {
		return client.list_tube_used();
	}

	public HashMap<String, String> stats() {
		return client.stats();
	}

	public HashMap<String, String> stats_job(long id) {
		return client.stats_job(id);
	}

	public HashMap<String, String> stats_tube(String name) {
		return client.stats_tube(name);
	}

	public ArrayList<String> list_tubes() {
		return client.list_tubes();
	}

	public ArrayList<String> list_tubes_watched() {
		return client.list_tubes_watched();
	}

	public static void main(String args[]) {

		int key = 0;
		BeansConnect client = null;
		switch (key) {
		case 0:
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Timestamp time = new Timestamp(System.currentTimeMillis());
			String startDate = format.format(time);
			client = BeansConnectFactory.builder().server("10.58.22.16").port(11300).watch("ATP").build();
			System.out.println(startDate + "===" + client.stats());
			while (true) {
			Job job = new Job();
				boolean reserve = client.reserve(job, 2);
			if (reserve) {
					client.del(job);
					// String str =
					// StringUtil.toString(StringUtil.uncompress(body));
					// count++;
					// System.out.println(count + "====" + str);
				}
			}
			// for (int i = 0; i < 5; i++) {
			//
			// builder = new StringBuilder();
			//
			// stats = client.stats();
			// String del = stats.get("cmd-delete");
			// String tol = stats.get("total-jobs");
			//
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			//
			// stats = client.stats();
			// String oDel = stats.get("cmd-delete");
			// String oTal = stats.get("total-jobs");
			//
			// builder.append("[").append("b-tol: ").append(tol).append("]");
			// builder.append("[").append("b-del: ").append(del).append("]");
			// builder.append("[").append("e-tol: ").append(oDel).append("]");
			// builder.append("[").append("e-del: ").append(oTal).append("]");
			// builder.append("[").append("dif-tal: ").append((Long.parseLong(oTal)
			// - Long.parseLong(tol))).append("]");
			// builder.append("[").append("dif-del: ").append((Long.parseLong(oDel)
			// - Long.parseLong(del))).append("]");
			//
			// logger.info(builder.toString());
			//
			// }
			// client.disconnect();
			// break;
		}

	}
}
