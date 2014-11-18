package org.x.server.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;

public class SalesAppraiseClickHandler {
	protected static Logger logger = LoggerFactory.getLogger(SalesAppraiseClickHandler.class);
	
	private static boolean isRunning = false;
	
	public static BasicDBList salesTopList = new BasicDBList();
	public static BasicDBList appraiseTopList = new BasicDBList();
	public static BasicDBList clickTopList = new BasicDBList();
	
	/**
	 * 更新销量和评论数TopList
	 */
	@SuppressWarnings("unused")
	private static void salesAppraiseCacheUpdate(){
		String salesTop = "select * from (select product_id as productId,sum(sales_quantity) as num from UAT_PRODX_6.gome_sales_drecord"
//								+ "where created_time > sysdate - 7"
								+ "group by product_id"
								+ "order by num desc) a where rownum <= 100";
		String appraiseTop = "select * from (select productid as productId,count(0) as num from UAT_PRODX_6.GOME_TBL_GOODS_APPRAISE"
//								+ "where create_date > sysdate - 7"
								+ "group by productid"
								+ "order by num desc) a where rownum <= 100";
		Connection conn = null;
		try {
			conn = null;//OracleConnect.getGomeDBConnection();
			Statement stat = conn.createStatement();

			ResultSet set = stat.executeQuery(salesTop);
			while (set.next()) {
				String productId = set.getString(1);
				Integer num = set.getInt(2);
			}
			logger.info("SalesTopCache updated complete!");
			set = stat.executeQuery(appraiseTop);
			while (set.next()) {
				String productId = set.getString(1);
				Integer num = set.getInt(2);
			}
			logger.info("AppraiseTopCache updated complete!");
		} catch (SQLException e) {
			logger.debug("salesAppraiseCacheUpdate Error! \n"+e.getMessage());
		}finally{
		}
	}
	
	/**
	 * 更新点击数TopList
	 */
	private static void clickCacheUpdate(){
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static String salesAppraiseClickCacheUpdate(){
		if(isRunning){
			logger.info("salesAppraiseClickCacheUpdate is running,please wait.");
			return "fail";
		}
		isRunning = true;
		try{
			salesAppraiseCacheUpdate();
			clickCacheUpdate();
		}finally{
			isRunning = false;
		}
		return "success";
	}
}
