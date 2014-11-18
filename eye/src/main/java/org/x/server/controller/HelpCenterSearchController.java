package org.x.server.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.z.global.connect.ZeroConnect;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Controller
@RequestMapping("/cloud/business")
public class HelpCenterSearchController{

	protected static Logger logger = LoggerFactory.getLogger(HelpCenterSearchController.class);

	@RequestMapping(value="/helpcenter")
	protected void helpcenter(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String module = req.getParameter("module");
		if(module.equals("ccfullIndex")){
			ccFullIndex(req,resp);
		}else{
			resp.getWriter().print("error");
		}
	}
	
	private void ccFullIndex(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		DBObject rtn = ZeroConnect.remoteCall("synhelpdata", "synFullDataToIndex", new Object[]{});
		resp.getWriter().print(new BasicDBObject().append("msg", "success"));
	}
}
