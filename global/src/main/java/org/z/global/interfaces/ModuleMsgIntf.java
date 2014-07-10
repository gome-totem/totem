package org.z.global.interfaces;

import org.z.global.dict.Global.ModuleMessageType;



public interface ModuleMsgIntf {

	public ModuleMessageType type();

	public String id();

	public long tag();

	public Object content();

	public String toJson();

}
