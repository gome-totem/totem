package org.z.core.intf;

import org.z.global.dict.Global.SocketDataType;


public interface SocketEvent {

	public void onMessage(SocketDataType dataType, Object content);

	public SocketSession session();

	public String id();

	public String jobId();

	public int device();

	public String serverName();

	public String serverIP();

	public String serviceName();

	public int serviceIndex();

	public int messageScope();

	public int messageType();

	public int messageVersion();

	public long timestamp();

	public String messageTag();

	public String messageId();

	public String messageTo();

	public int compressMode();

	public byte[] content();

}
