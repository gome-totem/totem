package org.x.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.eye.Eye;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;

import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
@Controller
@RequestMapping(value="/cloud/business")
public class SearchRulesController{

	protected static Logger logger = LoggerFactory.getLogger(SearchRulesController.class);
	private static final String collname = "rules";
	
	@RequestMapping(value="/searchRule")
	protected void searchRule(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
		String action = req.getParameter("action");
		String question = req.getParameter("question");
		String value = req.getParameter("value");		
		BasicDBObject result = new BasicDBObject();
		BasicDBObject logMsg = new BasicDBObject();
		logMsg.append("question", question);
		logMsg.append("from", "searchRule");
		logMsg.append("value", value);
		Eye.eyeLogger.printLog(logMsg);
		
		if(value == null){result.put("msg", "error");}
		if(question == null) {result.put("msg", "error");}
		if(action == null) {result.put("msg", "error");}
		
		if(value!=null && action!=null && question!=null){
			update(action, question, value, resp);
		}else{
			Write2PageUtil.write(resp, result);
		}
	}
	
	private void update(String action, String question, String value, HttpServletResponse resp) {
		BasicDBObject qFieldQuery = new BasicDBObject().append("question", question);
		DataResult dataResult = DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, collname , qFieldQuery, null);
		
		BasicDBList results = dataResult.getList();
		BasicDBObject oResult = null;
		
		BasicDBObject oField = new BasicDBObject().append("question", question).append(action, value);
		
		if(results!=null && results.size()>0){
			oResult = (BasicDBObject) results.get(0);
			oField = oResult.append("question", question).append(action, value);
			DataCollection.update(Const.defaultDictMongo, Const.defaultDictMongoDB, collname, qFieldQuery, oField);
		}else{
			DataCollection.insert(Const.defaultDictMongo, Const.defaultDictMongoDB, collname, oField);
		}
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "set").append("context", "设置了 ["+ action +"]值为["+ value +"]！"));
		Write2PageUtil.write(resp, new BasicDBObject().append("msg", "success"));
	}
}
