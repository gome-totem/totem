package org.x.server.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.x.server.eye.Eye;
import org.z.global.dict.Global;
import org.z.global.environment.Const;
import org.z.global.epub.RmiRositoryImpl;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
@RequestMapping(value="/cloud/business")
public class IncrementController{

	private static RmiRositoryImpl rmiRositoryImpl;
	private static final String COLLECTION = "businessIncrementIndex";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public IncrementController(){
		rmiRositoryImpl = new RmiRositoryImpl();
		rmiRositoryImpl.setCurrentEnv(Global.currentEnv);
	}

	@RequestMapping(value="/incrementindex",method=RequestMethod.GET)
	protected void incrementindexGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String ip = request.getRemoteHost();
		int limit = 10;
		if(StringUtils.isNotBlank(request.getParameter("rows"))){
			limit=Integer.parseInt(request.getParameter("rows"));
		}
		int page = Integer.parseInt(request.getParameter("page"));
		int offset = (page - 1) * limit;
		BasicDBObject qField = new BasicDBObject("ip", ip);
		BasicDBObject sortField = new BasicDBObject();
		sortField.put("addTime", -1);
		sortField.put("state", 1);
		DataResult dataResult = DataCollection.find(Const.defaultLogServer, Const.defaultLogDB, COLLECTION, qField, sortField, offset, limit);
		ServletOutputStream sos = response.getOutputStream();
		BasicDBObject dbObject = new BasicDBObject();
		int totalCount = dataResult.getTotalCount();
		dbObject.put("total", totalCount);
		int totalPage = 0;
		totalPage = totalCount / limit;
		if (totalCount % limit != 0) {
			totalPage++;
		}
		dbObject.put("totalPage", totalPage);
		dbObject.put("rows", dataResult.getList());
		String json = dbObject.toString();
		sos.write(json.getBytes());
		sos.flush();
	}

	@RequestMapping(value="/incrementindex",method=RequestMethod.POST)
	protected void incrementindexPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream sos = response.getOutputStream();
		String method = request.getParameter("method");
		BasicDBObject result = new BasicDBObject();
		String ip = request.getRemoteHost();
		if (method.equalsIgnoreCase("addIncrementIndex")) {
			String type = request.getParameter("type");
			String id = request.getParameter("id");
			if (id.contains("，")) {
				id.replace("，", ",");
			}
			String[] ids = id.split(",");
			if (ids == null || ids.length == 0) {
				result.put("msg", "id不能够为空");
			} else {
				for (String str : ids) {
					BasicDBObject dbObject = new BasicDBObject();
					dbObject.put("ip", ip);
					dbObject.put("type", type);
					dbObject.put("id", str.trim().replaceAll("\n", "").replaceAll("\t", "").replaceAll("\r", ""));
					dbObject.put("addTime", DATE_FORMAT.format(new Date()));
					dbObject.put("status", 0);
					DataCollection.insert(Const.defaultLogServer, Const.defaultLogDB, COLLECTION, dbObject);
				}
				Eye.eyeLogger.printLog(new BasicDBObject().append("action", "increment").append("context", "对[" + type + "]为[" + id + "],添加增量成功！"));
				result.put("msg", "添加增量成功");
			}
		} else if (method.equalsIgnoreCase("executeIncrementIndex")) {
			BasicDBObject qField = new BasicDBObject();
			qField.put("ip", ip);
			qField.put("status", 0);
			List<DBObject> dbObjects = null;//DataCollection.findAll(Const.defaultLogServer, Const.defaultLogDB, COLLECTION, qField, null);
			if (dbObjects != null && dbObjects.size() > 0) {
				for (DBObject dbObject : dbObjects) {
					List<String> list = new ArrayList<String>();
					list.add(dbObject.get("type").toString());
					list.add(dbObject.get("id").toString());
					try {
						rmiRositoryImpl.invokeFiled("increment", list);
//						rmiRositoryImpl.invokeMethod("forceIncrement");
					} catch (Exception e) {
						result.put("msg", "执行增量失败，" + e.getMessage());
						Eye.eyeLogger.printLog(new BasicDBObject().append("action", "increment").append("context",
								"对[" + dbObject.get("type").toString() + "]为[" + dbObject.get("id").toString() + "]，执行增量失败！"));
					}
				}
			}
			BasicDBObject oField = new BasicDBObject();
			oField.put("ip", ip);
			oField.put("status", 1);
			for (DBObject dbObject : dbObjects) {
				dbObject.put("status", 1);
				dbObject.put("executeTime", DATE_FORMAT.format(new Date()));
				DataCollection.update(Const.defaultLogServer, Const.defaultLogDB, COLLECTION, qField, dbObject);
			}
			Eye.eyeLogger.printLog(new BasicDBObject().append("action", "increment").append("context", "对["+dbObjects.toString()+"]执行增量成功！"));
			result.put("msg", "执行增量成功");
			result.put("status", 1);
		} else {
			result.put("msg", "没有需要执行增量");
			result.put("status", 0);
		}
		sos.write(result.toString().getBytes());
		sos.flush();
	}

	public static void main(String[] args) {
		DataCollection.dropCollection(Const.defaultLogServer, Const.defaultLogDB, COLLECTION);
	}
}
