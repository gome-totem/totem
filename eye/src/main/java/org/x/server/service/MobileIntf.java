package org.x.server.service;

import com.mongodb.BasicDBObject;

public interface MobileIntf {
	
	/**
	 * eye监控发送信息
	 * */
	public boolean sendTextMessage(BasicDBObject msg);
	
	/**
	 * 短信页面调用接口
	 * */
	public boolean sendHtmlMessage(BasicDBObject msg);
}
