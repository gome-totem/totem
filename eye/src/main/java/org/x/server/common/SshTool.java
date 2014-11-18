package org.x.server.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class SshTool {

	private static Logger logger = LoggerFactory.getLogger(SshTool.class);
	private static final int defaultPort = 22;

	public static String countTagTomcatApp(String date, int tomcatNum, String keywords) {
		String logPath = "/server/logs/";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tomcatNum; i++) {
			String tomcatLogPath = logPath + "tomcat" + (i + 1) + "/catalina.out";
			sb.append(countDestroyTemplate(tomcatLogPath, date, keywords)).append(" ; ");
		}
		sb.append(countDestroyTemplate(logPath + "appserver.log", date,keywords)).append(" ;");
		return sb.toString();
	}
	
	public static String countTagTomcat(String date, int tomcatNum, String keywords) {
		String logPath = "/server/logs/";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tomcatNum; i++) {
			String tomcatLogPath = logPath + "tomcat" + (i + 1) + "/catalina.out";
			sb.append(countDestroyTemplate(tomcatLogPath, date, keywords)).append(" ; ");
		}
		return sb.toString();
	}
	
	public static String countTagApp(String date, String keywords) {
		String logPath = "/server/logs/";
		StringBuilder sb = new StringBuilder();
		sb.append(countDestroyTemplate(logPath + "appserver.log", date,keywords)).append(" ;");
		return sb.toString();
	}

	private static String countDestroyTemplate(String path, String date,String keywords) {
		return "cat " + path + " | grep \"" + date + ".* "+ keywords +"\" | wc -l";
	}

	public static BasicDBList execCmd(String username, String password, String host, String command) throws IOException {
		return execCmd(username, password, host, command, null);
	}

	public static BasicDBList execCmd(String username, String password, String host, String command, Integer port) throws IOException {
		if (port == null) {
			port = defaultPort;
		}
		logger.info("server [{}] begin to execute command [{}]", new Object[] { host, command });
		BasicDBList infos = new BasicDBList();
		BufferedReader reader = null;
		JSch jSch = new JSch();
		JSch.setConfig("StrictHostKeyChecking", "no");
		Session session = null;
		ChannelExec channel = null;
		try {
			session = jSch.getSession(username, host, port);
			session.setPassword(password);
			session.connect();
			logger.info("server [{}] session is connected [{}]", new Object[] { host, session.isConnected() });
			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.connect();
			logger.info("server [{}] channel status [{}]", new Object[] { host, channel.getExitStatus() });
			reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				infos.add(line);
			}
		} catch (JSchException e) {
			logger.error("can not reach host [{}] with username [{}] port [{}]", new Object[] { host, username, port });
			logger.error("JSchException", e);
		} finally {
			if (reader != null) {
				reader.close();
			}
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
		return infos;
	}

	public static void main(String args[]) throws Exception {

		String date = "2013-11-13";
		int tomcatNum = 3;

		String command = countTagTomcatApp(date, tomcatNum,"destory");

		BasicDBList info = SshTool.execCmd("root", "123", "10.58.22.10", command, 22);

		System.err.println(info);
		
		BasicDBObject returnInfo = new BasicDBObject();
		for (int i = 0, j = info.size(); i < j; i++) {
			if (i < tomcatNum) {
				returnInfo.append("tomcat" + (i + 1), info.get(i));
			} else {
				returnInfo.append("appserver", info.get(i));
			}
		}

		System.err.println(returnInfo);
	}
}
