package org.z.global.object;

public class RemoteResult {

	private boolean state = false;
	private String[] messages = null;
	private byte[] content = null;

	public void setState(boolean value) {
		this.state = value;
	}

	public boolean getState() {
		return this.state;
	}

	public void setContent(byte[] value) {
		this.content = value;
	}

	public void setContent(String content) {
		try {
			this.content = content.getBytes("UTF-8");
		} catch (Exception e) {
			this.content = content.getBytes();
		}
	}

	public byte[] getContent() {
		return this.content;
	}

	public void setMessages(String[] values) {
		this.messages = values;
	}

	public String[] getMessages() {
		return this.messages;
	}

	public void setMessage(int index, String value) {
		this.messages[index] = value;
	}

}
