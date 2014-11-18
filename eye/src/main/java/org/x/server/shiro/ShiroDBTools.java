package org.x.server.shiro;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.x.server.tools.MysqlTools;

import com.gome.totem.sniper.util.Globalkey;
import com.mongodb.BasicDBObject;

public class ShiroDBTools {
	/**
	 * 通过用户名查找用户
	 * @param uname
	 * @return
	 */
	public static BasicDBObject getUserByName(String uname){
		BasicDBObject user = new BasicDBObject();
		Statement stmt = null;
		ResultSet rs = null;
		Connection conn = MysqlTools.getConnection(Globalkey.ShiroConnUrl, Globalkey.ShiroUname, Globalkey.ShiroUpwd);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from users where username='" + uname + "'");
			while(rs.next()){
				user.append("username", rs.getString("username"));
				user.append("password", rs.getString("password"));
				user.append("enable", rs.getString("enable"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			MysqlTools.release(rs, stmt, conn);
		}
		return user;
	}
	/**
	 * 通过用户名修改密码
	 * @param uname
	 * @param upwd
	 * @return
	 */
	public static int updatePasswordByName(String uname,String upwd){
		int updateNum = 0;
		Statement stmt = null;
		Connection conn = MysqlTools.getConnection(Globalkey.ShiroConnUrl, Globalkey.ShiroUname, Globalkey.ShiroUpwd);
		try {
			stmt = conn.createStatement();
			updateNum = stmt.executeUpdate("update users set password='" + upwd + "' where username='" + uname + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			MysqlTools.release(null, stmt, conn);
		}
		return updateNum;
	}
}
