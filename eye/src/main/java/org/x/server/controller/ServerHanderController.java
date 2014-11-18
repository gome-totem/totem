package org.x.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.x.server.eye.Eye;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.HashMode;

import com.gome.totem.sniper.util.MessageType;
import com.gome.totem.sniper.util.StringUtil;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
@Controller
@RequestMapping(value="/cloud/eye")
public class ServerHanderController{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1261512362059329501L;
	private int act = 0;
	private String ip;
	private String routerId;
	
	@RequestMapping(value="/server/handle")
	protected void handle(HttpServletRequest req, HttpServletResponse resp){
		String action = req.getParameter("act");
		String server = req.getParameter("server");
		routerId = req.getParameter("routerIp");
		ip = req.getParameter("ip");
		
		if (action == null) {
			Write2PageUtil.writeJson(resp, false, "no act");
		}
		if (StringUtils.isEmpty(ip)) {
			Write2PageUtil.writeJson(resp, false, "no ip");
		}
		if(!StringUtils.isEmpty(routerId)){
			routerId = routerId.replace("_", " ").replace("appRouterId", "");
		}
		
		act = Integer.parseInt(action);
		ip = StringUtil.decodeIp(ip);
		try {
			this.handleEvent(resp, server);
		} catch (Exception e) {
			Write2PageUtil.writeJson(resp, false, "event error");
			e.printStackTrace();
		}
	}

	private void handleEvent(HttpServletResponse response, String server) {
		HashMode mode = HashMode.router;
		if(server.equalsIgnoreCase("app")){
			mode = HashMode.appserver;
		}
		Object object = null;
		switch (act) {
		case MessageType.READ:
			Eye.eyeLogger.printLog(new BasicDBObject().append("action", "read " + mode).append("context", "执行read " + mode  + ip + "操作！"));
			object = (BasicDBObject) ZeroConnect.readServerRuntime(mode, ip);
			break;
		case MessageType.ONLINE:
			Eye.eyeLogger.printLog(new BasicDBObject().append("action", "online " + mode).append("context", "执行online " + mode  + ip + "操作！"));
			object = (BasicDBObject) ZeroConnect.online(mode, ip);
			break;
		case MessageType.OFFLINE:
			Eye.eyeLogger.printLog(new BasicDBObject().append("action", "offline " + mode).append("context", "执行offline " + mode  + ip + "操作！"));
			object = (BasicDBObject) ZeroConnect.offline(mode, ip);
			break;
		case MessageType.UPDATE:
			Eye.eyeLogger.printLog(new BasicDBObject().append("action", "update " + mode).append("context", "执行update " + mode  + ip + "操作！"));
			object = (BasicDBObject) ZeroConnect.changeRouter(ip, routerId);
			break;
		default:
			Eye.eyeLogger.printLog(new BasicDBObject().append("action", "read " + mode).append("context", "执行read " + mode  + ip + "操作！"));
			object = (BasicDBList) ZeroConnect.readAppServers(ip);
			break;
		}
		Write2PageUtil.write(response, object);
	}
}
