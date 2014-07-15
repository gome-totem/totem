package org.z.core.module;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.Context;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.dict.Global.ActionType;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.PayCode;
import org.z.global.dict.Global.PayMode;
import org.z.global.environment.Const;
import org.z.global.object.UserRecord;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * Appoint 以fromUserId-ToUserId为键值，也就是一对一的对话机制。其中server属性指定具体的消息存放在那个服务器上。
 * messages存放所有的Conversation对应的所有消息. msgbox_ + userId是用来通知用户的收件箱。
 * 
 * @author 肖鸣
 * 
 */
public class ModuleAppoint implements ServiceIntf {
	protected final Logger logger = LoggerFactory.getLogger(ModuleAppoint.class);

	public ModuleAppoint() {
	}

	public BasicDBObject buyAppointService(long userId, int amount, String title) {
		// 1: 检查当前用户的账户状态是否正常
		BasicDBObject oResult = ServiceFactory.bill().checkAccountBalance(userId, false);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		int balance = oResult.getInt("balance");
		PayCode code = PayCode.Fee;
		// 2：检查是否有余额支付
		if (balance < amount) {
			return oResult.append("message", "您的现金账户余额不足" + amount + "元,<a class='underline link' target='_blank' href='/finance'>点击此处充值</a>");
		}
		// 3: 为当前用户增加流水记录，减少他的账户余额
		String orderId = StringUtil.createUniqueID();
		balance -= amount;
		UserRecord rec = ServiceFactory.user().userRecordByCache(userId);
		ServiceFactory.bill().addRecord(rec.server, userId, code, PayMode.Dec, title, 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(rec.balanceId));
		DataCollection.update(rec.server, ServiceFactory.bill().dbName, "bill_" + userId, qField, new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance), String.valueOf(userId) });
		return oResult.append("xeach", true);
	}

	public BasicDBObject backAppointService(long userId, int amount, String title) {
		// 1: 检查当前用户的账户状态是否正常
		BasicDBObject oResult = ServiceFactory.bill().checkAccountBalance(userId, true);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		int balance = oResult.getInt("balance");
		String balanceId = oResult.getString("balanceId");
		oResult.removeField("balanceId");
		String server = oResult.getString("server");
		oResult.removeField("server");
		PayCode code = PayCode.Fee;
		// 3: 为当前用户增加流水记录，增加他的账户余额
		String orderId = StringUtil.createUniqueID();
		balance += amount;
		ServiceFactory.bill().addRecord(server, userId, code, PayMode.Inc, "退款-" + title, 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(balanceId));
		DataCollection.update(server, ServiceFactory.bill().dbName, "bill_" + userId, qField, new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance), String.valueOf(userId) });
		return oResult.append("xeach", true);
	}

	/* 更新已读标志 */
	public void clearMsgBoxReadTag(long userId, String conversationId) {
		String collName = "msgbox_" + userId;
		UserRecord rec = ServiceFactory.user().userRecordByCache(userId);
		BasicDBObject qField = new BasicDBObject().append("keyId", conversationId);
		BasicDBObject oReturn = new BasicDBObject().append("readTag", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(rec.server, Const.defaultMongoDB, collName, qField, oReturn);
		if (one != null && one.getInt("readTag") == 0) {
			DataCollection.update(rec.server, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", new BasicDBObject().append("readTag", 1)), false);
		}
	}

	/* 读取会话 */
	public BasicDBObject read(Context context, String id, String from, String to) {
		if (context.user.getUserId() == 0) {
			return null;
		}
		BasicDBObject one = null;
		if (!StringUtils.isEmpty(id)) {
			BasicDBObject oReq = new BasicDBObject().append("_id", new ObjectId(id));
			one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "appoints", oReq);
		} else if (!StringUtils.isEmpty(from) && !StringUtils.isEmpty(to) && StringUtils.isNumeric(from) && StringUtils.isNumeric(to)) {
			BasicDBObject oReq = new BasicDBObject().append("fromUserId", Long.parseLong(from)).append("toUserId", Long.parseLong(to));
			one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "appoints", oReq);
		} else {
			return null;
		}
		if (one == null) {
			return null;
		}
		if (context.user.getUserId() != one.getInt("toUserId") && context.user.getUserId() != one.getInt("fromUserId")) {
			return null;
		}
		int serviceIndex = one.getInt("serviceIndex");
		BasicDBList services = (BasicDBList) one.get("services");
		one.removeField("services");
		BasicDBObject service = (BasicDBObject) services.get(serviceIndex);
		one.append("service", service);
		one.append("keyId", one.getString("_id"));
		clearMsgBoxReadTag(context.user.getUserId(), one.getString("_id"));
		if (one.getInt("action") == 2 || one.getInt("action") == 4) {
			long shortId = service.getLong("shortId");
//			ModuleActivity activity = ServiceFactory.activityBy(service.getInt("type"));
//			one.append("contact", activity.readContactBy(shortId, BizType.values()[service.getInt("type")]));
		}
		return one;
	}

	public BasicDBObject readMessages(BasicDBObject oReq) {
		int pageNumber = oReq.getInt("pageNumber");
		int pageSize = oReq.getInt("pageSize");
		BasicDBObject oResult = new BasicDBObject();
		String server = oReq.getString("server");
		String keyId = oReq.getString("keyId");
		BasicDBObject qField = new BasicDBObject().append("keyId", keyId);
		BasicDBObject oSort = new BasicDBObject().append("timestamp", -1);
		pageNumber = pageNumber - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		DataResult dr = (DataResult) DataCollection.find(server, Const.defaultMongoDB, "messages", qField, oSort, null, pageNumber * pageSize, pageSize);
		oResult.append("totalCount", dr.totalCount);
		oResult.append("items", dr.list);
		return oResult.append("xeach", true);
	}

	// 读取消息库
	public BasicDBObject readMessage(Context context, String server, String messageId) {
		if (StringUtil.isEmpty(server) || StringUtil.isEmpty(messageId))
			return null;
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(messageId));
		BasicDBObject oResult = (BasicDBObject) DataCollection.findOne(server, Const.defaultMongoDB, "messages", qField);
		if (oResult == null) {
			return null;
		}
		if (context.user.getUserId() != oResult.getInt("toUserId")) {
			return null;
		}
		return oResult;
	}

	public String addMessage(ActionType action, BasicDBObject bizMeta, String server, String keyId, long fromUserId, String fromUserName, long toUserId, String toUserName, String content) {
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("action", action.ordinal());
		if (action == ActionType.book || action == ActionType.accept || action == ActionType.offer) {
			oRecord.append("bizMeta", bizMeta);
		}
		oRecord.append("keyId", keyId);
		oRecord.append("fromUserId", fromUserId);
		oRecord.append("fromUserName", fromUserName);
		oRecord.append("toUserId", toUserId);
		oRecord.append("toUserName", toUserName);
		oRecord.append("content", content);
		oRecord.append("timestamp", System.currentTimeMillis());
		return DataCollection.insert(server, Const.defaultMongoDB, "messages", oRecord).toString();
	}

	private int compareDBObject(BasicDBObject oService, BasicDBObject bizMeta) {
		if (bizMeta.getInt("chargeWay") == 0 && bizMeta.getInt("hour") != oService.getInt("hour")) {
			return -1;
		}
		if (bizMeta.getInt("chargeWay") == 1 && bizMeta.getInt("day") != oService.getInt("day")) {
			return -1;
		}
		if (bizMeta.containsField("deptDate") && !bizMeta.getString("deptDate").equalsIgnoreCase(oService.getString("deptDate"))) {
			return -1;
		}
		if (bizMeta.containsField("checkinDate") && !bizMeta.getString("checkinDate").equalsIgnoreCase(oService.getString("checkinDate"))) {
			return -1;
		}
		if (bizMeta.containsField("roomCount") && !bizMeta.getString("roomCount").equalsIgnoreCase(oService.getString("roomCount"))) {
			return -1;
		}
		if (bizMeta.getInt("total") != oService.getInt("total")) {
			return -1;
		}
		return 0;
	}

	private boolean updateServices(BasicDBObject one, BasicDBObject bizMeta) {
		BasicDBList oServices = (BasicDBList) one.get("services");
		for (int i = 0; i < oServices.size(); i++) {
			BasicDBObject oService = (BasicDBObject) oServices.get(i);
			long shortId = oService.getLong("shortId");
			if (shortId != bizMeta.getLong("shortId"))
				continue;
			if (compareDBObject(oService, bizMeta) == 0)
				continue;
			oService.putAll((BSONObject) bizMeta);
			return true;
		}
		return false;
	}

	public int checkAppointPool(long currentUserId, long toUserId) {
		String sql = "select count(*) from appoint where user_id=? and state=0";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(currentUserId) }, 0, 0);
		if (Integer.parseInt(rows[0][0]) >= 10) {
			return 1;
		}
		if (toUserId != 0) {
			sql = "select count(*) from appoint where user_id=? and toUserId=?";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(currentUserId), String.valueOf(toUserId) }, 0, 0);
			if (Integer.parseInt(rows[0][0]) != 0) {
				return 2;
			}
		}
		return 0;
	}

	public void addAppointPool(String id, long fromUserId, String fromUserName, long toUserId, String title, String content, BasicDBObject bizMeta) {
		String sql = "insert appoint(id,user_id,user_name,toUserId,timestamp,title,content,bizMeta)values(?,?,?,?,?,?,?,?)";
		DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
				new String[] { id, String.valueOf(fromUserId), fromUserName, String.valueOf(toUserId), String.valueOf(System.currentTimeMillis()), title, content, bizMeta.toString() });
	}

	public void deleteAppointPool(String id, int action) {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,title,bizMeta from appoint where id=?", new String[] { id }, 0, 0);
		if (rows == null || rows.length == 0)
			return;
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update appoint set state=1,action=? where id=?", new String[] { String.valueOf(action), id });
		long fromUserId = Long.parseLong(rows[0][0]);
		String title = rows[0][1];
		BasicDBObject meta = (BasicDBObject) JSON.parse(rows[0][2]);
		if (meta.getInt("fee") > 0) {
			backAppointService(fromUserId, meta.getInt("fee"), title);
		}
	}

	/* 建立一个会话,会话的key是 fromUserId+toUserId */
	public BasicDBObject create(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		if (context.checkCaptcha(oReq) == false) {
			oResult.append("message", "抱歉，您输入的验证码不正确");
		}
		long toUserId = oReq.getLong("toUserId");
		String toUserName = oReq.getString("toUserName");
		if (toUserId == context.user.getUserId() && context.user.getUserId() != 8) {
			return oResult.append("message", "抱歉，不能自己预约自己.");
		}
		if (!oReq.containsField("bizMeta")) {
			return oResult.append("message", "参数错误");
		}
		if (context.user.getBindMobile() == 0) {
			return oResult.append("message", "为了预约成功后,能马上联系您,请<a target='_blank' class='link' href='/auth'>点击此处绑定手机</a>.");
		}
		int poolState = checkAppointPool(context.user.getUserId(), toUserId);
		if (poolState == 1) {
			return oResult.append("message", "您已经发起了10个预约了，请等待预约被处理.");
		} else if (poolState == 2) {
			return oResult.append("message", "抱歉 ,您的上一个<" + toUserName + ">预约,还在处理中!");
		}
		BasicDBObject bizMeta = (BasicDBObject) oReq.get("bizMeta");
		int amount = 0;
		if (!context.user.licenceIsValid()) {
			amount = 10;
			if ((bizMeta.getInt("type") == BizType.trip.ordinal()) && (bizMeta.getInt("catalog") == 0)) {
				amount = 30;
			}
			BasicDBObject oBill = ServiceFactory.bill().buyAppointService(context, amount, StringUtil.trim(oReq.getString("title")));
			if (oBill.getBoolean("xeach", true) == false) {
				return oBill;
			}
		}
		bizMeta.append("fee", amount);
		String collName = "appoints";
		BasicDBObject qField = new BasicDBObject().append("fromUserId", context.user.getUserId()).append("toUserId", toUserId);
		BasicDBObject oReturn = new BasicDBObject().append("services", 1).append("server", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, oReturn);
		BasicDBObject oRecord = new BasicDBObject();
		// 对话涉及的服务
		BasicDBList oServices = null;
		if (one == null) {
			oServices = new BasicDBList();
		} else {
			oServices = (BasicDBList) one.get("services");
		}
		String activityId = bizMeta.getString("id");
		BasicDBObject oService = null;
		int serviceIndex = -1;
		for (int i = 0; i < oServices.size(); i++) {
			oService = (BasicDBObject) oServices.get(i);
			String id = oService.getString("id");
			if (!id.equalsIgnoreCase(activityId))
				continue;
			serviceIndex = i;
			break;
		}
		if (serviceIndex == -1) {
			oService = new BasicDBObject();
			oServices.add(oService);
			serviceIndex = oServices.size() - 1;
		}
		oRecord.append("serviceIndex", serviceIndex);
		oRecord.append("services", oServices);
		oService.putAll((BSONObject) bizMeta);
		if (!bizMeta.containsField("total")) {
//			BasicDBObject oPrice = ServiceFactory.calendar().calcPrice(bizMeta);
//			oService.append("total", oPrice.getInt("total"));
//			bizMeta.append("total", oPrice.getInt("total"));
		}
		ActionType action = ActionType.values()[oReq.getInt("action")];
		oRecord.append("action", action.ordinal());
		oRecord.append("timestamp", System.currentTimeMillis());
		String server = null;
		String appointId = null;
		if (one != null) {
			server = one.getString("server");
			appointId = one.getString("_id");
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oRecord), false);
		} else {
			server = context.user.getServer();
			oRecord.append("server", server);
			oRecord.append("fromUserId", context.user.getUserId());
			oRecord.append("fromUserName", context.user.getUserName());
			oRecord.append("toUserId", toUserId);
			oRecord.append("toUserName", toUserName);
			appointId = DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, collName, oRecord).toString();
		}
		String title = oReq.getString("title");
		String content = oReq.getString("content");
		bizMeta.append("type", bizMeta.getInt("type"));
		this.addAppointPool(appointId, context.user.getUserId(), context.user.getUserName(), toUserId, title, content, bizMeta);
		bizMeta.append("title", title);
		String msgId = addMessage(action, bizMeta, server, appointId, context.user.getUserId(), context.user.getUserName(), toUserId, toUserName, content);
		notifyUser(appointId, server, msgId, bizMeta.getInt("type"), action, context.user.getUserId(), context.user.getUserName(), toUserId, title, content);
		notifyUser(appointId, server, msgId, bizMeta.getInt("type"), action, toUserId, toUserName, context.user.getUserId(), title, content);
//		ServiceFactory.mail().sendMail(toUserId, "客人[" + context.user.getUserName() + "]预约您，请尽快处理!",
//				"[<b>" + title + "</b>]<br>内容:" + content + "<br>请登录<a target='_blank' href='http://www.yiqihi.com/login'>www.yiqihi.com</a>后台，处理该预约！");
		return new BasicDBObject().append("keyId", appointId).append("xeach", true);
	}

	/* 重新分配预约 fromUserId+toUserId */
	public BasicDBObject assign(long fromUserId, String fromUserName, long toUserId, String toUserName, String title, String content, int action, BasicDBObject bizMeta) {
		BasicDBObject oResult = new BasicDBObject();
		String collName = "appoints";
		BasicDBObject qField = new BasicDBObject().append("fromUserId", fromUserId).append("toUserId", toUserId);
		BasicDBObject oReturn = new BasicDBObject().append("services", 1).append("server", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, oReturn);
		BasicDBObject oRecord = new BasicDBObject();
		// 对话涉及的服务
		BasicDBList oServices = null;
		if (one == null) {
			oServices = new BasicDBList();
		} else {
			oServices = (BasicDBList) one.get("services");
		}
		String activityId = bizMeta.getString("id");
		BasicDBObject oService = null;
		int serviceIndex = -1;
		for (int i = 0; i < oServices.size(); i++) {
			oService = (BasicDBObject) oServices.get(i);
			String id = oService.getString("id");
			if (!id.equalsIgnoreCase(activityId))
				continue;
			serviceIndex = i;
			break;
		}
		if (serviceIndex == -1) {
			oService = new BasicDBObject();
			oServices.add(oService);
			serviceIndex = oServices.size() - 1;
			oRecord.append("serviceIndex", serviceIndex);
		}
		oRecord.append("services", oServices);
		oService.putAll((BSONObject) bizMeta);

		ActionType actionType = ActionType.values()[action];
		if ((actionType != ActionType.talk) && (actionType != ActionType.deny)) {
			return oResult.append("message", "预约的服务状态不正确.");
		}
		actionType = ActionType.talk;
		oRecord.append("action", actionType);
		oRecord.append("timestamp", System.currentTimeMillis());
		String server = null;
		String appointId = null;
		if (one != null) {
			server = one.getString("server");
			appointId = one.getString("_id");
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oRecord), false);
		} else {
			server = ServiceFactory.user().userRecordByCache(fromUserId).server;
			oRecord.append("server", server);
			oRecord.append("fromUserId", fromUserId);
			oRecord.append("fromUserName", fromUserName);
			oRecord.append("toUserId", toUserId);
			oRecord.append("toUserName", toUserName);
			appointId = DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, collName, oRecord).toString();
		}
		bizMeta.append("type", bizMeta.getInt("type"));
		this.addAppointPool(appointId, fromUserId, fromUserName, toUserId, title, content, bizMeta);
		bizMeta.append("title", title);
		String msgId = addMessage(actionType, bizMeta, server, appointId, fromUserId, fromUserName, toUserId, toUserName, content);
		notifyUser(appointId, server, msgId, bizMeta.getInt("type"), actionType, fromUserId, fromUserName, toUserId, title, content);
		notifyUser(appointId, server, msgId, bizMeta.getInt("type"), actionType, toUserId, toUserName, fromUserId, title, content);
//		ServiceFactory.mail().sendMail(toUserId, "客人[" + fromUserName + "]预约您，请尽快处理!",
//				"[<b>" + title + "</b>]<br>内容:" + content + "<br>请登录<a target='_blank' href='http://www.yiqihi.com/login'>www.yiqihi.com</a>后台，处理该预约！");
		return new BasicDBObject().append("keyId", appointId).append("xeach", true);
	}

	/* 更新会话的消息 */
	public BasicDBObject sendMessage(String currentServer, long currentUserId, String currentUserName, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String appointId = oReq.getString("keyId");
		long toUserId = oReq.getLong("toUserId");
		if (toUserId == currentUserId && currentUserId != 8) {
			return oResult.append("message", "抱歉，消息不能发给自己");
		}
		String toUserName = oReq.getString("toUserName");
		String content = oReq.getString("content");
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(appointId));
		String collName = "appoints";
		BasicDBObject oReturn = new BasicDBObject().append("server", 1).append("fromUserId", 1).append("toUserId", 1).append("action", 1);
		ActionType action = ActionType.values()[oReq.getInt("action")];
		if (action == ActionType.offer) {
			oReturn.append("services", 1);
		}
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, oReturn);
		if (one == null || ((currentUserId != one.getLong("fromUserId")) && (currentUserId != one.getLong("toUserId"))))
			return oResult.append("message", "预约记录不存在.");
		if (one.getInt("action") == ActionType.deny.ordinal()) {
			return oResult.append("message", "预约已经被拒绝，不能发送消息.");
		}
		if ((action != ActionType.talk) && (one.getInt("action") == ActionType.accept.ordinal() || one.getInt("action") == ActionType.offer.ordinal())) {
			return oResult.append("message", "预约已经接受，您只能发送消息.");
		}
		BasicDBObject bizMeta = (BasicDBObject) oReq.get("bizMeta");
		int bizType = bizMeta.getInt("type");
		String title = oReq.getString("title");
		// 如果是主人 ,且如果当前的状态是接受或拒绝；如果是客人,无权修改action
		if (one.getLong("toUserId") == currentUserId && action.ordinal() > ActionType.talk.ordinal()) {
			BasicDBObject oUpdate = new BasicDBObject().append("action", action.ordinal());
			deleteAppointPool(appointId, action.ordinal());
			if (action == ActionType.offer && updateServices(one, bizMeta)) {
				oUpdate.append("services", one.get("services"));
			}
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oUpdate), false);
			/* 更新自己的消息状态 */
			collName = "msgbox_" + currentUserId;
			BasicDBObject oRecord = new BasicDBObject();
			oRecord.append("action", action.ordinal());
			oRecord.append("content", content);
			oRecord.append("timestamp", System.currentTimeMillis());
			qField = new BasicDBObject().append("keyId", appointId);
			oReturn = new BasicDBObject().append("keyId", 1);
			if (DataCollection.findOne(currentServer, Const.defaultMongoDB, collName, qField, oReturn) != null) {
				DataCollection.update(currentServer, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oRecord), false);
			}
		} else {
			action = ActionType.values()[one.getInt("action")];
		}
		String msgId = addMessage(ActionType.values()[oReq.getInt("action")], bizMeta, one.getString("server"), appointId, currentUserId, currentUserName, toUserId, toUserName, content);
		this.notifyUser(appointId, one.getString("server"), msgId, bizType, action, currentUserId, currentUserName, toUserId, title, content);
//		ServiceFactory.mail().sendMail(toUserId, "预约[" + title + "]有新消息，请尽快处理!", "内容:" + content + "<br>请登录<a target='_blank' href='http://www.yiqihi.com/login'>www.yiqihi.com</a>后台，处理该预约！");
		return this.readMessages(oReq);
	}

	/* 更新用户的消息信箱 */
	public void notifyUser(String appointId, String msgServer, String msgId, int bizType, ActionType action, long fromUserId, String fromUserName, long toUserId, String title, String content) {
		String server = ServiceFactory.user().userRecordByCache(toUserId).server;
		String collName = "msgbox_" + toUserId;
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("action", action.ordinal());
		oRecord.append("content", content);
		oRecord.append("readTag", 0);
		oRecord.append("msgServer", msgServer);
		oRecord.append("msgId", msgId);
		oRecord.append("timestamp", System.currentTimeMillis());
		oRecord.append("bizType", bizType);
		oRecord.append("title", title);
		BasicDBObject qField = new BasicDBObject().append("keyId", appointId);
		BasicDBObject oReturn = new BasicDBObject().append("keyId", 1);
		DBObject one = DataCollection.findOne(server, Const.defaultMongoDB, collName, qField, oReturn);
		if (one == null) {
			oRecord.append("keyId", appointId);
			oRecord.append("fromUserId", fromUserId);
			oRecord.append("fromUserName", fromUserName);
			DataCollection.insert(server, Const.defaultMongoDB, collName, oRecord);
		} else {
			DataCollection.update(server, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oRecord), false);
		}
	}

	/* 系统通知 */
	public String notify(String server, long userId, BizType type, String keyId, String title, String content) {
		return notifyByUser(10, "系统管理员", server, userId, type, ActionType.system, keyId, title, content);
	}

	public String notifyByUser(long fromUserId, String fromUserName, String server, long userId, BizType type, ActionType actionType, String keyId, String title, String content) {
		String collName = "msgbox_" + userId;
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("action", actionType.ordinal());
		oRecord.append("content", content);
		oRecord.append("readTag", 0);
		oRecord.append("timestamp", System.currentTimeMillis());
		oRecord.append("bizType", type.ordinal());
		oRecord.append("title", title);
		BasicDBObject qField = new BasicDBObject().append("keyId", keyId);
		BasicDBObject oReturn = new BasicDBObject().append("keyId", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(server, Const.defaultMongoDB, collName, qField, oReturn);
		if (one == null) {
			oRecord.append("fromUserId", fromUserId);
			oRecord.append("fromUserName", fromUserName);
			oRecord.append("keyId", keyId);
			return DataCollection.insert(server, Const.defaultMongoDB, collName, oRecord).toString();
		} else {
			DataCollection.update(server, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oRecord), false);
			return one.getString("_id");
		}
	}

	public void clearReadTag(String server, long userId, String id) {
		String collName = "msgbox_" + userId;
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("readTag", 1);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(id));
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(server, Const.defaultMongoDB, collName, qField, oRecord);
		if (one != null && one.getInt("readTag") == 0) {
			DataCollection.update(server, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oRecord), false);
		}
	}

	public BasicDBObject readMsgBox(String server, long userId, int pageNumber, int pageSize) {
		BasicDBObject result = new BasicDBObject().append("xeach", false).append("items", new String[0]);
		String collName = "msgbox_" + userId;
		BasicDBObject oSort = new BasicDBObject().append("timestamp", -1);
		pageNumber = pageNumber - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		pageSize = pageSize > 10 ? 10 : pageSize;
		int offset = pageNumber * pageSize;
		DataResult dr = DataCollection.find(server, Const.defaultMongoDB, collName, null, oSort, offset, pageSize);
		result.append("totalCount", dr.totalCount);
		result.append("items", dr.list);
		return result.append("xeach", true);
	}

	public BasicDBObject readMsgBox(Context context, BasicDBObject oReq) {
		return readMsgBox(context.user.getServer(), context.user.getUserId(), oReq.getInt("pageNumber"), oReq.getInt("pageSize"));
	}

	/* 是否有新消息 */
	public int unReadMessageCount(String server, long userId) {
		String collName = "msgbox_" + userId;
		int count = DataCollection.selectCount(server, Const.defaultMongoDB, collName, new BasicDBObject().append("readTag", 0));
		return count;
	}

	public BasicDBList unReadMessages(String server, long userId, int maxCount) {
		String collName = "msgbox_" + userId;
		BasicDBObject oSort = new BasicDBObject().append("timestamp", -1);
		DataResult dr = DataCollection.find(server, Const.defaultMongoDB, collName, new BasicDBObject().append("readTag", 0), oSort, null, 0, maxCount);
		return dr.list;
	}

	public BasicDBObject handleExpire() {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		long time = System.currentTimeMillis() - Const.Day1Milis;
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select id from appoint where timestamp<=?", new String[] { String.valueOf(time) }, 0, 0);
		for (int i = 0; rows != null && i < rows.length; i++) {
			String appointId = rows[i][0];
			this.deleteAppointPool(appointId, ActionType.system.ordinal());
			BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(appointId));
			String collName = "appoints";
			BasicDBObject oUpdate = new BasicDBObject().append("action", ActionType.deny);
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, collName, qField, new BasicDBObject().append("$set", oUpdate), false);
		}
		return oResult.append("count", rows.length);
	}

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
		return true;
	}

	@Override
	public String getId() {
		return "appoint";
	}

	@Override
	public DBObject handle(Context context, DBObject oReq) {
		BasicDBObject oResult = null;
		int funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		switch (funcIndex) {
		/** 创建预约 **/
		case 0:
			oResult = this.create(context, context.request);
			break;
		/** 读取预约消息 **/
		case 1:
			oResult = this.readMessages(context.request);
			break;
		/** 回复预约 **/
		case 2:
			oResult = this.sendMessage(context.user.getServer(), context.user.getUserId(), context.user.getUserName(), context.request);
			break;
		/** 读取收件箱 **/
		case 3:
			oResult = this.readMsgBox(context, context.request);
			break;
		/** 处理过期预约 **/
		case 6:
			oResult = this.handleExpire();
			break;
		}
		return oResult;
	}


	public static void main(String[] args) {
		int mode = 0;
		switch (mode) {
		/* 清除消息表 */
		case 0:
			String[] servers = new String[] { Const.defaultMysqlServer, Const.defaultMongoServer, "x2" };
			for (int i = 0; i < servers.length; i++) {
				String server = servers[i];
				Set<String> tables = DataCollection.getCollections(server, Const.defaultMongoDB);
				for (Iterator<String> t = tables.iterator(); t.hasNext();) {
					String table = t.next();
					if (!table.startsWith("msgbox_"))
						continue;
					DataCollection.dropCollection(server, Const.defaultMongoDB, table);
					System.out.println("delete Table=" + table);
				}
			}
			DataCollection.remove(Const.defaultMongoServer, Const.defaultMongoDB, "conversations", new BasicDBObject());
			break;
		}
	}

}
