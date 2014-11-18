package org.x.server.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.x.server.controller.CityKeywordsBrandController;
import org.z.global.connect.ZeroConnect;
import org.z.global.util.StringUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Service
public class ICityKeywordsBrandImpl implements ICityKeywordsBrand {
	protected static Logger logger = LoggerFactory.getLogger(CityKeywordsBrandController.class);

	/**
	 * 通过分类查找品牌信息
	 * @param req
	 * @param resp
	 */
	public void getBrandsByCatId(HttpServletRequest req, HttpServletResponse resp){
		getFacetGroupByCatId(req);
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
	
	public void writeStr(HttpServletResponse resp,String str){
		PrintWriter out = null;
		try {
			out = resp.getWriter();
			out.print(str);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(out!=null){
				out.flush();
				out.close();
			}
		}
	}
	
}
