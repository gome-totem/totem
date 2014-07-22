package org.z.core.common;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.BSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.common.cache.UserCache;
import org.z.core.module.ModuleAppoint;
import org.z.core.module.ModuleMail;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.IllegalType;
import org.z.global.dict.Global.RecordState;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.factory.SocketSession;
import org.z.global.object.RemoteResult;
import org.z.global.object.SecurityObject;
import org.z.global.object.UserRole;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.EmptyUtil;
import org.z.global.util.JodaUtil;
import org.z.global.util.NetTool;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeType;
import org.z.store.mongdb.DBFileSystem;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;
import org.z.store.mongdb.DataSet;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;

public class User extends org.z.common.htmlpage.User {
	protected static final Logger logger = LoggerFactory.getLogger(User.class);
	protected static Pattern patternUserName = Pattern.compile("[\u4E00-\u9FA5]+");
	protected static Pattern patternEmail = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	public String callServer = null;
	public String belongCity = null;
	public int userCredit = 0;
	public String userPeerId = null;
	public String lastUri = null;
	public String remoteAddr = null;
	public String validateCode = null;
	public BasicDBObject oUser = null;
	public String peerApiKey = null;
	public String peerSessionKey = null;
	public int bindSina = 0;
	public int bindQQ = 0;
	public int bindWeixin = 0;
	public int bindMail = 0;
	public int bindMobile = 0;
	public int serviceCount = 0;
	public int tripCount = 0;
	public int reqCount = 0;
	public int topicCount = 0;
	public String balanceId = null;
	public long licence = 0;
	public String socketService = "{}";
	public String quickLogin;

	public User(Context ctx) {
		super(ctx);
		init();
	}

	private void init() {
		if (ctx == null)
			return;
		try {
			if (ctx.request.containsField("user")) {
				oUser = (BasicDBObject) ctx.request.get("user");
				ctx.request.removeField("user");
			}
			if ((oUser == null || oUser.size() == 0) && StringUtil.isEmpty(this.ctx.cookieId) == false) {
				String c = RedisPool.use().get(this.ctx.cookieId);
				if (StringUtil.isEmpty(c) == false) {
					oUser = (BasicDBObject) JSON.parse(c);
				}
			}
			if (oUser != null) {
				userEmail = oUser.containsField("email") ? oUser.getString("email") : null;
				userName = oUser.containsField("userName") ? oUser.getString("userName") : null;
				userId = oUser.containsField("userId") ? oUser.getLong("userId") : 0;
				userCredit = oUser.containsField("userCredit") ? oUser.getInt("userCredit") : 0;
				belongCity = oUser.containsField("belongCity") ? oUser.getString("belongCity") : null;
				lastUri = oUser.containsField("lastUri") ? oUser.getString("lastUri") : null;
				remoteAddr = oUser.containsField("remoteAddr") ? oUser.getString("remoteAddr") : null;
				validateCode = oUser.containsField("validateCode") ? oUser.getString("validateCode") : null;
				bindSina = oUser.containsField("bindSina") ? oUser.getInt("bindSina") : 0;
				bindQQ = oUser.containsField("bindQQ") ? oUser.getInt("bindQQ") : 0;
				bindWeixin = oUser.containsField("bindWeixin") ? oUser.getInt("bindWeixin") : 0;
				bindMail = oUser.containsField("bindMail") ? oUser.getInt("bindMail") : 0;
				bindMobile = oUser.containsField("bindMobile") ? oUser.getInt("bindMobile") : 0;
				server = oUser.containsField("server") ? oUser.getString("server") : null;
				callServer = oUser.containsField("callServer") ? oUser.getString("callServer") : null;
				serviceCount = oUser.containsField("serviceCount") ? oUser.getInt("serviceCount") : 0;
				reqCount = oUser.containsField("reqCount") ? oUser.getInt("reqCount") : 0;
				tripCount = oUser.containsField("tripCount") ? oUser.getInt("tripCount") : 0;
				topicCount = oUser.containsField("topicCount") ? oUser.getInt("topicCount") : 0;
				balanceId = oUser.containsField("balanceId") ? oUser.getString("balanceId") : null;
				licence = oUser.containsField("licence") ? oUser.getLong("licence") : 0;
				socketService = oUser.containsField("socketService") ? oUser.getString("socketService") : "{}";
				String r = oUser.getString("role");
				if (StringUtils.isNumeric(r)) {
					role = oUser.containsField("role") ? new UserRole(Long.parseLong(r)) : new UserRole();
				}
				quickLogin = oUser.getString("quickLogin", null);
			}
		} catch (Exception e) {
			logger.error("init", e);
		}
		if (role == null) {
			role = new UserRole();
		}
	}

	@Override
	public String getSocketService() {
		return this.socketService;
	}

	@Override
	public void setSocketService(String value) {
		this.socketService = value;
		this.updateUserCache();
	}

	@Override
	public void setBindMobile(int value) {
		this.bindMobile = value;
	}

	@Override
	public void setLicence(long value) {
		this.licence = value;
	}

	@Override
	public String getCallServer() {
		return this.callServer;
	}

	@Override
	public String getPeerApiKey() {
		return this.peerApiKey;
	}

	@Override
	public String getPeerSessionKey() {
		return this.peerSessionKey;
	}

	@Override
	public String getRemoteAddr() {
		return this.remoteAddr;
	}

	@Override
	public boolean isOwner() {
		String value = this.ctx.getParam("u");
		if (StringUtil.isEmpty(value)) {
			value = this.ctx.getParam("id");
		}
		return (StringUtil.isEmpty(value)) || (value.equalsIgnoreCase(String.valueOf(userId)));
	}

	public boolean completeProfile() {
		BasicDBObject profile = this.readProfile(String.valueOf(this.userId), false);
		if (EmptyUtil.isEmpty(profile) || EmptyUtil.isEmpty(profile.getString("country")) || EmptyUtil.isEmpty(profile.getString("city")) || EmptyUtil.isEmpty(profile.getString("age_drive"))
				|| profile.getInt("height", -1) == -1 || profile.getInt("sex", -1) == -1 || EmptyUtil.isEmpty(profile.getString("birthdayString")) || profile.getInt("build", -1) == -1
				|| profile.getInt("blood", -1) == -1 || EmptyUtil.isEmpty(profile.getString("user_name")) || EmptyUtil.isEmpty(profile.getString("msn")) || EmptyUtil.isEmpty(profile.getString("qq"))
				|| EmptyUtil.isEmpty(profile.getString("mobile"))) {
			return false;
		}
		return true;
	}

	@Override
	public String getServerId() {
		return this.userId + "-" + this.server.replaceAll("x", "");
	}

	@Override
	public UserRole getRole() {
		return this.role;
	}

	@Override
	public int getUserCredit() {
		return this.userCredit;
	}

	@Override
	public long getLicence() {
		return this.licence;
	}

	@Override
	public boolean isLogined() {
		return this.userId != 0;
	}

	@Override
	public int getBindSina() {
		return this.bindSina;
	}

	@Override
	public int getBindQQ() {
		return this.bindQQ;
	}

	@Override
	public int getBindWeixin() {
		return this.bindWeixin;
	}

	@Override
	public int getBindMail() {
		return this.bindMail;
	}

	@Override
	public int getBindMobile() {
		return this.bindMobile;
	}

	@Override
	public String getBalanceId() {
		return this.balanceId;
	}

	@Override
	public String getLastUri() {
		return this.lastUri;
	}

	@Override
	public String getBelongCity() {
		return this.belongCity;
	}

	@Override
	public int getServiceCount() {
		return this.serviceCount;
	}

	@Override
	public int getTripCount() {
		return this.tripCount;
	}

	@Override
	public int getReqCount() {
		return this.reqCount;
	}

	@Override
	public int getTopicCount() {
		return this.topicCount;
	}

	@Override
	public boolean licenceIsValid() {
		if (this.userId == 0)
			return false;
		return this.licence >= System.currentTimeMillis();
	}

	@Override
	public int readCheckAppointCount() {
		String sql = "select count(*) from appoint where state=0";
		return Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0)[0][0]);
	}

	@Override
	public BasicDBObject readFollowCount() {
		BasicDBObject oResult = new BasicDBObject().append("followCount", 0).append("beFollowCount", 0);
		if (this.userId == 0) {
			return oResult;
		}
		FollowRecords records = new FollowRecords(this.server, this.userId, this.userName);
		BasicDBObject oMatch = records.readCount();
		if (oMatch == null) {
			return oResult;
		}
		return oMatch;
	}

	@Override
	public String readFollowCountBy(String name) {
		return this.readFollowCount().getString(name);
	}

	@Override
	public String readPhoneAccount() {
		BasicDBObject oAccount = readAccountMoney(String.valueOf(userId));
		if (oAccount == null || !oAccount.containsField("money")) {
			return "0";
		}
		double m = Double.parseDouble(oAccount.getString("money"));
		DecimalFormat f = new DecimalFormat("#,##0.00");
		return f.format(m);
	}

	@Override
	public int readPhoneAccountByInt() {
		if (this.userId == 0)
			return 0;
		return this.readPhoneAccountByInt(this.userId);
	}

	@Override
	public int readPhoneAccountByInt(long id) {
		BasicDBObject oAccount = readAccountMoney(String.valueOf(id));
		if (oAccount == null || !oAccount.containsField("money")) {
			return 0;
		}
		float m = Float.parseFloat(oAccount.getString("money"));
		return Math.round(m);
	}

	@Override
	public void addUserCredit(IllegalType type, String message) {
		DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into user_credit(user_id,type,message)values(?,?,?)",
				new String[] { String.valueOf(userId), String.valueOf(type.ordinal()), message });
	}

	@Override
	public void updateLastUri(String uri) {
		try {
			if (oUser == null) {
				String userContent = RedisPool.use().get(this.ctx.cookieId);
				if (!StringUtils.isEmpty(userContent)) {
					oUser = (BasicDBObject) JSON.parse(userContent);
				}
			}
			if (oUser == null) {
				oUser = new BasicDBObject();
			}
			oUser.append("lastUri", uri);
			RedisPool.use().set(this.ctx.cookieId, oUser.toString());
		} catch (Exception e) {
			logger.error("updateLastUri", e);
		}
	}

	@Override
	public RemoteResult existUser(String email) {
		RemoteResult result = new RemoteResult();
		String email_ = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Do, email))[0];
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(user_id) from user where email=?", new String[] { email_ }, 0, 0);
		if (Integer.parseInt(rows[0][0]) >= 1) {
			result.setState(false);
			result.setMessages(new String[] { "-1" });
			return result;
		}
		result.setState(true);
		return result;
	}

	@Override
	public BasicDBObject forgetPassword(BasicDBObject oReq) {
		String c = UserCache.getCaptcha(ctx.cookieId);
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		if (StringUtils.isEmpty(c) || !c.equalsIgnoreCase(oReq.getString("code"))) {
			return result.append("element", "#forget_verif_code").append("message", "验证码不正确");
		}
		String email = oReq.getString("email");
		String email_ = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, Global.SecurityMode.Do, email))[0];
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select password,user_name from user where email=?", new String[] { email_ }, 0, 0);
		if (rows == null || rows.length == 0) {
			return result.append("element", "#account").append("message", "账号不存在");
		}
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][0]));
		if (values == null || values.length == 0) {
			return result.append("element", "#account").append("message", "账号不存在");
		}
		ModuleMail mail = (ModuleMail) ModuleFactory.moduleInstanceBy("mail");
		String content = mail.createProfileDoc(rows[0][1], email, values[0]);
		mail.sendMail(email, "yiqihi找回密码", content);
		return result.append("xeach", true).append("email", email).append("mailUrl", "http://mail." + email.substring(email.indexOf("@") + 1));
	}

	@Override
	public BasicDBObject bindAccount(BasicDBObject oReq) {
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, oReq.getString("email")),
				SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Do, oReq.getString("password")));
		if (values == null) {
			return result.append("error", -50);
		}
		String email = values[0];
		String password = values[1];
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(user_id) from user where user_name=?", new String[] { oReq.getString("userName") }, 0, 0);
		if (Integer.parseInt(rows[0][0]) >= 1) {
			return result.append("error", -70);
		}
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id from user where email=?", new String[] { email }, 0, 0);
		if ((rows.length >= 1) && (Integer.parseInt(rows[0][0]) != ctx.user.getUserId())) {
			return result.append("error", -60);
		}
		String code = UserCache.getCaptcha(ctx.cookieId);
		if (StringUtils.isEmpty(code) || !oReq.containsField("code") || !oReq.getString("code").equalsIgnoreCase(code)) {
			return result.append("message", "验证码输入错误").append("error", -1);
		}
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set user_type=?,password=?,email=?,user_name=? where user_id=?",
				new String[] { String.valueOf(oReq.getInt("userType")), password, email, oReq.getString("userName"), String.valueOf(userId) });
		this.userName = oReq.getString("userName");
		return this.login(oReq);
	}

	@Override
	public BasicDBObject readLoginAccount(String userId) {
		BasicDBObject oResult = new BasicDBObject();
		if (this.role.isRoot() == false) {
			return oResult;
		}
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select email,password from user where user_id=?", new String[] { userId }, 0, 0);
		if (rows == null || rows.length == 0) {
			return oResult;
		}
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][0]),
				SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][1]));
		if (values == null) {
			return oResult;
		}
		oResult.append("email", values[0]);
		oResult.append("password", values[1]);
		return oResult;
	}

	@Override
	public BasicDBObject register( String password, String email, String code, boolean autoLogin) {
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		String c = UserCache.getCaptcha(ctx.cookieId);
		email = StringUtil.trim(email).toLowerCase();
		if (StringUtils.isBlank(email) || patternEmail.matcher(email).matches() == false) {
			return result.append("error", "邮件地址非法").append("element", "#regEmail");
		}
		password = StringUtil.trim(password);
		if (StringUtils.isBlank(password)) {
			return result.append("error", "密码不能包含空格").append("element", "#regPassword");
		}
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email),
		SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Do, password));
		if (values == null) {
			return result.append("error", "服务器故障.").append("element", "#regPassword");
		}
		if (autoLogin && (StringUtils.isEmpty(c) || !c.equalsIgnoreCase(code))) {
			return result.append("error", "验证码输入错误").append("element", "#regVerifCode");
		}
		String[][]  rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(user_id) from user where email=?", new String[] { values[0] }, 0, 0);
		if (Integer.parseInt(rows[0][0]) >= 1) {
			return result.append("error", "邮件地址已经存在").append("element", "#regEmail");
		}
		if (role == null) {
			role = new UserRole();
		}
		result.append("userType", Global.UserType.Register.ordinal());
		result.append("email", email);
		result.append("password", password);
		result.append("code", code);
		userId = DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into user(password,email,role,user_type,sms_count)values(?,?,?,?,5)", new String[] { 
				values[1], values[0], role.toString(), String.valueOf(Global.UserType.Register.ordinal()) });
		BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, String.valueOf(userId));
		server = rec.getString("id");
		rec = ServerDict.self.serverListener.hashBy(NodeType.call, String.valueOf(userId));
		if (rec == null) {
			callServer = "x3";
		} else {
			callServer = rec.getString("id");
		}
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set server=?,call_server=? where user_id=?", new String[] { server, callServer, String.valueOf(userId) });
		initBill();
		sendRegisterMail(email, userName, password);
		if (autoLogin) {
			return this.login(result);
		}
		return result;
	}

	private void sendRegisterMail(String email, String userName, String password) {
		String html = ServiceFactory.mail().createRegisterDoc(userName, email, password);
		ServiceFactory.mail().sendMail(email, "欢迎注册一起嗨", html);
	}

	@Override
	public BasicDBObject registerBy(String authMode, String site, String peerId, String name, String city, int sex, String face, String authToken, String authSecret) {
		BasicDBObject result = new BasicDBObject().append("xeach", true);
		ArrayList<String> fieldNames = new ArrayList<String>();
		ArrayList<String> fieldValues = new ArrayList<String>();
		if (authMode == null) {
			String userPeerId = site + "@" + peerId;
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
					"select user_id,email,password,credit,role,city,server,service_count,balance_id,cookie_id,licence,call_server,topic_count,req_count from user where user_peer_id=?",
					new String[] { userPeerId }, 0, 0);
			if (role == null) {
				role = new UserRole();
			}
			userEmail = null;
			if (rows == null || rows.length == 0) {
				this.userId = DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into user" + "(user_name,user_peer_id,role,city,sex,user_face,bind_" + site
						+ ",cookie_id,cookie_addr)" + "values(?,?,b?,?,?,?,1,?,?)", new String[] { name, userPeerId, role.toString(), city, String.valueOf(sex), face, ctx.cookieId, ctx.ip });
				BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, String.valueOf(userId));
				this.server = rec.getString("id");
				fieldNames.add("server");
				fieldValues.add(this.server);

				rec = ServerDict.self.serverListener.hashBy(NodeType.call, String.valueOf(userId));
				this.callServer = rec.getString("id");
				fieldNames.add("call_server");
				fieldValues.add(this.callServer);

				userName = name;
				this.belongCity = city;
				userName = name;
				/*
				 * 初始化用户环境 try { (new MatrixTable(Const.defaultMongoServer,
				 * Const.defaultMongoDB, userId, userName)) .createGroup("all");
				 * } catch (Exception e) { }
				 */
			} else {
				if (StringUtil.isEmpty(rows[0][6])) {
					BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, String.valueOf(userId));
					this.server = rec.getString("id");
					fieldNames.add("server");
					fieldValues.add(this.server);

					rec = ServerDict.self.serverListener.hashBy(NodeType.call, String.valueOf(userId));
					this.callServer = rec.getString("id");
					fieldNames.add("call_server");
					fieldValues.add(this.callServer);

				} else {
					this.server = rows[0][6];
					this.callServer = rows[0][11];
				}
				userId = Long.parseLong(rows[0][0]);
				this.deleteOldCookie(rows[0][9]);
				userEmail = rows[0][1];
				this.userCredit = Integer.parseInt(StringUtils.isEmpty(rows[0][3]) ? "0" : rows[0][3]);
				this.role = new UserRole(Long.parseLong(rows[0][4]));
				this.belongCity = rows[0][5];
				this.serviceCount = Integer.parseInt(rows[0][7]);
				this.balanceId = rows[0][8];
				this.licence = Long.parseLong(rows[0][10]);
				this.topicCount = Integer.parseInt(rows[0][12]);
				this.reqCount = Integer.parseInt(rows[0][13]);
			}
			userName = name;
			result.append("userId", userId).append("userName", userName);
			if (!StringUtil.isEmpty(userEmail)) {
				result.append("userEmail", userEmail);
			}
		} else if (authMode.equalsIgnoreCase("active")) {
			fieldNames.add("bind_" + site);
			fieldValues.add("1");
		}
		if (site.equalsIgnoreCase("sina")) {
			this.bindSina = 1;
		} else if (site.equalsIgnoreCase("qq")) {
			this.bindQQ = 1;
		} else if (site.equalsIgnoreCase("weixin")) {
			this.bindWeixin = 1;
		}
		fieldNames.add("cookie_id");
		fieldValues.add(ctx.cookieId);
		fieldNames.add("cookie_addr");
		fieldValues.add(ctx.ip);
		if (fieldNames.size() > 0) {
			StringBuilder sql = new StringBuilder();
			sql.append("update user set ");
			for (int i = 0; i < fieldNames.size(); i++) {
				sql.append(fieldNames.get(i) + "=?");
				if (i < fieldNames.size() - 1) {
					sql.append(",");
				}
			}
			sql.append(" where user_id=?");
			fieldValues.add(String.valueOf(userId));
			String[] values = new String[fieldValues.size()];
			fieldValues.toArray(values);
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), values);
		}
		this.initBill();
		this.updateUserCache();
		String f1 = site + "_key";
		String f2 = site + "_secret";
		String sql = "insert into user_bind(user_id," + f1 + "," + f2 + ")values(?,?,?) ON DUPLICATE KEY UPDATE " + f1 + "=values(" + f1 + ")," + f2 + "=values(" + f2 + ")";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(userId), authToken, authSecret });
		return result.append("xeach", true);
	}

	@Override
	public void updateServiceCount(int serviceCount) {
		String sql = "update user set service_count=service_count+?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(serviceCount), String.valueOf(userId) });
		this.serviceCount += serviceCount;
		this.updateUserCache();
	}

	@Override
	public void updateRequireCount(int reqCount) {
		String sql = "update user set req_count=req_count+?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(reqCount), String.valueOf(userId) });
		this.reqCount += reqCount;
		this.updateUserCache();
	}

	@Override
	public void updateTopicCount(int topicCount) {
		String sql = "update user set topic_count=topic_count+?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(topicCount), String.valueOf(userId) });
		this.topicCount += topicCount;
		this.updateUserCache();
	}

	@Override
	public void updateTripCount(int tripCount) {
		String sql = "update user set trip_count=trip_count+?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(tripCount), String.valueOf(userId) });
		this.tripCount += tripCount;
		this.updateUserCache();
	}

	@Override
	public void initBill() {
		BasicDBObject oField = new BasicDBObject().append("balance", 0).append("income", 0).append("withdraw", 0).append("timestamp", System.currentTimeMillis()).append("userId", this.userId)
				.append("userName", this.userName);
		String balanceId = DataCollection.insert(this.server, "finance", "bill_" + this.userId, oField).toString();
		if (StringUtil.isEmpty(balanceId))
			return;
		DataCollection.createIndex(server, "finance", "bill_" + this.userId, "keyIdx", new BasicDBObject().append("shortId", 1), false);
		String sql = "update user set balance_id=?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { balanceId, String.valueOf(userId) });
		this.balanceId = balanceId;
	}

	public void updateUserFace(String url) {
		if (StringUtils.isEmpty(url))
			return;
		DBFileSystem fs = new DBFileSystem(Const.defaultMongoServer, "profile");
		byte[] bytes = NetTool.downloadBytes(url);
		if (bytes == null)
			return;
//		ArrayList<MagickImage> destroies = new ArrayList<MagickImage>();
		try {
//			MagickImage sImage = ImageUtil.readMagickImage(bytes, destroies);
//			String contentType = "image/jpg";
//			sImage = ImageUtil.resize(ImageType.Face, sImage, destroies);
//			BasicDBObject meta = new BasicDBObject().append("userId", userId).append("last_modified", System.currentTimeMillis());
//			Dimension dim = sImage.getDimension();
//			meta.append("width", dim.width);
//			meta.append("height", dim.height);
			String fileName = "face_" + userId;
			fs.removeFile(fileName);
//			fs.saveFile(bytes, fileName, contentType, meta);
		} catch (Exception e) {
		} finally {
//			ImageUtil.free(destroies);
		}
	}

	@Override
	public BasicDBObject login(BasicDBObject oUser) {
		BasicDBObject result = new BasicDBObject().append("xeach", false).append("error", 0).append("message", "");
		int userType = oUser.getInt("userType");
		String code = UserCache.getCaptcha(ctx.cookieId);
		if (oUser.getBoolean("checkedCode", true) && (StringUtils.isEmpty(code) || !oUser.containsField("code") || !oUser.getString("code").equalsIgnoreCase(code))) {
			result.append("error", "验证码输入错误").append("element", "#verif_code");
			return result;
		}
		switch (userType) {
		case 0:
		case 1:
			String email = StringUtil.trim(oUser.getString("email")).toLowerCase();
			String password = oUser.getString("password");
			String email_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email))[0];
			String[][] rows = null;
			if (StringUtils.isNumeric(email)) {
				rows = DataSet
						.query(Const.defaultMysqlServer,
								Const.defaultMysqlDB,
								"select password,user_name,role,user_id,credit,cookie_id,state,city,server,bind_sina,bind_qq,bind_weixin,bind_mail,bind_mobile,service_count,balance_id,licence,call_server,topic_count,req_count,quicklogin from user where mobile=?",
								new String[] { email_ }, 0, 0);
			} else {
				rows = DataSet
						.query(Const.defaultMysqlServer,
								Const.defaultMysqlDB,
								"select password,user_name,role,user_id,credit,cookie_id,state,city,server,bind_sina,bind_qq,bind_weixin,bind_mail,bind_mobile,service_count,balance_id,licence,call_server,topic_count,req_count,quicklogin from user where email=?",
								new String[] { email_ }, 0, 0);
			}

			if (rows == null || rows.length == 0) {
				return result.append("error", "用户账号不存在.").append("element", "#username");
			}
			String password_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][0]))[0];
			if (!password_.equals(password)) {
				logger.error("UserName=" + rows[0][1] + " 密码输入错误.");
				return result.append("error", "密码输入错误").append("element", "#password");
			}
			if (Integer.parseInt(rows[0][6]) == RecordState.Delete.ordinal()) {
				return result.append("error", "您的账号被管理员禁止,请联系我们。").append("element", "#username");
			}
			this.userId = Integer.parseInt(rows[0][3]);
			deleteOldCookie(rows[0][5]);
			this.userCredit = Integer.parseInt(StringUtils.isEmpty(rows[0][4]) ? "0" : rows[0][4]);
			this.role = new UserRole(Long.parseLong(rows[0][2]));
			this.userEmail = email;
			this.userName = rows[0][1];
			this.belongCity = rows[0][7];
			this.server = rows[0][8];
			this.bindSina = Integer.parseInt(rows[0][9]);
			this.bindQQ = Integer.parseInt(rows[0][10]);
			this.bindWeixin = Integer.parseInt(rows[0][11]);
			this.bindMail = Integer.parseInt(rows[0][12]);
			this.bindMobile = Integer.parseInt(rows[0][13]);
			this.serviceCount = Integer.parseInt(rows[0][14]);
			this.balanceId = rows[0][15];
			this.licence = Long.parseLong(rows[0][16]);
			this.callServer = rows[0][17];
			this.topicCount = Integer.parseInt(rows[0][18]);
			this.reqCount = Integer.parseInt(rows[0][19]);
			this.quickLogin = rows[0][20];
			break;
		}
		new FollowRecords(this.server, this.userId, this.userName).check();
		updateUserCache();
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set cookie_id=?,cookie_addr=? where user_id=?",
				new String[] { this.ctx.cookieId, this.ctx.ip, String.valueOf(this.userId) });
		logger.info("CookieId=" + this.ctx.cookieId + "&City=" + ctx.cityName + "&UserId=" + this.userId + "&UserName=" + this.userName + " Login");
		if (ctx.browserName.equalsIgnoreCase("mobile")) {
			return result.append("xeach", true).append("userid", this.userId).append("username", this.userName).append("userRole", this.role.value).append("server", this.server)
					.append("callServer", this.callServer).append("serverIP", ServerDict.self.serverIPBy(this.server)).append("callServerIP", ServerDict.self.serverIPBy(this.callServer));
		} else if (this.lastUri != null) {
			return result.append("xeach", true).append("lastUri", lastUri).append("userid", this.userId).append("username", this.userName);
		} else {
			return result.append("xeach", true).append("lastUri", "/messages").append("userid", this.userId).append("username", this.userName);
		}
	}

	@Override
	public void deleteOldCookie(String id) {
		/*
		 * 保证只有一个用户Id登录 if (StringUtil.isEmpty(id)) return; if (this.userId <=
		 * 10) return; try { MemCachedClient mc = new MemCachedClient();
		 * mc.delete(id); } catch (Exception e) {
		 * logger.error("deleteOldCookie", e); }
		 */
	}

	@Override
	public int readBalance() {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select balance,licence from user where user_id=?", new String[] { String.valueOf(userId) }, 0, 0);
		return Integer.parseInt(rows[0][0]);
	}

	@Override
	public BasicDBObject readAccountMoney(String userId) {
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("money", 0);
		oResult.append("feerate", 0);
		return oResult;
	}

	@Override
	public void updateUserCache() {
		if (ctx == null)
			return;
		if (StringUtil.isEmpty(ctx.cookieId))
			return;
		try {
			String userContent = RedisPool.use().get(ctx.cookieId);
			boolean isExist = true;
			if (userContent == null) {
				oUser = new BasicDBObject();
				isExist = false;
			} else {
				oUser = (BasicDBObject) JSON.parse(userContent);
			}
			oUser.append("loginTime", System.currentTimeMillis());
			oUser.append("remoteAddr", this.ctx.ip);
			oUser.append("email", this.userEmail);
			oUser.append("userId", this.userId);
			oUser.append("userCredit", this.userCredit);
			oUser.append("userName", this.userName);
			oUser.append("belongCity", this.belongCity);
			oUser.append("role", this.role.toString());
			oUser.append("lastUri", null);
			oUser.append("server", this.server);
			oUser.append("bindSina", this.bindSina);
			oUser.append("bindQQ", this.bindQQ);
			oUser.append("bindWeixin", this.bindWeixin);
			oUser.append("bindMail", this.bindMail);
			oUser.append("bindMobile", this.bindMobile);
			oUser.append("serviceCount", this.serviceCount);
			oUser.append("reqCount", this.reqCount);
			oUser.append("topicCount", this.topicCount);
			oUser.append("tripCount", this.tripCount);
			oUser.append("balanceId", this.balanceId);
			oUser.append("licence", this.licence);
			oUser.append("callServer", this.callServer);
			oUser.append("socketService", socketService);
			userContent = oUser.toString();
			RedisPool.use().set(ctx.cookieId, userContent);
			if (logger.isDebugEnabled()) {
				logger.debug("Login cookieId={}&CacheExist={}&UserObject={}", new Object[] { this.ctx.cookieId, isExist, userContent });
			}
		} catch (Exception e) {
			logger.error("updateUserCache", e);
		}
	}

	@Override
	public BasicDBObject logout() {
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		try {
			RedisPool.use().delete(this.ctx.cookieId);
			result.append("xeach", true);
		} catch (Exception e) {
			logger.error("logout", e);
		}
		return result;
	}

	@Override
	public BasicDBObject changeEmail(String oldPassword, String newEmail) {
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select password,email from user where user_id=?", new String[] { String.valueOf(this.userId) }, 0, 0);
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][0]),
				SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, newEmail));
		if (!values[0].equals(oldPassword)) {
			return result.append("message", "密码错误");
		}
		String email = values[1];
		if (!StringUtils.isEmpty(email) && !rows[0][1].equalsIgnoreCase(email)) {
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from user where email=? and user_id!=?", new String[] { email, String.valueOf(this.userId) }, 0, 0);
			if (Integer.parseInt(rows[0][0]) >= 1) {
				return result.append("message", "抱歉该邮件地址已经被使用，\r\n如果他确实是您的邮件地址，请联系我们。");
			}
		}
		result.append("xeach", DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set email=?,bind_mail=0 where user_id=?", new String[] { email, String.valueOf(userId) }));
		return result;
	}

	@Override
	public BasicDBObject changePassword(String oldPassword, String newPassword) {
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select password,email from user where user_id=?", new String[] { String.valueOf(this.userId) }, 0, 0);
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][0]),
				SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Do, newPassword));
		if (!values[0].equals(oldPassword)) {
			return result.append("message", "密码错误");
		}
		newPassword = values[1];
		result.append("xeach", DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set password=? where user_id=?", new String[] { newPassword, String.valueOf(this.userId) }));
		return result.append("xeach", true);
	}

	@Override
	public BasicDBObject getGroups() {
		BasicDBObject result = new BasicDBObject().append("xeach", true);
		FollowRecords table = new FollowRecords(Const.defaultMongoServer, this.userId, this.userName);
		BasicDBList list = table.getGroups();
		if (list == null) {
			result.append("groups", new String[] {});
		} else {
			result.append("groups", list);
		}
		return result;
	}

	@SuppressWarnings("null")
	@Override
	public BasicDBObject readProfile(String id, boolean includeDesc) {
		if (StringUtils.isEmpty(id)) {
			id = String.valueOf(this.userId);
		}
		if (id.equals("0")) {
			return new BasicDBObject().append("bind_mobile", 0).append("mobile", "");
		}
		StringBuilder sql = new StringBuilder();
		sql.append("select user_name,name");
		boolean includeContact = id.equalsIgnoreCase(String.valueOf(this.userId)) || this.role.isRoot();
		if (includeContact) {
			sql.append(",mobile,qq,msn,email");
		}
		sql.append(",identity,user_type,sex,birthday,city,country,age_drive,credit,height,blood,build,marriage,bind_mail,bind_mobile,picture");
		if (includeDesc) {
			sql.append(",description");
		}
		sql.append(" from user where user_id=?");
		BasicDBObject result = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), new String[] { String.valueOf(id) }, 0, 0);
		if (result != null) {
			String[] values = null;
			if (includeContact) {
				values =null;// ServiceFactory.appDict().decode(result.getString("name"), result.getString("mobile"), result.getString("qq"), result.getString("msn"), result.getString("email"));
				result.append("name", values[0]);
				result.append("mobile", values[1]);
				result.append("qq", values[2]);
				result.append("msn", values[3]);
				result.append("email", values[4]);
			}
			BasicDBObject oPicture = null;
			if (StringUtil.isEmpty(result.getString("picture"))) {
				oPicture = new BasicDBObject().append("person", "").append("guider", "").append("driver", "");
			} else {
				oPicture = (BasicDBObject) JSON.parse(result.getString("picture"));
				DBObjectUtil.fillZero(oPicture, "person", "guider", "driver");
			}
			result.append("picture", oPicture);
			long birthday = result.getLong("birthday");
			if (birthday != 0) {
				result.append("birthdayString", JodaUtil.toString(new DateTime(birthday), "yyyy-MM-dd"));
			}
		}
		return result;
	}

	@SuppressWarnings({ "unused", "null" })
	@Override
	public BasicDBObject updateProfile(BasicDBObject oReq) {
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		BasicDBList roles = (BasicDBList) oReq.get("roles");
		for (int i = 0; roles != null && i < roles.size(); i++) {
			Global.Role r = Global.Role.valueOf(roles.get(i).toString());
			if (r.ordinal() > 7)
				continue;
			this.role.set(r);
		}
		if (oReq.getString("qq").length() > 0 && !StringUtils.isNumeric(oReq.getString("qq"))) {
			return result.append("message", "qq必须是全数字");
		}
		String name = oReq.getString("name");
		String[] values = null;//ServiceFactory.appDict().encode(name, oReq.getString("mobile"), oReq.getString("qq"), oReq.getString("msn"));
		String sql = "update user set name=?,mobile=?,qq=?,msn=?,city=?,sex=?,birthday=?,role=?,country=?,age_drive=?,height=?,build=?,blood=?,marriage=?,description=? where user_id=?";
		result.append(
				"xeach",
				DataSet.update(
						Const.defaultMysqlServer,
						Const.defaultMysqlDB,
						sql,
						new String[] { values[0], values[1], values[2], values[3], oReq.getString("city"), oReq.getString("sex"), oReq.getString("birthday"), this.role.toString(),
								oReq.getString("country"), oReq.getString("driveAge"), oReq.getString("height"), oReq.getString("build"), oReq.getString("blood"), oReq.getString("marriage"),
								oReq.getString("description"), String.valueOf(this.userId) }));
		updateUserCache();
		return result;
	}

	@Override
	public boolean checkFollow(String id) {
		return checkFollow(Long.parseLong(id));
	}

	@Override
	public boolean checkFollow(long id) {
		if (userId == 0) {
			return false;
		}
		FollowRecords table = new FollowRecords(server, this.userId, this.userName);
		return table.followExist(id);
	}

	@Override
	public BasicDBObject checkEmailService() {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select expire_email from user where user_id=?", new String[] { String.valueOf(this.userId) }, 0, 0);
		if (rows == null || rows.length == 0) {
			return oResult.append("message", "user not exist.");
		}
		long value = Long.parseLong(rows[0][0]);
		if (value == 0) {
			return oResult.append("message", "closed");
		}
		if (value <= System.currentTimeMillis()) {
			return oResult.append("message", "expired");
		}
		return oResult.append("xeach", true);

	}

	@Override
	public BasicDBObject readBy(String... fieldNames) {
		StringBuilder sql = new StringBuilder();
		for (int i = 0; i < fieldNames.length; i++) {
			if (sql.length() > 0)
				sql.append(",");
			sql.append(fieldNames[i]);
		}
		BasicDBObject oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, "select " + sql.toString() + " from user where user_id=?",
				new String[] { String.valueOf(this.userId) }, 0, 0);
		return oResult;
	}

	@Override
	public BasicDBObject readContact() {
		return readContact(this.userId);
	}

	@SuppressWarnings("null")
	@Override
	public BasicDBObject readContact(long uId) {
		if (uId == 0)
			return new BasicDBObject();
		BasicDBObject oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, "select name,qq,msn,mobile,city,birthday,country from user where user_id=?",
				new String[] { String.valueOf(uId) }, 0, 0);
		if (oResult == null)
			return null;
		String[] values = null;//ServiceFactory.appDict().decode(oResult.getString("name"), oResult.getString("mobile"), oResult.getString("qq"), oResult.getString("msn"));
		oResult.append("name", values[0]);
		oResult.append("mobile", values[1]);
		oResult.append("qq", values[2]);
		oResult.append("msn", values[3]);
		if (ctx != null && ctx.tool != null) {
			oResult.append("birthday", ctx.tool.formatDate(oResult.getLong("birthday"), "yyyy-MM-dd"));
		} else {
			oResult.append("birthday", "");
		}
		return oResult;
	}

	@Override
	public BasicDBList readServices(String userId) {
		if (StringUtil.isEmpty(userId)) {
			userId = String.valueOf(this.userId);
		}
		BasicDBList items = new BasicDBList();
		String sql = "select catalog,snapshot,short_id,title from activity_car where user_id=? and state<=3";
		BasicDBList list = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId }, 0, 0);
		for (int i = 0; i < list.size(); i++) {
			BasicDBObject o = (BasicDBObject) list.get(i);
			o.put("type", 5);
			items.add(o);
		}
		sql = "select catalog,snapshot,short_id,title from activity_trip where user_id=? and state<=3";
		list = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId }, 0, 0);
		for (int i = 0; i < list.size(); i++) {
			BasicDBObject o = (BasicDBObject) list.get(i);
			o.put("type", 6);
			items.add(o);
		}
		sql = "select catalog,snapshot,short_id,title from activity_room where user_id=? and state<=3";
		list = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId }, 0, 0);
		for (int i = 0; i < list.size(); i++) {
			BasicDBObject o = (BasicDBObject) list.get(i);
			o.put("type", 7);
			items.add(o);
		}
		return items;
	}

	@Override
	public BasicDBObject readUserCounter(long userId) {
		BasicDBObject oUser = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, "select server,service_count,topic_count from user where user_id=?",
				new String[] { String.valueOf(userId) }, 0, 0);
		FollowRecords records = new FollowRecords(oUser.getString("server"), userId, "");
		BasicDBObject oMatch = records.readCount();
		if (oMatch == null) {
			oUser.append("followCount", 1).append("beFollowCount", 1);
		} else {
			oUser.putAll((BSONObject) oMatch);
		}
		return oUser;
	}

	@Override
	public int newMessageCount() {
		return 1;//ServiceFactory.appoint().unReadMessageCount(this.server, this.userId);
	}

	public BasicDBObject cropFace(BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		GridFSDBFile file = null;
		if (StringUtil.isEmpty(oReq.getString("picture"))) {
			DBFileSystem fs = new DBFileSystem(Const.defaultMongoServer, "profile");
			file = fs.findFileByName("face_" + userId);
		} else {
			DBFileSystem fileSystem = new DBFileSystem("image", "pictures");
			file = fileSystem.findFileById(oReq.getString("picture"));
		}
		if (file == null) {
			return oResult.append("message", "文件未找到");
		}
//		ArrayList<MagickImage> destroies = new ArrayList<MagickImage>();
		try {
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			file.writeTo(bStream);
//			MagickImage image = ImageUtil.readMagickImage(bStream.toByteArray(), destroies);
//			image = ImageUtil.scale(image, oReq.getInt("bw"), oReq.getInt("bh"), destroies);
//			Dimension dim = image.getDimension();
//			image = ImageUtil.crop(image, x, y, w, h, destroies);
//			dim = image.getDimension();
//			if (dim.width > 300) {
//				image = ImageUtil.resize(ImageType.Face, image, destroies);
//			}
			DBFileSystem fs = new DBFileSystem(Const.defaultMongoServer, "profile");
//			meta.append("width", dim.width);
//			meta.append("height", dim.height);
			String fileName = "face_" + userId;
			fs.removeFile(fileName);
//			byte[] bytes = ImageUtil.readBytes(image);
//			fs.saveFile(bytes, fileName, "jpg", meta);
		} catch (Exception e) {
			logger.error("crop", e);
			return oResult.append("message", e.getMessage());
		} finally {
//			ImageUtil.free(destroies);
		}
		return oResult.append("xeach", true);
	}

	@Override
	public BasicDBObject updatePicture(BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		BasicDBObject oPic = (BasicDBObject) oReq.get("picture");
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set picture=? where user_id=?", new String[] { oPic.toString(), String.valueOf(this.userId) });
		return oResult;
	}

	@Override
	public BasicDBObject readMsgBox(int pageNum, int pageSize) {
		return new ModuleAppoint().readMsgBox(server, userId, pageNum, pageSize);
	}

	@Override
	public BasicDBList readBills() {
		BasicDBList results = new BasicDBList();
		results.add(readBillByDay(1, 0));
		results.add(readBillByDay(2, 0));
		results.add(readBillByDay(3, 1));
		return results;
	}

	@Override
	public BasicDBObject readBillByDay(int day, int pageNumber) {
		long time = System.currentTimeMillis() - day * Const.Day1Milis;
		BasicDBObject oTime = new BasicDBObject();
		BasicDBObject oReq = new BasicDBObject().append("timestamp", oTime).append("mode", new BasicDBObject().append("$exists", true));
		BasicDBObject oResult = new BasicDBObject();
		switch (day) {
		case 1:
			oTime.append("$gte", time);
			oResult.append("dayName", "今天");
			oResult.append("className", "i_msg_today");
			break;
		case 2:
			oTime.append("$gte", time).append("$lte", System.currentTimeMillis() - (day - 1) * Const.Day1Milis);
			oResult.append("dayName", "昨天");
			oResult.append("className", "i_msg_before");
			break;
		case 3:
			oTime.append("$lte", time);
			oResult.append("dayName", "以前");
			oResult.append("className", "i_msg_before");
			break;
		}
		oResult.append("dayIndex", day);
		BasicDBList items = null;
		BasicDBObject oSort = new BasicDBObject().append("timestamp", -1);
		pageNumber--;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		if (day <= 2) {
			items = DataCollection.findAll(this.server, "finance", "bill_" + this.userId, oReq, new BasicDBObject(), oSort);
			oResult.append("items", items);
			oResult.append("totalCount", items.size());
		} else {
			DataResult dr = DataCollection.find(this.server, "finance", "bill_" + this.userId, oReq, oSort, pageNumber, 10);
			oResult.append("items", dr.list);
			oResult.append("totalCount", dr.totalCount);
		}
		items = (BasicDBList) oResult.get("items");
		for (int i = 0; items != null && i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			oItem.append("id", oItem.getString("_id"));
			oItem.removeField("_id");
			oItem.append("ago", ctx.tool.dateDiff(oItem.getLong("timestamp")));
			oItem.append("date", ctx.tool.formatDate(oItem.getLong("timestamp"), "yyyy-MM-dd HH:mm"));
		}
		return oResult.append("xeach", true);
	}

	@Override
	public BasicDBList readMessageHeaders() {
		BasicDBList results = new BasicDBList();
		results.add(readMessageHeaderByDay(1, 0));
		results.add(readMessageHeaderByDay(2, 0));
		results.add(readMessageHeaderByDay(3, 1));
		return results;
	}

	@Override
	public BasicDBObject readMessageHeaderByDay(int day, int pageNumber) {
		long time = System.currentTimeMillis() - day * Const.Day1Milis;
		BasicDBObject oTime = new BasicDBObject();
		BasicDBObject oReq = new BasicDBObject().append("timestamp", oTime);
		BasicDBObject oResult = new BasicDBObject();
		switch (day) {
		case 1:
			oTime.append("$gte", time);
			oResult.append("dayName", "今天");
			oResult.append("className", "i_msg_today");
			break;
		case 2:
			oTime.append("$gte", time).append("$lte", System.currentTimeMillis() - (day - 1) * Const.Day1Milis);
			oResult.append("dayName", "昨天");
			oResult.append("className", "i_msg_before");
			break;
		case 3:
			oTime.append("$lte", time);
			oResult.append("dayName", "以前");
			oResult.append("className", "i_msg_before");
			break;
		}
		BasicDBList items = null;
		BasicDBObject oSort = new BasicDBObject().append("timestamp", -1);
		pageNumber--;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		if (day <= 2) {
			items = DataCollection.findAll(this.server, Const.defaultMongoDB, "msgheader_" + this.userId, oReq, new BasicDBObject(), oSort);
			oResult.append("items", items);
			oResult.append("totalCount", items.size());
		} else {
			DataResult dr = DataCollection.find(this.server, Const.defaultMongoDB, "msgheader_" + this.userId, oReq, oSort, pageNumber, 10);
			oResult.append("items", dr.list);
			oResult.append("totalCount", dr.totalCount);
		}
		items = (BasicDBList) oResult.get("items");
		for (int i = 0; items != null && i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			oItem.append("id", oItem.getString("_id"));
			oItem.removeField("_id");
			oItem.append("ago", ctx.tool.dateDiff(oItem.getLong("timestamp")));
		}
		return oResult.append("xeach", true);
	}

	@Override
	public boolean checkBindMail() {
		String id = this.ctx.getParam("id");
		String code = this.ctx.getParam("code");
		if (StringUtil.isEmpty(id) || StringUtil.isEmpty(code)) {
			return false;
		}
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select mail_key from user_bind where user_id=?", new String[] { id }, 0, 0);
		if (rows == null || rows.length == 0) {
			return false;
		}
		if (!rows[0][0].equalsIgnoreCase(Const.defaultMysqlDB) || rows[0][0].equalsIgnoreCase(code)) {
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set bind_mail=1 where user_id=?", new String[] { id });
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user_bind set mail_key='yiqihi' where user_id=?", new String[] { id });
			this.bindMobile = 1;
			updateUserCache();
			return true;
		}
		return false;
	}

	@Override
	public BasicDBList sendMessage(String action, long contactId, BasicDBList sockets, BasicDBObject oMsgHeader, BasicDBObject oMsg) {
		String msgId = oMsgHeader.getString("_id");
		BasicDBList oResults = new BasicDBList();
		BasicDBObject oSocketMsg = new BasicDBObject();
		oSocketMsg.putAll((BSONObject) oMsg);
		oSocketMsg.append("action", action);
		oSocketMsg.append("msgId", msgId);
		oSocketMsg.append("userId", String.valueOf(this.userId));
		oSocketMsg.append("userName", this.userName);
		oSocketMsg.append("userServer", this.server);
		oSocketMsg.append("fromIP", this.ctx.ip);
		oSocketMsg.append("fromCity", this.ctx.cityName);
		BasicDBObject oSocket = null;
		BasicDBObject oResult = null;
		for (int i = 0; i < sockets.size(); i++) {
			oSocket = (BasicDBObject) sockets.get(i);
			if (Long.parseLong(oSocket.getString("userId")) == this.userId) {
				continue;
			}
			try {
				oSocketMsg.append("toUserId", oSocket.getLong("userId"));
				oSocketMsg.append("toUserName", oSocket.getString("userName"));
				if (Global.ServerName.equalsIgnoreCase(oSocket.getString("appserver"))) {
					ArrayList<SocketSession> sessions = ServiceFactory.socket().userBy(Long.parseLong(oSocket.getString("userId")));
					logger.info("userId[{}] has SessionsCount[{}]=", new String[] { oSocket.getString("userId"), String.valueOf(sessions == null ? 0 : sessions.size()) });
					int count = 0;
					for (int t = 0; sessions != null && t < sessions.size(); t++) {
						SocketSession s = sessions.get(t);
						count += ServiceFactory.socket().sendMessage(s, oSocketMsg) ? 1 : 0;
					}
					oResult = DBObjectUtil.selectBy(oSocket, "userId", "userName").append("online", count != 0);
				} else {
					oResult = (BasicDBObject) ZeroConnect.sendSocket(Global.DevelopMode == true ? "localhost" : oSocket.getString("appserver"), oSocketMsg);
					oResult = DBObjectUtil.selectBy(oSocket, "userId", "userName").append("online", oResult.getBoolean("xeach", false));
				}
			} catch (Exception e) {
				logger.error("sendSocket", e);
				continue;
			}
			boolean online = oResult.getBoolean("online", false);
			if (online == false) {
				MessageBox.unReadMsg(oSocket.getString("appserver"), oSocket.getLong("userId"), msgId, contactId, System.currentTimeMillis(), oMsg.getString("content"));
			}
			oResults.add(oResult);
		}
		return oResults;
	}

	public BasicDBObject resetPassword(BasicDBObject req) {
		BasicDBObject result = new BasicDBObject();
		String code = req.getString("code");
		String email = req.getString("email");
		BasicDBObject session = UserCache.get(ctx.cookieId);
		String cacheCode = session.getString("code", "");
		Long cacheTime = session.getLong("time", 0);
		UserCache.reset(ctx.cookieId, "code");
		UserCache.reset(ctx.cookieId, "time");
		long min = (System.currentTimeMillis() - cacheTime) / 1000 / 60;
		if (min > 5) {
			return result.append("error", -1).append("message", "验证码已经过期");
		}
		if (cacheCode == null || !cacheCode.equals(code)) {
			return result.append("error", -1).append("message", "验证码不正确");
		}
		String password = req.getString("password");
		result.append("xeach", DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set password=? where email=?", new String[] { password, String.valueOf(email) }));
		return result;
	}

	@Override
	public BasicDBObject bindUser(BasicDBObject req) {
		BasicDBObject result = new BasicDBObject();
		String cookieId = ctx.cookieId;
		BasicDBObject userCache = UserCache.get(cookieId);
		String quickLogin = ctx.user.getQuickLogin();
		BasicDBObject userInfo = (BasicDBObject) ((BasicDBObject) userCache.get(ctx.user.getQuickLogin())).get("user");
		String email = req.getString("username");
		String password = req.getString("password");
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email),
				SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Do, password));
		email = values[0];
		password = values[1];
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from user where email=?", new String[] { email }, 0, 0);
		String row = rows[0][0];
		if (Integer.parseInt(row) > 0) {
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from user where email=? and password=?", new String[] { email, password }, 0, 0);
			if (Integer.parseInt(rows[0][0]) == 0) {
				return result.append("error", -1).append("message", "用户名密码不正确");
			}
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id from user where email=? and password=?", new String[] { email, password }, 0, 0);
			userId = Long.parseLong(rows[0][0]);
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user_bind set " + quickLogin + "_key =?," + quickLogin + "_secret  =? where user_id=?",
					new String[] { userInfo.getString("id"), ((BasicDBObject) userCache.get(ctx.user.getQuickLogin())).getString("accessToken"), String.valueOf(userId) });
		} else {
			register( password, email, null, false);
			DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into user_bind(user_id," + quickLogin + "_key," + quickLogin + "_secret)values(?,?,?)",
					new String[] { String.valueOf(userId), userInfo.getString("id"), ((BasicDBObject) userCache.get(ctx.user.getQuickLogin())).getString("accessToken") });
		}
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set bind_" + quickLogin + " =?,quicklogin=? where user_id=?",
				new String[] { "1", quickLogin, String.valueOf(userId) });
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_type from user where email=?", new String[] { email }, 0, 0);
		this.login(new BasicDBObject().append("email", email).append("password", password).append("checkedCode", false).append("userType", Integer.parseInt(rows[0][0])));
		return result.append("xeach", true);
	}

	public String getQuickLogin() {
		return quickLogin;
	}

	public void setQuickLogin(String quickLogin) {
		this.quickLogin = quickLogin;
	}

	@Override
	public BasicDBObject queryUser(BasicDBObject request) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		boolean hasRight = ctx.user.getRole().isCustomerService();
		oResult.append("message", "用户名不存在");
		String username = request.getString("username");
		String[][] rows = null;
		BasicDBObject user = null;
		if (StringUtils.isNumeric(username)) {
			oResult.append("message", "服务编号不存在");
			String sql = "select mapping,bizType from short_mapping where id=?";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { username }, 0, 0);
			if (rows == null || rows.length == 0) {
				return oResult;
			}
			BasicDBObject oMap = (BasicDBObject) JSON.parse(rows[0][0]);
			BizType bizType = BizType.values()[Integer.parseInt(rows[0][1])];
			String tableName = null;
			switch (bizType) {
			case car:
				tableName = "activity_car";
				break;
			case trip:
				tableName = "activity_trip";
				break;
			default:
				break;
			}
			if (tableName == null)
				return oResult;
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,country,city from " + tableName + " where id=?", new String[] { oMap.getString("id") }, 0, 0);
			if (rows == null || rows.length == 0) {
				return oResult;
			}
			user = DataSet
					.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,user_name,name,sex,country,city from user where user_id =?", new String[] { rows[0][0] }, 0, 0);
			if (StringUtil.isEmpty(user.getString("country"))) {
				user.append("country", rows[0][1]);
			}
			if (StringUtil.isEmpty(user.getString("city"))) {
				user.append("city", rows[0][2]);
			}
			user.append("bizId", username);
			user.append("bizType", bizType.name());
		} else {
			user = DataSet
					.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,user_name,name,sex,country,city from user where user_name =?", new String[] { username }, 0, 0);
		}
		if (user == null) {
			return oResult;
		}
		if (hasRight) {
			String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, user.getString("name")));
			if (values != null && values.length > 0) {
				user.append("name", values[0]);
			}
		} else {
			user.append("name", "");
		}
		return user.append("xeach", true);
	}
}
