package org.z.web.common;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Device;
import org.z.global.dict.Global.PayCode;
import org.z.global.dict.Global.PayMode;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Business;
import org.z.global.environment.Business.ClassName;
import org.z.global.environment.Const;
import org.z.global.object.UserRole;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServiceName;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class Context {
	private HttpServletRequest req = null;
	private HttpServletResponse response = null;
	public BasicDBObject request = new BasicDBObject();
	public BasicDBObject cookies = new BasicDBObject();
	public BasicDBObject pageParams = new BasicDBObject();
	public String pageName = null;
	public String cityName = null;
	public String langName = null;
	public String cookieId = null;
	public String uri = null;
	public BasicDBObject user = null;
	public UserRole role = null;
	public String remoteIP = null;
	protected static Logger logger = LoggerFactory.getLogger(Context.class);

	public Context(boolean parseRequestReader, HttpServletRequest req, HttpServletResponse response) {
		this.req = req;
		this.response = response;
		init(parseRequestReader);
	}

	public Context(HttpServletRequest req, HttpServletResponse response) {
		this(true, req, response);
	}

	public BasicDBObject currentUser() {
		BasicDBObject result = new BasicDBObject();
		result.append("cookieId", this.cookieId);
		result.append("id", this.user.getString("userId"));
		result.append("name", this.user.getString("userName"));
		result.append("role", this.role.value);
		return result;
	}

	public boolean isServerMachine() {
		return ServerDict.self.hasIP(remoteIP);
	}

	public void init(boolean parseRequestReader) {
		if (this.req == null) {
			return;
		}
		if (parseRequestReader) {
			parseRequestContent();
		}
		parsePageParams();
		parseCookie();
		pageParams.put("_method", req.getMethod());
		try {
			String c = RedisPool.use().get(this.cookieId);
			if (StringUtil.isEmpty(c) == false) {
				user = (BasicDBObject) JSON.parse(c);
				String r = user.getString("role");
				if (StringUtils.isNumeric(r)) {
					role = user.containsField("role") ? new UserRole(Long.parseLong(r)) : new UserRole();
				} else {
					role = new UserRole();
				}
			} else {
				user = new BasicDBObject();
				role = new UserRole();
			}
		} catch (Exception e) {
			logger.error("init", e);
		}
	}

	public void parsePageParams() {
		this.request.append("pageParams", pageParams);
		for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements();) {
			String keyName = String.valueOf(e.nextElement());
			pageParams.put(keyName, req.getHeader(keyName));
		}
		for (Enumeration<String> e = req.getParameterNames(); e.hasMoreElements();) {
			String keyName = String.valueOf(e.nextElement());
			pageParams.put(keyName, req.getParameter(keyName));
		}
		uri = req.getRequestURI();
		String[] params = StringUtils.split(uri, '/');
		pageName = params.length >= 1 ? params[0] : "index";
		if (pageName.equalsIgnoreCase("index.jsp")) {
			pageName = "index";
		}
		String pageParam = params.length >= 2 ? params[params.length - 1] : null;
		parsePageParams(pageParam);
		parsePageParams(req.getQueryString());
		remoteIP = getRemoteIP(req);
		try {
			pageParams.append("uri", uri);
			pageParams.append("pageName", pageName);
			pageParams.append("ip", remoteIP);
			pageParams.append("siteName", Const.SiteName);
			langName = cookies.containsField("langName") ? cookies.getString("langName") : "zh";
			pageParams.append("langName", langName);
			cityName = cookies.getString("cityName");
			if (!StringUtil.isEmpty(cityName)) {
				cityName = java.net.URLDecoder.decode(cityName, "utf-8");
				pageParams.append("cityName", cityName);
			}
		} catch (Exception e) {
			logger.error("readHtmlPage", e);
		}
	}

	public void parseCookie() {
		this.request.append("cookies", cookies);
		Cookie[] list = req.getCookies();
		for (int i = 0; list != null && i < list.length; i++) {
			Cookie c = list[i];
			cookies.put(c.getName(), c.getValue());
		}
		if (cookies.containsField("langName") == false) {
			Cookie c = new Cookie("langName", langName);
			c.setPath("/");
			c.setMaxAge(3 * 24 * 60 * 60);
			if (response != null) {
				response.addCookie(c);
			}
		}
		cookies.append("langName", langName);
		if (cookies.containsField("cookieId")) {
			cookieId = cookies.getString("cookieId");
		}
		if (pageParams.containsField("cookieId")) {
			cookieId = pageParams.getString("cookieId");
			pageParams.removeField("cookieId");
		} else if (request.containsField("cookieId")) {
			cookieId = request.getString("cookieId");
			request.removeField("cookieId");
		} else if (StringUtils.isEmpty(cookieId)) {
			cookieId = StringUtil.createUniqueID();
			Cookie c = new Cookie("cookieId", cookieId);
			c.setMaxAge(30 * 24 * 60 * 60);
			c.setPath("/");
			if (response != null) {
				response.addCookie(c);
			}
		}
		cookies.append("cookieId", cookieId);
		if (cookies.containsField("xbrowser")) {
			pageParams.put("xbrowser", "true");
		} else if (pageParams.containsField("xbrowser")) {
			Cookie c = new Cookie("xbrowser", "true");
			c.setPath("/");
			c.setMaxAge(3 * 24 * 60 * 60);
			if (response != null) {
				response.addCookie(c);
			}
		}
	}

	public void parseRequestContent() {
		try {
			BufferedReader br = req.getReader();
			String line = null;
			StringBuilder buffer = new StringBuilder();
			while ((line = br.readLine()) != null) {
				buffer.append(line);
				buffer.append(System.lineSeparator());
			}
			String content = buffer.toString().trim();
			if (content.startsWith("{")) {
				request = (BasicDBObject) JSON.parse(content);
			} else {
				String[] values = content.split("\\&");
				request = new BasicDBObject();
				for (int i = 0; i < values.length; i++) {
					String[] items = values[i].split("\\=");
					if (items.length != 2)
						continue;
					request.put(items[0], URLDecoder.decode(items[1], "utf-8"));
				}
			}
		} catch (Exception e) {
			logger.error("parseRequest", e);
		}
	}

	public void parsePageParams(String pageParam) {
		if (pageParam != null) {
			int index = pageParam.lastIndexOf("#");
			if (index > 0) {
				pageParam = pageParam.substring(0, index);
			}
			String[] pList = StringUtils.split(pageParam, '&');
			for (int i = 0; i < pList.length; i++) {
				String value = pList[i];
				String[] values = value.split("\\=");
				try {
					if (values.length == 1) {
						if (value.endsWith("=")) {
							pageParams.put(values[0], "");
						} else {
							pageParams.put("id", values[0]);
						}
						continue;
					} else if (values.length == 2) {
						pageParams.put(values[0], URLDecoder.decode(values[1], "utf-8"));
					}
				} catch (Exception e) {

				}
			}
		}
	}

	public String getRemoteIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("http_client_ip");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip != null) {
			if (ip.indexOf(",") != -1) {
				ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
			} else if (ip.indexOf(":") != -1) {
				ip = "127.0.0.1";
			}
		}
		return ip;
	}

	public void forceLogin(String lastUri) {
		pageParams.append("pageName", "service");
		pageParams.append("action", "login");
		try {
			user.append("lastUri", lastUri);
			RedisPool.use().set(this.cookieId, user.toString());
		} catch (Exception e) {
			logger.error("init", e);
		}

	}

	public String requestContent() {
		return this.request.getString("requestContent");
	}

	public void readHtmlPage(String pageName) {
		if (response == null) {
			return;
		}
		try {
			PrintWriter writer = response.getWriter();
			BasicDBObject oResult = (BasicDBObject) ZeroConnect.readHtmlPage(pageName, this.request);
			if (oResult == null) {
				writer.println("read HtmlPage[" + pageName + "] Empty.");
				writer.flush();
				writer.close();
				return;
			}
			BasicDBObject oResponse = (BasicDBObject) oResult.get("response");
			if (oResponse.getBoolean("xeach", false) == false) {
				writer.print(oResponse.getString("message"));
				writer.flush();
				writer.close();
				return;
			}
			if (oResponse.containsField("cityName")) {
				Cookie c = new Cookie("cityName", java.net.URLEncoder.encode(oResponse.getString("cityName"), "utf-8"));
				c.setPath("/");
				c.setMaxAge(3 * 24 * 60 * 60);
				response.addCookie(c);
			}
			writer.println(oResponse.getString("message"));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error("readHtmlPage", e);
		}
	}

	public void prepareRequest() {
		request.append("cookies", cookies);
		request.append("pageParams", pageParams);
		if (user != null) {
			request.append("user", user);
		}
	}

	public BasicDBObject post(ServiceName service, ClassName className) {
		return post(service, className, null);
	}

	public BasicDBObject post(ServiceName service, ClassName className, String[] classParams) {
		request.append("action", "post");
		request.append("classIndex", className.ordinal());
		if (classParams != null) {
			request.append("classParams", classParams);
		}
		prepareRequest();
		BasicDBObject oResult = (BasicDBObject) ZeroConnect.post(service, MessageType.REQUEST, String.valueOf(this.userId()), request);
		if (oResult != null && oResult.containsField("response")) {
			oResult = (BasicDBObject) oResult.get("response");
		}
		return oResult;
	}

	public String remoteCall(int classIndex, BasicDBList classParams, String functionName, BasicDBObject params) {
		request.append("action", "remoteCall");
		request.append("classIndex", classIndex);
		request.append("funcName", functionName);
		prepareRequest();
		if (params != null) {
			request.putAll((BSONObject) params);
		}
		if (classParams != null) {
			request.append("classParams", classParams);
		}
		BasicDBObject o = (BasicDBObject) ZeroConnect.post(ServiceName.HtmlPage, MessageType.REQUEST, String.valueOf(this.userId()), request);
		if (o == null) {
			return null;
		}
		if (o.containsField("response")) {
			o = (BasicDBObject) o.get("response");
		}
		return o.getString("return");
	}

	public BasicDBObject changeCity(String cityName) {
		this.cityName = cityName;
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		Cookie[] list = this.req.getCookies();
		for (int i = 0; list != null && i < list.length; i++) {
			Cookie c = list[i];
			if (c.getName().equalsIgnoreCase("cityName")) {
				try {
					c.setValue(java.net.URLEncoder.encode(this.cityName, "utf-8"));
				} catch (Exception e) {

				}
				c.setPath("/");
				response.addCookie(c);
				break;
			}
		}
		return oResult;
	}

	public void changeLang(String langName_) {
		Cookie[] list = this.req.getCookies();
		for (int i = 0; list != null && i < list.length; i++) {
			Cookie c = list[i];
			if (c.getName().equalsIgnoreCase("langName")) {
				c.setValue(langName_);
				c.setPath("/");
				response.addCookie(c);
				break;
			}
		}
	}

	public long userId() {
		return user == null ? 0 : user.getLong("userId", 0);
	}

	public String userServer() {
		return user == null ? null : user.getString("server", null);
	}

	public String userCallServer() {
		return user == null ? null : user.getString("callServer", null);
	}

	public boolean licenceIsValid() {
		if (this.userId() == 0) {
			return false;
		}
		long licence = user.getLong("licence", 0);
		return licence >= System.currentTimeMillis();
	}

	public String addRecharge(long shortId, String orderId, int amount) {
		request.append("payCode", PayCode.Recharge.ordinal());
		request.append("payMode", PayMode.PendingInc.ordinal());
		request.append("shortId", shortId);
		request.append("orderId", orderId);
		request.append("amount", amount);
		request.append("desc", "充值");
		request.append("classIndex", Business.ClassName.Bill.ordinal());
		request.append("funcIndex", Business.Bill.addRecharge.ordinal());
		BasicDBObject oBill = null;//(BasicDBObject) ZeroConnect.send(Device.SERVER, ServiceName.Servlet, MessageScope.APP, MessageType.UPDATE, MessageVersion.MQ,
				//"addRecharge", request);
		if (oBill == null || oBill.containsField("response") == false) {
			return null;
		}
		oBill = (BasicDBObject) oBill.get("response");
		return oBill.getString("billId");
	}

	public void recharge(String server, String uId, String billId, String money, String email) {
		request.append("server", server);
		request.append("uId", uId);
		request.append("billId", billId);
		request.append("money", money);
		request.append("email", email);
		request.append("classIndex", Business.ClassName.Bill.ordinal());
		request.append("funcIndex", Business.Bill.recharge.ordinal());
//		ZeroConnect.send(Device.SERVER, ServiceName.Servlet, MessageScope.APP, MessageType.UPDATE, MessageVersion.MQ, "recharge", request);
	}

	public static void main(String[] args) {
	}
}
