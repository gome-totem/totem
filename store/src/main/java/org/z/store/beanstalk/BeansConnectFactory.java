package org.z.store.beanstalk;

public class BeansConnectFactory {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String server = "10.58.50.56";
		private int port = 11300;
		private long timeout_secs = 0;
		private String use;
		private String watch;

		public String getUse() {
			return use;
		}

		public String getWatch() {
			return watch;
		}

		public BeansConnect build() {
			return new BeansConnect(this);
		}

		public Builder watch(String watch) {
			this.watch = watch;
			return this;
		}

		public Builder server(String server) {
			this.server = server;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder use(String use) {
			this.use = use;
			return this;
		}

		public Builder timeoutSecs(long timeoutSecs) {
			this.timeout_secs = timeoutSecs;
			return this;
		}

		public String getServer() {
			return server;
		}

		public int getPort() {
			return port;
		}

		public long getTimeout_secs() {
			return timeout_secs;
		}

	}
}
