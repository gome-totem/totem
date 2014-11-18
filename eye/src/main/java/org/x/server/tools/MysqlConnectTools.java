package org.x.server.tools;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.z.global.object.AccountObject;


public class MysqlConnectTools {


	private static String driver = "com.mysql.jdbc.Driver";
	public static Class<?> driverClass = null;
	public static Map<String, AccountObject> accounts = new HashMap<String, AccountObject>();
	private static Log log = LogFactory.getLog(MysqlConnectTools.class);
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
			log.error("init", e);
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

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
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
			log.error("startPool", e);
			return null;
		}

	}

	public static void shutDownPool(String poolname) {
		try {
			poolDriver.closePool(poolname);
		} catch (SQLException e) {
			log.error("shutDownPool", e);
		}
	}

	public static boolean getPoolStatus(String poolname) {
		if (!accounts.containsKey(poolname)) {
			//return poolname + ":not exist!";
			return false;
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
			log.error("getPoolStatus", e);
		}
		//return stat.toString();
		return true;
	}

	public synchronized static Connection getMysqlDbConnection(String ip, String username, String password, String dbName, String poolName) {
		Connection conn = null;
		String uri = "jdbc:mysql://" + ip + "/" + dbName;
		if(getPoolStatus(poolName)==true){
			try {
				conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + poolName);
				return conn;
			} catch (SQLException e) {
				log.error("getDbConnection", e);
				//e.printStackTrace();
			}
		}
		ObjectPool<?> pool = createPool(uri, username, password, 20, 10000);
		poolDriver.registerPool(poolName, pool);
		AccountObject oAccount = new AccountObject();
		oAccount.user = username;
		oAccount.password = password;
		accounts.put(poolName, oAccount);
		log.info("==Create PoolName[" + poolName + "] for Database Connection Succees.");
		try {
			conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + poolName);
		} catch (SQLException e) {
			log.error("getDbConnection", e);
		}
		return conn;
	}
	public static void close(Connection c) {
		try {
			if (c != null)
				c.close();
		} catch (SQLException e) {
			log.error("Connection", e);
		}
	}

	public static void main(String[] args){
		Connection connect = MysqlConnectTools.getMysqlDbConnection("10.58.50.26", "root", "mysql", "edm", "edmpool");
		

	}

}
