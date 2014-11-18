package org.x.server.eye;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.x.server.eye.Job.MobileSendTask;
import org.x.server.service.LogImpl;
import org.x.server.service.LogIntf;
import org.x.server.service.MobileIntf;
import org.x.server.service.SearchImpl;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeAction;
import org.z.global.zk.ServerDict.NodeType;
import org.z.global.zk.ZooKeeperWatchIntf;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.ZookeeperUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class Eye implements OnChange, ZooKeeperWatchIntf {
	public static HashSet<WebSocketSession> sessions = new HashSet<WebSocketSession>();
	public static ConcurrentHashMap<String, WebSocketConnect> connects = new ConcurrentHashMap<String, WebSocketConnect>();
	public static HashMap<String,Long> engineers = new HashMap<String,Long>();
	public static HashMap<String,Long> etxs = new HashMap<String,Long>();
	public static HashMap<String,Long> tmps = new HashMap<String,Long>();
	public static Set<String> jdCategory = new HashSet<String>();
	public static BasicDBList errorRouters = new BasicDBList();
	public static BasicDBList errorApps = new BasicDBList();
	public static BasicDBList confRouters = new BasicDBList();
	public static BasicDBList confApps = new BasicDBList();
	public static LogIntf eyeLogger = new LogImpl();
	public EventQueue queue = null;
	public Timer appTimer = null;
	public Timer routerTimer = null;
	public Timer webSocketTimer = null;
	public Timer mobileSendTask = null;
	public Timer searchMapTimer = null;
	public Timer indexDocTimer = null;
	public Timer salesAppraiseClickTopTimer = null;
	public Timer  esMonitorTimer = null;
	
	
	public static Eye instance = null;
	public static MobileIntf MMMIntf = null;
	public static SearchImpl sIntf = new SearchImpl();;
	private static Log log = LogFactory.getLog(Eye.class);
	public static ServletContext servletContext = null;
	public static String eyeHost = null;

	public static void init(ServletContext context) {
		log.info("eye init...");
		instance = new Eye();
		instance.start();
		servletContext = context;
		MMMIntf = new MobileHandler();
		eyeHost = Globalkey.EYEHOST;
	}

	public void start() {
		ServerDict.self.routerListener.addWather(this);
		ServerDict.self.appListener.addWather(this);
		this.queue = new EventQueue(this);
		this.queue.start();
		appTimer = new Timer();
		routerTimer = new Timer();
		webSocketTimer = new Timer();
		mobileSendTask = new Timer();
		//searchMapTimer = new Timer();
		esMonitorTimer=new Timer();
//		indexDocTimer = new Timer();
		salesAppraiseClickTopTimer = new Timer();
		routerTimer.schedule(new Job.RouterTimerTask(this), 4, 5000);
		appTimer.schedule(new Job.AppTimerTask(this), 4, 5000);
		webSocketTimer.schedule(new Job.WebSocketTask(), 5, 5000);
		mobileSendTask.schedule(new MobileSendTask(), 5, 300000);
//		indexDocTimer.schedule(new Job.IndexDocTask(), 10, 300000);
		salesAppraiseClickTopTimer.schedule(new Job.SalesAppraiseClickTopTask(), 20, 7200000);
		
		esMonitorTimer.schedule(new ESMonitorTask(),  10*1000,60*1000);
		
		engineersInit(Globalkey.XM, Globalkey.PH, engineers);
		engineersInit(Globalkey.XM_ETX, Globalkey.PH_ETX, etxs);
		engineersInit(Globalkey.XM_TMP, Globalkey.PH_TMP, tmps);
		serversInit();
		CatesInit();
		//searchMapTimer.schedule(new Job.SearchMapTask(), 10, 3600000);
		log.info("eye start.");
	}

	public void execute(BasicDBObject oMsg) {
		for (WebSocketSession session : sessions) {
			session.send(oMsg);
		}
	}

	public void zooNodeChange(NodeType type, NodeAction action, String id, String ip) {
		ZookeeperUtils.dealZooInfo(servletContext);
		BasicDBObject oMsg = new BasicDBObject();
		oMsg.append("serverType", "zookeeper");
		oMsg.append("msgType", "runtime");
		oMsg.append("type", type.name());
		oMsg.append("action", action.name());
		oMsg.append("id", id);
		execute(oMsg);
	}
	
	public void reconnect() {
	}
	
	private void serversInit() {
		String[] routers = (Globalkey.RouterIps).split(",");
		for (int i = 0; routers!=null && i < routers.length; i++) {
			confRouters.add(routers[i]);
		}
		String[] apps = (Globalkey.AppIps).split(",");
		for (int i = 0; i < apps.length; i++) {
			confApps.add(apps[i]);
		}
	}

	private void engineersInit(String xm, String ph, HashMap<String,Long> contains) {
		String[] xms = xm.split(",");
		String[] phs = ph.split(",");
		if(xms.length != phs.length){
			log.info("Address list init fail.");
			return;
		}
		for (int i = 0; i < phs.length; i++) {
			contains.put(xms[i], Long.valueOf(phs[i]));
		}
	}
	
	private void CatesInit() {
		String[] jds = Globalkey.jdCats.split(",");
		for (String jdC : jds) {
			jdCategory.add(jdC);
		}
	}
}
