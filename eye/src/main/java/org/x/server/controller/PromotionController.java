package org.x.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.common.Tool;
import org.x.server.service.IPromotion;

import com.gome.totem.sniper.util.StringUtil;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBObject;
@Controller
@RequestMapping(value="/cloud/business")
public class PromotionController{
	@Autowired
	private IPromotion promotion;
	
	/**
	 * 数据总线
	 * 
	 * */
	@RequestMapping(value={"promotion","categoryTree"})
	public  void promotion(HttpServletRequest req, HttpServletResponse resp)
			throws  IOException {
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("application/json");
		String ps = req.getParameter("ps"), skuId = req.getParameter("skuId"), catId = req.getParameter("catId"), score = req.getParameter("score");
		
		if(skuId != null){skuId = skuId.trim();}
		if(catId != null){catId = catId.trim();}
		if(score != null){score = score.trim();}
		
		boolean isHomeCat = promotion.checkCategory(catId);
		
		if(!StringUtils.isEmpty(score)){
			int len = 1;
			String[] scoreStrs = score.split("\\.");
			if(scoreStrs != null && scoreStrs.length == 2){
				len = scoreStrs[1].length();
			}
			
			float promSc = Float.parseFloat(score);
			if(!StringUtil.isNullOrEmpty(ps) && ps.equalsIgnoreCase("ps")){
				if(isHomeCat && len != 2){
					Write2PageUtil.write(resp, new BasicDBObject().append("msg", "保留两位小数！"));
				}else	if(promotion.checkScore(catId, promSc)){
					promotion.setScore(skuId, catId, promSc, isHomeCat, resp);
				}else if(isHomeCat){
					Write2PageUtil.write(resp, new BasicDBObject().append("msg", "输入的分值非法(检查分值是否在3.01～5.99之间)！"));
				}else{
					Write2PageUtil.write(resp, new BasicDBObject().append("msg", "输入的分值非法(检查分值是否为2，3，4，5)！"));
				}
			}else if(!StringUtil.isNullOrEmpty(ps) && ps.equalsIgnoreCase("pc")){
				if(isHomeCat && len != 2){
					Write2PageUtil.write(resp, new BasicDBObject().append("zError", 4));//输入的分值小数位不对
				}else if( promotion.checkScore(catId, promSc)){
					BasicDBObject result = promotion.dataCheck(skuId, catId, promSc, isHomeCat);
					BasicDBObject r = new BasicDBObject();
					int error = result.getInt("zError", 0);
					if(error == 0){
						r = Tool.parseSku(result);
					}
					r.append("zError", error);
					Write2PageUtil.write(resp, r);
				}else if(isHomeCat){
					Write2PageUtil.write(resp, new BasicDBObject().append("zError", 3));//输入的分值非法
				}else{
					Write2PageUtil.write(resp, new BasicDBObject().append("zError", 5));//输入的分值非法
				}
			}
		}else{
			if(!StringUtil.isNullOrEmpty(ps) && ps.equalsIgnoreCase("firesearch")){
				promotion.getCategory(req, resp);
			}else if(!StringUtil.isNullOrEmpty(ps) && ps.equalsIgnoreCase("getPromoScore")){
				promotion.getPromoScore(req, resp, isHomeCat);
			}else if(!StringUtil.isNullOrEmpty(ps) && ps.equalsIgnoreCase("pf")){
				promotion.dosearch(req, resp, isHomeCat);
			}else if(!StringUtil.isNullOrEmpty(ps) && ps.equalsIgnoreCase("del")){
				promotion.delScore(skuId, catId, resp);
			}else{
				Write2PageUtil.write(resp, new BasicDBObject().append("msg", "请输入分值"));//输入的分值非法
			}
		}
	}
}
