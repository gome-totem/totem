package com.gome.totem.sniper.util;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.x.server.eye.Eye;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.HashMode;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ZookeeperUtils {

	private static final Log log = LogFactory.getLog(ZookeeperUtils.class);

	private static HashMap<String, BasicDBObject> routerMap = null;
	private static HashMap<String, BasicDBObject> appMap = null;
	
	/**
	 * 把router对象和app对象关联起来
	 * 
	 * @param routers
	 * @param appServers
	 * @return
	 */
	public static BasicDBList mergeRouters(BasicDBList routers,	BasicDBList appServers) {

		if (routers == null || routers.size() == 0) {
			return null;
		}
		for (int i = 0, len = routers.size(); i < len; i++) {
			BasicDBObject router = (BasicDBObject) routers.get(i);
			BasicDBObject rMsg = null;
			String routerId = router.getString("id", "");
			String routerIp = router.getString("ip", "");
			router.append("nickName", StringUtil.getNickName(Globalkey.PREFIX_ROUTER_CLASS, router));
			boolean runRouter = !StringUtils.isEmpty(routerId);
			if(runRouter){
				rMsg = (BasicDBObject) ZeroConnect.readServerRuntime(HashMode.router, routerIp);
				router.append("routerInfo", rMsg);
				router.append("state", "0");
			}
			if(runRouter && rMsg == null)	router.append("state", "2");
			if(!runRouter) router.append("state", "1");
			
			BasicDBList apps = new BasicDBList();
			for (int j = 0; appServers != null && j < appServers.size(); j++) {
				BasicDBObject app = (BasicDBObject) appServers.get(j);
				BasicDBObject aMsg = null;
				String status = app.getString("status", "0");
				String routerIdInApp = app.getString("routerId", "");
				boolean runApp = routerId.equals(routerIdInApp);
				if(runApp){
					aMsg = (BasicDBObject) ZeroConnect.readServerRuntime(HashMode.appserver, app.getString("ip"));
					app.append("appInfo", aMsg);
					status = "0";
				}
				if(runApp && aMsg == null)	status = "2";
				if(!runApp) status = "1";
				if (runApp || (!status.equals("0") && routerIp.equals(routerIdInApp))) {
					app.append("routerIp", routerIp);
					app.append("nickName", StringUtil.getNickName(Globalkey.PREFIX_APP_CLASS, app));
					app.append("state", status);
					apps.add(app);
				}
			}
			router.append("appservers", apps);
		}
		if (log.isInfoEnabled()) {
			log.info("Create router and app relation finish");
		}
		return routers;
	}


	/**
	 * 获取当前zookeeper的时时信息
	 * 
	 * @param servletContext
	 * @return
	 */
	public static void dealZooInfo(ServletContext servletContext) {
		BasicDBList dictRouters = (BasicDBList) servletContext.getAttribute(Globalkey.DICT_SERVER);

		branchDict(dictRouters);
		
		BasicDBList runningAppServers = Eye.sIntf.readApps();
		BasicDBList runningRouters = Eye.sIntf.readRouters();

		BasicDBList routers = createServers(runningRouters, routerMap);
		BasicDBList apps = createServers(runningAppServers, appMap);
		
		BasicDBList mergeRouters = ZookeeperUtils.mergeRouters(routers, apps);
		
		int appSize = runningAppServers != null ? runningAppServers.size() : 0;
		int routerSize = runningRouters != null ? runningRouters.size() : 0;

		BasicDBObject dbObject = new BasicDBObject();

		dbObject.append("appSize", appSize);
		dbObject.append("routerSize", routerSize);

		BasicDBObject server = new BasicDBObject(); // jstemplate.js控制模板移除空table设计的数据格式
		server.append("count", routerSize);
		server.append("routers", mergeRouters);

		dbObject.append("server", server);

		servletContext.setAttribute("zooTree", dbObject);
	}


	public static void branchDict(BasicDBList dict){
		routerMap = new HashMap<String, BasicDBObject>();
		appMap = new HashMap<String, BasicDBObject>();
		for (int i = 0; dict != null && i < dict.size(); i++) {
			BasicDBObject dictServer = (BasicDBObject) dict.get(i);
			String routerIp = dictServer.getString("routerId", "");
			String ip = dictServer.getString("ip", "");
			if(StringUtils.isEmpty(routerIp)){
				routerMap.put(ip, dictServer);
			}else{
				dictServer.append("routerId", routerIp);
				appMap.put(ip, dictServer);
			} 
		}
	}
	
	public static BasicDBList createServers(BasicDBList servers, HashMap<String, BasicDBObject> map) {
		if(map == null || map.isEmpty()) return servers;
		BasicDBList copys = (BasicDBList) servers.copy();
		for (int i = 0; i < servers.size(); i++) {
			BasicDBObject router = (BasicDBObject) servers.get(i);
			String ip = router.getString("ip", "");
			String routerIp = router.getString("routerIp", "");
			
			BasicDBObject dictObject = map.get(ip);
			if(dictObject==null) continue; //map不存在此ip
			if(StringUtils.isEmpty(routerIp)){ //router节点直接移除
				map.remove(ip);
				continue;
			}
			String dictRouterIp = dictObject.getString("routerId", "");
			if(routerIp.equalsIgnoreCase(dictRouterIp)){ //若此节点为app并且appIp,routerIp也一样，移除
				map.remove(ip);
			}
		}
		for(String ip : map.keySet()){
			copys.add(map.get(ip));
		}
		return copys;
	}

}