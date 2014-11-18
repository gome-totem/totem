package org.x.server.controller;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.eye.Eye;

import com.gome.totem.sniper.util.FileUtils;
import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.IpUtils;
import com.gome.totem.sniper.util.TimeUtil;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.gome.totem.sniper.util.ZookeeperUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
/**
 * 监控系统模块
 * @author doujintong
 *
 */
@Controller
@RequestMapping(value = "/cloud/eye")
public class MonitorManageController {

	private Logger log = Logger.getLogger(MonitorManageController.class);
	private static final String PARSE_ERROR_INFO = "Parse params to json error";
	
	/**
	 * 云眼访问
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/zookeeper/ajax-info")
	public void zookeeper(HttpServletRequest request,
			HttpServletResponse response) {
		
		ServletContext servletContext = request.getServletContext();
		BasicDBObject zooTree = (BasicDBObject) servletContext
				.getAttribute("zooTree");
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "visit")
				.append("context", "执行访问了云眼！"));
		Write2PageUtil.write(response, zooTree);
	}
	/**
	 * dict配置
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/config")
	protected void config(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String op = req.getParameter("op");
		if (op.equals("w")) {
			dealDictLog(req, resp);
		} else if (op.equals("r")) {
			readDictLog(req, resp);
		}
	}

	private void readDictLog(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession();
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "read")
				.append("context", "启动上次配置！"));
		String logPath = session.getServletContext().getRealPath("/") + "log/";
		Write2PageUtil.write(resp, FileUtils.bufferedReadFile(logPath
				+ Globalkey.CONFIG_STORE_LOG));
	}

	private void dealDictLog(HttpServletRequest req, HttpServletResponse resp) {
		BasicDBList dict = new BasicDBList();

		String params = req.getParameter("params");
		try {
			dict = (BasicDBList) JSON.parse(params);
		} catch (Exception e) {
			log.error(PARSE_ERROR_INFO + ": " + params);
		}
		ServletContext servletContext = req.getServletContext();
		HttpSession session = req.getSession();
		session.getServletContext().setAttribute(Globalkey.DICT_SERVER, dict);
		String logPath = session.getServletContext().getRealPath("/") + "log/";
		String date = TimeUtil.getStringFromDate(System.currentTimeMillis());

		StringBuffer context = new StringBuffer();
		context.append("ip: " + IpUtils.getIp(req)).append("\n");
		context.append("date: " + date).append("\n");
		context.append("data: " + params).append("\n");

		FileUtils.write2File(logPath + Globalkey.CONFIG_STORE_LOG, params,
				false);
		FileUtils.write2File(logPath + Globalkey.CONFIG_STORE_LOG_BAK,
				context.toString(), true);
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "read")
				.append("context", "配置了Dict！"));
		ZookeeperUtils.dealZooInfo(servletContext);

		try {
			resp.sendRedirect("/dispacher/monitorPage");
		} catch (IOException e) {
			log.error("IOException sendRedirect to monitor.jsp error! ");
		}
	}
	
	
	
}
