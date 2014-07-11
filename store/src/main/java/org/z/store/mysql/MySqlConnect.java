package org.z.store.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.object.AccountObject;
import org.z.global.object.SecurityObject;
import org.z.global.zk.ServerDict;

import com.mongodb.BasicDBObject;

public class MySqlConnect {
	private static String driver = "com.mysql.jdbc.Driver";
	public static Class<?> driverClass = null;
	public static Map<String, AccountObject> accounts = new HashMap<String, AccountObject>();
	protected static Logger logger = LoggerFactory.getLogger(MySqlConnect.class);
	protected static PoolingDriver poolDriver = null;

	static {
		init();
	}

	public static void init() {
		try {
			Class.forName("org.apache.commons.dbcp.PoolingDriver");
			driverClass = Class.forName(driver);
			poolDriver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
		} catch (Exception e) {
			logger.error("init", e);
		}
	}

	public synchronized static void checkPool(String serverName, String user, String password) {
		String[] values = new String[accounts.keySet().size()];
		accounts.keySet().toArray(values);
		for (int i = 0; i < values.length; i++) {
			String name = values[i];
			if (name.startsWith(serverName + "@")) {
				AccountObject oAccount = accounts.get(name);
				if ((oAccount.user.equalsIgnoreCase(user)) && (oAccount.password.equalsIgnoreCase(password))) {
					continue;
				}
				accounts.remove(name);
				shutDownPool(name);
			}
		}
	}

	public static ObjectPool<?> createPool(String uri, String dbUser, String dbPwd, int max, long wait) {
		try {
			GenericObjectPool.Config config = new GenericObjectPool.Config();
			config.maxActive = 150;
			config.maxIdle = 100;
			config.minIdle = 30;
			config.maxWait = 1000;
			config.testOnBorrow = true;
			config.testOnReturn = true;
			config.testWhileIdle = true;
			/* GenericObjectPool 保存所有连接对象 */
			ObjectPool<?> connectionPool = new GenericObjectPool(null, config);
			Properties p = new Properties();
			p.setProperty("user", dbUser);
			p.setProperty("password", dbPwd);
			p.setProperty("useUnicode", "true");
			p.setProperty("characterEncoding", "UTF8");
			/* 根据ObjectPool设定的URL和登陆用户信息,建立工厂模式创建实际的连接对象 */
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, p);
			/* PoolableConnectionFactory 为创建的连接对象增加过期校验等属性 */
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, "select 1", false, true);
			return connectionPool;
		} catch (Exception e) {
			logger.error("startPool", e);
			return null;
		}

	}

	public static void shutDownPool(String poolname) {
		try {
			poolDriver.closePool(poolname);
		} catch (SQLException e) {
			logger.error("shutDownPool", e);
		}
	}

	public static String getPoolStatus(String poolname) {
		if (!accounts.containsKey(poolname)) {
			return poolname + ":not exist!";
		}
		StringBuffer stat = new StringBuffer();
		try {
			ObjectPool<?> connectionPool = poolDriver.getConnectionPool(poolname);
			stat.append("-- Active Connection: ");
			stat.append(connectionPool.getNumActive());
			stat.append(" ,");
			stat.append("Free Connection: ");
			stat.append(connectionPool.getNumIdle());
			stat.append(" . --");
		} catch (Exception e) {
			logger.error("getPoolStatus", e);
		}
		return stat.toString();
	}

	public synchronized static Connection getDbConnection(String serverName, String dbName) {
		Connection conn = null;
		String poolName = serverName + "@" + dbName;
		if (accounts.get(poolName) == null) {
			BasicDBObject oServer = ServerDict.self.serverBy(serverName);
			if (oServer == null) {
				logger.error("**serverName=" + serverName + " not exist in DictServer");
				return conn;
			}
			String uri = "jdbc:mysql://" + oServer.getString("ip") + "/" + dbName;
			BasicDBObject oMysql = (BasicDBObject) oServer.get("mysql");
			if (oMysql == null) {
				logger.error("**serverName=" + serverName + " has not mysql define in DictServer");
				return conn;
			}
			String userName = oMysql.getString("user");
			String password = oMysql.getString("password");
			if (oServer.getBoolean("encrypt", false) == true) {
				String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.MysqlUser, SecurityMode.Undo, userName),
						SecurityObject.create(SecurityType.MysqlPassword, SecurityMode.Undo, password));
				userName = values[0];
				password = values[1];
			}
			ObjectPool<?> pool = createPool(uri, userName, password, 20, 10000);
			poolDriver.registerPool(poolName, pool);
			AccountObject oAccount = new AccountObject();
			oAccount.server = serverName;
			oAccount.user = userName;
			oAccount.password = password;
			accounts.put(poolName, oAccount);
			logger.info("==Create PoolName[" + poolName + "] for Database Connection Succees.");
		}
		try {
			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + poolName);
		} catch (SQLException e) {
			logger.error("getDbConnection", e);
		}
		return conn;
	}

	public static void close(Connection c) {
		try {
			if (c != null)
				c.close();
		} catch (SQLException e) {
			logger.error("Connection", e);
		}
	}

}
