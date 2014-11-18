package org.x.server.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gome.totem.sniper.util.Globalkey;

public class MysqlTools {
	static {
		try {
			// 加载驱动
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection(String url,String user,String pwd) {
		Connection conn = null;
		try {
			// 连接数据库
			conn = DriverManager.getConnection(url, user, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 释放资源
	 * @param o
	 */
	public static void release(Object o) {
		if (o == null) {
			return;
		}
		if (o instanceof ResultSet) {
			try {
				((ResultSet) o).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (o instanceof Statement) {
			try {
				((Statement) o).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (o instanceof Connection) {
			Connection c = (Connection) o;
			try {
				if (!c.isClosed()) {
					c.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 释放链接资源
	 * @param rs
	 * @param stmt
	 * @param conn
	 */
	public static void release(ResultSet rs, Statement stmt, Connection conn) {
		release(rs);
		release(stmt);
		release(conn);
	}
	
	public static void main(String [] args){
		String url = Globalkey.ShiroConnUrl;// 获取URL
		String user = Globalkey.ShiroUname;// 获取登录名
		String pwd = Globalkey.ShiroUpwd;// 获取登录密码
		Connection conn = getConnection(url, user, pwd);
		Statement stmt = null;
		ResultSet rs = null;
		String pname = "testDB";
		String [] pupdate = {"testDB","u8541"};
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from users where username='" + pname +"'");
			//stmt.executeUpdate("update users set password='" +pupdate[1]+ "',enable=1 where username='" + pupdate[0] + "'");
//			stmt.execute("insert into users(username,password,enable) values('testDB','123456',0)");
			while(rs.next()){
				String name = rs.getString("username");
				String upwd = rs.getString("password");
				int enable = rs.getInt("enable");
				System.out.println(">>>" +name+ "  " +upwd+ "  " + enable);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			release(rs,stmt,conn);
		}
	}
}
