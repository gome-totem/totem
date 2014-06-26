package org.z.store.beanstalk;

public class Job {
	protected long id;
	protected byte[] body;

	public long getId() {
		return id;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Job() {

	}

	public Job(int id, byte[] body) {
		this.id = id;
		this.body = body;
	}
}
