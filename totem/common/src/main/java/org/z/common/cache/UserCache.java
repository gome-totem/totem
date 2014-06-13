package org.z.common.cache;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danga.MemCached.MemCachedClient;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class UserCache {

	protected static final Logger logger = LoggerFactory.getLogger(UserCache.class);

	public static void reset(String cookieId, String key) {
		BasicDBObject oUser = null;
		try {
			MemCachedClient mc = new MemCachedClient();
			String content = (String) mc.get(cookieId);
			if (!StringUtils.isEmpty(content)) {
				oUser = (BasicDBObject) JSON.parse(content);
			} else {
				oUser = new BasicDBObject();
			}
			oUser.removeField(key);
			mc.set(cookieId, oUser.toString());
		} catch (Exception e) {
			logger.error("reset", e);
		}
	}

	public static void set(String cookieId, String key, Object value) {
		BasicDBObject oUser = null;
		try {
			MemCachedClient mc = new MemCachedClient();
			String content = (String) mc.get(cookieId);
			if (!StringUtils.isEmpty(content)) {
				oUser = (BasicDBObject) JSON.parse(content);
			} else {
				oUser = new BasicDBObject();
			}
			oUser.append(key, value);
			mc.set(cookieId, oUser.toString());
		} catch (Exception e) {
			logger.error("reset", e);
		}
	}

	public static BasicDBObject get(String cookieId) {
		BasicDBObject oUser = null;
		try {
			MemCachedClient mc = new MemCachedClient();
			String content = (String) mc.get(cookieId);
			if (!StringUtils.isEmpty(content)) {
				oUser = (BasicDBObject) JSON.parse(content);
			} else {
				oUser = new BasicDBObject();
			}
			return oUser;
		} catch (Exception e) {
			logger.error("UserService", e);
			return null;
		}
	}

	public static String get(String cookieId, String key) {
		BasicDBObject oUser = null;
		try {
			MemCachedClient mc = new MemCachedClient();
			String content = (String) mc.get(cookieId);
			if (!StringUtils.isEmpty(content)) {
				oUser = (BasicDBObject) JSON.parse(content);
			} else {
				oUser = new BasicDBObject();
			}
			return oUser.getString(key);
		} catch (Exception e) {
			logger.error("UserService", e);
			return null;
		}
	}

	public static void setCaptcha(String cookieId, String value) {
		set(cookieId, "captcha", value);
	}

	public static String getCaptcha(String cookieId) {
		return get(cookieId, "captcha");
	}

}
