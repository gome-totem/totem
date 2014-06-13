package org.x.cloud.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

public enum MessageType {

	REQUEST("0"), REGISTER("1"), CONNECT("2"), DISCONNECT("3"), READ("4"), REPLY("5"), SEARCH("6"), PUBLISH("7"), UPDATE("8"), REMOVE("9"), COMMIT("10"), SYNC("11"), HEARTBEAT("12"), ONLINE("13"), OFFLINE(
			"14"), RELAY("15"), DATACHANGE("16"), SECURITY("17"), CLEARCACHE("18"), REMOTECALL("19"), UPDATEFILED("20"), DRAGON("21"), DELETERULEDICT("22"), UPDATERULEDICT("23"), FACETDATA("24"), UPDATESORTRULE(
			"25"), REMOTEJOB("26");
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
