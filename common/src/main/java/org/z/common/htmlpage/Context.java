package org.z.common.htmlpage;

import java.io.StringWriter;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global;
import org.z.global.interfaces.ModuleHtmlPageIntf;
import org.z.global.interfaces.UserIntf;
import org.z.global.ip.IPSeeker;
import org.z.global.object.KeyValue;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * 上下文
 */
public class Context {
	protected static final Logger logger = LoggerFactory.getLogger(Context.class);
	public VelocityContext ctx = null;
	public ModuleHtmlPageIntf module = null;
	public String pageName = null;
	public String langName = null;
	public String browserName = "";
	public String browserAgent = "";
	public boolean isSpider = false;
	public String cityName = "";
	public Lang lang = null;
	public UserIntf user = null;
	public Tool tool = null;
	public boolean pdf = false;
	public String cookieId = null;
	public String ip = null;
	public String uri = null;
	public String siteName = null;
	public BasicDBObject request = null;
	public HashSet<String> includeScripts = new HashSet<String>();
	public int ErrorCode = 0;
	public StringBuilder requestContent = null;
	public BasicDBObject pageParams = null;
	public BasicDBObject cookies = null;

	public void initRequest(ModuleHtmlPageIntf module, BasicDBObject oReq) {
		this.module = module;
		this.pageParams = oReq.containsField("pageParams") ? (BasicDBObject) oReq.get("pageParams") : new BasicDBObject();
		this.pdf = pageParams.getString("pdf", "").equalsIgnoreCase("true");
		this.cookies = oReq.containsField("cookies") ? (BasicDBObject) oReq.get("cookies") : new BasicDBObject();
		this.pageName = pageParams.getString("pageName", "");
		oReq.removeField("cookies");
		oReq.removeField("pageParams");
		this.request = oReq;
		this.cookieId = oReq.getString("cookieId");
		if (StringUtil.isEmpty(this.cookieId)) {
			this.cookieId = cookies.getString("cookieId");
		}
		this.ip = pageParams.getString("ip");
		this.uri = pageParams.getString("uri");
		this.siteName = pageParams.getString("siteName");
		this.langName = this.cookies.getString("langName", "zh");
		try {
			String v = cookies.getString("cityName");
			if (!StringUtil.isEmpty(v)) {
				this.cityName = java.net.URLDecoder.decode(v, "utf-8");
			}
			if (StringUtil.isEmpty(v) || this.cityName.equals("本机地址") || this.cityName.equals("未知国家")) {
				this.cityName = IPSeeker.getCityNameByIP(this.ip);
			}
			lang = new Lang(pageName, langName);
			tool = new Tool(lang);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
		parseAgent();
	}

	public void parseAgent() {
		if (pageParams == null) {
			return;
		}
		if (pageParams.containsField("user-agent")) {
			browserAgent = pageParams.getString("user-agent").toLowerCase();
		} else if (pageParams.containsField("user-Agent")) {
			browserAgent = pageParams.getString("user-Agent").toLowerCase();
		}
		if (!StringUtils.isEmpty(browserAgent)) {
			if (browserAgent.indexOf("msie 6.0") != -1) {
				browserName = "ie6";
			} else {
				browserName = "other";
			}
		}
		if (pageParams.getString("isSpider", "").equalsIgnoreCase("true") || StringUtil.isEmpty(browserAgent) || (browserAgent.indexOf("spider") >= 0) || (browserAgent.indexOf("bot") >= 0)
				|| (browserAgent.indexOf("yahoo!") >= 0)) {
			isSpider = true;
		}
	}

	public boolean isServerMachine() {
		return ServerDict.self.hasIP(ip);
	}

	public boolean pdf() {
		return pdf;
	}

	public String session(String name) {
		try {
			String c = RedisPool.use().get(this.cookieId);
			if (c != null) {
				BasicDBObject ouser = (BasicDBObject) JSON.parse(c);
				return ouser.getString(name);
			}
		} catch (Exception e) {
			logger.error("init", e);
		}
		return "";
	}

	public String session(String name, String value) {
		try {
			String c = RedisPool.use().get(this.cookieId);
			BasicDBObject ouser = null;
			if (c != null) {
				ouser = (BasicDBObject) JSON.parse(c);
			} else {
				ouser = new BasicDBObject();
			}
			ouser.append(name, value);
			RedisPool.use().set(this.cookieId, ouser.toString());
		} catch (Exception e) {
			logger.error("init", e);
		}
		return value;
	}

	protected Object createRemoteObject(String className, Context context, BasicDBList params) {
		Class<?> cls = module.classBy(className);
		if (cls == null) {
			return null;
		}
		try {
			Object o = cls.newInstance();
			return o;
		} catch (Exception e) {
			logger.error("createRemoteObject", e);
			return null;
		}
	}

	public void initUser() {
		user = new User(this);
	}

	public BasicDBObject currentUser() {
		BasicDBObject result = new BasicDBObject();
		result.append("cookieId", this.cookieId);
		result.append("id", this.user.getUserId());
		result.append("name", this.user.getUserName());
		result.append("role", this.user.getRole().value);
		return result;
	}

	public boolean initHtmlPage() {
		if (module == null) {
			logger.info("ModuleHtmlPage not start.");
			return false;
		}
		BasicDBObject oPage = module.pageBy(pageName);
		if (oPage == null) {
			logger.info("Page [{}] not register in ZooKeeper", new Object[] { pageName });
			return false;
		}
		ctx = new VelocityContext();
		ctx.put("user", this.user);
		ctx.put("context", this);
		ctx.put("lang", this.lang);
		ctx.put("tool", this.tool);
		ctx.put("isSpider", this.isSpider);
		BasicDBList objects = (BasicDBList) oPage.get("objects");
		for (int i = 0; objects != null && i < objects.size(); i++) {
			BasicDBObject object = (BasicDBObject) objects.get(i);
			String name = object.getString("name");
			String className = object.getString("class");
			Object instance = createRemoteObject(className, this, (BasicDBList) object.get("params"));
			if (instance == null) {
				continue;
			}
			ctx.put(name, instance);
		}
		return true;
	}

	public String getCookieId() {
		return this.cookieId;
	}

	public String cityByIP(boolean forceHot) {
		String v = IPSeeker.getCityNameByIP(this.ip);
		return v;
	}

	public boolean hasPageRight(String pageName, long userId) {
		boolean b = true;
		if (pageName.equalsIgnoreCase("guide") || pageName.equalsIgnoreCase("trip")) {
			b = user.getRole().isTripAssistant();
		} else if (pageName.equalsIgnoreCase("room")) {
			b = user.getRole().isRoomAssistant();
		} else if (pageName.equalsIgnoreCase("car")) {
			b = user.getRole().isCarAssistant();
		} else if (pageName.equalsIgnoreCase("topic")) {
			b = user.getRole().isTopicAssistant();
		}
		return b || user.getUserId() == userId;
	}

	public boolean isRoot() {
		if (user.getRole() == null) {
			return false;
		}
		return user.getRole().isRoot();
	}

	public boolean isTopicAssistant() {
		if (user.getRole() == null) {
			return false;
		}
		return user.getRole().isTopicAssistant();
	}

	public String getLangName() {
		return this.langName;
	}

	public boolean containsParam(String key) {
		return pageParams.containsField(key);
	}

	public String getIp() {
		return this.ip;
	}

	public BasicDBObject getPageParams() {
		return this.pageParams;
	}

	public String getParam(String key) {
		if (pageParams.containsField(key)) {
			return String.valueOf(pageParams.get(key));
		}
		return "";
	}

	public String getParam(String key, String defaultValue) {
		if (pageParams.containsField(key)) {
			return String.valueOf(pageParams.get(key));
		}
		return defaultValue;
	}

	public String getCityName() {
		return this.cityName;
	}

	public int getParamByInt(String key) {
		return this.getParamByInt(key, 0);
	}

	public int getParamByInt(String key, int defaultValue) {
		if (pageParams.containsField(key)) {
			String value = String.valueOf(pageParams.get(key));
			if (!StringUtils.isEmpty(value) && StringUtils.isNumeric(value)) {
				return Integer.parseInt(value);
			}
		}
		return defaultValue;
	}

	public long getParamByLong(String key, long defaultValue) {
		if (pageParams.containsField(key)) {
			String value = String.valueOf(pageParams.get(key));
			if (!StringUtils.isEmpty(value) && StringUtils.isNumeric(value)) {
				return Long.parseLong(value);
			}
		}
		return defaultValue;
	}

	public long getParamByLong(String key) {
		return getParamByLong(key, 0);
	}

	public String getPageName() {
		if (this.pageName == null) {
			return "";
		}
		return this.pageName;
	}

	public boolean checkPageRequest(BasicDBObject oResult) {
		if (pageName.equalsIgnoreCase("mapdetail")) {
			if (!pageParams.containsField("group") || !pageParams.containsField("mapid")) {
				oResult.append("message", String.format("PageName=%s request parameter error", pageName));
				return false;
			}
		}
		return true;
	}

	public boolean authenticate(String pageName) {
		if (!module.isAuthPage(pageName) == false) {
			return true;
		}
		if (this.user.getUserId() == 0) {
			return false;
		}
		return true;
	}

	public String includeCss(String path, String name) {
		String id = name;
		if (id.endsWith(".css")) {
			id = id.substring(0, id.length() - 4);
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
		BasicDBObject oStyle = module.styleBy(id);
		if (Global.DevelopMode == false && oStyle != null) {
			buffer.append("/styles/" + path + "org.x." + oStyle.getString("dest") + "." + oStyle.getString("version") + ".css\"/>");
		} else {
			buffer.append("/css/" + path + id + ".css\"/>");
		}
		return buffer.toString();
	}

	public String includeJavaScript(String path, String name) {
		String id = name.toLowerCase();
		if (id.endsWith(".js")) {
			id = id.substring(0, id.length() - 3);
		}
		BasicDBObject oScript = module.scriptBy(id);
		double version = -1;
		if (Global.DevelopMode == false && oScript != null) {
			version = oScript.getDouble("version");
			id = oScript.getString("dest");
		}
		if (includeScripts.contains(id)) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("<script type=\"text/javascript\" ");
		if (version >= 0) {
			buffer.append("src=\"/scripts/" + path + (version == 0 ? id : "org.x." + id + "." + version) + ".js\">");
		} else {
			buffer.append("src=\"/js/" + path + id + ".js\">");
		}
		buffer.append("</script>");
		includeScripts.add(id);
		return buffer.toString();
	}

	public String include(String pageName) {
		Template t = module.getTemplate(pageName);
		if (t == null) {
			return pageName + " Template is null";
		}
		StringWriter writer = new StringWriter();
		try {
			t.merge(ctx, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.error("include", e);
			return "";
		}
	}

	public String includeTalkCommon(boolean canEdit, KeyValue... values) {
		Template t = module.getTemplate("talkcommon");
		if (t == null) {
			return "talkcommon Template is null";
		}
		StringWriter writer = new StringWriter();
		try {
			ctx.put("inplaceStyle", "");
			ctx.put("suffix", 0);
			for (int i = 0; i < values.length; i++) {
				KeyValue item = values[i];
				ctx.put(item.name, item.value);
			}
			ctx.put("canEdit", canEdit);
			t.merge(ctx, writer);
			String content = writer.toString();
			return content;
		} catch (Exception e) {
			logger.error("Context", e);
			return "";
		}
	}

	public String includeCall(BasicDBObject activity, int bizType, int catalog) {
		Template t = module.getTemplate("dlgcall");
		if (t == null) {
			return "dlgcall Template is null";
		}
		StringBuilder buffer = new StringBuilder();
		try {
			StringWriter writer = new StringWriter();
			ctx.put("activity", activity);
			ctx.put("bizType", bizType);
			ctx.put("catalog", catalog);
			t.merge(ctx, writer);
			buffer.append(writer.toString());
		} catch (Exception e) {
			logger.error("dlgcall", e);
		}
		return buffer.toString();
	}

	public String includeConsole(String pageName) {
		Template t = module.getTemplate("console");
		if (t == null) {
			return "console Template is null";
		}
		if (pageName.equalsIgnoreCase("car") && !user.getRole().isCarAssistant()) {
			return "";
		} else if (pageName.equalsIgnoreCase("room") && !user.getRole().isRoomAssistant()) {
			return "";
		} else if ((pageName.equalsIgnoreCase("guide") || (pageName.equalsIgnoreCase("trip"))) && !user.getRole().isTripAssistant()) {
			return "";
		}
		StringBuilder buffer = new StringBuilder();
		try {
			StringWriter writer = new StringWriter();
			ctx.put("pageName", pageName);
			t.merge(ctx, writer);
			buffer.append(writer.toString());
		} catch (Exception e) {
			logger.error("readConsole", e);
		}
		return buffer.toString();
	}

	public String includeHeader() {
		return includeHeader(null, null);
	}

	public String includeHeader(String pageName, String headerName) {
		Template t = module.getTemplate("header");
		if (t == null) {
			return "header Template is null";
		}
		StringBuilder buffer = new StringBuilder();
		try {
			StringWriter writer = new StringWriter();
			t.merge(ctx, writer);
			buffer.append(writer.toString());
		} catch (Exception e) {
			logger.error("readHeader", e);
		}
		buffer.append("<script>");
		buffer.append("function initHeader(){");
		if (pageName != null && headerName != null) {
			buffer.append("headerNames[\"" + pageName + "\"]=\"" + headerName + "\";");
		}
		buffer.append("return false;");
		buffer.append("}");
		buffer.append("</script>");
		return buffer.toString();
	}

	public String renderPage() {
		Template t = module.getTemplate(pageName);
		if (t == null) {
			return pageName + " Template is null";
		}
		StringBuilder buffer = new StringBuilder();
		try {
			StringWriter writer = new StringWriter();
			if (request != null) {
				ctx.put("request", request);
			}
			t.merge(ctx, writer);
			buffer.append(writer.toString());
		} catch (Exception e) {
			logger.error("renderPage", e);
		}
		return buffer.toString();
	}

	public String includeFooter() {
		return this.includeFooter(false);
	}

	public String includeFooter(boolean includeDlgPicture) {
		Template t = module.getTemplate("footer");
		if (t == null) {
			return "footer Template is null";
		}
		StringWriter writer = new StringWriter();
		try {
			ctx.put("includeDlgPicture", includeDlgPicture);
			t.merge(ctx, writer);
			return writer.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public String includeAdvertiseByName(String pageName, int count) {
		Template t = module.getTemplate(pageName);
		if (t == null) {
			return pageName + " Template is null";
		}
		StringWriter writer = new StringWriter();
		try {
			ctx.put("advertise_count", count);
			t.merge(ctx, writer);
			return writer.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public UserIntf getUser() {
		return user;
	}
}
