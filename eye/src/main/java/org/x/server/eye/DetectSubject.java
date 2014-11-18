package org.x.server.eye;

import com.mongodb.BasicDBObject;

public interface DetectSubject {

	public interface Observer {
		public void execute(Message data);
	}

	public void registerObserver(Observer observer);

	public void removeObserver(Observer observer);

	public void notifyObserver(Message data);

	public class Address {

		private String url;
		private String ip;
		private int port;

		public Address(String url, String ip, int port) {
			super();
			this.url = url;
			this.ip = ip;
			this.port = port;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

	}

	public class Message {

		private String ip;
		private int port;
		private int status;
		private long interval;
		private String url;

		public Message(Address address, int status, long interval) {
			super();
			this.ip = address.ip;
			this.port = address.port;
			this.url = address.url;
			this.status = status;
			this.interval = interval;
		}

		@Override
		public String toString() {
			BasicDBObject result = new BasicDBObject();
			result.append("ip", ip);
			result.append("port", port);
			result.append("url", url);
			result.append("status", status);
			result.append("time", interval);
			return result.toString();
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public long getInterval() {
			return interval;
		}

		public void setInterval(long interval) {
			this.interval = interval;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

	}

}
