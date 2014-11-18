package org.x.server.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.common.MongoDB;
import org.x.server.tools.MysqlConnectTools;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;


@Controller
@RequestMapping(value = "/eye/data")
public class EdmPlatformController {

	/**
	 * 
	 */
	private static Log log = LogFactory.getLog(EdmPlatformController.class);
	private static DB db = MongoDB.getAuthMongoDB("10.58.50.26", 19753, "edm",
			true, "gome", "totem");
	private static Connection mysqlconnect = null;


	@RequestMapping(value = "/edmPlat")
	protected void edmPlat(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String flag = req.getParameter("flag");
		if (flag == null || flag.isEmpty())
			return;
		int iflag = Integer.parseInt(flag.substring(0, 1));
		switch (iflag) {
		case 1:
			processInsertData(req, resp);// 處理頁面第一個行爲邏輯
			break;
		case 2:
			processSubmmitData(req, resp);// 處理頁面第二個行爲邏輯
			break;
		case 3:
			returnData(req, resp); // 返回第二個行爲中的下拉框數據
			break;
		case 4:
			returnInfoListData(req, resp); // 返回信息列表中的數據
			break;
		case 5:
			deleteData(req, resp,"triggertypedict");// 删除列表中的数据
			break;
		case 6:
			deleteSelectData(req, resp); //删除下拉框中的选项
			break;
		case 7:
			searchTemplateDatas(req, resp); //返回邮件模板数据，重组返回
			break;
		case 8:
			searchConditionDatas(req, resp); //返回触发条件下拉框的数据
			break;
		default:
			log.info("no case can be satisfied");
			break;
		}
	}
	


	private void searchTemplateDatas(HttpServletRequest req,
			HttpServletResponse resp) {
		String key = req.getParameter("key");
		if(key==null)
			return;
		DBCollection coll = db.getCollection("mailtemplate");
		BasicDBObject o = (BasicDBObject)coll.findOne(new BasicDBObject().append("mailingid", key),new BasicDBObject().append("_id", 0).append("mailingid", 0));
		if(o==null)
			return;
		Write2PageUtil.write(resp, o);
	}
	
	private void searchConditionDatas(HttpServletRequest req,
			HttpServletResponse resp) {
		mysqlconnect = MysqlConnectTools.getMysqlDbConnection("10.58.50.26", "root", "mysql", "edm", "edmpool");
		String key = req.getParameter("key");
		if(key==null)
			return;
		
		String query = "select * from combine_dict where actionname="+"'"+key+"'";
		Statement state = null;
		ResultSet rs  = null;
		try {
			state = mysqlconnect.createStatement();
			 rs = state.executeQuery(query);
			 BasicDBObject info= null;
			 if(rs == null)
				 return;
			 while(rs.next()){
				 info = new BasicDBObject();
				 info.append("actionname", rs.getString("actionname"));
				 info.append("catid", rs.getString("catId"));
				 info.append("clickdowntimes", rs.getInt("clickdowntimes"));
				 info.append("clickuptimes", rs.getInt("clickuptimes"));
				 info.append("totalclicktimes", rs.getInt("totalclicktimes"));
				 info.append("time", rs.getInt("time"));
			 }
				Write2PageUtil.write(resp, info);

		} catch (SQLException e) {
			log.error("query data is error from mysql!====>"+e);
		}
		finally{
			try {
				state.close();
				rs.close();
			} catch (SQLException e) {
				log.error("close statement error!====>"+e);
			}
		}
		Write2PageUtil.write(resp, null);
	}



	private void deleteSelectData(HttpServletRequest req,
			HttpServletResponse resp) {
		String flag = req.getParameter("flag");
		String[] delId = req.getParameterValues("delId");
		int iflag = Integer.parseInt(flag);
		switch(iflag){
		case 61:
			DBCollection coll = db.getCollection("mailtemplate");
			WriteResult r = null;
			for(int i=0;i<delId.length;i++){
				r = coll.remove(new BasicDBObject().append("mailingid", delId[i]));
			}
			if(r == null){
				Write2PageUtil.write(resp, new BasicDBObject("result","error"));
			}
			Write2PageUtil.write(resp, new BasicDBObject("result","success"));
			break;
		case 62:
			String key = req.getParameter("delId");
			String query = "delete from combine_dict where actionname="+"'"+key+"'";
			Statement state = null;
			mysqlconnect = MysqlConnectTools.getMysqlDbConnection("10.58.50.26", "root", "mysql", "edm", "edmpool");
			try {
				state = mysqlconnect.createStatement();
				 int rs = state.executeUpdate(query);
				 if(rs > 0 ){
						Write2PageUtil.write(resp, new BasicDBObject("result","success"));
				 }
 
			} catch (SQLException e) {
				Write2PageUtil.write(resp, new BasicDBObject("result","error"));
				log.error("delete data is error from mysql!====>"+e);
			}
			finally{
				try {
					state.close();
				} catch (SQLException e) {
					log.error("close statement error!====>"+e);
				}
			}
			
			break;
		}	
	}



	public void deleteData(HttpServletRequest req, HttpServletResponse resp,String dbname) {
		String id=req.getParameter("delId");
		DBCollection coll = db.getCollection(dbname);
		WriteResult r = coll.remove(new BasicDBObject("_id",new ObjectId(id)));
		if(r == null){
			Write2PageUtil.write(resp, new BasicDBObject("result","error"));
		}
		Write2PageUtil.write(resp, new BasicDBObject("result","success"));
	}

	public void returnInfoListData(HttpServletRequest req,
			HttpServletResponse resp) {
		DBCollection coll = db.getCollection("triggertypedict");
		DBCursor aresult = coll.find();
		if (aresult != null) {
			Write2PageUtil.write(resp, aresult.toArray());
		}
		else{
			Write2PageUtil.write(resp, new BasicDBObject("result","error"));
		}

	}

	public void returnData(HttpServletRequest req, HttpServletResponse resp) {
		String select = req.getParameter("select");
		// 返回郵件模板數據
		if (select.equals("1")) {
			DBCollection coll = db.getCollection("mailtemplate");
			BasicDBObject keys = new BasicDBObject();
			keys.append("mailingid", 1).append("templatename", 1)
					.append("_id", 0);
			DBCursor aresult = coll.find(new BasicDBObject(), keys);
			if (aresult != null) {
				Write2PageUtil.write(resp, aresult.toArray());
				return;
			}
		}
		// 返回觸發行爲數據
		if (select.equals("2")) {
			String query = "select * from combine_dict";
			Statement state = null;
			ResultSet rs  = null;
			mysqlconnect = MysqlConnectTools.getMysqlDbConnection("10.58.50.26", "root", "mysql", "edm", "edmpool");

			ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
			try {
				state = mysqlconnect.createStatement();
				 rs = state.executeQuery(query);
				 while(rs.next()){
					BasicDBObject info = new BasicDBObject();
					 info.append("actionname", rs.getString("actionname"));
					 info.append("catid", rs.getString("catId"));
					 info.append("clickdowntimes", rs.getInt("clickdowntimes"));
					 info.append("clickuptimes", rs.getInt("clickuptimes"));
					 info.append("totalclicktimes", rs.getInt("totalclicktimes"));
					 info.append("time", rs.getInt("time"));
					 list.add(info);
				 }
					Write2PageUtil.write(resp, list);
 
			} catch (SQLException e) {
				log.error("query data is error from mysql!====>"+e);
			}
			finally{
				try {
					state.close();
					rs.close();
				} catch (SQLException e) {
					log.error("close statement error!====>"+e);
				}
			}
			
		}
	}

	public void processInsertData(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String flag = req.getParameter("flag");
		int iflag = Integer.parseInt(flag);
		switch (iflag) {
		case 11:
			processInsertData(req, resp, "cid", "mailingid", "templatename",
					"mailtemplate");
			break;
		case 12:
			processCombineCondition(req,resp);
			break;
		case 13:
			processInsertData(req, resp, "ldmailingid", "ldsendplanid",
					"ldtemplatename", "mailtemplate");
			break;
		}

	}

	private void processCombineCondition(HttpServletRequest req,
			HttpServletResponse resp) throws UnsupportedEncodingException {
		String comname = new String(req.getParameter("comname").getBytes("iso8859-1"),"utf-8");
		String totalclicktimes = req.getParameter("totalclicktimes");
		String clickuptimes = req.getParameter("clickuptimes");
		String clickdowntimes = req.getParameter("clickdowntimes");
		String time = req.getParameter("time");
		String catid = req.getParameter("catid");
		
		if(comname == null || totalclicktimes == null || clickuptimes== null || clickdowntimes == null ||
				time == null || catid == null)
			return;
		 String query = " insert into combine_dict (actionname, time, totalclicktimes, clickuptimes, clickdowntimes,catId)"
			        + " values (?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = null;
		 try {
				mysqlconnect = MysqlConnectTools.getMysqlDbConnection("10.58.50.26", "root", "mysql", "edm", "edmpool");
			ps =  mysqlconnect.prepareStatement(query);
			ps.setString(1, comname);
			ps.setInt(2, Integer.parseInt(time));
			ps.setInt(3, Integer.parseInt(totalclicktimes));
			ps.setInt(4, Integer.parseInt(clickuptimes));
			ps.setInt(5, Integer.parseInt(clickdowntimes));
			ps.setString(6, catid);
			
			int result = ps.executeUpdate();
			if(result > 0){
				Write2PageUtil.write(resp,new BasicDBObject().append("result", "success"));
			}
				
		} catch (SQLException e) {
			Write2PageUtil.write(resp,new BasicDBObject().append("result", "error"));
			log.error("insert to mysql is error"+e);
		}
		 finally{
				try {
					ps.close();
					mysqlconnect.close();
				} catch (SQLException e) {
					log.error("close preparedstatement is error"+e);
				}	
		 }

	}

	private void processInsertData(HttpServletRequest req,
			HttpServletResponse resp, String s1, String s2, String s3,
			String collName) throws UnsupportedEncodingException {
		String cid = req.getParameter(s1);
		String mailingid = req.getParameter(s2);
		String temName = new String(req.getParameter(s3).getBytes("iso8859-1"),"utf-8");

		if (cid != null && mailingid != null && temName != null) {
			DBCollection coll = db.getCollection(collName);
			BasicDBObject info = new BasicDBObject().append("mailingid",
					cid + "@" + mailingid).append("templatename", temName);
			BasicDBObject queryResult = (BasicDBObject) coll.findOne(info);
			
			BasicDBObject result = null;
			if (queryResult != null) {
				Write2PageUtil.write(resp,new BasicDBObject().append("result", "error"));
				return;
			}
			DBObject nameresult = coll.findOne(new BasicDBObject().append("templatename", temName));
			if(nameresult !=null){
				Write2PageUtil.write(resp,new BasicDBObject().append("result", "error"));
				return;
			}
			WriteResult r = coll.insert(info);
			if (r != null) {
				result = new BasicDBObject().append("result", "success");
			}
			log.info("insertdata success" + info.toString() + result.toString());
			Write2PageUtil.write(resp, result);
		}
	}

	public static String AGE(String s) {
		if (s.equals("0"))
			return "1-20";
		if (s.equals("1"))
			return "20-40";
		if (s.equals("2"))
			return "40-60";
		if (s.equals("3"))
			return "60以上";
		if (s.equals("4"))
			return "全天";
		return null;

	}

	public static String TIMERANGE(String s) {
		if (s.equals("0"))
			return "0-6";
		if (s.equals("1"))
			return "6-12";
		if (s.equals("2"))
			return "12-18";
		if (s.equals("3"))
			return "18-24";
		if (s.equals("4"))
			return "全部";
		return null;

	}

	public static String GENDER(String s) {
		if (s.equals("0"))
			return "男";
		if (s.equals("1"))
			return "女";
		if (s.equals("2"))
			return "全部";
		return null;

	}

	public void processSubmmitData(HttpServletRequest req,
			HttpServletResponse resp) {
		String systempara = req.getParameter("systemPara");
		String mailtemplate = req.getParameter("mailtemplate");
		String paramrange = req.getParameter("paramrange");
		String gender = req.getParameter("gender");
		String[] sendcondition = req.getParameterValues("sendcondition");
		String[] timerange = req.getParameterValues("timerange");
		String[] age = req.getParameterValues("age");
		DBCollection coll = db.getCollection("triggertypedict");

		for (int i = 0; i < sendcondition.length; i++) {
			BasicDBObject info = new BasicDBObject();
			info.put("systemPara", systempara);
			info.put("mailtemplate", mailtemplate);
			info.put("paramrange", paramrange);
			info.put("gender", gender);
			String send = sendcondition[i];
			info.put("sendcondition", send);
			for (int j = 0; j < age.length; j++) {
				BasicDBObject sObj = new BasicDBObject(info);
				String a = age[j];
				sObj.put("age", a);
				for (int k = 0; k < timerange.length; k++) {
					BasicDBObject tObj = new BasicDBObject(sObj);
					tObj.put("timerange", timerange[k]);
					DBObject queryResult = coll.findOne(tObj);
					if (queryResult != null) {
						Write2PageUtil.write(resp,new BasicDBObject().append("result", "error"));
						continue;
					}
					coll.insert(tObj);
				}
			}
		}
		Write2PageUtil.write(resp,new BasicDBObject().append("result", "success"));
	}

}
