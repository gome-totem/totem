package org.x.server.service;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class RMTShellHandler {
	private Connection conn;
	private String ip;
	private String usr;
	private String psword;
	private String charset = Charset.defaultCharset().toString();
	private static final int TIME_OUT = 1000 * 5 * 60;
	public static Logger log = LoggerFactory.getLogger(RMTShellHandler.class);
	
	public RMTShellHandler(String ip, String usr, String ps) {
		this.ip = ip;
		this.usr = usr;
		this.psword = ps;
	}

	public RMTShellHandler() {
	}

	private boolean login() throws IOException {
		conn = new Connection(ip);
		conn.connect();
		return conn.authenticateWithPassword(usr, psword);
	}

	public int exec(String cmds) throws Exception {

		StreamGobbler stdOut = null;
		StreamGobbler stdErr = null;
		String outStr = "";
		String outErr = "";
		int ret = -1;

		try {
			if (login()) {
				Session session = conn.openSession();
				session.execCommand(cmds);
				stdOut = new StreamGobbler(session.getStdout());
				outStr = processStream(stdOut, charset);
				stdErr = new StreamGobbler(session.getStderr());
				outErr = processStream(stdErr, charset);
				session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
				if(!StringUtils.isEmpty(outStr)){
					log.info("outStr:" + outStr);
				}
				if(!StringUtils.isEmpty(outErr)){
					log.info("outErr:" + outErr);
				}
				ret = session.getExitStatus();
			} else {
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	private String processStream(StreamGobbler stdOut, String charset)
			throws Exception {
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		while (stdOut.read(buf) != -1) {
			sb.append(new String(buf, charset));
		}
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {

		RMTShellHandler exe = new RMTShellHandler("10.58.50.99", "root","password");
		System.out.println(exe.exec("sh /workspace/tomcat1/bin/shutdown.sh"));
		System.out.println(exe.exec("sh /workspace/tomcat1/bin/startup.sh"));
	}
}
