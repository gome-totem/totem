package org.z.core.interfaces;


import org.z.global.dict.Global.SearchMode;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.ModuleMsgIntf;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public interface IndexServiceIntf extends ModuleIntf {

	public void attachMQ(ModuleIntf module);

	public BasicDBObject search(DBObject oReq, BasicDBObject oUser, SearchMode searchMode);

	public int add(DBObject oDoc);

	public void remove(DBObject oReq);

	public void commit();

	public boolean handleMsg(ModuleMsgIntf event);
	
	public void updateField(DBObject oDoc);

}
