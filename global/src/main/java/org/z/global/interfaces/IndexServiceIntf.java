package org.z.global.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public interface IndexServiceIntf extends ModuleIntf {

	public void attachMQ(ModuleIntf module);

	public BasicDBObject search(DBObject oReq);

	public int add(DBObject oDoc);

	public BasicDBObject remove(DBObject oReq);

	public void commit();

	public void commitDynamicDB();

	public void updateFiled(DBObject oReq);

	public void updateFileds(Object oReq);

	public void handleMsg(BasicDBObject event);

	public void updateProductInfo(String productId, String field, Object obj);

	// public String getCategoryId(BasicDBObject oReq, SolrQuery query, String
	// catalog, BasicDBObject oResult);

	public String getCategoryId(BasicDBObject oReq, Object query, String catalog, BasicDBObject oResult);

	public void updateMasloc(ArrayList<String> skuNos);

	/**
	 * 根据分类id获取当前分类下所有的商品id
	 *
	 * @param catId
	 * @param page
	 * @return
	 */
	public BasicDBObject getProductIds(String catId, int page, int pageSize);

	public BasicDBList getProductReqs(List<String> productIds);

	public void updateFacet(BasicDBObject facet);

	public Object getDynamicDB();

	public void updatePromoTag();

	public void clearDynamicDB(BasicDBObject oReq);

}
