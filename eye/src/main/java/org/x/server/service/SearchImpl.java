package org.x.server.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.z.global.connect.ZeroConnect;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServiceName;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SearchImpl implements SearchIntf{

	private static final Log logger = LogFactory.getLog(SearchImpl.class);
	
	public DBObject loadCategories(BasicDBObject oReq) {
		return ZeroConnect.eyeBussinessSend("loadCategory",oReq);
	}

	public DBObject search(BasicDBObject oReq) {
		return ZeroConnect.searchIndex(ServiceName.ProductIndex, "search", oReq);
	}

	public BasicDBList readRouters() {
		BasicDBList routers = null;
		try {
			routers = ServerDict.self.routers();
		} catch (Exception e) {
			logger.error("Parse ZooConnect.readRouters error");
			e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("router: " + routers);
		}
		return routers;
	}
	
	public BasicDBList readApps() {
		BasicDBList apps = null;
		try {
			apps = ServerDict.self.apps();
		} catch (Exception e) {
			logger.error("Parse ZooConnect.readApps error");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("router: " + apps);
		}
		return apps;
	}
}
