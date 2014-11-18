package org.x.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.x.server.common.MongoDB;
import org.x.server.eye.Eye;
import org.x.server.service.ISearchManage;
import org.x.server.service.RMTShellHandler;
import org.z.global.connect.ZeroConnect;
import org.z.global.util.StringUtil;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * 
 * @author doujintong
 * 搜索工具模块
 */

@Controller
@RequestMapping(value="/cloud")
public class SearchManageController {

	private static Logger log = LoggerFactory.getLogger(SearchManageController.class);
	
	private static DB db = null;
	private static DBCollection coll = null;
	private RMTShellHandler exe = null;
	public static BasicDBList serverInfos = new BasicDBList();
	@Autowired
	private ISearchManage searchManage;
	
	static {
		db = MongoDB.getAuthMongoDB("10.58.50.99", 19753, "logs", false,"gome", "totem");
		coll = db.getCollection("voteInfo");
	}
	
	/**
	 * 多音字修正
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	@RequestMapping(value="/eye/dicSM")
	public void dicSM(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String facetId = req.getParameter("facetId");
		String facetName = req.getParameter("facetName");
		String shortpy = req.getParameter("short");
		String fullpy = req.getParameter("full");
		
		if(StringUtil.isEmpty(facetId)||
				StringUtil.isEmpty(facetName)||
				StringUtil.isEmpty(shortpy)||
				StringUtil.isEmpty(fullpy)||
				facetId.length()!=4){
			
			resp.getWriter().print(new BasicDBObject().append("msg", "valiErr"));
			return;
		}
		facetName = URLDecoder.decode(facetName, "utf-8");
		
		BasicDBObject param = new BasicDBObject();
		param.append("facetId", facetId);
		param.append("chinese", facetName);
		param.append("short", shortpy);
		param.append("long", fullpy);
		
		DBObject res = ZeroConnect.eyeBussinessSend("pinyin", param);
		
		BasicDBObject zresp = (BasicDBObject)res.get("response");
		log.info(res.toString());
		if(zresp!=null){
			
		}else{
			resp.getWriter().print(new BasicDBObject().append("msg", "error"));
			return;
		}
		System.out.println(param);
		resp.getWriter().print(new BasicDBObject().append("msg", "success"));
	}	
	/**
	 * EDM投票
	 * @param req
	 * @param resp
	 * @return
	 */
	@RequestMapping(value = "/business/edmServlet")
	public ModelAndView edmServlet(HttpServletRequest req,HttpServletResponse resp) {
		String voteStr = req.getParameter("vote");
		String userId = req.getParameter("uuid");
		String loginname = req.getParameter("loginname");
		String question = req.getParameter("question");
		String answer = req.getParameter("newcontext");
		if (answer != null) {

			BasicDBObject ob = new BasicDBObject();
			String id = req.getParameter("uid");
			String login = req.getParameter("login");
			String showvote = req.getParameter("showvote");
			String showquery = req.getParameter("showquery");
			if (id != null && login != null && showvote != null
					&& showquery != null && !id.isEmpty() && !login.isEmpty()
					&& !showvote.isEmpty() && !showquery.isEmpty()) {
				BasicDBObject obj = new BasicDBObject();
				ob.append("userId", id);
				ob.append("loginname", login);
				ob.append("question", showquery);
				ob.append("vote", Integer.parseInt(showvote));

				BasicDBObject result = (BasicDBObject) coll.findOne(ob);
				if (result != null) {
					obj.append("userId", id);
					obj.append("loginname", login);
					obj.append("vote", result.getInt("vote"));
					obj.append("question", result.getString("question"));
					obj.append("answer", answer);
					coll.update(ob, obj);
				}

				else {
					ob.append("answer", answer);
					coll.insert(ob);
				}
				resp.setContentType("text/xml;charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				return new ModelAndView("/business/votefinish.jsp");
			} else {
				BasicDBObject obj = new BasicDBObject();
				obj.append("answer", answer);
				coll.insert(obj);
				resp.setContentType("text/xml;charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				return new ModelAndView("/business/votefinish.jsp");
			}

		} else {

			if (userId != null && voteStr != null && loginname != null
					&& question != null) {
				int vote = Integer.parseInt(voteStr);
				BasicDBObject object = new BasicDBObject();

				object.append("userId", userId);
				object.append("vote", vote);
				object.append("loginname", loginname);
				object.append("question", question);
				BasicDBObject res = (BasicDBObject) coll.findOne(object);
				if (res == null || res.isEmpty()) {
					coll.insert(object);
				}
				BasicDBObject obj = null;
				BasicDBObject query = new BasicDBObject();
				query.put("vote", new BasicDBObject().append("$gte", 1));
				query.put("vote", new BasicDBObject().append("$lte", 5));

				long sum = coll.count(query);
				for (int i = 1; i <= 5; i++) {
					obj = new BasicDBObject();
					obj.append("vote", i);
					long count = coll.count(obj);
					double percent = (double) count * 100 / (double) sum;
					req.setAttribute("count" + i, count);
					req.setAttribute("percent" + i, (int) percent);
				}
				req.setAttribute("loginname", loginname);
				req.setAttribute("uuid", userId);
				req.setAttribute("vote", vote);
				req.setAttribute("question", question);

				resp.setContentType("text/xml;charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				return new ModelAndView("/business/searchvote.jsp");

			}else {
				BasicDBObject obj = null;
				BasicDBObject query = new BasicDBObject();
				query.put("vote", new BasicDBObject().append("$gte", 1));
				query.put("vote", new BasicDBObject().append("$lte", 5));
				long sum = coll.count(query);
				for (int i = 1; i <= 5; i++) {
					obj = new BasicDBObject();
					obj.append("vote", i);
					long count = coll.count(obj);
					double percent = (double) count * 100 / (double) sum;
					req.setAttribute("count" + i, count);
					req.setAttribute("percent" + i, (int) percent);

				}
				resp.setContentType("text/xml;charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				return new ModelAndView("/business/searchvote.jsp");
			}
			
		}
	}

	/**
	 * tomcat远程
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/business/ssh-tom")
	public void sshTomcat(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String host = req.getParameter("host");
		String port = req.getParameter("port");
		if (host == null)
			return;
		exe = new RMTShellHandler(host, Globalkey.RTUname, Globalkey.RTPassd);

		BasicDBObject result = new BasicDBObject();
		try {
			if (port.equals("0")) {
				result.append("shutdown",
						exe.exec("sh /usr/local/tomcat/bin/shutdown.sh"));
				result.append("startup",
						exe.exec("sh /usr/local/tomcat/bin/startup.sh"));
			} else {
				result.append(
						"shutdown",
						exe.exec("sh /workspace/tomcat" + port
								+ "/bin/shutdown.sh"));
				result.append(
						"startup",
						exe.exec("sh /workspace/tomcat" + port
								+ "/bin/startup.sh"));
			}
		} catch (Exception e) {
			log.error("Shell run error!" + e);
		}

		log.info("{} tomcat[{}]====>[{}]", new Object[] { host, port, result });
		BasicDBObject logMsg = new BasicDBObject();
		logMsg.append("host", host);
		logMsg.append("port", port);
		logMsg.append("from", "sshTomcat");
		Eye.eyeLogger.printLog(logMsg);
		Eye.eyeLogger.printLog(new BasicDBObject().append("action", "restart")
				.append("context", "重启了 [" + host + "tomcat" + port + "]！"));

		PrintWriter out = resp.getWriter();
		out.write(result.toString());
		out.flush();
		out.close();
	}
	
	

/**
 * beanTalk监控
 * @param req
 * @param resp
 * @throws ServletException
 * @throws IOException
 */

	@RequestMapping(value="/eye/totemSM")
	public void totemSM(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String module = req.getParameter("module");
		if(!StringUtils.isEmpty(module)){
			if(module.equals("beansTalkMonitor")){
				searchManage.beansTalkMonitor(req, resp);
			}else{
				resp.getWriter().print("error");
			}
		}else{
			resp.getWriter().print("error");
		}
	}

	
	
	@RequestMapping(value="/eye/msg-send")
	public void msgSend(HttpServletRequest req, HttpServletResponse resp){
		String mehtod=req.getMethod();
		if(mehtod.equalsIgnoreCase("post")){
			Write2PageUtil.write(resp, new BasicDBObject().append("status", Globalkey.MassageSend));
			return;
		}
		String massage = req.getParameter("msg");
		boolean before = Globalkey.MassageSend;
		BasicDBObject result = new BasicDBObject().append("msg", "发送失败");
		
		String isTotemMonitorMsgSend = req.getParameter("msgSend");
		if(!StringUtils.isEmpty(isTotemMonitorMsgSend)){
			if("true".equals(isTotemMonitorMsgSend)){			
				Globalkey.MassageSend=true;
				result.append("msg", before + "——>" + Globalkey.MassageSend + "  Totem监控短信功能已打开！");
			}else if("false".equals(isTotemMonitorMsgSend)){
				Globalkey.MassageSend=false;
				result.append("msg", before + "——>" + Globalkey.MassageSend + "  Totem监控短信功能关闭！");
			}
			Write2PageUtil.write(resp, result);
			return;
		}
		
		if(!StringUtils.isEmpty(massage)){
			searchManage.msgSend(massage);
			result.append("msg", "发送成功！");
		}
		Write2PageUtil.write(resp, result);
	}

	
	/**
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	@RequestMapping(value="/eye/indexSM")
	public void indexSM(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String module = req.getParameter("module");
		if(!StringUtils.isEmpty(module)){
			if(module.equals("docCount")){
				searchManage.appsDoc(req, resp);
			}else if(module.equals("promoFull")){
				searchManage.promoProductFull(req, resp);
			}else if(module.equals("promoFullWeight")){
				searchManage.promoProductFullWeight(req, resp);
			}else if(module.equals("promoSingle")){
				searchManage.promoSingle(req, resp);
			}else{
				resp.getWriter().print("error");
			}
		}else{
			resp.getWriter().print("error");
		}
	}
}
