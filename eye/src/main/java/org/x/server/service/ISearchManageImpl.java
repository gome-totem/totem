package org.x.server.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.x.server.common.EyeCache;
import org.x.server.eye.Eye;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.CompressMode;
import org.z.global.dict.Device;
import org.z.global.dict.Global.HashMode;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Const;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServiceName;
import org.z.store.beanstalk.BeansConnect;
import org.z.store.beanstalk.BeansConnectFactory;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.TimeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Service
public class ISearchManageImpl implements ISearchManage{
	
	private static boolean isRunning = false;
	private static boolean isRunningDocSize = false;

	public void beansTalkMonitor(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
		BasicDBObject result = new BasicDBObject();
		if(isRunning==true){
			resp.getWriter().print(result.append("msg", "running"));
			return;
		}else{
			isRunning = true;
		}
		
		BeansConnect client = null;

		client = BeansConnectFactory.builder().server("10.58.50.56").use("product").build();
		HashMap<String, String> stats = null;
		
		stats = client.stats();
		String del = stats.get("cmd-delete");
		String tol = stats.get("total-jobs");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		stats = client.stats();
		String oDel = stats.get("cmd-delete");
		String oTal = stats.get("total-jobs");

		BasicDBObject data = new BasicDBObject();
		data.append("b_tol", tol);
		data.append("b_del", del);
		data.append("e_tol", oDel);
		data.append("e_del", oTal);
		data.append("dif_tal", (Long.parseLong(oTal) - Long.parseLong(tol)));
		data.append("dif_del", (Long.parseLong(oDel) - Long.parseLong(del)));
		
		result.append("msg", "success");
		result.append("val", data);
    	resp.getWriter().print(result);
		isRunning = false;
	}
	public void msgSend(String massage){
		BasicDBObject msg = new BasicDBObject();
		BasicDBObject content = new BasicDBObject();
		BasicDBList info = new BasicDBList();
		for(String name : Eye.engineers.keySet()){
			info.add(new BasicDBObject().append("phoneNumber", Eye.engineers.get(name).longValue())
					.append("context", "hi, " + name + TimeUtil.sayHelloByHour() + " " + massage + " 消息小助手 -- 搜索组")) ;
		}
		if(Globalkey.MassageTmp){
			for(String name : Eye.tmps.keySet()){
				info.add(new BasicDBObject().append("phoneNumber", Eye.tmps.get(name).longValue())
						.append("context", "hi, " + name + TimeUtil.sayHelloByHour() + " " + massage + " 消息小助手 -- 搜索组")) ;
			}
		}
		content.append("info", info);
		msg.append("content", content);
		
		Eye.MMMIntf.sendHtmlMessage(msg);
	}
	
	/**
	 * 统计所有app上的Doc数
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void appsDoc(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		BasicDBList newAppsDocNum = new BasicDBList();
		if(isRunningDocSize==true){
			resp.getWriter().print(EyeCache.appsDoc);
			return;
		}else{
			isRunningDocSize = true;
		}
		BasicDBList apps = ServerDict.self.apps();
		BasicDBObject oReq = new BasicDBObject();
		oReq.append("docCount", true);
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
    	resp.getWriter().print(EyeCache.appsDoc);
		isRunningDocSize = false;
	}
	/**
	 * 配置促销因子的商品权重更新
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void promoProductFullWeight(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
		BasicDBObject result = new BasicDBObject();
		ZeroConnect.send(Device.SERVER, ServiceName.ProductIndex, Const.AppConnMode == HashMode.appserver ? MessageScope.ALLAPP : MessageScope.ROUTER, MessageType.CLEARCACHE, MessageVersion.MQ,
				"updateAllProWeight", new BasicDBObject().append("sku", 1), null);
		result.append("msg", "success");
		resp.getWriter().print(result);
	}
	
	/**
	 * 配置促销因子的商品全量更新索引
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void promoProductFull(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String catId=req.getParameter("catId");
		long start = System.currentTimeMillis();
		BasicDBObject result = new BasicDBObject();
		ThreadPoolExecutor executor = new ThreadPoolExecutor(40, 40, 10, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
		DataResult find=null;
		if(!StringUtils.isBlank(catId)){
			find = DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, "promo_score", new BasicDBObject().append("catId", catId), new BasicDBObject("promoScore", -1));
		}else{
			find = DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, "promo_score", null, new BasicDBObject("promoScore", -1));
		}
		BasicDBList list = find.list;
		int size = list.size();
		int i = 0;
		for (Object obj : list) {
			BasicDBObject object = (BasicDBObject) obj;
			BasicDBObject newObject = new BasicDBObject();
			String skuId = object.getString("skuId");
			newObject.append("skuId", skuId.trim());
			newObject.append("promoScore", object.get("promoScore"));
			int s=++i;
			executor.execute(new PromoScoreTask(newObject, size, s, skuId));
		}
		long end = System.currentTimeMillis();
		result.append("size", size);
		result.append("time", (end-start)/1000.0);
		result.append("msg", "success");
		resp.getWriter().print(result);
	}
	static class PromoScoreTask implements Runnable {
		private BasicDBObject object;
		private int i;
		private int size;
		private String skuId;

		public PromoScoreTask(BasicDBObject object, int size, int i, String skuId) {
			this.object = object;
			this.size = size;
			this.i = i;
			this.skuId = skuId;
		}

		public void run() {
		}
	}
	/**
	 * 单个同步商品
	 */
	public void promoSingle(HttpServletRequest req, HttpServletResponse resp) {
		String skuId=req.getParameter("skuId");
		String score=req.getParameter("score");
		BasicDBObject result = new BasicDBObject();
		BasicDBObject newObject = new BasicDBObject();
		newObject.append("skuId", skuId.trim());
		newObject.append("promoScore", Double.parseDouble(score));
		try {
			new PromoScoreTask(newObject, 1, 1, skuId).run();
			result.append("msg", "success");
			resp.getWriter().print(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
