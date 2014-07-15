package org.z.core.interfaces;


import org.z.global.dict.Global.CodeType;

import com.mongodb.BasicDBObject;


public interface ServiceDictIntf extends ServiceIntf {
	
	public String[] decode(String name, String mobile, String qq, String msn);

	public String[] decode(String name, String mobile, String qq, String msn, String email);

	public String[] encode(String name, String mobile, String qq, String msn);

	public boolean onFacetChange(CodeType type, BasicDBObject oRecord);

}
