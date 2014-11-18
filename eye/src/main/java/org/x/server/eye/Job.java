package org.x.server.eye;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.common.EyeCache;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.CompressMode;
import org.z.global.dict.Device;
import org.z.global.dict.Global.HashMode;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServiceName;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.TimeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Job {

	protected static Logger logger = LoggerFactory.getLogger(Job.class);

	public static class RouterTimerTask extends TimerTask {

		private Eye eye = null;
		private boolean isRunning = false;

		public RouterTimerTask(Eye eye) {
			this.eye = eye;
		}

		@Override
		public void run() {
			if (isRunning == true)
				return;
			isRunning = true;
			try {
				BasicDBList routers = ServerDict.self.routers();
				final RouterTimerTask self = this;
				for (int i = 0; i < routers.size(); i++) {
					final BasicDBObject oRouter = (BasicDBObject) routers.get(i);
					BasicDBObject oMsg = (BasicDBObject) ZeroConnect.readServerRuntime(HashMode.router, oRouter.getString("ip"));
					if (oMsg == null)
						continue;
					oMsg.append("ip", oRouter.getString("ip"));
					oMsg.append("id", oRouter.get("id"));
					oMsg.append("serverType", "router");
					oMsg.append("msgType", "runtime");
					self.eye.queue.add(oMsg);
				}
			} finally {
				isRunning = false;
			}
		}
	}

	public static class AppTimerTask extends TimerTask {
		private Eye eye = null;
		private boolean isRunning = false;

		public AppTimerTask(Eye eye) {
			this.eye = eye;
		}

		@Override
		public void run() {
			if (isRunning == true)
				return;
			isRunning = true;
			try {
				BasicDBList apps = ServerDict.self.apps();
				final AppTimerTask self = this;
				for (int i = 0; i < apps.size(); i++) {
					final BasicDBObject oApp = (BasicDBObject) apps.get(i);
					BasicDBObject oMsg = (BasicDBObject) ZeroConnect.readServerRuntime(HashMode.appserver, oApp.getString("ip"));
					if (oMsg == null)
						continue;
					oMsg.append("ip", oApp.getString("ip",""));
					oMsg.append("id", oApp.get("id"));
					oMsg.append("serverType", "appserver");
					oMsg.append("msgType", "runtime");
					self.eye.queue.add(oMsg);
				}
			} finally {
				isRunning = false;
			}
		}
	}
	public static class MobileSendTask extends TimerTask{
		private boolean isRunning = false;
		
		@Override
		public void run() {
			logger.info("app_router mobile msg job run start!" + isRunning);
			if (isRunning == true)
				return;
			Eye.errorRouters.clear();
			Eye.errorApps.clear();
			isRunning = true;
			try {
				BasicDBList routers = ServerDict.self.routers();
				BasicDBList apps = ServerDict.self.apps();
				Eye.errorRouters.putAll(Eye.confRouters);
				Eye.errorApps.putAll(Eye.confApps);
				for (int i = 0; routers!=null && i < routers.size(); i++) {
					BasicDBObject router = (BasicDBObject) routers.get(i);
					String ip = router.getString("ip","");
					if(Eye.errorRouters.contains(ip)){
						Eye.errorRouters.remove(ip);
					}
				}
				for (int i = 0; apps!=null && i < apps.size(); i++) {
					BasicDBObject app = (BasicDBObject)apps.get(i);
					String ip = app.getString("ip","");
					if(Eye.errorApps.contains(ip)){
						Eye.errorApps.remove(ip);
					}
				}
				
				if(Globalkey.MassageSend && ((Eye.errorRouters != null && Eye.errorRouters.size() > 0) || (Eye.errorApps != null && Eye.errorApps.size() > 0))){
					sendMsg();
				}
			} finally {
				isRunning = false;
			}
			logger.info("app_router mobile msg job run end");
		}
		
		private void sendMsg(){
			BasicDBObject msg = new BasicDBObject();
			BasicDBObject content = new BasicDBObject() ;
			String routerMsg = "", appMsg = "";
			
			if(Eye.errorRouters.size()>0)
				routerMsg = "以下是掉线的router： " + Eye.errorRouters.toString();
			if(Eye.errorApps.size()>0)
				appMsg = "以下是掉线的app： " + Eye.errorApps.toString();
			
			routerMsg.replace("[", "【").replace("]", "】");
			appMsg.replace("[", "【").replace("]", "】");
			
			content.append("id", System.currentTimeMillis()) ;
			BasicDBList info = new BasicDBList();
			for(String name : Eye.engineers.keySet()){
				info.add(new BasicDBObject().append("phoneNumber", Eye.engineers.get(name).longValue())
						.append("context", "hi, " + name + TimeUtil.sayHelloByHour() + " " + routerMsg + " " + appMsg + " 云眼小助手提醒您，请快快处理哦！")) ;
			}
			for (String name : Eye.etxs.keySet()) {
				info.add(new BasicDBObject().append("phoneNumber", Eye.etxs.get(name).longValue())
						.append("context", "hi, " + name + TimeUtil.sayHelloByHour() + " " + routerMsg + " " + appMsg + " 云眼小助手提醒您，请快快处理哦！")) ;
			}
			
			content.append("info", info) ;
			msg.append("content", content);
			
			Eye.MMMIntf.sendTextMessage(msg);
		} 
		
	}
	
	public static class WebSocketTask extends TimerTask {
		private boolean isRunning = false;
		@Override
		public void run() {
			if (isRunning == true)
				return;
			isRunning = true;
			try {
				for (Iterator<Entry<String, WebSocketConnect>> i = Eye.connects.entrySet().iterator(); i.hasNext();) {
					Entry<String, WebSocketConnect> entry = i.next();
					WebSocketConnect connect = entry.getValue();
					connect.checkExpire();
				}
			} finally {
				isRunning = false;
			}
		}
	}

	public static class SearchMapTask extends TimerTask {
		private boolean isRunning = false;
		@Override
		public void run() {
			if (isRunning == true)
				return;
			isRunning = true;
			try {
				//SearchMapHandler.City_Top_100();
				//SearchMapHandler.Question_Top_100();
				//SearchMapHandler.CatId_Top_100();
				//SearchMapHandler.All();
				//SearchMapHandler.copyTable();
			} finally {
				isRunning = false;
			}
		}
	}
	
	public static class IndexDocTask extends TimerTask {
		private boolean isRunning = false;
		@Override
		public void run() {
			if (isRunning == true)
				return;
			isRunning = true;
			try {
		    	BasicDBList apps = ServerDict.self.apps();
				BasicDBObject oReq = new BasicDBObject();
				oReq.append("docCount", true);
				BasicDBList newAppsDocNum = new BasicDBList();
		    	for(int i = 0;i < apps.size();i++){
		    		BasicDBObject itemApp = (BasicDBObject)apps.get(i);
		    		String ip = itemApp.getString("ip");
		    		int port = itemApp.getInt("port");
		    		DBObject res = ZeroConnect.sendBy(ip, port, Device.SERVER, ServiceName.ProductIndex, MessageScope.APP, 
		    				             MessageType.SEARCH, MessageVersion.MQ, "search", CompressMode.NONE ,oReq.toString().getBytes(), oReq);
		    		
		    		if(res!=null){
		    			BasicDBObject response = (BasicDBObject)res.get("response");
		    			if(response!=null){
		    				BasicDBObject results = (BasicDBObject)response.get("results");
		    				if(results!=null){
		    					BasicDBObject appDocCount = new BasicDBObject();
		    					appDocCount.append("ip", ip);
		    					appDocCount.append("docCount", results.getInt("docCount", -1));
		    					newAppsDocNum.add(appDocCount);
		    				}
		    			}
		    		}
		    	}
		    	EyeCache.appsDoc = newAppsDocNum;
//		    	logger.info(EyeCache.appsDoc.toString());
			} finally {
				isRunning = false;
			}
		}
	}
	
	public static class SalesAppraiseClickTopTask extends TimerTask{
		private boolean isRunning = false;
		@Override
		public void run() {
			if (isRunning == true)
				return;
			isRunning = true;
			try {
				
			}finally{
				isRunning = false;
			}
		}
		
	}
}
