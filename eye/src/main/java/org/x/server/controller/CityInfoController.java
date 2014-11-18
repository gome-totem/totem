package org.x.server.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * 更新地区的mongo库
 * 
 * 
 */
@Controller
@RequestMapping(value = "/cloud/eye")
public class CityInfoController {

	private static final String EYE_DEVISION = "eye_division_data";
	private static long count = 0;

	@RequestMapping(value = "/indexCity")
	public void indexCity(HttpServletRequest req, HttpServletResponse resp) {
		long startTime = System.currentTimeMillis();
		boolean isSuccess = initRegion();
		BasicDBObject o = null;
		if (isSuccess) {
			o = new BasicDBObject();
			long endTime = System.currentTimeMillis();
			o.append("size", count)
					.append("time", (endTime - startTime) / 1000);
		}
		Write2PageUtil.write(resp, o);
	}

	/**
	 * 初始化省市区到MongoDB
	 * 
	 * @return
	 */
	public boolean initRegion() {

		Connection conn = null;
		Statement state = null;
		ResultSet result = null;
		try {
			conn = null;//OracleConnect.getGomeDBConnection();
			String sql = "select division_code,division_name,parent_division_code,parent_division_name,div_level"
					+ " from prod_ogg.gome_mst_division gmd1 where  gmd1.Is_show = 1 and gmd1.is_enabled = 1 order by division_code asc";
			state = conn.createStatement();
			result = state.executeQuery(sql);
			BasicDBList distinctList = new BasicDBList();
			while (result.next()) {
				distinctList.add(new BasicDBObject()
						.append("code", result.getString("division_code"))
						.append("name", result.getString("division_name"))
						.append("pcode",
								result.getString("parent_division_code"))
						.append("pname",
								result.getString("parent_division_name"))
						.append("level", result.getString("div_level")));
			}
			if (!distinctList.isEmpty()) {
				count = distinctList.size();
				insertIntoMongoDB(distinctList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				result.close();
				state.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 写入省市区信息到MongoDB
	 * 
	 * @param distinct
	 * @return
	 */
	public void insertIntoMongoDB(BasicDBList distinct) {
	}
}
