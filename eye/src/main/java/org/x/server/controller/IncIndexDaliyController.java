package org.x.server.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.eye.Eye;
import org.x.server.service.IIncIndexDaliy;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBObject;

@Controller
@RequestMapping(value = "/cloud/business")
public class IncIndexDaliyController {

	public static Logger log = LoggerFactory.getLogger(IncIndexDaliyController.class);
	private static final String INDEXLOGS = "indexlogs";

	@Autowired
	private IIncIndexDaliy incIndexDaily;

	@RequestMapping(value = "/daliylog")
	protected void daliylog(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BasicDBObject dbResult = new BasicDBObject();

		String id = req.getParameter("id");

		if (id == null || StringUtils.isEmpty(id)) {

			BasicDBObject today =incIndexDaily. dealDaliyInMongo(INDEXLOGS, 0);
			dbResult.append("today", today);

			BasicDBObject yesterday = incIndexDaily.dealDaliyInMongo(INDEXLOGS, 1);
			dbResult.append("yesterday", yesterday);

			BasicDBObject todayBeforYesterday = incIndexDaily.dealDaliyInMongo(INDEXLOGS, 2);
			dbResult.append("todayBeforYesterday", todayBeforYesterday);

			Eye.eyeLogger.printLog(new BasicDBObject().append("action",
					"daliyLog").append("context", "查看增量信息！"));
		} else {
			BasicDBObject today = incIndexDaily.findOneInMongo(INDEXLOGS, id);
			dbResult.append("searchResult", today);
			Eye.eyeLogger.printLog(new BasicDBObject().append("action",
					"searchLog").append("context",
					"查看productId为 [" + id + "] 的增量信息！"));
		}

		Write2PageUtil.write(resp, dbResult);
	}
}
