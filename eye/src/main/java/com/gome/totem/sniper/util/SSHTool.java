package com.gome.totem.sniper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;

public class SSHTool {
	
	protected static Logger logger = LoggerFactory.getLogger(SSHTool.class);
	
	public static String execCmd(String command, BasicDBObject token) throws Exception {

		String user = token.getString("user", "");
		String passwd = token.getString("passwd","");
		String host = token.getString("host","");
//		String ssh_log_src = Globalkey.SshLogSrc;

		StringBuffer sb = new StringBuffer("\n");
		BufferedReader reader = null;
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, 22);
		session.setPassword(passwd);
		JSch.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		try {
			channel.connect();
			InputStream in = channel.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			String buf = null;
			while ((buf = reader.readLine()) != null) {
				sb.append(buf).append("\n");
				logger.info(sb.toString());
			}
			while (true) {
				if (!channel.isClosed()) {
					try {
						Thread.sleep(3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}
				int exitStatus = channel.getExitStatus();
				if (exitStatus != 0) {
					System.out.println("existStatus=" + exitStatus);
				}
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			channel.disconnect();
			session.disconnect();
		}
		return sb.toString();
	}

	public static String execCmd(String user, String passwd, String host, String command) throws Exception {
		StringBuffer sb = new StringBuffer("\n");
		BufferedReader reader = null;
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, 22);
		session.setPassword(passwd);
		JSch.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		try {
			channel.connect();
			
			//排查java ssh出现错误
			 channel.setInputStream(null);  
             ((ChannelExec) channel).setErrStream(System.err); 
			
			InputStream in = channel.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			String buf = null;
			while ((buf = reader.readLine()) != null) {
				sb.append(buf).append("\n");
				logger.info(sb.toString());
			}
			while (true) {
				if (!channel.isClosed()) {
					try {
						Thread.sleep(3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}
				int exitStatus = channel.getExitStatus();
				if (exitStatus != 0) {
					System.out.println("existStatus=" + exitStatus);
				}
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			channel.disconnect();
			session.disconnect();
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		try {
			SSHTool.execCmd("source /etc/profile;source ~/.bash_profile;source ~/.bashrc;vdir;", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
