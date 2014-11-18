package org.x.server.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.x.server.common.Tool;
import org.x.server.eye.Eye;
import org.z.global.connect.ZeroConnect;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service
public class IRecommendImpl implements IRecommend {

	private static final String RECOMMEND = "recommend";
	/**
	 * 检索商品
	 * @param resp 
	 * @param skuId 
	 * */
	public void search(HttpServletResponse resp, String skuId){
		BasicDBObject oReq = new BasicDBObject().append("skuId", skuId);
		oReq.append("internalSearch", true);
		BasicDBObject oResult = (BasicDBObject) Eye.sIntf.search(oReq);
		Write2PageUtil.write(resp, Tool.parseSku(oResult));
	}
	
	/**
	 * 提交表单
	 * @param catId
	 * @param skus
	 * */
	public boolean commitList(String catId, BasicDBList skus){
		BasicDBObject req = new BasicDBObject();
		req.append("catId", catId);
		req.append("flag", 1);
		req.append("skus", skus);
		BasicDBObject result = (BasicDBObject) ZeroConnect.eyeBussinessSend("recommend", req);
		return Tool.isSuccessed(result);
	}
	
	/**
	 * 使用八叉乐推荐
	 * @param resp 
	 * @param catId
	 * **/
	public void cancelRecommed(HttpServletResponse resp, String catId ){
		BasicDBObject result = (BasicDBObject) ZeroConnect.eyeBussinessSend("recommend", new BasicDBObject().append("catId", catId).append("flag", 0));
		Write2PageUtil.write(resp, new BasicDBObject().append("msg", Tool.isSuccessed(result) ? "ok" : "fail"));
	}
	
	/**
	 * 查列表页数据
	 * 
	 * */
	public void queryCatList(HttpServletResponse resp, String catId){
		BasicDBObject qField = new BasicDBObject().append("catId", catId);
		DBObject dataResult = DataCollection.findOne(Const.defaultDictMongo, Const.defaultDictMongoDB, RECOMMEND, qField);
		Write2PageUtil.write(resp, new BasicDBObject().append("msg", dataResult));
	}

}
