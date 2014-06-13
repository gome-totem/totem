package org.x.cloud.dict;

import java.util.Arrays;

import org.zeromq.ZFrame;

/**
 * @author xiaoming@yundiz.com
 */

public enum ServiceName {
	Server("0"), HtmlPage("1"), User("2"), Bill("3"), Order("4"), Activity("5"), Car("6"), Trip("7"), Room("8"), Booking("9"), Calendar("10"), Recommend("11"), Tag("12"), Appoint("13"), Call("14"), SMS(
			"15"), TripIndex("16"), RoomIndex("17"), RequireIndex("18"), ProductIndex("19"), TopicIndex("20"), Dict("21"), Schedule("22"), Servlet("23"), RemoteCall("24"), DRAGON("25"), RuleDict("26"), SortRule(
			"27"), Defectiveindex("28"), RemoteJob("29");

	private byte[] data;
	public String value = "";
	public static boolean inited = false;

	ServiceName(String value) {
		init();
		this.value = Global.byServiceIndexes.get(Integer.parseInt(value));
		this.value = Global.DevelopMode ? this.value + "@" + Global.DevelopName : this.value;
		this.data = this.value.getBytes();
	}

	private void addItem(String key, Integer value) {
		Global.byServiceNames.put(key, value);
		Global.byServiceIndexes.put(value, key);
	}

	private void init() {
		if (inited == true) {
			return;
		}
		inited = true;
		int index = 0;
		addItem("server", index++);
		addItem("htmlpage", index++);
		addItem("user", index++);
		addItem("bill", index++);
		addItem("order", index++);
		addItem("activity", index++);
		addItem("car", index++);
		addItem("trip", index++);
		addItem("room", index++);
		addItem("booking", index++);
		addItem("calendar", index++);
		addItem("recommend", index++);
		addItem("tag", index++);
		addItem("appoint", index++);
		addItem("call", index++);
		addItem("sms", index++);
		addItem("tripindex", index++);
		addItem("roomindex", index++);
		addItem("requireindex", index++);
		addItem("productindex", index++);
		addItem("topicindex", index++);
		addItem("dict", index++);
		addItem("schedule", index++);
		addItem("servlet", index++);
		addItem("remotecall", index++);
		addItem("dragon", index++);
		addItem("ruledict", index++);
		addItem("sortrule", index++);
		addItem("defectiveindex", index++);
		addItem("remoteJob", index++);
	}

	public ZFrame newFrame() {
		return new ZFrame(data);
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean frameEquals(ZFrame frame) {
		return Arrays.equals(data, frame.getData());
	}

	public static void main(String[] args) {
		System.out.println(ServiceName.Bill.ordinal());
	}

}
