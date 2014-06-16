package org.z.global.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

public enum MessageVersion {

	MQ("0"), SOCKET("1"), WEBSOCKET("2");
	private final byte[] data;

	MessageVersion(String value) {
		this.data = value.getBytes();
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean frameEquals(ZFrame frame) {
		return Arrays.equals(data, frame.getData());
	}

	@Override
	public String toString() {
		return new String(this.data);
	}
}
