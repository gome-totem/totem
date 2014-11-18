package org.x.server.eye;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.service.IJVMDetectImpl;
import org.x.server.service.MobileIntf;
import org.x.server.tools.HiveJdbcTools;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ContextListener extends HttpServlet implements
		ServletContextListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6194361030181744408L;
	private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);


	public ContextListener() {
	}

	private java.util.Timer timer = null;
	private java.util.Timer JVMTimer = null;

	public void contextInitialized(ServletContextEvent event) {
		timer = new java.util.Timer(true);
		JVMTimer = new java.util.Timer(true);
		logger.info("定时器已启动");
		timer.schedule(new HiveQueryTask(), 0, 60 * 60 * 1000);
		JVMTimer.schedule(new JVMTask(), 0, 5 * 60 * 1000);
		logger.info("已经添加任务调度表");
	}

	public void contextDestroyed(ServletContextEvent event) {
		timer.cancel();
		JVMTimer.cancel();
		logger.info("定时器销毁");
	}
	public static void main(String[] args) {
		Map<String, String> map=new HashMap<String, String>();
		System.out.println(map.toString());
	}

}
class JVMTask extends TimerTask {
	private static boolean isRunning = false;
	private static final Logger logger = LoggerFactory.getLogger(JVMTask.class);
	public JVMTask() {
	}
	public void run() {
		
		if (!isRunning) {
				isRunning = true;
				BasicDBObject jvm=new IJVMDetectImpl().JVMDetect();
				if(jvm!=null){
					BasicDBList detail=(BasicDBList)jvm.get("detail");
					for (int i = 0,size=detail.size(); i < size; i++) {
						BasicDBObject data=(BasicDBObject)detail.get(i);
						if(data.getString("name").equals("PS Old Gen")){
							String old=data.getString("use");
							if(StringUtils.isNotBlank(old)){
								String oldPer=old.substring(old.indexOf("(")+1,old.indexOf("%"));
								logger.info("年老态："+oldPer+"%");
								if(StringUtils.isNumeric(oldPer) &&  Integer.parseInt(oldPer)>95){
									//发送警告短信
									MobileIntf MMMIntf = new MobileHandler();
									BasicDBObject msg = new BasicDBObject();
									BasicDBObject content = new BasicDBObject() ;
									
									content.append("id", System.currentTimeMillis()) ;
									BasicDBList info = new BasicDBList() ;
									
									info.add(new BasicDBObject().append("phoneNumber", 13391735298l).append("context", "云眼系统JVM内存报警，old区已使用"+oldPer+"%,即将发生OOM现象")) ;
									info.add(new BasicDBObject().append("phoneNumber", 18810028676l).append("context", "云眼系统JVM内存报警，old区已使用"+oldPer+"%,即将发生OOM现象")) ;
									content.append("info", info) ;
									msg.append("content", content);
									
									MMMIntf.sendTextMessage(msg);
								}
							}
						}
					}
				}
				isRunning=false;
		} else {
			logger.info("上一次任务执行还未结束");
		}
	}

}
class HiveQueryTask extends TimerTask {
	private static final int C_SCHEDULE_HOUR = 1;
	private static boolean isRunning = false;
	private static boolean startTime = true;
	private static final Logger logger = LoggerFactory.getLogger(HiveQueryTask.class);

	public HiveQueryTask() {
	}
	public void run() {
		
		if (!isRunning) {
				isRunning = true;
				logger.info("开始执行指定任务");
				long start=System.currentTimeMillis();
				Calendar cal = Calendar.getInstance();
				if(startTime){
					HiveJdbcTools.queryHiveCache();
					startTime=false;
				}else{
					if (C_SCHEDULE_HOUR == cal.get(Calendar.HOUR_OF_DAY)) {
						HiveJdbcTools.queryHiveCache();
					}else{
						logger.info("任务执行时间还未到达");
					}
				} 
				isRunning = false;
				long end=System.currentTimeMillis();
				logger.info("指定任务执行结束,用时总共："+(end-start));
		} else {
			logger.info("上一次任务执行还未结束");
		}
	}

}