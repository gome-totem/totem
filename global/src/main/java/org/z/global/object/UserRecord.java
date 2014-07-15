package org.z.global.object;

public class UserRecord {

	public String name = null;
	public String server = null;
	public String callServer = null;
	public String balanceId = null;

	public UserRecord(String name, String server, String callServer, String balanceId) {
		this.name = name;
		this.server = server;
		this.callServer = callServer;
		this.balanceId = balanceId;
	}

}
