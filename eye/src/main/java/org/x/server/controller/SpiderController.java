package org.x.server.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.eye.Eye;
import org.z.global.connect.ZeroConnect;
import org.z.global.util.StringUtil;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBObject;

@Controller
@RequestMapping(value = "/cloud/business")
public class SpiderController {
	
	
	@RequestMapping(value = "/spider")
	protected void spider(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		startSpider(req, resp);
	}

	private void startSpider(HttpServletRequest req, HttpServletResponse resp) {
		String jarPath = req.getParameter("jar"), logPath = req
				.getParameter("log"), xmlPath = req.getParameter("xml");

		BasicDBObject msg = null;
		if (StringUtil.isEmpty(xmlPath)) {
			msg = new BasicDBObject().append("error", "xml路径配置错误！").append(
					"info", "");
		} else if (StringUtil.isEmpty(logPath)) {
			msg = new BasicDBObject().append("error", "log路径配置错误！").append(
					"info", "");
		} else if (StringUtil.isEmpty(jarPath)) {
			msg = new BasicDBObject().append("error", "jar路径配置错误！").append(
					"info", "");
		} else {
			StringBuilder commad = new java.lang.StringBuilder();
			commad.append("/bin/sh -c 'export DISPLAY=:1.0;java -jar ")
					.append(jarPath).append(" ").append(logPath).append(" ")
					.append(xmlPath).append("'");
			ZeroConnect.remoteCall("spider", "startSpider", commad.toString());
			msg = new BasicDBObject().append("info", "Spider运行完成！").append(
					"error", "");
		}

		BasicDBObject logMsg = new BasicDBObject();
		logMsg.append("jarPath", jarPath);
		logMsg.append("logPath", logPath);
		logMsg.append("xmlPath", xmlPath);
		logMsg.append("from", "spider");
		Eye.eyeLogger.printLog(logMsg);
		Write2PageUtil.write(resp, msg);
	}
}
