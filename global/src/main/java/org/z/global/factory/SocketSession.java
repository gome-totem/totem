package org.z.global.factory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.z.global.dict.Device;
import org.z.global.dict.Global.SocketConnectType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


public class SocketSession {

	public static class Friend {
		public String server = null;
		public HashSet<Long> users = new HashSet<Long>();

		public Friend(String server) {
			this.server = server;
		}

		public void addUser(Long userId) {
			this.users.add(userId);
		}

		public BasicDBList values() {
			BasicDBList items = new BasicDBList();
			for (Iterator<Long> i = users.iterator(); i.hasNext();) {
				items.add(i.next());
			}
			return items;
		}
	}

	public Channel channel = null;
	public Integer channelId = 0;
	public SocketConnectType type = null;
	public Object draft = null;
	public Object attachment = null;
	private AtomicLong jobNo = new AtomicLong(0);
	public boolean auth = false;
	public String authKey = null;
	public long timestamp = 0;
	public String ip = null;
	public long userId = 0;
	public String userServer = null;
	public String callServer = null;
	public String userName = null;
	public BasicDBObject meta = new BasicDBObject();
	public Device device = null;

	public SocketSession() {
	}

	public SocketSession(Channel channel, SocketConnectType type) {
		this.channelId = channel.getId();
		this.channel = channel;
		String[] values = channel.getRemoteAddress().toString().replaceAll("/", "").split(":");
		this.ip = values[0];
		this.type = type;
	}

	public void clear() {
		this.attachment = null;
		this.draft = null;
		this.auth = false;
		this.authKey = null;
		this.ip = null;
		this.userId = 0;
		this.userName = null;
		this.callServer = null;
		this.meta.clear();
	}

	public void setAttachment(Object o) {
		this.attachment = o;
	}

	public void setDraft(Object draft) {
		this.draft = draft;
	}

	public String allocateJobNo() {
		if (jobNo.get() >= Long.MAX_VALUE) {
			jobNo.set(0);
		}
		return String.valueOf(jobNo.addAndGet(1));
	}

	public void close() {
		this.channel.close().awaitUninterruptibly();
	}

}
