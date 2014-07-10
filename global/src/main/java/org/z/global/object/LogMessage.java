package org.z.global.object;

import java.util.Date;
import java.util.Iterator;

import org.z.global.dict.Global.LogLevel;
import org.z.global.util.StringUtil;

import com.mongodb.BasicDBObject;

public class LogMessage extends BasicDBObject {

	private static final long serialVersionUID = 1L;
	protected BasicDBObject message = null;

	public LogMessage(LogLevel level, String action) {
		this(level, action, new BasicDBObject());
	}

	public LogMessage(LogLevel level, String action, String message) {
		this(level, action, new BasicDBObject().append("content", message));
	}

	public LogMessage(LogLevel level, String action, BasicDBObject message) {
		super.append("time", StringUtil.formatTime(new Date()));
		super.append("level", level.ordinal());
		super.append("action", action);
		super.append("message", message);
		this.message = message;
	}

	public LogMessage add(String key, String value) {
		this.message.append(key, value);
		return this;
	}

	public LogMessage add(String key, int value) {
		this.message.append(key, value);
		return this;
	}

	public LogMessage add(String key, long value) {
		this.message.append(key, value);
		return this;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public String content() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("time:[");
		buffer.append(this.getString("time"));
		buffer.append("] ");
		for (Iterator<java.util.Map.Entry<String, Object>> i = this.message.entrySet().iterator(); i.hasNext();) {
			java.util.Map.Entry<String, Object> entry = i.next();
			buffer.append(entry.getKey() + ":");
			buffer.append("[");
			buffer.append(entry.getValue().toString());
			buffer.append("] ");
		}
		return buffer.toString();
	}

}
