package org.z.core.common;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.common.cache.UserCache;
import org.z.common.htmlpage.User;
import org.z.common.joor.Reflect;
import org.z.core.module.ModuleRecommend;
import org.z.global.dict.Global;
import org.z.global.dict.Global.CodeType;
import org.z.global.environment.Const;
import org.z.global.object.DBListObject;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.DateUtil;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServiceName;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * 上下文
 */
public class Context extends org.z.common.htmlpage.Context {
	protected static final Logger logger = LoggerFactory.getLogger(Context.class);
	public static ConcurrentHashMap<String, DBListObject> guideNumByContinent = new ConcurrentHashMap<String, DBListObject>();
	public static ConcurrentHashMap<String, DBListObject> notices = new ConcurrentHashMap<String, DBListObject>();

	@Override
	public void initUser() {
		this.user = new User(this);
	}

	@Override
	public boolean initHtmlPage() {
		if (super.initHtmlPage() == false)
			return false;
		BasicDBObject oPage = module.pageBy(pageName);
		ctx.put("user", this.user);
		BasicDBList instances = (BasicDBList) oPage.get("instances");
		for (int i = 0; instances != null && i < instances.size(); i++) {
			BasicDBObject object = (BasicDBObject) instances.get(i);
			String name = object.getString("name");
			Object instance = null;
			if (object.containsField("module")) {
				String moduleName = object.getString("module");
				instance = ServiceFactory.moduleInstanceBy(moduleName);
			} else if (object.containsField("class")) {
				String className = object.getString("class");
				BasicDBList items = (BasicDBList) object.get("args");
				Object[] args = new Object[items == null ? 0 : items.size()];
				for (int t = 0; t < args.length; i++) {
					args[i] = items.get(t);
				}
				instance = Reflect.on(className).create(args).get();
			}
			if (instance == null) {
				continue;
			}
			ctx.put(name, instance);
		}
		return true;
	}

	@Override
	public String renderPage() {
		return super.renderPage();
	}

	public boolean isTopicAssistant() {
		if (user.getRole() == null) {
			return false;
		}
		return user.getRole().isTopicAssistant();
	}

	public boolean developMode() {
		return Global.DevelopMode;
	}

	public boolean authenticate(String pageName) {
		if (!module.isAuthPage(pageName) == false)
			return true;
		if (this.user.getUserId() == 0)
			return false;
		return true;
	}

	public String readHtmlHeader() {
		return this.include("htmlheader");
	}

	public String readUserTitle() {
		return this.include("usertitle");
	}

	public String readUserPanel() {
		return this.include("userpanel");
	}

	public String readHeader() {
		return readHeader(null, null, true);
	}

	public String readHeader(boolean enableBorderShadow) {
		return readHeader(null, null, enableBorderShadow);
	}

	public String readHeader(String pageName, String headerName, boolean enableBorderShadow) {
		Template t = this.module.getTemplate("header");
		if (t == null)
			return "header Template is null";
		StringBuilder buffer = new StringBuilder();
		try {
			StringWriter writer = new StringWriter();
			this.ctx.put("enableBorderShadow", enableBorderShadow);
			t.merge(this.ctx, writer);
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

	public String readFooter() {
		return this.readFooter(false);
	}

	public String readFooter(boolean includeDlgPicture) {
		Template t = this.module.getTemplate("footer");
		if (t == null)
			return "footer Template is null";
		StringWriter writer = new StringWriter();
		try {
			ctx.put("includeDlgPicture", includeDlgPicture);
			t.merge(ctx, writer);
			return writer.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public String readConsole(String pageName) {
		Template t = module.getTemplate("console");
		if (t == null)
			return "console Template is null";
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

	public String readCall(BasicDBObject activity, int bizType, int catalog) {
		Template t = module.getTemplate("dlgcall");
		if (t == null)
			return "dlgcall Template is null";
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

	public BasicDBObject readProfile(long userId) {
		return ((User) user).readProfile(String.valueOf(userId), true);
	}


	public BasicDBList readAdvertiseLinks(String site, String type, int count) {
		BasicDBList list = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select * from advertise where site=? and type=? order by rand() limit " + count, new String[] { site, type }, 0, 0);
		return list;
	}

	public String reqUserId() {
		String uId = this.getParam("u", "");
		if (StringUtil.isEmpty(uId) && (this.pageName.equalsIgnoreCase("i"))) {
			uId = this.getParam("id");
		}
		if (StringUtil.isEmpty(uId)) {
			uId = String.valueOf(this.user.getUserId());
		}
		return uId;
	}

	public BasicDBObject reqUserBy(String... fields) {
		String uId = reqUserId();
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		for (int i = 0; i < fields.length; i++) {
			sql.append(fields[i]);
			if (i < fields.length - 1) {
				sql.append(",");
			}
		}
		sql.append(" from user where user_id=?");
		BasicDBObject oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), new String[] { uId }, 0, 0);
		return oResult;
	}

	public BasicDBObject reqUserBy() {
		return reqUserBy("user_name", "user_id", "sex", "country", "city", "req_count", "topic_count", "service_count", "description");
	}

	public String readAdvertiseByName(String pageName) {
		return this.readAdvertiseByName(pageName, 10);
	}

	public String readAdvertiseByName(String pageName, int count) {
		Template t = this.module.getTemplate(pageName);
		if (t == null)
			return pageName + " Template is null";
		StringWriter writer = new StringWriter();
		try {
			ctx.put("advertise_count", count);
			t.merge(ctx, writer);
			return writer.toString();
		} catch (Exception e) {
			return "";
		}
	}

	public BasicDBList readRecommends(String city, int bizType, String position, int pageNumber, int pageSize) {
		BasicDBObject oResult = ServiceFactory.recommend().read(city, bizType, position, pageNumber, pageSize);
		return (BasicDBList) oResult.get("items");
	}

	public ModuleRecommend recommend() {
		return new ModuleRecommend();
	}

	public void createOrder(String out_trade_no, String subject, String body, String total_fee, String type, String time) {
		String sql = "insert into `order`(id,subject,body,fee,user_id,user_name,timestamp,licence_version,licence_time)values(?,?,?,?,?,?,?,?,?)";
		DataSet.update(
				Const.defaultMysqlServer,
				Const.defaultMysqlDB,
				sql,
				new String[] { out_trade_no, subject, body, total_fee, String.valueOf(user.getUserId()), user.getUserName(),
						String.valueOf(System.currentTimeMillis()), type, time });
	}


	public String readPerson(String id) {
		if (StringUtil.isEmpty(id)) {
			id = cookieId;
		}
		String sql = "select content from person where id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { id }, 0, 0);
		if (rows == null || rows.length == 0) {
			return "{}";
		}
		return rows[0][0];
	}

	public String readPersonCount() {
		String sql = "select count(*) from person ";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		return rows[0][0];
	}

	public BasicDBObject updatePerson(BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		String sql = "insert into person(id,page,content)values(?,?,?) ON DUPLICATE KEY UPDATE page=values(page),content=values(content)";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
				new String[] { oReq.getString("cookieId"), oReq.getString("pageName"), oReq.toString() });
		return oResult;
	}

	public boolean checkCaptcha(BasicDBObject oReq) {
		String c = UserCache.getCaptcha(cookieId);
		if (StringUtils.isEmpty(c) || !oReq.containsField("captcha") || !oReq.getString("captcha").equalsIgnoreCase(c)) {
			return false;
		}
		UserCache.setCaptcha("captcha", StringUtil.createPassword(8));
		return true;
	}

	public DBListObject guideNumByContinent(String continent) {
		DBListObject o = guideNumByContinent.get(continent);
		if (o != null && o.timestamp < (System.currentTimeMillis() + Const.Hour)) {
			return o;
		}
		o = new DBListObject();
		o.value = DataSet
				.queryDBObjects(
						Const.defaultMysqlServer,
						Const.defaultMysqlDB,
						"select country ,count(country) as total from activity_trip where country in(  select country from country_dict where continent=?) group by country order by total desc",
						new String[] { continent }, 0, 0);
		if (o.value == null) {
			o.value = new ArrayList<BasicDBObject>();
		}
		o.timestamp = System.currentTimeMillis() + Const.Hour;
		guideNumByContinent.put(continent, o);
		return o;
	}

	public BasicDBObject searchByUrl(int serviceIndex, BasicDBObject pageParams) {
		BasicDBObject req = new BasicDBObject();
		DBObjectUtil.copyBy(pageParams, "d", req, "date");
		DBObjectUtil.copyBy(pageParams, "p", req, "pageNumber");
		DBObjectUtil.copyBy(pageParams, "c", req, CodeType.country.name());
		DBObjectUtil.copyBy(pageParams, "ct", req, CodeType.contient.name());
		DBObjectUtil.copyBy(pageParams, "ln", req, CodeType.location.name());
		DBObjectUtil.copyBy(pageParams, "le", req, CodeType.language.name());
		DBObjectUtil.copyBy(pageParams, "pr", req, CodeType.priceRange.name());
		DBObjectUtil.copyBy(pageParams, "pt", req, CodeType.priceTag.name());
		DBObjectUtil.copyBy(pageParams, "st", req, CodeType.serviceTag.name());
		DBObjectUtil.copyBy(pageParams, "cy", req, CodeType.category.name());
		DBObjectUtil.copyBy(pageParams, "bc", req, CodeType.belongCity.name());
		req.putAll((BSONObject) pageParams);
		DBObjectUtil.deleteFields(pageParams, "d", "p", "c", "ct", "ln", "le", "pr", "pt", "st", "cy");
		return searchBy(serviceIndex, req);
	}

	public BasicDBObject searchBy(int serviceIndex, BasicDBObject req) {
		ServiceName service = ServiceName.values()[serviceIndex];
		switch (service) {
		default:
			return new BasicDBObject();
		}
	}

	public BasicDBList search(int serviceIndex, BasicDBObject req) {
		BasicDBObject oResult = this.searchBy(serviceIndex, req);
		if (oResult.containsField("items")) {
			BasicDBList items = (BasicDBList) oResult.get("items");
			return items;
		} else {
			return new BasicDBList();
		}
	}

	public DBListObject noticesBy(String position, int pageNum, int pageSize) {
		DBListObject o = notices.get(position + pageNum);
		if (o != null && o.timestamp >= (System.currentTimeMillis() + Const.Hour)) {
			return o;
		}
		pageNum--;
		pageNum = pageNum < 0 ? 0 : pageNum;
		o = new DBListObject();
		o.value = DataSet.queryDBObjects(Const.defaultMysqlServer, Const.defaultMysqlDB, "select timestamp,title,content from notice order by id desc", pageNum
				* pageSize, pageSize);
		if (o.value == null) {
			o.value = new ArrayList<BasicDBObject>();
		}
		for (int i = 0; i < o.value.size(); i++) {
			BasicDBObject oItem = o.value.get(i);
			oItem.append("timestamp", DateUtil.formatShortDate(DateUtil.convertDate(oItem.getLong("timestamp")), '/'));
			oItem.append("content", oItem.getString("content"));
		}
		notices.put(position + pageNum, o);
		return o;
	}
}
