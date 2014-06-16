package org.z.global.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

public enum Device {

	BROWSER("0"), ANDRIOD("1"), IOS("2"), SERVER("3");

	private final byte[] data;

	Device(String value) {
		this.data = value.getBytes();
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean frameEquals(ZFrame frame) {
		return Arrays.equals(data, frame.getData());
	}

	public boolean frameEquals(Device frame) {
		return Arrays.equals(data, frame.data);
	}

	@Override
	public String toString() {
		return new String(this.data);
	}
}
