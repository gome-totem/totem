package org.z.core.stocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global.SocketDataType;
import org.z.global.factory.SocketSession;
import org.z.global.interfaces.SocketEvent;

public class SocketEventImpl implements SocketEvent {

	private String id = null;
	private byte[] content = null;
	protected static Logger logger = LoggerFactory.getLogger(SocketEventImpl.class);
	private SocketHeader header = null;
	private String jobId = null;
	private SocketSession session = null;

	public SocketEventImpl(SocketSession session, SocketHeader header, byte[] content, String jobId) {
		this.session = session;
		this.header = header;
		this.content = content;
		this.jobId = jobId;
		StringBuilder buf = new StringBuilder();
		buf.append(header.serverName);
		buf.append("@");
		buf.append(session.channelId);
		buf.append("-");
		buf.append(jobId);
		this.id = buf.toString();
	}

	@Override
	public SocketSession session() {
		return this.session;
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public String jobId() {
		return this.jobId;
	}

	@Override
	public int device() {
		return this.header.device;
	}

	@Override
	public String serverName() {
		return this.header.serverName;
	}

	@Override
	public String serverIP() {
		return this.header.serverIP;
	}

	@Override
	public String userId() {
		return this.header.userId;
	}

	@Override
	public String serviceName() {
		return this.header.serviceName;
	}

	@Override
	public int serviceIndex() {
		return this.header.serviceIndex;
	}

	@Override
	public int messageScope() {
		return this.header.messageScope;
	}

	@Override
	public int messageType() {
		return this.header.messageType;
	}

	@Override
	public int messageVersion() {
		return this.header.messageVersion;
	}

	@Override
	public long timestamp() {
		return this.header.timestamp;
	}

	@Override
	public String messageTag() {
		return this.header.messageTag;
	}

	@Override
	public String messageId() {
		return this.header.messageId;
	}

	@Override
	public String messageTo() {
		return this.header.messageTo;
	}

	@Override
	public int compressMode() {
		return this.header.compressMode;
	}

	@Override
	public byte[] content() {
		return this.content;
	}

	@Override
	public void onMessage(SocketDataType type, Object content, boolean enableCompress) {
		SocketResponse record = new SocketResponse(type, content,enableCompress);
		session.channel.write(record);
	}

}