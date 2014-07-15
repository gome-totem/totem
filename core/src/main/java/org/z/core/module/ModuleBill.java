package org.z.core.module;

import java.util.Date;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.Context;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.dict.Global.OrderState;
import org.z.global.dict.Global.PayCode;
import org.z.global.dict.Global.PayMode;
import org.z.global.dict.Global.PayState;
import org.z.global.dict.Global.RecordState;
import org.z.global.environment.Business.Bill;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ModuleBill implements ServiceIntf {
	protected final Logger logger = LoggerFactory.getLogger(ModuleBill.class);
	protected String dbName = "finance";
	public static ModuleBill self = null;
	private static final String addRecordDesc = "您的账户余额会在1个工作日内由财务审核后,才能更新";

	public ModuleBill() {
	}

	@Override
	public boolean init(boolean isReload) {
		self = this;
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
		return "bill";
	}

	public BasicDBObject readBefore(Context context, BasicDBObject oReq) {
		long time = System.currentTimeMillis() - 3 * Const.Day1Milis;
		BasicDBObject oTime = new BasicDBObject();
		BasicDBObject qField = new BasicDBObject().append("timestamp", oTime);
		oTime.append("$lte", time);
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		int pageNumber = oReq.getInt("pageNumber");
		pageNumber -= 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		int pageSize = oReq.getInt("pageSize");
		pageSize = pageSize > 10 ? 10 : pageSize;
		BasicDBObject sortField = new BasicDBObject().append("timestamp", -1);
		DataResult dr = DataCollection.find(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField, sortField, null, pageNumber
				* pageSize, pageSize);
		oResult.append("totalCount", dr.totalCount);
		int i = 0;
		while (i < dr.list.size()) {
			BasicDBObject oItem = (BasicDBObject) dr.list.get(i);
			if (!oItem.containsField("amount")) {
				dr.list.remove(i);
				continue;
			}
			oItem.removeField("_id");
			oItem.append("ago", context.tool.dateDiff(oItem.getLong("timestamp")));
			oItem.append("date", context.tool.formatDate(oItem.getLong("timestamp"), "yyyy-MM-dd HH:mm"));
			i++;
		}
		oResult.append("items", dr.list);
		return oResult.append("xeach", true);
	}

	public BasicDBObject addBill(Context context, BasicDBObject oReq) {
		PayCode payCode = PayCode.values()[oReq.getInt("payCode")];
		String orderIds = oReq.getString("orderIds", "");
		int amount = oReq.getInt("amount");
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		StringBuilder desc = new StringBuilder();
		switch (payCode) {
		case Order:
			desc.append("充值[订单:");
			desc.append(orderIds);
			desc.append("]");
			break;
		case Recharge:
			desc.append("充值");
			break;
		case AirTicket:
			desc.append("充值机票订单[订单:");
			desc.append(orderIds);
			desc.append("]");
			break;
		}
		String billId = this
				.addRecord(context.user.getServer(), context.user.getUserId(), payCode, PayMode.PendingInc, desc.toString(), 0, orderIds, 0, amount);
		if (StringUtil.isEmpty(billId)) {
			oResult.append("message", "创建付款凭证失败，请联系一起嗨客服,电话010-56216655");
			return oResult;
		}
		oResult.append("billId", billId);
		return oResult.append("xeach", true);
	}

	/* 增加一条账单记录 */
	public String addRecord(String server, long userId, PayCode code, PayMode mode, String desc, long shortId, String orderIds, int balance, int amount) {
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("code", code.ordinal());
		oRecord.append("mode", mode.ordinal());
		oRecord.append("desc", desc);
		oRecord.append("shortId", shortId);
		oRecord.append("orderIds", orderIds);
		oRecord.append("amount", amount);
		oRecord.append("balance", balance);
		oRecord.append("timestamp", System.currentTimeMillis());
		return DataCollection.insert(server, "finance", "bill_" + userId, oRecord).toString();
	}

	/* 读取账户余额 */
	public BasicDBObject readBalance(Context context) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		oResult.append("amount", 0);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField);
		if (one == null) {
			return oResult.append("state", PayState.AccountAbnormal.ordinal());
		}
		int balance = one.getInt("balance");
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select balance,income,withdraw,income_total,withdraw_total,sms_count from user where user_id=?",
				new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		int balance_ = Integer.parseInt(rows[0][0]);
		if (balance_ != balance) {
			return oResult.append("state", PayState.AccountAbnormal.ordinal());
		}
		oResult.append("balance", balance).append("income", Integer.parseInt(rows[0][1])).append("withdraw", Integer.parseInt(rows[0][2]))
				.append("income_total", Integer.parseInt(rows[0][3])).append("withdraw_total", Integer.parseInt(rows[0][4]))
				.append("sms_count", Integer.parseInt(rows[0][5])).append("state", PayState.Success.ordinal());
		return oResult.append("xeach", true);
	}

	// 检查当前用户的账户状态是否正常
	public BasicDBObject checkAccountBalance(long userId, boolean include) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select balance,balance_id,server from user where user_id=?",
				new String[] { String.valueOf(userId) }, 0, 0);
		if (rows == null || rows.length == 0) {
			return oResult.append("message", "用户记录不存在");
		}
		int balance_ = Integer.parseInt(rows[0][0]);
		String balanceId = rows[0][1];
		String server = rows[0][2];
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(balanceId));
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(server, dbName, "bill_" + userId, qField);
		if (one == null) {
			return oResult.append("message", "用户账单不存在");
		}
		int balance = one.getInt("balance");
		if (balance_ != balance) {
			return oResult.append("message", "财务账户不正常,请立即与一起海网站联系");
		}
		oResult.append("balance", balance);
		if (include == true) {
			oResult.append("balanceId", balanceId);
			oResult.append("server", server);
		}
		return oResult.append("xeach", true);
	}

	// 申请提现:
	public BasicDBObject requestWithdraw(Context context, int amount) {
		BasicDBObject oResult = new BasicDBObject().append("state", PayState.AccountAbnormal.ordinal()).append("withdraw", 0).append("balance", 0);
		// 1：检查流水账和用户表的财务数据是否一致
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField);
		if (one == null) {
			return oResult;
		}
		int withdraw = one.getInt("withdraw");
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select balance,withdraw from user where user_id=?",
				new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		int balance = one.getInt("balance");
		int balance_ = Integer.parseInt(rows[0][0]);
		if (balance_ != balance) {
			return oResult;
		}
		int withdraw_ = Integer.parseInt(rows[0][1]);
		if (withdraw != withdraw_) {
			return oResult;
		}
		// 2: 检查余额是否大于100元
		if (balance < amount || amount % 100 != 0) {
			return oResult.append("state", PayState.NoMoney.ordinal());
		}
		// 3：将余额减少，增加到提现中
		withdraw += amount;
		balance -= amount;
		String billId = addRecord(context.user.getServer(), context.user.getUserId(), PayCode.Withdraw, PayMode.Dec, "申请提现", 0, "", balance, amount).toString();
		DataCollection.update(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField,
				new BasicDBObject().append("$set", new BasicDBObject().append("withdraw", withdraw).append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=?,withdraw=? where user_id=?",
				new String[] { String.valueOf(balance), String.valueOf(withdraw), String.valueOf(context.user.getUserId()) });
		// 4：产生一张提现单，供yiqihi财务人员审核
		DataSet.insert(
				Const.defaultMysqlServer,
				Const.defaultMysqlDB,
				"insert into withdraw(billId,user_id,user_name,amount,state,timestamp,message)values(?,?,?,?,?,?,?)",
				new String[] { billId, String.valueOf(context.user.getUserId()), context.user.getUserName(), String.valueOf(amount),
						String.valueOf(PayState.Pending.ordinal()), String.valueOf(System.currentTimeMillis()), "申请" });
		return oResult.append("withdraw", withdraw).append("balance", balance).append("state", PayState.Success.ordinal());
	}

	public BasicDBObject payAirTicketOrder(String userServer, long userId, String balanceId, String orderIdStr, int balance) {
		BasicDBObject oResult = new BasicDBObject();
		BasicDBList success = new BasicDBList();
		BasicDBList fail = new BasicDBList();
		oResult.append("success", success);
		oResult.append("fail", fail);
		String[] orderIds = orderIdStr.split("-");
		int amount = 0;
		for (String orderId : orderIds) {
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
					"select totalPrice,updateTime,status,customerUserId from `ticketorder` where id=?", new String[] { orderId }, 0, 0);
			BasicDBObject oPay = new BasicDBObject().append("id", orderId);
			if (rows == null || rows.length == 0) {
				oPay.append("status", "订单不存在");
				fail.add(oPay);
				continue;
			}
			if (userId != Long.parseLong(rows[0][3])) {
				oPay.append("status", "您不是订单的客人");
				fail.add(oPay);
				continue;
			}
			int state =Integer.parseInt(rows[0][2]);
			if (state>= 3) {
				oPay.append("status", "订单已经支付");
				fail.add(oPay);
				continue;
			}
			int money = (int) (Float.parseFloat(rows[0][0]));
			// 1：检查是否有余额支付
			if (balance < money)
				break;
			// 2: 为当前用户增加流水记录，减少他的账户余额
			amount += money;
			balance -= money;
			this.addRecord(userServer, userId, PayCode.AirTicket, PayMode.Dec, "支付机票订单[" + orderId + "]", 0L, orderId, balance, money);
			BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(balanceId));
			DataCollection.update(userServer, dbName, "bill_" + userId, qField,
					new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
					String.valueOf(userId) });
			// 3: 更新订单状态
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "UPDATE `ticketorder` set updateTime=?,status=? where id=?", new Object[] { new Date(),
					3, orderId });
			success.add(orderId);
		}
		oResult.append("balance", balance);
		oResult.append("amount", amount);
		return oResult;
	}

	public BasicDBObject payOrder(String userServer, long userId, String balanceId, String orderIdStr, int balance) {
		BasicDBObject oResult = new BasicDBObject();
		BasicDBList success = new BasicDBList();
		BasicDBList fail = new BasicDBList();
		oResult.append("success", success);
		oResult.append("fail", fail);
		String[] orderIds = orderIdStr.split("-");
		int amount = 0;
		for (String orderId : orderIds) {
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
					"select prepaidPrice,rate,paidTime,status,customerUserId from `order` where id=?", new String[] { orderId }, 0, 0);
			BasicDBObject oPay = new BasicDBObject().append("id", orderId);
			if (rows == null || rows.length == 0) {
				oPay.append("status", "订单不存在");
				fail.add(oPay);
				continue;
			}
			if (userId != Long.parseLong(rows[0][4])) {
				oPay.append("status", "您不是订单的客人");
				fail.add(oPay);
				continue;
			}
			OrderState oState = OrderState.values()[Integer.parseInt(rows[0][3])];
			if (oState.ordinal() >= OrderState.Paid.ordinal()) {
				oPay.append("status", "订单已经支付");
				fail.add(oPay);
				continue;
			}
			int money = (int) (Float.parseFloat(rows[0][0]) * Float.parseFloat(rows[0][1]));
			// 1：检查是否有余额支付
			if (balance < money)
				break;
			// 2: 为当前用户增加流水记录，减少他的账户余额
			amount += money;
			balance -= money;
			this.addRecord(userServer, userId, PayCode.Order, PayMode.Dec, "支付订单[" + orderId + "]", 0L, orderId, balance, money);
			BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(balanceId));
			DataCollection.update(userServer, dbName, "bill_" + userId, qField,
					new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
					String.valueOf(userId) });
			// 3: 更新订单状态
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "UPDATE `order` set paidTime=?,status=? where id=?", new Object[] { new Date(),
					OrderState.Paid.ordinal(), orderId });
			success.add(orderId);
		}
		oResult.append("balance", balance);
		oResult.append("amount", amount);
		return oResult;
	}

	public BasicDBObject payUseBalance(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = checkAccountBalance(context.user.getUserId(), true);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		int balance = oResult.getInt("balance");
		PayCode payCode = PayCode.values()[oReq.getInt("payCode")];
		String orderIds = oReq.getString("orderIds");
		int amount = oReq.getInt("amount");
		switch (payCode) {
		case Order:
			oResult = payOrder(oResult.getString("server"), context.user.getUserId(), oResult.getString("balanceId"), orderIds, balance);
			break;
		}
		return oResult.append("xeach", amount == oResult.getInt("amount"));
	}

	public BasicDBObject buyPhoneCard(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = checkAccountBalance(context.user.getUserId(), false);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		PayCode code = PayCode.PhoneCard;
		int amount = 0;
		switch (oReq.getInt("cardType")) {
		case 1:
			amount = 30;
			break;
		case 2:
			amount = 50;
			break;
		case 3:
			amount = 100;
			break;
		default:
			amount = 30;
			break;
		}
		int balance = oResult.getInt("balance");
		// 2：检查是否有余额支付
		if (balance < amount) {
			return oResult.append("message", "您的现金账户余额不足" + amount + "元,请立即充值!");
		}
		// 3: 为当前用户增加流水记录，减少他的账户余额
		String orderId = StringUtil.createUniqueID();
		balance -= amount;
		ServiceFactory.bill().addRecord(context.user.getServer(), context.user.getUserId(), code, PayMode.Dec, "购买云通讯", 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		DataCollection.update(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField,
				new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
				String.valueOf(context.user.getUserId()) });
//		if (!ServiceFactory.call().charge(String.valueOf(context.user.getUserId()), amount).equals("0")) {
//			logger.info("buyPhoneCard={}={}", new String[] { String.valueOf(context.user.getUserId()), String.valueOf(amount) });
//			return oResult.append("message", "现金扣款成功,但本次充值异常终止,<br>为保证您的权益,请立即与一起嗨客户联系!");
//		}
		return oResult.append("xeach", true);
	}

	public BasicDBObject buyUserCard(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = checkAccountBalance(context.user.getUserId(), false);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		PayCode code = PayCode.UserCard;
		int amount = 0;
		long licence = 0;
		switch (oReq.getInt("cardType")) {
		case 1:
			amount = 99;
			licence += Const.Day1Milis * 30;
			break;
		case 3:
			amount = 199;
			licence += Const.Day1Milis * 90;
			break;
		case 12:
			amount = 399;
			licence += Const.Day1Milis * 360;
			break;
		default:
			amount = 99;
			licence += Const.Day1Milis * 30;
			break;
		}
		int balance = oResult.getInt("balance");
		// 2：检查是否有余额支付
		if (balance < amount) {
			return oResult.append("message", "您的现金账户余额不足" + amount + "元,请立即充值!");
		}
		// 3: 为当前用户增加流水记录，减少他的账户余额
		String orderId = StringUtil.createUniqueID();
		balance -= amount;
		addRecord(context.user.getServer(), context.user.getUserId(), code, PayMode.Dec, "购买会员卡", 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		DataCollection.update(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField,
				new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
				String.valueOf(context.user.getUserId()) });
//		if (!ServiceFactory.call().charge(String.valueOf(context.user.getUserId()), amount).equals("0")) {
//			logger.info("buyUserCard={}={}", new String[] { String.valueOf(context.user.getUserId()), String.valueOf(amount) });
//		}
		long time = System.currentTimeMillis();
		if (context.user.getLicence() <= time) {
			licence += time;
		} else {
			licence += context.user.getLicence();
		}
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set licence=? where user_id=?", new String[] { String.valueOf(licence),
				String.valueOf(context.user.getUserId()) });
		context.user.setLicence(licence);
		context.user.updateUserCache();
		return oResult.append("xeach", true);
	}

	public BasicDBObject chargePhoneMoney(Context context, int amount) {
		// 1: 检查当前用户的账户状态是否正常
		BasicDBObject oResult = checkAccountBalance(context.user.getUserId(), false);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		int balance = oResult.getInt("balance");
		PayCode code = PayCode.PhoneCard;
		// 2：检查是否有余额支付
		if (balance < amount) {
			return oResult.append("message", "您的现金账户余额不足" + amount + "元,<a class='underline link' target='_blank' href='/finance'>点击此处充值</a>");
		}
		// 3: 为当前用户增加流水记录，减少他的账户余额
		String orderId = StringUtil.createUniqueID();
		balance -= amount;
		addRecord(context.user.getServer(), context.user.getUserId(), code, PayMode.Dec, "购买电话卡", 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		DataCollection.update(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField,
				new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
				String.valueOf(context.user.getUserId()) });
//		if (!ServiceFactory.call().charge(String.valueOf(context.user.getUserId()), amount).equals("0")) {
//			logger.info("chargePhoneCard={}={}", new String[] { String.valueOf(context.user.getUserId()), String.valueOf(amount) });
//			return oResult.append("message", "现金扣款成功,但云电话依然不能使用,<br>请立即与我们联系!");
//		}
		return oResult.append("xeach", true);
	}

	public BasicDBObject buyAppointService(Context context, int amount, String title) {
		// 1: 检查当前用户的账户状态是否正常
		BasicDBObject oResult = checkAccountBalance(context.user.getUserId(), false);
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
		addRecord(context.user.getServer(), context.user.getUserId(), code, PayMode.Dec, title, 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		DataCollection.update(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField,
				new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
				String.valueOf(context.user.getUserId()) });
		return oResult.append("xeach", true);
	}

	public BasicDBObject backAppointService(long userId, int amount, String title) {
		// 1: 检查当前用户的账户状态是否正常
		BasicDBObject oResult = checkAccountBalance(userId, true);
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
		addRecord(server, userId, code, PayMode.Inc, "退款-" + title, 0, orderId, balance, amount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(balanceId));
		DataCollection.update(server, dbName, "bill_" + userId, qField, new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)),
				false);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
				String.valueOf(userId) });
		return oResult.append("xeach", true);
	}

	public BasicDBObject buyDizService(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = checkAccountBalance(context.user.getUserId(), false);
		if (oResult.getBoolean("xeach", true) == false)
			return oResult;
		oResult.append("xeach", false);
		int balance = oResult.getInt("balance");
		PayCode code = PayCode.PhoneCard;
		int year = oReq.getInt("year");
		int yearAmount = 0;
		int smsCount = 0;
		if (year == 1) {
			yearAmount = 36;
			smsCount = 5;
		} else if (year == 3) {
			yearAmount = 100;
			smsCount = 10;
		} else if (year == 5) {
			yearAmount = 150;
			smsCount = 15;
		} else {
			return oResult.append("message", "购买年数不正确");
		}
		// 2：检查是否有余额支付
		if (balance < yearAmount) {
			return oResult.append("message", "您的现金账户余额不足" + yearAmount + "元,请立即充值!");
		}
		// 3: 为当前用户增加流水记录，减少他的账户余额
		String orderId = StringUtil.createUniqueID();
		balance -= yearAmount;
		addRecord(context.user.getServer(), context.user.getUserId(), code, PayMode.Dec, "购买云服务", 0, orderId, balance, yearAmount);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(context.user.getBalanceId()));
		DataCollection.update(context.user.getServer(), dbName, "bill_" + context.user.getUserId(), qField,
				new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)), false);
		long licence = context.user.getLicence();
		if (licence == 0) {
			licence = System.currentTimeMillis();
		}
		licence += (long) year * 360 * Const.Day1Milis;
		context.user.setLicence(licence);
		context.user.updateUserCache();
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=?,licence=?,sms_count=sms_count+? where user_id=?",
				new String[] { String.valueOf(balance), String.valueOf(licence), String.valueOf(smsCount), String.valueOf(context.user.getUserId()) });
		return oResult.append("xeach", true);
	}

	public boolean create(long userId) {
		String sql = "select server,user_name from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(userId) }, 0, 0);
		if (rows == null || rows.length == 0)
			return false;
		String server = rows[0][0];
		String userName = rows[0][1];
		BasicDBObject oField = new BasicDBObject().append("balance", 0).append("income", 0).append("withdraw", 0)
				.append("timestamp", System.currentTimeMillis()).append("userId", userId).append("userName", userName);
		String balanceId = DataCollection.insert(server, "finance", "bill_" + userId, oField).toString();
		if (StringUtil.isEmpty(balanceId))
			return false;
		DataCollection.createIndex(server, "finance", "bill_" + userId, "keyIdx", new BasicDBObject().append("shortId", 1), false);
		sql = "update user set balance_id=?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { balanceId, String.valueOf(userId) });
		return true;
	}

	// 充值业务流程：
	public PayState recharge(String server, String uId, String billId, String money) {
		return this.recharge(server, uId, billId, money, "");
	}

	public PayState recharge(String server, String uId, String billId, String money, String email) {
		long userId = Long.parseLong(uId);
		String dbName = "finance";
		int amount = Math.round(Float.parseFloat(money));

		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select server,balance_id,balance from user where user_id=?",
				new String[] { String.valueOf(userId) }, 0, 0);
		if (rows == null || rows.length == 0) {
			return PayState.NoRecord;
		}
		String[] userInfos = new String[] { rows[0][0], rows[0][1], rows[0][2] };
		if (!userInfos[0].equalsIgnoreCase(server))
			return PayState.Fail;
		// 1：检查充值业务是否存在
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(billId));
		BasicDBObject oBill = (BasicDBObject) DataCollection.findOne(server, dbName, "bill_" + userId, qField);
		if (oBill == null) {
			return PayState.NoRecord;
		}
		// 2：检查充值流水单的状态和金额是否正确
		if (oBill.getInt("mode") != PayMode.PendingInc.ordinal()) {
			return PayState.Fail;
		}
		if (oBill.getInt("amount") != amount) {
			return PayState.Fail;
		}
		// 3: 检查当前用户的账户状态是否正常
		qField = new BasicDBObject().append("_id", new ObjectId(userInfos[1]));
		BasicDBObject oBalance = (BasicDBObject) DataCollection.findOne(server, dbName, "bill_" + userId, qField);
		if (oBalance == null) {
			return PayState.NoRecord;
		}
		int balance = oBalance.getInt("balance");
		if (balance != Integer.parseInt(userInfos[2])) {
			return PayState.Fail;
		}
		balance += amount;
		// 4:更新充值流水记录状态
		qField = new BasicDBObject().append("_id", new ObjectId(billId));
		BasicDBObject oUpdateField = new BasicDBObject().append(
				"$set",
				new BasicDBObject().append("mode", PayMode.Inc.ordinal()).append("timestamp", System.currentTimeMillis()).append("email", email)
						.append("balance", balance));
		DataCollection.update(server, dbName, "bill_" + userId, qField, oUpdateField, false);
		PayCode code = PayCode.values()[oBill.getInt("code")];
		BasicDBObject oPayResult = null;
		PayState state = null;
		switch (code) {
		case Order:
			// 消费余额，冲减订单
			String orderIdStr = oBill.getString("orderIds");
			oPayResult = this.payOrder(server, userId, userInfos[1], orderIdStr, balance);
			state = oPayResult.getInt("amount") > 0 ? PayState.Success : PayState.Fail;
			break;
		case AirTicket:
			// 消费余额，冲减机票订单
			orderIdStr = oBill.getString("orderIds");
			oPayResult = this.payAirTicketOrder(server, userId, userInfos[1], orderIdStr, balance);
			state = oPayResult.getInt("amount") > 0 ? PayState.Success : PayState.Fail;
			break;
		case Recharge:
			// 更新余额
			qField = new BasicDBObject().append("_id", new ObjectId(userInfos[1]));
			DataCollection.update(server, dbName, "bill_" + userId, qField, new BasicDBObject().append("$set", new BasicDBObject().append("balance", balance)),
					false);
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user set balance=? where user_id=?", new String[] { String.valueOf(balance),
					String.valueOf(userId) });
			state = PayState.Success;
			break;
		}
		return state;
	}

	// jsp中调用:在流水账单增加一条充值记录,并记录其Id2, 将Id放入SESSION中
	public String addRecharge(Context context, long shortId, String orderId, int amount) {
		PayCode code = PayCode.Recharge;
		PayMode mode = PayMode.PendingInc;
		String desc = "充值";
		return addRecord(context.user.getServer(), context.user.getUserId(), code, mode, desc, (long) code.ordinal(), "", 0, amount).toString();
	}

	public String addRecharge(Context context, DBObject o) {
		BasicDBObject oReq = (BasicDBObject) o;
		long shortId = oReq.getLong("shortId");
		String orderId = oReq.getString("orderId");
		int amount = oReq.getInt("amount");
		return this.addRecharge(context, shortId, orderId, amount);
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		BasicDBObject oResult = null;
		int funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		Bill bill = Bill.values()[funcIndex];
		switch (bill) {
		/** 读取账单详情 */
		case read:
			oResult = this.readBefore(ctx, ctx.request);
			break;
		/** 用户提现 **/
		case requestWithdraw:
			oResult = this.requestWithdraw(ctx, ctx.request.getInt("amount"));
			break;
		/** 提现结算 **/
		case readWithdraw:
			oResult = null;
			break;
		/** 确认=提现结算 **/
		case postWithdraw:
			oResult = null;
			break;
		case buyPhoneCard:
			oResult = this.buyPhoneCard(ctx, ctx.request);
			break;
		case buyUserCard:
			oResult = this.buyUserCard(ctx, ctx.request);
			break;
		case addRecharge:
			oResult = new BasicDBObject();
			oResult.append("billId", this.addRecharge(ctx, ctx.request));
			break;
		case recharge:
			oResult = new BasicDBObject();
			oResult.append(
					"state",
					this.recharge(ctx.request.getString("server"), ctx.request.getString("uId"), ctx.request.getString("billId"),
							ctx.request.getString("money")).name());
			break;
		case addRecord:
			oResult = addRecord(ctx);
			break;
		case checkRecord:
			oResult = checkRecord(ctx);
			break;
		case moreRecord:
			oResult = new BasicDBObject();
			oResult.append("items", getReceipts(Integer.parseInt(ctx.request.getString("count")))).append("xeach", true);
			break;
		case addBill:
			oResult = this.addBill(ctx, ctx.request);
			break;
		case payUseBalance:
			oResult = this.payUseBalance(ctx, ctx.request);
			break;
		}
		return oResult;
	}

	private BasicDBObject checkRecord(Context ctx) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		if (ctx.user.getRole().isAccounter() == false) {
			oResult.append("message", "没有权限");
			return oResult;
		}
		boolean flag = Boolean.valueOf(ctx.request.getString("flag"));
		String receiveReceipt = ctx.request.getString("receiveReceipt");
		String description = ctx.request.getString("description");
		int id = Integer.parseInt(ctx.request.getString("id", "0"));
		int state = 0;
		if (flag) {
			state = RecordState.Close.ordinal();
		} else {
			state = RecordState.Expire.ordinal();
		}
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update receipts set receiveReceipt=?,description=?,state=?,updateTime=? where id = ?",
				new Object[] { receiveReceipt, description, state, new Date(), id });
		if (flag) {
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select objectId,amount from receipts  where id = ?",
					new Object[] { id }, 0, 0);
			if (rows.length > 0) {
				String billId = rows[0][0];
				PayState payState = recharge(ctx.user.getServer(), String.valueOf(ctx.user.getUserId()), billId, rows[0][1], "");
				if (payState != PayState.Success) {
					oResult.append("xeach", false).append("message", "支付失败,代码:" + payState.name());
					return oResult;
				}
			}
		}
		return oResult.append("xeach", true);
	}

	public BasicDBObject addRecord(Context ctx) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		BasicDBObject request = ctx.request;
		try {
			String id = this.addRecord(ctx.user.getServer(), ctx.user.getUserId(), PayCode.Recharge, PayMode.PendingInc, addRecordDesc, 0, "0", 0,
					Integer.parseInt(request.getString("amount")));
			DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB,
					"insert into receipts(userId,userName,paymentReceipt,objectId,amount,state,createTime)values(?,?,?,?,?,?,?)",
					new Object[] { ctx.user.getUserId(), ctx.user.getUserName(), request.getString("receipt"), id, Float.valueOf(request.getString("amount")),
							RecordState.New.ordinal(), new Date() });
			return oResult.append("xeach", true).append("msg", addRecordDesc);
		} catch (Exception e) {
			return oResult.append("msg", "提交财务审核错误,重新提交");
		}
	}

	public int getReceiptCount() {
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from `receipts` where state=?",
				new Object[] { RecordState.New.ordinal() }, 0, 0);
		if (rows.length >= 0) {
			return Integer.parseInt(rows[0][0]);
		}
		return 0;
	}

	public BasicDBList getReceipts(int count) {
		BasicDBList receipts = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select * from `receipts` where state = ? order by createTime desc ", new Object[] { RecordState.New.ordinal() }, 0, count);
		BasicDBObject receipt;
		for (Object obj : receipts) {
			receipt = (BasicDBObject) obj;
			receipt.append("createTime", new DateTime(receipt.getLong("createTime")).toString("yyyy-MM-dd"));
		}
		return receipts;
	}

	protected void test1(String server, long userId, String billId, int balance, int withdraw, int income) {
		BasicDBObject oReq = new BasicDBObject().append("_id", new ObjectId(billId));
		BasicDBObject oRec = new BasicDBObject().append("balance", balance).append("withdraw", withdraw).append("income", income);
		DataCollection.update(server, "finance", "bill_" + userId, oReq, new BasicDBObject().append("$set", oRec), false);
	}

	/* clear user balance */
	protected void initUserAccount(String userServer, long userId, String userName, String balanceId) {
		DataCollection.remove(userServer, "finance", "bill_" + userId, new BasicDBObject());
		BasicDBObject oField = null;
		if (StringUtil.isEmpty(balanceId)) {
			oField = new BasicDBObject().append("balance", 0).append("income", 0).append("withdraw", 0).append("timestamp", System.currentTimeMillis())
					.append("userId", userId).append("userName", userName);
		} else {
			oField = new BasicDBObject().append("_id", new ObjectId(balanceId)).append("balance", 0).append("income", 0).append("withdraw", 0)
					.append("timestamp", System.currentTimeMillis()).append("userId", userId).append("userName", userName);
		}
		balanceId = DataCollection.insert(userServer, "finance", "bill_" + userId, oField).toString();
		DataCollection.createIndex(userServer, "finance", "bill_" + userId, "keyIdx", new BasicDBObject().append("shortId", 1), false);
		String sql = "update user set balance=0,income=0,withdraw=0,income_total=0,withdraw_total=0,sms_count=0,balance_id=?  where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { balanceId, String.valueOf(userId) });
	}

	public static void main(String[] args) {
		int mode = 0;
		ModuleBill bill = new ModuleBill();
		switch (mode) {
		// 清除全部账户
		case 0:
			String sql = "select user_id,user_name,server,balance_id from user where user_id=?";
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Object[] { 33291 }, 0, 0);
			long userId = Long.parseLong(rows[0][0]);
			String userName = rows[0][1];
			String server = rows[0][2];
			String balanceId = rows[0][3];
			bill.initUserAccount(server, userId, userName, balanceId);
			System.out.println("clear user Finance userId=" + userId + "&userName=" + userName);
			break;
		// 检查财务账户
		case 1:
			bill.checkAccountBalance(32125, true);
			break;
		case 2:
			userId = 32125;
			System.out.println(bill.create(userId));
			break;
		case 3:
			userId = 32543;
			server = Const.defaultMysqlServer;
			String orderId = "53426f79c8f2b133f3105bc6";
			String amount = "10000";
			System.out.print(bill.recharge(server, String.valueOf(userId), orderId, amount));
			break;
		}
	}

}
