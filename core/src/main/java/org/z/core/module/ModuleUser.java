package org.z.core.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.common.cache.Cache;
import org.z.common.cache.CacheEntry;
import org.z.common.htmlpage.User;
import org.z.core.common.Context;
import org.z.core.common.FollowRecords;
import org.z.core.common.MessageBox;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.Role;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.environment.Const;
import org.z.global.object.SecurityObject;
import org.z.global.object.UserRecord;
import org.z.global.object.UserRole;
import org.z.global.util.CompressTool;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeType;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ModuleUser implements ServiceIntf {
	protected Cache<UserRecord> cacheByServer = new Cache<UserRecord>(100000);
	protected static final Logger logger = LoggerFactory.getLogger(ModuleUser.class);
	protected boolean isAlive = true;

	@Override
	public boolean init(boolean isReload) {
		return true;
	}

	@Override
	public void afterCreate(Object[] params) {
	}

	@Override
	public void start(boolean isReload) {
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public String getId() {
		return "user";
	}

	public BasicDBObject readBy(BasicDBObject oReq) {
		if (oReq.containsField("shortId") == false || oReq.containsField("bizType") == false) {
			return null;
		}
		BizType type = BizType.values()[oReq.getInt("bizType")];
		long shortId = oReq.getLong("shortId");
		String sql = null;
		switch (type) {
		case car:
			sql = "select user_id,user_name from activity_car where short_id=?";
			break;
		case require:
			sql = "select user_id,user_name from activity_require where short_id=?";
			break;
		case trip:
			sql = "select user_id,user_name from activity_trip where short_id=?";
			break;
		default:
			sql = null;
			break;
		}
		if (sql == null) {
			return null;
		}
		return DataSet.queryDBObject(Const.defaultDictMysql, Const.defaultDictMysqlDB, sql, new String[] { String.valueOf(shortId) }, 0, 0);
	}

	public UserRecord userRecordByCache(long userId) {
		CacheEntry<UserRecord> entry = cacheByServer.get(String.valueOf(userId));
		if (entry != null) {
			return entry.value;
		} else {
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
					"select user_name,server,call_server,balance_id from user where user_id=?", new String[] { String.valueOf(userId) }, 0, 0);
			if (rows == null || rows.length == 0) {
				logger.error("userId=" + userId + " not exist");
				return null;
			}
			return cacheByServer.put(String.valueOf(userId), new UserRecord(rows[0][0], rows[0][1], rows[0][2], rows[0][3])).value;
		}
	}

	public String[] loginAccountByUser(String userId) {
		String sql = "select email,password from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId }, 0, 0);
		if (rows == null || rows.length == 0) {
			return null;
		}
		String email = rows[0][0];
		String password = rows[0][1];
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Undo, email),
				SecurityObject.create(SecurityType.UserPassword, SecurityMode.Undo, password));
		return values;
	}

	public String[] serverBalanceBy(long userId) {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select server,balance_id from user where user_id=?",
				new String[] { String.valueOf(userId) }, 0, 0);
		if (rows == null || rows.length == 0) {
			return null;
		}
		return new String[] { rows[0][0], rows[0][1] };
	}

	public BasicDBObject postQuestion(Context ctx, String title, String content) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		long targetUserId = Long.parseLong(ctx.getParam("u"));
		if (targetUserId == ctx.user.getUserId()) {
			return oResult.append("message", "抱歉,不能对自己提问.");
		}
		BasicDBList sockets = null;
		BasicDBObject oMsgHeader = null;
		String msgId = null;
		String userServer = ctx.user.getServer();
		long userId = ctx.user.getUserId();
		String userName = ctx.user.getUserName();
		oMsgHeader = MessageBox.readHeader(userServer, userId, 0, BizType.ask.ordinal(), targetUserId);
		int level = 3;
		if (oMsgHeader == null) {
			oMsgHeader = MessageBox.writeHeader(userServer, userId, userServer, userId, null, ctx.user.getUserId(), ctx.user.getUserName(), 0, 0,
					BizType.ask.ordinal(), title, "", targetUserId);
			msgId = oMsgHeader.getObjectId("_id").toString();
			logger.info("new MsgHeader[{}]", new String[] { msgId });
			sockets = MessageBox.createSockets(ctx.user.getServer(), userId, userName, targetUserId);
			MessageBox.writeSockets(userServer, userId, msgId, oMsgHeader.getLong("timestamp"), sockets);
			BasicDBObject oContact = ServiceFactory.socket().socketHandler().readContact(userId);
			for (int i = 0; i < sockets.size(); i++) {
				BasicDBObject oSocket = (BasicDBObject) sockets.get(i);
				if (oSocket.getLong("userId") == userId) {
					continue;
				}
				MessageBox.writeHeader(oSocket.getString("appserver"), oSocket.getLong("userId"), userServer, userId, msgId, userId, ctx.user.getUserName(), 0,
						0, BizType.ask.ordinal(), title, "", targetUserId);
				ServiceFactory.socket().socketHandler().writeContact(oSocket.getString("appserver"), oSocket.getLong("userId"), oContact);
			}
		} else {
			msgId = oMsgHeader.getObjectId("_id").toString();
			String boxServer = oMsgHeader.getString("boxServer");
			long boxId = oMsgHeader.getLong("boxId");
			BasicDBObject record = MessageBox.readSocketRecord(userId, boxServer, boxId, msgId);
			if (record == null) {
				oResult.append("message", "抱歉,您没有权限.");
				return oResult;
			}
			sockets = MessageBox.doFilterSockets(record, userId);
			if (sockets.size() == 0) {
				oResult.append("message", "抱歉,您没有权限.");
				return oResult;
			}
			level = record.getInt("w" + String.valueOf(ctx.user.getUserId()));
		}
		MessageBox.writeBody(userServer, userId, msgId, level, userId, userName, content);
		BasicDBObject oMsg = new BasicDBObject().append("bizTitle", title).append("content", content).append("catalog", 0)
				.append("shortId", ctx.user.getUserId()).append("bizType", BizType.ask.ordinal()).append("bizUserId", userId).append("bizName", "")
				.append("boxServer", userServer).append("boxId", userId).append("msgId", msgId);
		ctx.user.sendMessage("hi", userId, sockets, oMsgHeader, oMsg);
		return oResult.append("xeach", true);
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		int funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		BasicDBObject oResult = null;
		switch (funcIndex) {
		/* 登录 */
		case 0:
			oResult = ctx.user.login(ctx.request);
			break;
		/* 注册 */
		case 1:
			String email = ctx.request.getString("email");
			String code = ctx.request.getString("code");
			String password = ctx.request.getString("password");
			oResult = ctx.user.register( password, email, code, true);
			break;
		/* 找回密码 */
		case 2:
			oResult = ctx.user.forgetPassword(ctx.request);
			break;
		/* 检查是否登录 */
		case 4:
			oResult = new BasicDBObject();
			if (ctx.user.getUserId() == 0) {
				oResult.append("xeach", false);
				oResult.append("logout", true);
			} else {
				oResult.append("xeach", true);
			}
			break;
		/* 上传头像 */
		case 8:
			oResult = ctx.user.cropFace(ctx.request);
			break;
		/* 上传身份认证照片 */
		case 9:
			oResult = ctx.user.updatePicture(ctx.request);
			break;
		/* 得到用户组 */
		case 10:
			oResult = ctx.user.getGroups();
			break;
		/* 修改用户资料 */
		case 11:
			oResult = ctx.user.updateProfile(ctx.request);
			break;
		/* 修改密码 */
		case 13:
			String oldPassword = ctx.request.getString("oldPassword");
			password = ctx.request.getString("password");
			oResult = ctx.user.changePassword(oldPassword, password);
			break;
		/* 修改邮件地址 */
		case 23:
			oldPassword = ctx.request.getString("password");
			email = ctx.request.getString("email");
			oResult = ctx.user.changeEmail(oldPassword, email);
			break;
		/* 退出登录 */
		case 14:
			oResult = ctx.user.logout();
			break;
		/* 增加组 */
		case 15:
			FollowRecords table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			oResult = table.createGroup(ctx.request.getString("groupName"));
			break;
		/* 删除组 */
		case 16:
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			oResult = table.removeGroup(DBObjectUtil.wrapArray(ctx.request.get("groups")));
			break;
		/* 读取组 */
		case 17:
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			oResult = table.readGroups();
			break;
		/* 增加关注到group */
		case 18:
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			long userId = Long.parseLong(ctx.request.getString("userId"));
			int pageNumber = ctx.request.getInt("pageNumber");
			int pageSize = ctx.request.getInt("pageSize");
			oResult = table.addUserToGroup(DBObjectUtil.wrapArray(ctx.request.get("groups")), userId, pageNumber, pageSize);
			break;
		/* 把关注从group移除 */
		case 19:
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			userId = Long.parseLong(ctx.request.getString("userId"));
			String groupName = ctx.request.getString("groupName");
			pageNumber = ctx.request.getInt("pageNumber");
			pageSize = ctx.request.getInt("pageSize");
			oResult = table.removeUserFromGroup(groupName, userId, pageNumber, pageSize);
			break;
		/* 取消关注 */
		case 20:
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			userId = Long.parseLong(ctx.request.getString("userId"));
			String server = userRecordByCache(userId).server;
			table.unFollow(server, userId, ctx.request.getString("userName"));
			pageNumber = ctx.request.getInt("pageNumber");
			pageSize = ctx.request.getInt("pageSize");
			oResult = table.readFollowsWithGroup(pageNumber, pageSize);
			break;
		/* 关注 */
		case 21:
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			userId = Long.parseLong(ctx.request.getString("userId"));
			server = userRecordByCache(userId).server;
			table.follow(server, userId, ctx.request.getString("userName"));
			oResult = new BasicDBObject().append("xeach", true);
			break;
		/* 读取关注 */
		case 25:
			pageNumber = ctx.request.getInt("pageNumber");
			pageSize = ctx.request.getInt("pageSize");
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			oResult = table.readFollowsWithGroup(pageNumber, pageSize);
			break;
		/* 读取被关注 */
		case 26:
			pageNumber = ctx.request.getInt("pageNumber");
			pageSize = ctx.request.getInt("pageSize");
			table = new FollowRecords(ctx.user.getServer(), ctx.user.getUserId(), ctx.user.getUserName());
			oResult = table.readBeFollows(pageNumber, pageSize);
			break;
		/* 提问 */
		case 27:
			String title = ctx.request.getString("title");
			String content = ctx.request.getString("content");
			oResult = postQuestion(ctx, title, content);
			break;
		/** 读取用户的余额 */
		case 29:
			oResult = ServiceFactory.bill().readBalance(ctx);
			break;
		/** 读取用户邮件推送服务过期状态 */
		case 30:
			oResult = ctx.user.checkEmailService();
			break;
		/** 绑定微博登录的账户 */
		case 31:
			oResult = ctx.user.bindAccount(ctx.request);
			break;
		/** 发送邮件确认 */
		case 32:
			ModuleMail mail = ServiceFactory.mail();
			oResult = (BasicDBObject) mail.handle(ctx, ctx.request);
			break;
		/** 发送验证码 */
		case 33:
			ModuleSMS sms = ServiceFactory.sms();
			oResult = sms.sendAuth(ctx, ctx.request);
			break;
		/** 验证手机 */
		case 35:
			sms = ServiceFactory.sms();
			oResult = sms.checkAuth(ctx, ctx.request);
			break;
		/** 找回密码 */
		case 36:
			oResult = ctx.user.resetPassword(ctx.request);
			break;
		case 37:
			oResult = ctx.user.bindUser(ctx.request);
			break;
		case 38:
			oResult = ctx.user.queryUser(ctx.request);
			break;
		case 39:
			oResult = ServiceFactory.sms().sendUnbindAuth(ctx);
			break;
		case 40:
			oResult = ServiceFactory.sms().unbindAuth(ctx);
			break;
		}
		return oResult;
	}

	public static boolean checkIt(String content) {
		for (int i = 0; i < content.length(); i++) {
			if ((content.charAt(i) >= 'A' && content.charAt(i) <= 'Z') || (content.charAt(i) == ' ')) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		int mode = 2;
		switch (mode) {
		// 删除Cookie
		case -7:
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select cookie_id from user where user_id=?",
					new String[] { "30216" }, 0, 0);
			if (rows != null && rows.length > 0) {
			}
			break;
		case -5:
			String sql = "select user_id,user_name,email,password,mobile,qq,msn from user ";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
			for (int i = 0; i < rows.length; i++) {
				String userId = rows[i][0];
				String userName = rows[i][1];
				String email = rows[i][2];
				String password = rows[i][3];
				String mobile = rows[i][4];
				String qq = rows[i][5];
				String msn = rows[i][6];
				String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, email),
						SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, password),
						SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, mobile),
						SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, qq),
						SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, msn));
				System.out.println("userId=" + userId + "&userName=" + userName + "&email=" + values[0] + "&password=" + values[1] + "&mobile=" + values[2]
						+ "&qq=" + values[3] + "&msn=" + values[4]);
			}
			break;
		case -3:
			sql = "select user_id,user_name from user where server is null";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
			User user = new User(null);
			for (int i = 0; i < rows.length; i++) {
				String userId = rows[i][0];
				BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, userId);
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set server=? where user_id=?", new String[] { rec.getString("id"),
						userId });
				user.userId = Long.parseLong(userId);
				user.userName = rows[i][1];
				user.server = rec.getString("id");
				user.initBill();
			}
			break;

		case -6:
			sql = "select user_id,user_name,server from user where balance_id is null";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
			user = new User(null);
			for (int i = 0; i < rows.length; i++) {
				String userId = rows[i][0];
				user.userId = Long.parseLong(userId);
				user.userName = rows[i][1];
				user.server = rows[i][2];
				user.initBill();
			}
			break;

		case 0:
			String email = "124524378@qq.com";
			String email_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email))[0];
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select password,user_name,role,user_id,credit from user where email=?",
					new String[] { email_ }, 0, 0);
			if (rows == null || rows.length == 0) {
				System.out.println("user not exist.");
				return;
			}
			System.out.println("UserId=" + rows[0][3] + "&UserName=" + rows[0][1]);
			String password_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][0]))[0];
			System.out.println("登录账号:" + email + "密码:" + password_);
			break;
		case 1:
			String userName = "tonylin";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select password,email,mobile,user_id from user where user_name=?",
					new String[] { userName }, 0, 0);
			if (rows == null || rows.length == 0) {
				System.out.println("user not exist.");
				return;
			}
			String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][1]),
					SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][0]),
					SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][2]));
			System.out.println("email=" + values[0] + "&password=" + values[1] + "&mobile=" + values[2] + "&userId=" + rows[0][3]);
			break;
		case 2:
			long userId = 33624;
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select email,password,user_name from user where user_id=?",
					new String[] { String.valueOf(userId) }, 0, 0);
			if (rows == null || rows.length == 0) {
				System.out.println("user not exist.");
				return;
			}
			System.out.println("UserId=" + userId + "&userName=" + rows[0][2]);
			values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][0]),
					SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][1]));
			System.out.println(values[0] + "@@@" + values[1]);
			break;
		case 3:
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select contact_msn from activity_trip", 0, 0);
			for (int i = 0; i < rows.length; i++) {
				values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, rows[i][0]));
				System.out.println("Trip=" + values[0]);
			}
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select contact_msn from activity_car", 0, 0);
			for (int i = 0; i < rows.length; i++) {
				values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, rows[i][0]));
				System.out.println("car=" + values[0]);
			}
			break;
		/* 更新账户名称 */
		case -1:
			email = "0@yiqihi.com";
			userName = "陈然";
			email_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email))[0];
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set user_type=3,user_name=? where email=?", new String[] { userName,
					email_ });
			System.out.println("done");
			break;
		/* 更新邮件地址 */
		case -2:
			userId = 33191;
			email = "zhangguoxia19@sohu.com";
			email_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email))[0];
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set email=? where user_id=?",
					new String[] { email_, String.valueOf(userId) });
			System.out.println("done");
			break;

		case 4:
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select  email,password,user_id from user where user_name=?",
					new String[] { "我要去丽江" }, 0, 0);
			for (int t = 0; t < rows.length; t++) {
				values = rows[t];
				values[0] = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, values[0]))[0];
				values[1] = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, values[1]))[0];
				System.out.println(values[0] + "：" + values[1] + "  userId:" + values[2]);
			}
			break;
		/* 修改邮件地址 */
		case 5:
			email = "cookie@roitour.com";
			email_ = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email))[0];
			userId = 32524;
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set email=? where user_id=?",
					new String[] { email_, String.valueOf(userId) });
			break;
		// 更新权限
		case 6:
			int[] ids = new int[] { 32674 };
			for (int id : ids) {
				// userId = 30216;
				rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select role from user where user_id=?",
						new String[] { String.valueOf(id) }, 0, 0);
				if (rows != null && rows.length > 0) {
					UserRole role = new UserRole(Long.parseLong(rows[0][0]));
					role.clear(Role.Root);
					role.clear(Role.Accounter);
					role.clear(Role.CustomerService);
					role.set(Role.AirTicket);
					DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set role=? where user_id=?", new String[] { role.toString(),
							String.valueOf(id) });
				}
			}
			break;
		case 7:
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select email,password,mobile  from user where user_id=?",
					new String[] { "3380" }, 0, 0);
			values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][0]),
					SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][1]),
					SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][2]));
			System.out.println("email:" + values[0]);
			System.out.println("password:" + values[1]);
			System.out.println("mobile:" + values[2]);
			break;
		case 8:
			String mobile = "13466631806";
			values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, mobile));
			mobile = values[0];
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,email,password  from user where mobile=?",
					new String[] { mobile }, 0, 0);
			values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, rows[0][1]),
					SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, rows[0][2]));
			System.out.println("userId:" + rows[0][0]);
			System.out.println("email:" + values[0]);
			System.out.println("password:" + values[1]);
			break;

		case 9:
			sql = "select user_id,vos from user where isnull(vos)";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
			while (rows != null && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					values = rows[i];
					String vos = CompressTool.createPassword(20);
					DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set vos=? where user_id=?", new String[] { vos, rows[i][0] });
					// CallClient.createUser(rows[i][0], vos);
					logger.info("UserId=" + rows[i][0]);
				}
				rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
			}
			break;

		case 10:
			sql = "select user_id,user_name,password from user ";
			int offset = 0;
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, offset, 20);
			while (rows != null && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					String uId = rows[i][0];
					userName = StringUtil.trim(rows[i][1]);
					DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set user_name=? where user_id=?",
							new String[] { userName, uId });
					logger.info("UserId=" + uId + "&userName=" + userName);
				}
				offset += 20;
				rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, offset, 20);
				System.out.println("offset:" + offset);
			}
			break;
		case 11:
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id from activity_trip where state=2", 0, 0);
			for (int i = 0; i < rows.length; i++) {
				String[] row = rows[i];
				sql = "select email,password,qq from user where user_id=?";
				String[][] users = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { row[0] }, 0, 0);
				values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, users[0][0]),
						SecurityObject.create(Global.SecurityType.UserPassword, Global.SecurityMode.Undo, users[0][1]),
						SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, users[0][2]));
				System.out.println("userId=" + row[0] + "&email=" + values[0] + "&password=" + values[1] + "&qq=" + values[2]);
			}
			break;

		case 12:
			userId = 28865;
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "delete from user where user_id=?", new String[] { String.valueOf(userId) });
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "delete from activity_trip where user_id=?", new String[] { String.valueOf(userId) });
			break;
		case 15:
			sql = "select user_id,role from user where role!=0";
			offset = 0;
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, offset, 20);
			while (rows != null && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					String uId = rows[i][0];
					UserRole role = new UserRole(Integer.parseInt(rows[i][1]));
					if (role.isCustomerService() || role.isAccounter() || role.isRoot()) {
						logger.info("UserId=" + uId + "&customerService=" + role.isCustomerService() + "& accounter=" + role.isAccounter() + "&Root="
								+ role.isRoot());
					}
				}
				offset += 20;
				rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, offset, 20);
			}
			break;
		case 13:
			System.setProperty("DEBUG.DB", "true");
			FollowRecords records = new FollowRecords(Const.defaultMongoServer, 8, "苍狼");
			System.out.println(records.readFollows(0, 0));
			String server = Const.defaultMongoServer;
			for (long i = 2; i < 30; i++) {
				records = new FollowRecords(Const.defaultMongoServer, i, "苍狼");
				records.follow(server, 8, "userName" + i);
			}
			System.out.println(records.readFollows(0, 0));
			System.out.println(records.followExist(2));

			records.unFollow(server, 2, "userName2");
			System.out.println(records.followExist(2));

			System.out.println(records.readFollows(0, 0));

			records.createGroup("VIP客人");
			records.addUserToGroup(new String[] { "VIP客人" }, 2, 0, 0);
			System.out.println(records.toString());
			System.out.println(records.readFollowsWithGroup(0, 0));

			records.removeGroup(new String[] { "VIP客人" });
			System.out.println(records.toString());
			break;
		}
		System.out.println("done.");

	}
}
