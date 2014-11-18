package org.x.server.service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.x.server.eye.DetectSubject.Address;
import org.x.server.eye.DetectSubject.Message;
import org.z.global.dict.GlobalPage;
import org.z.global.environment.Const;
import org.z.global.zk.ServerDict;
import org.z.store.mongdb.DataCollection;

import com.gome.totem.sniper.util.Globalkey;
import com.gome.totem.sniper.util.Write2PageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
@Service
public class IDictConfigImpl implements IDictConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(IDictConfigImpl.class);
	private static String collName="t_dict_data";
	private static final Queue<Address> addresses = new LinkedBlockingQueue<Address>();
	private static final int[] defaultPorts = new int[] { 8070, 8080, 8090 };
	private static final List<String> defaultIps = new ArrayList<String>();
	private static  Queue<Message> messages = new LinkedBlockingQueue<Message>();
	static {
		String detectAddress=Globalkey.detectAddress;
		if(detectAddress!=null && detectAddress.length()>0){
			String[] ips=StringUtils.split(detectAddress,",");
			for (String ip : ips) {
				defaultIps.add(ip);
			}
		}
	}

	public void list(HttpServletRequest req, HttpServletResponse resp) {
		// 数据模型：{ "_id" : { "$oid" : "53f2ac3f45ce2ac746d5c9c5"} , "data" : { "server" : { "ip_address" : "10.58.50.99" , "service" : [ "app" , "router"] , "env_code" : "10002_prd" , "msgStatus" : "1" , "update_time" : "2014-8-19 9:45:35" , "remark" : ""}}}
		String dataType=req.getParameter("dataType");
		String queryType=req.getParameter("queryType");
		String envCode=req.getParameter("envCode");
		String keyword=req.getParameter("keyword");
		if("server".equals(dataType)){
			BasicDBObject qObject=new BasicDBObject("data.server",new BasicDBObject("$exists",true));
			if(StringUtils.equals("1", queryType)){
				if(StringUtils.isNotBlank(keyword)){
					qObject.append("data.server.ip_address", keyword);
				}
			}else if(StringUtils.equals("2", queryType)){
				if(StringUtils.isNotBlank(keyword)){
					qObject.append("data.server.service", new BasicDBObject("$in",new String[]{keyword}));
				}
			}
			if(StringUtils.isNotBlank(envCode)){
				qObject.append("data.server.env_code", new BasicDBObject("$regex",envCode).append("$options", "i"));
			}
			DBCursor cursor=null;//DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, collName,qObject);
			BasicDBList result=new BasicDBList();
			while(cursor.hasNext()){
				BasicDBObject bo=(BasicDBObject)cursor.next();
				BasicDBObject server=(BasicDBObject)bo.get("data");
				BasicDBObject server1=(BasicDBObject)server.get("server");
				result.add(server1);
			}
			Write2PageUtil.write(resp, result);
		}else if("keywordsFilter".equals(dataType)){
			BasicDBObject qObject=new BasicDBObject("data.keywordsFilter",new BasicDBObject("$exists",true));
			DBCursor cursor= null;//DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, collName,qObject,new BasicDBObject("data.keywordsFilter.update_time",-1),1);
			BasicDBList result=new BasicDBList();
			while(cursor.hasNext()){
				BasicDBObject bo=(BasicDBObject)cursor.next();
				BasicDBObject keyWord=(BasicDBObject)bo.get("data");
				BasicDBObject keyWord1=(BasicDBObject)keyWord.get("keywordsFilter");
				result.add(keyWord1);
			}
			Write2PageUtil.write(resp, result);
		}else{
			DBCursor cursor=null;//DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, new BasicDBObject("data.basic",new BasicDBObject("$exists",true)));
			BasicDBList result=new BasicDBList();
			while(cursor.hasNext()){
				BasicDBObject bo=(BasicDBObject)cursor.next();
				BasicDBObject server=(BasicDBObject)bo.get("data");
				BasicDBObject server1=(BasicDBObject)server.get("basic");
				result.add(server1);
			}
			Write2PageUtil.write(resp, result);
		}
		
	}

	public void addServer(HttpServletRequest req, HttpServletResponse resp) {
		String ip=req.getParameter("ip_address");
		String[] service=req.getParameterValues("service");
		String envCode=req.getParameter("codeMap1")+"_"+req.getParameter("codeMap2");
		String msgStatus=req.getParameter("msgStatus");
		String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		BasicDBObject appServer=new BasicDBObject().append("ip_address", ip).append("service", service).append("env_code", envCode)
				.append("msgStatus", msgStatus).append("update_time", date).append("remark", "");
		ObjectId id=DataCollection.insert(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, new BasicDBObject("data",new BasicDBObject("server",appServer)));
		if(id!=null){
			Write2PageUtil.write(resp, new BasicDBObject("data","success"));
		}else{
			Write2PageUtil.write(resp, new BasicDBObject("data","error"));
		}
	}

	public void delServer(HttpServletRequest req, HttpServletResponse resp) {
		String ip=req.getParameter("ip_address");
		DataCollection.remove(Const.defaultDictMongo, Const.defaultDictMongoDB, collName,new BasicDBObject("data.server.ip_address",ip));
			Write2PageUtil.write(resp, new BasicDBObject("data","success"));
	}

	public void updateServer(HttpServletRequest req, HttpServletResponse resp) {
		this.delServer(req, resp);
		String ip=req.getParameter("ip_address");
		String[] service=req.getParameterValues("service");
		String msgStatus=req.getParameter("msgStatus");
		String envCode=req.getParameter("code_map1")+"_"+req.getParameter("code_map2");
		String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		BasicDBObject appServer=new BasicDBObject().append("ip_address", ip).append("service", service).append("env_code", envCode)
				.append("msgStatus", msgStatus).append("update_time", date).append("remark", "");
		ObjectId id=DataCollection.insert(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, new BasicDBObject("data",new BasicDBObject("server",appServer)));
		if(id!=null){
			Write2PageUtil.write(resp, new BasicDBObject("data","success"));
		}else{
			Write2PageUtil.write(resp, new BasicDBObject("data","error"));
		}
	}

	public void checkData(HttpServletRequest req, HttpServletResponse resp) {
		String data=req.getParameter("data");
		String type=req.getParameter("type");
		DBObject id=null;
		if("ip".equals(type)){
			id=DataCollection.findOne(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, new BasicDBObject("data.server.ip_address",data));
		}else if("filter".equals(type)){
			id=DataCollection.findOne(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, new BasicDBObject("data.keywordsFilter.filterWord",data));
		}
		if(id!=null){
			Write2PageUtil.write(resp, new BasicDBObject("data","success"));
		}else{
			Write2PageUtil.write(resp, new BasicDBObject("data","error"));
		}
	}

	public Map<String, Object> showServer(HttpServletRequest req, HttpServletResponse resp) {
		String ip=req.getParameter("ip_address");
		String tag=req.getParameter("tag");
		BasicDBList list=new BasicDBList();
		BasicDBObject result=new BasicDBObject();
		if("app".equalsIgnoreCase(tag)){
			list=ServerDict.self.apps();
		}
		if("router".equalsIgnoreCase(tag)){
			list=ServerDict.self.routers();
		}
		if("server".equalsIgnoreCase(tag)){
			list=ServerDict.self.servers();
		}
		
		for(int i=0;i<list.size();i++){
			BasicDBObject obj=(BasicDBObject)list.get(i);
			if(ip.equals(obj.get("ip"))){
				result=(BasicDBObject)list.get(i);
				break;
			}
		}
		Map<String, Object> map=result.toMap();
		return map;
	}

	public BasicDBObject addFilter(HttpServletRequest req,HttpServletResponse resp) {
		String data=req.getParameter("data");
		BasicDBList datas=new BasicDBList();
		if(StringUtils.isNotBlank(data)){
			String[] obj=StringUtils.split(data, "#");
			for (int i = 0; i < obj.length; i++) {
				String[] oData=StringUtils.split(obj[i],"@");
				String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				BasicDBObject insertData=new BasicDBObject().append("filterWord", oData[0]).append("url", oData[1]).append("update_time", date).append("remark", ""); 
				datas.add(new BasicDBObject("data",new BasicDBObject("keywordsFilter",insertData)));
			}
		}
		try {
			DataCollection.insertBatch(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, datas);
		} catch (Exception e) {
			e.printStackTrace();
			return new BasicDBObject("data","error");			
		}
		return new BasicDBObject("data","success");
	}

	public BasicDBObject initFilter(HttpServletRequest req,HttpServletResponse resp) {
		Map<String, String> keyFilterMap = new HashMap<String, String>();
		String url=GlobalPage.SATSITE.replace("http://", "").replace("www", "http://chongzhi");
		keyFilterMap.put("充值", url);
		keyFilterMap.put("手机话费", url);
		keyFilterMap.put("手机充值", url);
		keyFilterMap.put("话费", url);
		keyFilterMap.put("充话费", url);
		keyFilterMap.put("话费充值", url);
		keyFilterMap.put("移动", url);
		keyFilterMap.put("中国移动", url);
		keyFilterMap.put("联通", url);
		keyFilterMap.put("中国联通", url);
		keyFilterMap.put("电信", url);
		keyFilterMap.put("中国电信", url);
		keyFilterMap.put("充值卡", url);
		keyFilterMap.put("在线游戏", "http://game.gome.com.cn/");
		keyFilterMap.put("游戏", "http://game.gome.com.cn/game/");
		keyFilterMap.put("真命", "http://game.gome.com.cn/site/zm/");
		keyFilterMap.put("天界", "http://game.gome.com.cn/game/?id=2");
		keyFilterMap.put("大航海家", "http://game.gome.com.cn/game/?id=12");
		keyFilterMap.put("大将军", "http://game.gome.com.cn/game/?id=9");
		keyFilterMap.put("大闹天宫", "http://game.gome.com.cn/game/?id=8");
		keyFilterMap.put("斗破乾坤", "http://game.gome.com.cn/game/?id=11");
		keyFilterMap.put("大侠传", "http://game.gome.com.cn/game/?id=4");
		keyFilterMap.put("攻城掠地", "http://game.gome.com.cn/game/?id=15");
		keyFilterMap.put("九伐中原", "http://game.gome.com.cn/game/?id=14");
		keyFilterMap.put("龙纹战域", "http://game.gome.com.cn/game/?id=6");
		keyFilterMap.put("欧冠足球", "http://game.gome.com.cn/game/?id=3");
		keyFilterMap.put("神曲2", "http://game.gome.com.cn/game/?id=7");
		keyFilterMap.put("卧龙吟", "http://game.gome.com.cn/game/?id=16");
		keyFilterMap.put("武尊", "http://game.gome.com.cn/game/?id=10");
		keyFilterMap.put("轩辕变", "http://game.gome.com.cn/game/?id=5");
		keyFilterMap.put("战三国", "http://game.gome.com.cn/game/?id=13");
		
		keyFilterMap.put("空调维修", "http://weixiu.gome.com.cn/");
		keyFilterMap.put("电视维修", "http://weixiu.gome.com.cn/");
		keyFilterMap.put("洗衣机维修", "http://weixiu.gome.com.cn/");
		keyFilterMap.put("冰箱维修", "http://weixiu.gome.com.cn/");
		keyFilterMap.put("热水器维修", "http://weixiu.gome.com.cn/");
		keyFilterMap.put("燃气灶维修", "http://weixiu.gome.com.cn/");
		
		keyFilterMap.put("空调回收", "http://huishou.gome.com.cn/");
		keyFilterMap.put("电视回收", "http://huishou.gome.com.cn/");
		keyFilterMap.put("洗衣机回收", "http://huishou.gome.com.cn/");
		keyFilterMap.put("冰箱回收", "http://huishou.gome.com.cn/");
		BasicDBList datas=new BasicDBList();
		Set set=keyFilterMap.entrySet();
       Iterator it=set.iterator();
        while(it.hasNext()){
           Map.Entry me=(Map.Entry)it.next();
			String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
           BasicDBObject insertData=new BasicDBObject().append("filterWord", me.getKey()).append("url", me.getValue()).append("update_time", date).append("remark", ""); 
           datas.add(new BasicDBObject("data",new BasicDBObject("keywordsFilter",insertData)));
        }
        try {
        	DataCollection.remove(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, new BasicDBObject("data.keywordsFilter",new BasicDBObject("$exists",true)));
			DataCollection.insertBatch(Const.defaultDictMongo, Const.defaultDictMongoDB, collName, datas);
		} catch (Exception e) {
			e.printStackTrace();
			return new BasicDBObject("data","error");			
		}
		return new BasicDBObject("data","success");
	}

	public BasicDBObject delFilter(HttpServletRequest req,HttpServletResponse resp) {
		String keyword=req.getParameter("keyword");
		 try {
				DataCollection.remove(Const.defaultDictMongo, Const.defaultDictMongoDB, collName,new BasicDBObject("data.keywordsFilter.filterWord",keyword));
		} catch (Exception e) {
			e.printStackTrace();
			return new BasicDBObject("data","error");			
		}
		return new BasicDBObject("data","success");
	}

	public BasicDBObject ascFilter(HttpServletRequest req,HttpServletResponse resp) {
		BasicDBList result=new BasicDBList();
		messages.clear();
		createDefaultAscFilter();
		while(true){
			Message msg=messages.poll();
			if(msg==null){
				break;
			}else{
				result.add(new BasicDBObject().append("ip", msg.getIp()).append("port", msg.getPort()).append("status", msg.getStatus()));
			}
		}
		return new BasicDBObject("msg",result);
	}
	public void createDefaultAscFilter() {
		for (String ip : defaultIps)
			for (int port : defaultPorts)
				addresses.add(new Address(createAscFilterUrl(ip, port), ip, port));
	    execute(addresses);
	}
	
	public void execute(final Queue<Address> queue) {
		if (queue == null || queue.size() == 0)
			return;
		final CountDownLatch begin = new CountDownLatch(1);
		final CountDownLatch end = new CountDownLatch(10);
		final ExecutorService exec = Executors.newFixedThreadPool(10);
		for (int index = 0; index < 10; index++) {
			Runnable run = new Runnable() {
				public void run() {
					try {
						begin.await();
						while(true){
							Address address=queue.poll();
							if(address==null){
								break;
							}else {
								detectAll(address);
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						end.countDown(); 
					}
				}
			};

			exec.submit(run);
		}

		System.out.println("asc start!");
		begin.countDown(); 
		try {
			end.await(); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("asc end!");
		exec.shutdown();
	}

	private static String createAscFilterUrl(String ip, int port) {
		return "http://" + ip + ":" + port +"/cloud/init?opt=init";
	}
	private void detectAll(Address address) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		detectAddress(address, httpclient);
		closeHttpClient(httpclient);
	}
	
	private static void closeHttpClient(CloseableHttpClient httpclient) {
		if (httpclient == null)
			return;
		try {
			httpclient.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void detectAddress(Address address, CloseableHttpClient httpclient) {
		long begin = System.currentTimeMillis();
		handlerResponse(address, getResponse(address, httpclient), begin);
	}

	private void handlerResponse(Address address, HttpResponse response, long begin) {
		long intervalTime = System.currentTimeMillis() - begin;
		if(response != null){
			int status = response.getStatusLine().getStatusCode();
			messages.add(new Message(address, status, intervalTime));
			logger.info("ASCFILTER-----------{}",new Object[]{new Message(address, status, intervalTime)});
		}
	}

	private HttpResponse getResponse(Address address, CloseableHttpClient httpclient) {
		try {
			HttpGet httpGet =new HttpGet(address.getUrl());
			RequestConfig requestConfig = RequestConfig.custom().
					setConnectionRequestTimeout(5000).
					setSocketTimeout(5000).
					setConnectTimeout(5000).
					build();
			httpGet.setConfig(requestConfig);
			return httpclient.execute(httpGet);
		}catch(SocketTimeoutException e){
			messages.add(new Message(address, 888, 0));
			return null;
		}catch(ConnectTimeoutException e){
			messages.add(new Message(address, 888, 0));
			return null;
		}catch (ClientProtocolException e) {
			messages.add(new Message(address, 888, 0));
			return null;
		} catch (IOException e) {
			messages.add(new Message(address, 888, 0));
			return null;
		}
	}

}
