package org.z.global.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

public enum MessageScope {

	ROUTER("0"), APP("1"), ALLROUTER("2"), ALLAPP("3"), BROADCAST("4");

	private final byte[] data;

	MessageScope(String value) {
		this.data = value.getBytes();
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean frameEquals(ZFrame frame) {
		return Arrays.equals(data, frame.getData());
	}

	public boolean frameEquals(MessageScope frame) {
		return Arrays.equals(data, frame.data);
	}

	@Override
	public String toString() {
		return new String(this.data);
	}
}
