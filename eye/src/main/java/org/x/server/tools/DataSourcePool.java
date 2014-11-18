package org.x.server.tools;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;

import com.gome.totem.sniper.util.Globalkey;

public class DataSourcePool {
	private static BasicDataSource dataSource = null;
	private static String driverClass = Globalkey.driverClass;
	private static String url = Globalkey.hiveConnUrl;// 获取URL
	private static String user = Globalkey.hiveUname;// 获取登录名
	private static String pwd = Globalkey.hiveUpwd;// 获取登录密码
	
	public DataSourcePool() {
	}

	public static void init() {

		if (dataSource != null) {
			try {
				dataSource.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			dataSource = null;
		}

		try {
			Properties p = new Properties();
			p.setProperty("driverClassName", driverClass);
			p.setProperty("url", url);
			p.setProperty("password", user);
			p.setProperty("username", pwd);
			p.setProperty("maxActive", "20");
			p.setProperty("initialSize", "4");
			p.setProperty("maxIdle", "10");
			p.setProperty("minIdle", "10");
			p.setProperty("maxWait", "5000");
			p.setProperty("removeAbandoned", "true");
			p.setProperty("removeAbandonedTimeout", "300");
			p.setProperty("testOnBorrow", "true");
			p.setProperty("logAbandoned", "true");
			dataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized Connection getConnection() throws SQLException {
		if (dataSource == null) {
			init();
		}
		Connection conn = null;
		if (dataSource != null) {
			conn = dataSource.getConnection();
		}
		return conn;
	}
}