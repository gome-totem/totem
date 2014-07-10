package org.z.global.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

public enum MessageType {

	REQUEST("0"), REGISTER("1"), CONNECT("2"), DISCONNECT("3"), READ("4"), REPLY("5"), SEARCH("6"), PUBLISH("7"), UPDATE("8"), REMOVE("9"), COMMIT("10"), SYNC(
			"11"), HEARTBEAT("12"), ONLINE("13"), OFFLINE("14"), RELAY("15"), DATACHANGE("16"), SECURITY("17"), CLEARCACHE("18"), REMOTECALL("19"), TALK("20"), SOCKET(
			"21"), UPLOADPICTURE("22"), UPLOADFILE("23"), FACET("24"),UPDATEFIELD("25");
	private final byte[] data;

	MessageType(String value) {
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

	public static void main(String[] args) {
		System.out.print(MessageType.values()[1].name());
	}
}
