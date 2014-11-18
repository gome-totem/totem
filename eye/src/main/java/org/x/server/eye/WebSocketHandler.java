package org.x.server.eye;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gome.totem.sniper.util.FileUtils;
import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.StringUtil;
import com.gome.totem.sniper.util.ZookeeperUtils;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;

public class WebSocketHandler extends WebSocketServlet {

	private static final long serialVersionUID = 2322088002689732434L;
	protected static Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
	
	protected WebSocketSession session = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.info("init Stream Handler");
		Eye.init(config.getServletContext());
		logger.info("init dict server");
		this.loadDictServer(config.getServletContext());
		logger.info("init current zoo info");
		ZookeeperUtils.dealZooInfo(config.getServletContext());
	}

	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol,
			HttpServletRequest request) {
		session = new WebSocketSession(Eye.instance);
		Eye.sessions.add(session);
		return session;
	}

	private void loadDictServer(ServletContext servletContext) {
		if (logger.isInfoEnabled()) {
			logger.info("Begin to load dict info");
		}
		String configFilePath = servletContext.getRealPath("/") + "log/" + Globalkey.CONFIG_STORE_LOG;
		File f = new File(configFilePath);
		if (!f.exists()) {
			logger.info("dict is null please create it in  project");
			return;
		}
		String dictInfo = FileUtils.bufferedReadFile(configFilePath);
		if (StringUtils.isBlank(dictInfo)) {
			logger.info("dict info is empty please create it in  project");
			return;
		}
		BasicDBList dict = null;
		try {
			dict = (BasicDBList) JSON.parse(dictInfo);
		} catch (Exception e) {
			logger.error("Parse params to json error: " + dictInfo);
		}
		if (dict == null) {
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info("load dict info finish");
		}
		StringUtil.StoreDictServer(dict, servletContext);
	}
}
