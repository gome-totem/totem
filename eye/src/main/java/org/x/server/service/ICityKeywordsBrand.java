package org.x.server.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;

public interface ICityKeywordsBrand {
	/**
	 * 通过分类查找品牌信息
	 * @param req
	 * @param resp
	 */
	public void getBrandsByCatId(HttpServletRequest req, HttpServletResponse resp);
	
	/**
	 * 通过分类找到对应的筛选项
	 * @param req
	 * @return
	 */
	public BasicDBObject getFacetGroupByCatId(HttpServletRequest req);
	
}
