package org.x.server.service;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.BasicDBObject;

public interface ICategoryFacetsSet {
	/**
	 * 设置品牌排序信息
	 */
	public BasicDBObject orderFacets(HttpServletRequest req);
	/**
	 * 通过分类找到对应的筛选项
	 * @param req
	 * @return
	 */
	public BasicDBObject getFacetGroupByCatId(HttpServletRequest req);
	/**
	 * 排序
	 * @param req
	 * @return
	 */
	public BasicDBObject categoryOrderSet(HttpServletRequest req);
	
}
