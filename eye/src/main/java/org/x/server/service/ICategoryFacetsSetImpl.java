package org.x.server.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.x.server.controller.CategoryFacetsSetController;
import org.x.server.eye.Eye;
import org.z.global.connect.ZeroConnect;
import org.z.global.util.StringUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Service
public class ICategoryFacetsSetImpl implements ICategoryFacetsSet {
	
	protected static Logger logger = LoggerFactory.getLogger(CategoryFacetsSetController.class);

	/**
	 * 设置品牌排序信息
	 */
	public BasicDBObject orderFacets(HttpServletRequest req) {
		BasicDBObject result = new BasicDBObject();
		String msg = "error";
		
		String facetsId = req.getParameter("facetsId");
		String facetsOrder = req.getParameter("facetsOrder");
		String catId = req.getParameter("catId");

		if(facetsId.length()==0||facetsOrder.length()==0){
			msg = "err";
			result.append("msg", msg);
		}else{
			facetsId = "f." + facetsId;
			try {
				facetsOrder = URLDecoder.decode(facetsOrder, "utf-8");
			} catch (UnsupportedEncodingException e) {
				if (logger.isInfoEnabled()) {
					logger.info("facetsOrder decode error:", new Object[] { e });
				}
			}
			BasicDBObject oReq = new BasicDBObject();
			BasicDBList values = new BasicDBList();
			String[] orders = facetsOrder.split("\\,");
			for (int i = 0; i < orders.length; i++) {
				values.add(orders[i]);
			}
			oReq.append("values", values);
			oReq.append("id", facetsId);
			oReq.append("catId", catId);
			ZeroConnect.eyeBussinessSend("facetWeight", oReq);
			msg = "success";
		}
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "orderFacets").append("context", "进行 ["+ catId +"]下["+ facetsId +"]的排序！"));
		result.append("msg", msg);
		return result;
	}
	/**
	 * 通过分类找到对应的筛选项
	 * @param req
	 * @return
	 */
	public BasicDBObject getFacetGroupByCatId(HttpServletRequest req){
		BasicDBObject result = new BasicDBObject();
		String msg = "error";
		String catId = req.getParameter("selCatId");
		result.append("msg", msg);
		if(StringUtil.isEmpty(catId)){
			return result;
		}
		
		BasicDBObject oParam = new BasicDBObject();
		oParam.append("catId", catId);
		oParam.append("catalog", "homeStore");
		oParam.append("siteId", "homeSite");
		
		DBObject res = ZeroConnect.eyeBussinessSend("getFacetByCatId", oParam);
		
		BasicDBObject response = (BasicDBObject)res.get("response");
		if(response!=null){
			BasicDBList facets = (BasicDBList)response.get("sResult");
			if(facets!=null){
				BasicDBList nfacets = new BasicDBList(); 
				if(facets.size()>0){
					for(int i=0;i<facets.size();i++){
						BasicDBObject facet = (BasicDBObject)facets.get(i);
						BasicDBList items = (BasicDBList) facet.get("items");
						if (items == null || items.size() < 1)
							continue;
						String property = facet.getString("property", "");
						if (property.equals("shopno.$repositoryId"))
							continue;
						if(facet.getString("label","").equalsIgnoreCase("facet.label.Category"))
							continue;
						BasicDBObject nfacet = new BasicDBObject();
						nfacet.append("id", facet.getString("id", ""));
						String label = "";
						try {
							label = new String(facet.getString("label", "").getBytes(), "utf-8");
						} catch (UnsupportedEncodingException e) {
							logger.info("facetsOrder decode error:", new Object[] { e });
						}
						String itemsStr = "";
						int t = 0;
						for(int j = 0;j<items.size();j++){
							BasicDBObject item = (BasicDBObject)items.get(j);
							String name = item.getString("name", "");
							if(t==0){
								itemsStr = name;
							}else{
								itemsStr = itemsStr + "," + name;
							}
							t++;
						}
						nfacet.append("items", itemsStr);
						nfacet.append("label", label);
						nfacets.add(nfacet);
					}
					result.append("facets", nfacets);
					result.append("msg", "success");
				}else{
					result.append("msg", "分类下无数据!");
				}
			}
		}
		return result;
	}
	
	public BasicDBObject categoryOrderSet(HttpServletRequest req){
		BasicDBObject result = new BasicDBObject();
		String msg = "error";
		String catId = req.getParameter("parentCatId");
		String childCatIds = req.getParameter("categoryOrder");
		BasicDBObject param = new BasicDBObject();
		
		if(StringUtil.isEmpty(catId)||StringUtil.isEmpty(childCatIds)){
			result.append("msg", msg);
			return result;
		}
		
		try {
			childCatIds = URLDecoder.decode(childCatIds, "utf-8");
		} catch (UnsupportedEncodingException e) {
			if (logger.isInfoEnabled()) {
				logger.info("categoryOrders error:", new Object[] { e });
			}
		}
		BasicDBList catIdList = new BasicDBList();
		
		String [] ccatIds = childCatIds.split(",");
		for(String ccatId:ccatIds){
			catIdList.add(ccatId);
		}
		
		param.append("catId", catId);
		param.append("categories", catIdList);
		
		ZeroConnect.eyeBussinessSend("categoryOrder", param);
		
		msg = "success";
		result.append("msg", msg);
		return result;
		
	}
}
