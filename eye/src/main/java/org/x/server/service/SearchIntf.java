package org.x.server.service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public interface SearchIntf {
	
	//获取所有的category
	public DBObject loadCategories(BasicDBObject oReq);
	
	//搜索商品
	public DBObject search(BasicDBObject oReq);
	
	//读取running routers
	public BasicDBList readRouters();
	
	//读取running appservers
	public BasicDBList readApps();
}
