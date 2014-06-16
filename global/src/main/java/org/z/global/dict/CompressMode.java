package org.z.global.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

public enum CompressMode {

	NONE("0"), SNAPPY("1");

	private final byte[] data;

	CompressMode(String value) {
		this.data = value.getBytes();
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean frameEquals(ZFrame frame) {
		return Arrays.equals(data, frame.getData());
	}

	public boolean frameEquals(CompressMode frame) {
		return Arrays.equals(data, frame.data);
	}

	@Override
	public String toString() {
		return new String(this.data);
	}
}
