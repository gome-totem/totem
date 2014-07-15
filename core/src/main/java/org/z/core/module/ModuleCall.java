package org.z.core.module;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.z.core.common.Context;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.environment.Const;
import org.z.global.object.SecurityObject;
import org.z.global.object.UserRecord;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ModuleCall implements ServiceIntf {

	private AtomicLong jobNo = new AtomicLong(0);

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
		return "call";
	}

	public String allocateJobNo() {
		if (jobNo.get() >= Long.MAX_VALUE) {
			jobNo.set(0);
		}
		return String.valueOf(jobNo.addAndGet(1));
	}

	public BasicDBObject readUserAccount(Context context) {
		BasicDBObject oResult = context.user.readAccountMoney(String.valueOf(context.user.getUserId()));
		if (oResult.containsField("money")) {
			oResult.append("money", Double.parseDouble(oResult.getString("money")));
			oResult.append("xeach", true);
		} else {
			oResult.append("message", "您没有绑定手机");
			oResult.append("xeach", false);
		}
		return oResult;
	}

	public BasicDBObject charge(String userId, double money) {
		BasicDBObject oResult = new BasicDBObject();
		return oResult;
	}

	public BasicDBObject changeFeerate(String feerateGroup) {
		BasicDBObject oResult = new BasicDBObject();
		return oResult;
	}

	public BasicDBObject callNow(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		return oResult;
	}

	public BasicDBObject callbackByAppoint(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		BasicDBObject one = null;
		String id = oReq.getString("id");
		if (!StringUtils.isEmpty(id)) {
			BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(id));
			one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "appoints", qField);
		}
		if (one == null) {
			return oResult.append("message", "预约不存在.");
		}
		long fromUserId = one.getLong("fromUserId");
		long toUserId = one.getLong("toUserId");

		if (context.user.getUserId() != toUserId && context.user.getUserId() != fromUserId) {
			return oResult.append("message", "该预约您没有呼叫权限.");
		}

		String sql = "select bind_mobile from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(fromUserId) }, 0, 0);
		if (Integer.parseInt(rows[0][0]) == 0) {
			if (fromUserId == context.user.getUserId()) {
				return oResult.append("message", "您的手机没有绑定，请<a class='link' target='_blank' href='/auth'>点击此处绑定</a>");
			} else {
				return oResult.append("message", "对方的手机没有绑定，无法发起呼叫!");
			}
		}

		BasicDBObject oMoney = context.user.readAccountMoney(one.getString("fromUserId"));
		if (!oMoney.containsField("money") || Float.parseFloat(oMoney.getString("money")) < 1) {
			if (fromUserId == context.user.getUserId()) {
				if (context.user.licenceIsValid()) {
					return oResult.append("message", "您的云呼叫账号余额不足,，请<a class='link' target='_blank' href='/finance'>点击此处充值</a>");
				} else {
					return oResult.append("message", "您还不是会员,请<a class='link' target='_blank' href='/finance'>点击此处成为会员</a>");
				}
			} else {
				return oResult.append("message", "对方的云呼叫未开通，无法发起呼叫!");
			}
		}
		oResult.append("error", 0);
		if (oResult.getInt("error") != 0) {
			return oResult.append("message", "呼叫中心繁忙中,请稍候10秒钟，重试一次!");
		}
		return oResult.append("xeach", true).append("message", "先呼叫[" + one.getString("fromUserName") + "]然后接通[" + one.getString("toUserName") + "]");
	}

	@SuppressWarnings("unused")
	public BasicDBObject callbackByUser(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String sql = "select bind_mobile from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		if (Integer.parseInt(rows[0][0]) == 0) {
			return oResult.append("message", "您的手机没有绑定，请<a class='underline link' target='_blank' href='/auth'>点击此处绑定</a>");
		}
		if (oReq.getLong("userId") == context.user.getUserId()) {
			return oResult.append("message", "不能自己呼叫自己.");
		}
		String caller = oReq.getString("mobile");
		sql = "select mobile from user where user_id=?";
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { oReq.getString("userId") }, 0, 0);
		if (rows == null || rows.length != 1) {
			return oResult.append("message", "服务方手机已经被系统屏蔽！");
		}
		int balance = context.user.readBalance();
		BasicDBObject oMoney = context.user.readAccountMoney(String.valueOf(context.user.getUserId()));
		if (!oMoney.containsField("money")) {
			return oResult.append("message", "您的电话账户异常,不能呼叫，请联系我们！");
		}
		double money = Double.parseDouble(oMoney.getString("money"));
		if (money < 10) {
			if (balance < 10) {
				return oResult.append("message", "您的账户余额不足10元，无法发起呼叫，请<a class='underline link' target='_blank' href='/finance'>点击此处充值</a>.");
			} else {
				oResult = ServiceFactory.bill().chargePhoneMoney(context, 10);
				if (oResult.getBoolean("xeach", true) == false) {
					return oResult;
				}
				try {
					Thread.sleep(10000);
				} catch (Exception e) {
				}
			}
		}
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Undo, rows[0][0]));
		if (values == null) {
			return oResult.append("message", "解码服务方手机状态失败.");
		}
		String callee = values[0];
		if (callee.equalsIgnoreCase(caller)) {
			return oResult.append("message", "呼叫方和被叫方手机号码不能相同.");
		}
		UserRecord rec = ServiceFactory.user().userRecordByCache(context.user.getUserId());
		// oResult.append("error",
		// Integer.parseInt(CallClient.callback(ServerDict.self.serverIPBy(rec.callServer),
		// context.cookieId, caller, callee)));
		if (oResult.getInt("error") != 0) {
			// logger.error("Callback caller=" + caller + "&callee=" + callee +
			// "&Error=" + oResult.getInt("error"));
			return oResult.append("message", "呼叫中心繁忙中,请稍候10秒钟，重试一次!");
		}
		oResult.append("xeach", oResult.getInt("error") == 0);
		oResult.append("caller", caller);
		return oResult.append("xeach", true);
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		int funcIndex = 0;
		if (oReq.containsField("funcIndex")) {
			funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		}
		BasicDBObject oResult = null;
		switch (funcIndex) {
		/** 读取账号 **/
		case 0:
			oResult = this.readUserAccount(ctx);
			break;
		/** 充值 **/
		case 1:
			oResult = this.charge(String.valueOf(ctx.user.getUserId()), ctx.request.getDouble("money"));
			break;
		/** 修改计算汇率 **/
		case 2:
			oResult = this.changeFeerate(ctx.request.getString("feerateGroup"));
			break;
		/** 云呼叫-回拨 **/
		case 10:
			oResult = null;
			break;
		/** 云呼叫 **/
		case 11:
			oResult = this.callNow(ctx, ctx.request);
			break;
		/** 云呼叫-回拨 **/
		case 12:
			oResult = null;
			break;
		/** 增加话机 **/
		case 38:
			oResult = null;
			break;
		/** 更新话机 **/
		case 39:
			oResult = null;
			break;
		/** 删除话机 **/
		case 40:
			oResult = null;
			break;
		/** 读取密码 **/
		case 41:
			oResult = null;
			break;
		/** 解锁话机 **/
		case 42:
			oResult = null;
			break;
		/** 锁话机 **/
		case 43:
			oResult = null;
			break;
		}
		return oResult;
	}
	


}
