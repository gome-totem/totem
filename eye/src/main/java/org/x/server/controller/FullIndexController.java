package org.x.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.z.global.dict.Global;
import org.z.global.epub.RmiRositoryImpl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;

@Controller
@RequestMapping(value = "/cloud/eye")
public class FullIndexController {

	private static final Integer LOCK = new Integer(0);
	private static RmiRositoryImpl ri = new RmiRositoryImpl();
	private List<String> logs = new ArrayList<String>();
	private GetIndexLog indexLog = null;
	private boolean runing = false;
	private Logger logger = LoggerFactory.getLogger(FullIndexController.class);
	
	static{
		ri.setCurrentEnv(Global.currentEnv);
	}

	@RequestMapping(value="/fullindex",method=RequestMethod.GET)
	public void fullIndexGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		String opp = request.getParameter("opp");
		if ("start".equals(opp)) {
			indexLog = new GetIndexLog();
			runing = true;
			indexLog.start();
		} else if ("stop".equals(opp) && indexLog == null) {
			runing = false;
			indexLog.interrupt();
			indexLog = null;
		}
		synchronized (LOCK) {
			for (String log : logs) {
				response.getWriter().write(log);
				response.getWriter().write("<br/>");
			}
			logs.clear();
		}
		response.getWriter().flush();
	}

	@RequestMapping(value="/fullindex",method=RequestMethod.POST)
	public void fullIndexPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String type = request.getParameter("type");
		String opp = request.getParameter("opp");
		ri.invokeFiled("loggingError", "true");
		ri.invokeFiled("loggingDebug", "true");
		ri.invokeFiled("loggingInfo", "true");
		if ("category".equals(type)) {
			if ("start".equals(opp)) {
				ri.invokeMethod("startFullCategory");
			} else if ("stop".equals(opp)) {
				ri.invokeMethod("stopFullCategory");
			}
		} else if ("product".equals(type)) {
			if ("start".equals(opp)) {
				ri.invokeMethod("startFullProduct");
			} else if ("stop".equals(opp)) {
				ri.invokeMethod("stopFullProduct");
			}
		}
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.append("type", type);
		dbObject.append("opp", opp);
		response.getWriter().write(dbObject.toString());
	}

	class GetIndexLog extends Thread {
		@Override
		public void run() {
			BufferedReader reader = null;
			JSch jsch = new JSch();
			Channel channel = null;
			Session session = null;
			try {
				session = jsch.getSession("gome_guest", "10.58.22.17", 22);
				session.setPassword("123456");
				JSch.setConfig("StrictHostKeyChecking", "no");
				session.connect();
				channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand("tail -f atglogs/weblogic_atg_production.log");
				channel.connect();
				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);

				InputStream in = channel.getInputStream();
				reader = new BufferedReader(new InputStreamReader(in));
				String buf = null;
				while (runing && (buf = reader.readLine()) != null) {
					synchronized (LOCK) {
						logs.add(buf);
					}
				}
			} catch (IOException e) {
				logger.error("", e);
			} catch (JSchException e) {
				logger.error("", e);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (channel != null) {
					channel.disconnect();
				}
				if (session != null) {
					session.disconnect();
				}
			}
		}
	}

}
