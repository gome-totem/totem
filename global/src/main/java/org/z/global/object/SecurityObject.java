package org.z.global.object;

import org.z.global.dict.Global;


public class SecurityObject {

	public Global.SecurityType type = null;
	public Global.SecurityMode mode = null;
	public String content = null;

	public static SecurityObject create(Global.SecurityType type, Global.SecurityMode mode, String content) {
		SecurityObject object = new SecurityObject();
		object.type = type;
		object.mode = mode;
		object.content = content;
		return object;
	}

}
