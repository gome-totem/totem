package org.z.web.interfaces;



import org.z.global.interfaces.ModuleIntf;
import org.z.web.common.Context;

import com.mongodb.DBObject;


public interface ServiceIntf extends ModuleIntf {

	public DBObject handle(Context ctx, DBObject oReq);
	
}
