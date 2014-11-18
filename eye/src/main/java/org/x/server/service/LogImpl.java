package org.x.server.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataCollection;

import com.gome.totem.sniper.util.TimeUtil;
import com.mongodb.BasicDBObject;

public class LogImpl implements LogIntf{
	
	private String username = "";
	private String timestamp = StringUtil.currentTime();
	public static Logger logger = LoggerFactory.getLogger(LogImpl.class);
	private static final String USER_ACTION_LOG = "busi_action_log";
	
	public void printLog(BasicDBObject logMsg) {
		Subject user = SecurityUtils.getSubject();
		username = (String) user.getPrincipal();
		logger.info("[eye]-[{}] is {} at {}, Detail info:[{}]", new Object[]{username, logMsg.getString("action",""), timestamp, logMsg.get("context") == null ? "" : logMsg.get("context").toString()});
	}
	
	public void writeLog(BasicDBObject logMsg) {
		Subject user = SecurityUtils.getSubject();
		username = (String) user.getPrincipal();
		String time = TimeUtil.getTodayFormat("yyyy-MM-dd HH:mm:ss");
		logMsg.append("username", username);
		logMsg.append("timestamp", timestamp);
		logMsg.append("time", time);
		DataCollection.insert(Const.defaultLogServer, Const.defaultLogDB, USER_ACTION_LOG, logMsg);
	}

}
