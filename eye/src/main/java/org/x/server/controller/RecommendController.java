package org.x.server.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.x.server.eye.Eye;
import org.x.server.service.IRecommend;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
@Controller
@RequestMapping(value="/cloud/business")
public class RecommendController{
	@Autowired
	private IRecommend recomend;
	@RequestMapping(value="/recommad",method=RequestMethod.GET)
	protected void recommendGet(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("application/json");
		
		String skuId = req.getParameter("skuId"),	catId = req.getParameter("catId"), ps = req.getParameter("ps");
		
		if(ps.equalsIgnoreCase("ps")){
			recomend.search(resp, skuId);
		}else if(ps.equalsIgnoreCase("pc")){
			recomend.cancelRecommed(resp, catId);
		}else if(ps.equalsIgnoreCase("pq")){
			recomend.queryCatList(resp, catId);
		}
		
	}
	
	@RequestMapping(value="/recommad",method=RequestMethod.POST)
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("application/json");
		
		String catId = req.getParameter("catId");

		if(StringUtils.isEmpty(catId)){
			this.recommendGet(req, resp);
		}else{
			String datas = req.getParameter("datas");
			BasicDBList skus = (BasicDBList) JSON.parse(datas);
			boolean ok = recomend.commitList(catId, skus);

			BasicDBObject logMsg = new BasicDBObject();
			logMsg.append("catId", catId);
			logMsg.append("datas", skus);
			logMsg.append("from", "recommed");
			Eye.eyeLogger.writeLog(logMsg);
			
			Write2PageUtil.write(resp, new BasicDBObject().append("msg", ok ? "ok" : "fail").append("skus", skus));
		}
	}

}
