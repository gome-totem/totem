package org.x.server.eye;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.mongodb.BasicDBObject;

public class EventQueue implements Runnable {

	public ConcurrentLinkedQueue<BasicDBObject> items = new ConcurrentLinkedQueue<BasicDBObject>();
	protected OnChange onMessage = null;
	public EventQueue instance = null;

	public EventQueue(OnChange onMessage) {
		this.onMessage = onMessage;
	}

	public void add(BasicDBObject o) {
		items.add(o);
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		while (true && !Thread.interrupted()) {
			try {
				if (items.size() == 0) {
					Thread.sleep(1000);
				}
				BasicDBObject oMsg = items.poll();
				if (oMsg == null)
					continue;
				onMessage.execute(oMsg);
			} catch (InterruptedException e) {
			}
		}
	}
}
