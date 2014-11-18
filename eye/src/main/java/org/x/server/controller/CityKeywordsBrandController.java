package org.x.server.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.service.ICityKeywordsBrand;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
@Controller
@RequestMapping(value="/cloud/eye")
public class CityKeywordsBrandController{

	protected static Logger logger = LoggerFactory.getLogger(CityKeywordsBrandController.class);
	@Autowired
	private ICityKeywordsBrand cityWordsBrand;
	
	@RequestMapping(value="/cityKeywordsBrand")
	protected void cityKeywordsBrand(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");
		PrintWriter out = resp.getWriter();
		String module = req.getParameter("module");
		if(module.equals("getChildrens")){
			BasicDBList childrenList = new BasicDBList();
			childrenList.add(new BasicDBObject().append("id", "11").append("pid", "1").append("name", "电视机").append("isParent", true));
			childrenList.add(new BasicDBObject().append("id", "12").append("pid", "1").append("name", "手机").append("isParent", true));
			childrenList.add(new BasicDBObject().append("id", "13").append("pid", "1").append("name", "冰箱").append("isParent", true));
			childrenList.add(new BasicDBObject().append("id", "14").append("pid", "1").append("name", "空调").append("isParent", true));
			childrenList.add(new BasicDBObject().append("id", "15").append("pid", "1").append("name", "洗衣机").append("isParent", true));
			out.print(childrenList);
		}else if(module.equals("getBrands")){
			cityWordsBrand.getBrandsByCatId(req, resp);
		}
	}
}
