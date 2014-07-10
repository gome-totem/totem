package org.z.core.queue;


import org.z.global.dict.Global.ModuleMessageType;
import org.z.global.interfaces.ModuleMsgIntf;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
public class ModuleMsg implements ModuleMsgIntf {
	protected ModuleMessageType type = null;
	protected String id = null;
	protected long tag = 0;
	protected Object content = null;

	public ModuleMsg(String content) {
		this.fromJson(content);
	}

	public ModuleMsg(ModuleMessageType type, String id, long tag, Object content) {
		this.type = type;
		this.id = id;
		this.tag = tag;
		this.content = content;
	}

	@Override
	public ModuleMessageType type() {
		return this.type;
	}

	@Override
	public String id() {
		return this.id;
	}

	@Override
	public long tag() {
		return this.tag;
	}

	@Override
	public Object content() {
		return this.content;
	}

	@Override
	public String toJson() {
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("id", id);
		oResult.append("tag", tag);
		oResult.append("type", this.type.ordinal());
		oResult.append("content", content);
		return oResult.toString();
	}

	private void fromJson(String content) {
		BasicDBObject oValue = (BasicDBObject) JSON.parse(content);
		this.id = oValue.getString("id");
		this.tag = oValue.getLong("tag");
		this.type = ModuleMessageType.values()[oValue.getInt("type")];
		this.content = oValue.get("content");
	}

}
