package org.x.server.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.service.ICategoryFacetsSet;

import com.mongodb.BasicDBObject;

@Controller
@RequestMapping(value="/cloud/business")
public class CategoryFacetsSetController{

	protected static Logger logger = LoggerFactory.getLogger(CategoryFacetsSetController.class);
	@Autowired
	private ICategoryFacetsSet iCateoryFacets;
	
	@RequestMapping(value="/CategoryFacetsSet")
	protected void categoryFacetsSet(HttpServletRequest req, HttpServletResponse resp){
		String module = req.getParameter("module");
		PrintWriter out = null;
		resp.setCharacterEncoding("utf-8");
		try {
			out = resp.getWriter();
		} catch (IOException e) {
			logger.info("CategoryFacetsSetServlet error:", new Object[] { e });
		}
		BasicDBObject result = new BasicDBObject();
		if(module==null) module = "";
		if(module.equals("facetsOrder")){
			result = iCateoryFacets.orderFacets(req);
		}else if(module.equals("getFacetGroupByCatId")){
			result = iCateoryFacets.getFacetGroupByCatId(req);
		}else if(module.equals("catgoryOrder")){
			result = iCateoryFacets.categoryOrderSet(req);
		}else{
			result.put("msg", "error");
		}
		out.print(result);
	}
	

}
