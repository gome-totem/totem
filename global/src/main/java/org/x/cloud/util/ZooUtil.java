package org.x.cloud.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.cloud.dict.Global;
import org.x.cloud.dict.ServerDict;

public class ZooUtil {
	protected static Logger logger = LoggerFactory.getLogger(ZooUtil.class);

	public static CuratorFramework create() {
		StringBuilder buffer = new StringBuilder();
		String[] values = Global.ZooIP.split("\\,");
		for (int i = 0; i < values.length; i++) {
			String ip = values[i];
			if (StringUtils.isEmpty(ip)) {
				continue;
			}
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			if (ip.indexOf(":") <= 0) {
				buffer.append(ip);
				buffer.append(":");
				buffer.append(Global.ZooKeeperPort);
			} else {
				buffer.append(ip);
			}
		}
		RetryNTimes retryPolicy = new RetryNTimes(5, 5000);
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(buffer.toString()).retryPolicy(retryPolicy)
				.connectionTimeoutMs(Global.ZooKeeperTimeout).sessionTimeoutMs(Global.ZooKeeperTimeout * 3).build();
		return client;
	}

	public static String readNodeRole(String serverIP) {
		Socket sock = null;
		BufferedReader reader = null;
		try {
			sock = new Socket(serverIP, Global.ZooKeeperPort);
			OutputStream outstream = sock.getOutputStream();
			outstream.write("srvr".getBytes());
			outstream.flush();
			sock.shutdownOutput();
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Mode:")) {
					return line.replaceAll("Mode: ", "");
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (sock != null) {
					sock.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (Exception ee) {
			}
		}
	}

	public static boolean exists(CuratorFramework client, String path, CuratorWatcher watcher) {
		try {
			if (watcher != null) {
				return client.checkExists().usingWatcher(watcher).forPath(path) != null;
			} else {
				return client.checkExists().forPath(path) != null;
			}
		} catch (Exception e) {
			logger.error("exists", e);
			return false;
		}
	}

	public static boolean exists(CuratorFramework client, String path) {
		return exists(client, path, null);
	}

	public static void createPath(CuratorFramework client, String path, String content, CreateMode mode) {
		try {
			client.create().withMode(mode).forPath(path, StringUtil.toBytes(content));
		} catch (Exception e) {
			logger.error("createPath", e);
		}
	}

	public static void setPath(CuratorFramework client, String path, String content, CreateMode mode) {
		try {
			if (client.checkExists().forPath(path) == null) {
				client.create().withMode(mode).forPath(path, StringUtil.toBytes(content));
			} else {
				client.setData().forPath(path, StringUtil.toBytes(content));
			}
		} catch (Exception e) {
			logger.error("setPath", e);
		}
	}

	public static String getData(CuratorFramework client, String path) {
		return getData(client, path, null);
	}

	public static String getData(CuratorFramework client, String path, CuratorWatcher watcher) {
		try {
			if(client.checkExists().forPath(path) == null){
				return null;
			}
			if (watcher != null) {
				return StringUtil.toString(client.getData().usingWatcher(watcher).forPath(path));
			} else {
				return StringUtil.toString(client.getData().forPath(path));
			}
		} catch (Exception e) {
			logger.error("getData", e);
			return null;
		}
	}

	public static String createEphemeralSequential(CuratorFramework client, String path, byte[] payload) {
		try {
			return client.create().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
		} catch (Exception e) {
			logger.error("createEphemeralSequential", e);
			return null;
		}
	}

	public static void remove(CuratorFramework client, String path) {
		try {
			if(client.checkExists().forPath(path)==null){
				logger.info("this Path not exists");
				return;
			}
			client.delete().forPath(path);
		} catch (Exception e) {
			logger.error("remove", e);
		}
	}

	public static void delete(CuratorFramework client, String path) {
		try {
			client.delete().guaranteed().forPath(path);
		} catch (Exception e) {
			logger.error("delete", e);
		}
	}

	public static List<String> getChilds(CuratorFramework client, String path) {
		return getChilds(client, path, null);
	}

	public static List<String> getChilds(CuratorFramework client, String path, CuratorWatcher watcher) {
		try {
			if (watcher != null) {
				return client.getChildren().usingWatcher(watcher).forPath(path);
			} else {
				return client.getChildren().forPath(path);
			}
		} catch (Exception e) {
			logger.error("getChilds", e);
			return null;
		}
	}

	public static void main(String[] args) {
		int mode = 1;
		switch (mode) {
		case 0:
			String zooIP = "10.58.50.99";
			System.out.println(readNodeRole(zooIP));
			break;
		case 1:
			System.out.println(Global.ZooIP);
			System.out.println(ServerDict.self.routers().toString());
			break;
		}
	}
}
