package org.z.core.interfaces;



import org.z.core.common.Context;
import org.z.global.interfaces.ModuleIntf;

import com.mongodb.DBObject;

public interface ServiceIntf extends ModuleIntf {

	public DBObject handle(Context ctx, DBObject oReq);
	
}
