package org.x.server.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.common.ZExcel;
import org.x.server.controller.CityInfoController;
import org.x.server.eye.ContextListener;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public final class HiveJdbcTools {
	public static BasicDBList allCacheKeys=null;//分类分组
	public static BasicDBList noResultCacheKeys=null;//无结果搜索
	public static BasicDBList keys_100=null;//地图搜索中使用
	public static BasicDBList city_100=null;//地图搜索中使用
	public static HashMap<String, Object> city_keys=new HashMap<String, Object>();//城市搜索中使用
	private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);

	//public static StringBuffer sql = new StringBuffer("select question,catid, count(1) cnt from tbl_search where catid !='' group by question,catid order by cnt desc");
	public static StringBuffer sql=new StringBuffer("select question,catid,cnt from (select question,catid,cnt,row_number() over (partition by catid order by cnt desc) as row_num from (select question,catid, count(1) as cnt from tbl_search where catid !='' group by question,catid) t ) t1 where row_num<=200");
	public static StringBuffer nosql = new StringBuffer("select question,count(1) cnt from tbl_search where catid='' and facets is null and resultcount=0 group by question order by cnt desc");
	public static StringBuffer keys_sql = new StringBuffer("select question,count(1) value from tbl_search group by question order by value desc limit 100");
	public static StringBuffer city_sql = new StringBuffer("select districtcode,count(1) value from tbl_search where  districtcode is not null group by districtcode order by value desc limit 100");

	
	private static final String EYE_DEVISION = "eye_division_data";//地区mongo库的集合名

	public HiveJdbcTools() throws Exception {}

	/**
	 * 获得连接
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection( ) throws Exception {
		return DataSourcePool.getConnection();
	}

	// 关闭连接
	public static void freeConnection(ResultSet rs, Statement ps, Connection con)
			throws SQLException {
		try {
			if (rs != null) {
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
			if(con!=null){
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 使用Statement查询数据，返回ResultSet
	 * 
	 * @param sql
	 * @return
	 */
	public static ResultSet query(String sql) throws Exception {
		Statement stmt = null; 
		ResultSet res = null;
		Connection con=null;
		try {
			con=getConnection();
			stmt = con.createStatement();
			res = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			freeConnection(res,stmt,con);
		}
		return res;
	}
	
	/**
	 * 使用Statement查询数据，返回List集合
	 * @param sql
	 * @return
	 */
	public static List<Map<String, Object>> queryForList(String sql) throws Exception {
		Statement stmt = null;
		ResultSet res = null;
		Connection con=null;
		List<Map<String, Object>> list = null;
		try {
			con=getConnection();
			stmt = con.createStatement();
			res = stmt.executeQuery(sql);
			Map<String, Object> map = null;
			ResultSetMetaData rsmd = res.getMetaData();
			int rowCnt = rsmd.getColumnCount();
			list = new LinkedList<Map<String, Object>>();
			while (res.next()) {
				map = new LinkedHashMap<String, Object>(rowCnt);
				for (int i = 1; i <= rowCnt; i++) {
					map.put(rsmd.getColumnName(i), res.getObject(i));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			freeConnection(res,stmt,con);
		}
		return list;
	}
	
	/**
	 * 使用Statement查询数据，返回BasicDBList
	 * @param sql
	 * @return
	 */
	public static BasicDBList queryForBasicDBList(String sql) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		Connection con=null;
		BasicDBList gomeTop = null;
		try {
			con=getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int rowCnt = rsmd.getColumnCount();
			gomeTop=new BasicDBList();
			BasicDBObject bo=null;
			while (rs.next()) {
				bo=new BasicDBObject();
				for (int i = 1; i <= rowCnt; i++) {
					bo.append(rsmd.getColumnName(i), (rs.getObject(i)==null?"":rs.getObject(i)).toString().trim());
				}
				gomeTop.add(bo);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			freeConnection(rs,stmt,con);
		}
		return gomeTop;
	}
	
	public static void queryAllCache(String sql) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		Connection con=null;
		try {
			con=getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			long m=0;
			ResultSetMetaData rsmd = rs.getMetaData();
			int rowCnt = rsmd.getColumnCount();
			allCacheKeys=null;
			allCacheKeys=new BasicDBList();
			while (rs.next()) {
				BasicDBObject bo=new BasicDBObject();
				for (int i = 1; i <= rowCnt; i++) {
					bo.append(rsmd.getColumnName(i), (rs.getObject(i)==null?"":rs.getObject(i)).toString().trim());
				}
				allCacheKeys.add(bo);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			freeConnection(rs,stmt,con);
		}
	}
	
	public static void queryHiveCache(){
		try {
			new CityInfoController().initRegion();
			allCacheKeys=null;noResultCacheKeys=null;keys_100=null;city_100=null;
			keys_100=HiveJdbcTools.queryForBasicDBList(keys_sql.toString());
			logger.info("关键词top100执行完毕---------");
			city_100=HiveJdbcTools.ConvertData(HiveJdbcTools.queryForBasicDBList(city_sql.toString()));
			logger.info("搜索城市top100执行完毕---------");
			noResultCacheKeys=HiveJdbcTools.queryForBasicDBList(nosql.toString());
			logger.info("无结果搜索执行完毕---------");
			HiveJdbcTools.queryAllCache(sql.toString());
			city_keys.clear();
			logger.info("搜索关键词定时业务逻辑完毕---------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static BasicDBList ConvertData(BasicDBList list) {
		BasicDBList returnList=new BasicDBList();
		long len=list.size();
		for(int i=0;i<len;i++){
			BasicDBObject code=(BasicDBObject)list.get(i);
			BasicDBObject qField=new BasicDBObject().append("code",code.getString("districtcode"));
			DBObject distinct=DataCollection.findOne(Const.defaultDictMongo, Const.defaultDictMongoDB, EYE_DEVISION, qField);
			if(distinct!=null){
				returnList.add(new BasicDBObject().append("code", code.getString("districtcode")).append("name", distinct.get("name").toString()).append("value",code.getString("value")));
			}
		}
		return returnList;
	}

	public static BasicDBList getCacheGomeTop(BasicDBList allCacheKeys,String gomeCatId,String queryType) {
		BasicDBList returnList=new BasicDBList();
		long len=allCacheKeys.size();
		for(int i=0;i<len;i++){
			BasicDBObject o=(BasicDBObject)allCacheKeys.get(i);
			if(StringUtils.equals(o.getString("catid"), gomeCatId)){
				returnList.add(o);
			}
			if(StringUtils.equals("TOP100", queryType)){
				if(returnList.size()==100){
					break;
				}
			}
		}
		return returnList;
	}
	
	public static void exportExcel(BasicDBList gomeTop,String catId,String catName){ 
		String excelName = "/workspace/soft/keywords/"+catName+".xls";
		WritableWorkbook wwb = ZExcel.createExcel(excelName);
		WritableSheet ws=wwb.createSheet("关键词" + 1, 1);;
		for (int j = 0; j < gomeTop.size(); j++) {	
			try {
				if(j<62001){
					
					BasicDBObject dbObject = (BasicDBObject) gomeTop.get(j);
					ws.addCell(new Label(0,0,"分类ID"));
					ws.addCell(new Label(1,0,"搜索关键词"));
					ws.addCell(new Label(2,0,"搜索次数"));
					Label cell = new Label(0, j+1, dbObject.getString("catid"));
					ws.addCell(cell);
					cell = new Label(1, j+1, dbObject.getString("question"));
					ws.addCell(cell);
					cell = new Label(2, j+1, dbObject.getString("cnt"));
					ws.addCell(cell);
				}
			} catch (RowsExceededException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
		
		try {
			wwb.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			wwb.close();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * tables:
	 * t_dragon
	 * tbl_action
	 * tbl_action_clear
	 * tbl_presearchlogs
	 * tbl_search
	 * @throws Exception
	 */
	//@Test
	public void testConn() throws Exception {
		
		Connection conn = getConnection();
		System.out.println("--xxxxxxxxxx--");
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			//String sql="select question,catid,cnt from (select question,catid,cnt,row_number() over (partition by catid order by cnt desc) as row_num from (select question,catid, count(1) as cnt from tbl_search where catid !='' group by question,catid) t ) t1 where row_num<=100";
			String sql="describe tbl_search";
			//String sql="show tables";
			rs=stmt.executeQuery(sql.toString());
			while (rs.next()) {
				System.out.println(rs.getString(1) + "\t" + rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			freeConnection(rs, stmt, conn);
		}
		
	}
	
	@Test 
	public void findActionData() throws Exception{
		/*String sql="select productid,productname,salesvolume,catid,question,sortname" +
				",order,facets ,totelcount,pagenumber,posttion,pretime,ip,cityname," +
				"cookieid,time,server,dt,hour from tbl_action  where productid='9125571541'";*/
		String sql="select * from tbl_action  where productid='9125571541' limit 10";
		BasicDBList data=queryForBasicDBList(sql);
		System.out.println(data);
	}
	 
}
