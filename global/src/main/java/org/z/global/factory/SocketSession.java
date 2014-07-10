package org.z.global.factory;

import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.ChannelPipeline;
import org.z.global.dict.Global.SocketConnectType;


public class SocketSession {

	public ChannelPipeline pipeline = null;
	public SocketConnectType type = null;
	public Object draft = null;
	public Object attachment = null;
	private AtomicLong jobNo = new AtomicLong(0);
	public long id = 0;

	public SocketSession(long id, ChannelPipeline pipeline, SocketConnectType type) {
		this.pipeline = pipeline;
		this.type = type;
		this.id = id;
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

}
