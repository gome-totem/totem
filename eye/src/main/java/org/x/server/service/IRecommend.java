package org.x.server.service;

import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBList;

public interface IRecommend {

	/**
	 * 检索商品
	 * @param resp 
	 * @param skuId 
	 * */
	public void search(HttpServletResponse resp, String skuId);
	/**
	 * 提交表单
	 * @param catId
	 * @param skus
	 * */
	public boolean commitList(String catId, BasicDBList skus);
	
	/**
	 * 使用八叉乐推荐
	 * @param resp 
	 * @param catId
	 * **/
	public void cancelRecommed(HttpServletResponse resp, String catId );
	
	/**
	 * 查列表页数据
	 * 
	 * */
	public void queryCatList(HttpServletResponse resp, String catId);
}
