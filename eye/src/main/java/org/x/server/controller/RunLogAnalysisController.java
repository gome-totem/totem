package org.x.server.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.common.SshTool;
import org.z.global.util.StringUtil;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.TimeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
/**
 * 日志错误计数
 * @author doujintong
 *
 */
@Controller
@RequestMapping(value="/cloud/eye")
public class RunLogAnalysisController{

	public static BasicDBList serverInfos = new BasicDBList();
	private static BasicDBObject logs = new BasicDBObject(); 
	
	public RunLogAnalysisController(){
		String [] servers = Globalkey.TotemServers.split(",");
		for(String server:servers){
			BasicDBObject sItem = new BasicDBObject();
			sItem.append("ip", server.trim());
			sItem.append("uname", Globalkey.RTUname);
			sItem.append("upwd", Globalkey.RTPassd);
			serverInfos.add(sItem);
		}
	}
	@RequestMapping(value="/runLogAnsi")
	protected void runLogAnsi(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String date = req.getParameter("idate");
		String keywords = req.getParameter("keywords");
		String app = req.getParameter("app");
		
		//查看有没有缓存，如果有则直接查出返回
		BasicDBObject cacheResults = (BasicDBObject)logs.get(date+"@"+keywords+"_"+app);
		if(cacheResults!=null){
			resp.getWriter().print(cacheResults);
			return;
		}
		
		String command = ""; 
		int tomcatNum = 2;
		if("test".equals(Globalkey.TotemEnv)){
			tomcatNum = 2;
		}else if("prd".equals(Globalkey.TotemEnv)){
			tomcatNum = 3;
		}else{
			return;
		}
		
		if(StringUtil.isEmpty(keywords)){
			keywords = "destory";
		}
		
		if(!StringUtil.isEmpty(app)){
			if("app".equals(app)){
				command = SshTool.countTagApp(date, keywords);
			}else if("tomcat".equals(app)){
				command = SshTool.countTagTomcat(date, tomcatNum, keywords);
			}else if("all".equals(app)){
				command = SshTool.countTagTomcatApp(date, tomcatNum, keywords);
			}
		}else{
			command = SshTool.countTagTomcatApp(date, tomcatNum, keywords);
		}
		BasicDBObject serverInfo = new BasicDBObject();
		long tomcatLogNum = 0L;
		long appLogNum = 0L;
		for(int i=0;i<serverInfos.size();i++){
			BasicDBObject item = (BasicDBObject)serverInfos.get(i);
			BasicDBList info = SshTool.execCmd(item.getString("uname", ""), item.getString("upwd", ""), item.getString("ip", ""), command, 22);
			serverInfo.append(item.getString("ip"), info);
			for (int j = 0; j < info.size(); j++) {
				if (j < tomcatNum) {
					if(StringUtils.isNumeric((String)info.get(j))){
						tomcatLogNum += Long.parseLong((String)info.get(j));
					}
				} else {
					if(StringUtils.isNumeric((String)info.get(j))){
						appLogNum += Long.parseLong((String)info.get(j));
					}
				}
			}
		}
		
		serverInfo.append("All", new BasicDBObject().append("tomcat", tomcatLogNum).append("app", appLogNum));
		
		if(!TimeUtil.getTodayFormat("yyyy-MM-dd").equals(date)){
			logs.append(date+"@"+keywords+"_"+app, serverInfo);
		}
		
		resp.getWriter().print(serverInfo);
		
	}
}
