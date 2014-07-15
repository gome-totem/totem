package org.z.core.module;

import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.common.cache.UserCache;
import org.z.core.common.Context;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.environment.Config;
import org.z.global.environment.Const;
import org.z.global.object.SecurityObject;
import org.z.global.object.UserRole;
import org.z.global.util.CompressTool;
import org.z.global.util.JodaUtil;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataSet;

import cn.emay.sdk.client.api.Client;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ModuleSMS implements ServiceIntf {

	protected boolean isAlive = true;
	protected static final Logger logger = LoggerFactory.getLogger(ModuleSMS.class);
	protected Client client = null;
	protected String smsKey = Config.rock().getItem("Sms-Key", "");
	protected String smsPassword = Config.rock().getItem("Sms-Password", "");

	@Override
	public boolean init(boolean isReload) {
		if (StringUtil.isEmpty(smsKey) || StringUtil.isEmpty(smsPassword)) {
			return true;
		}
		try {
			client = new Client(smsKey, smsPassword);
		} catch (Exception e) {
			logger.info("init", e);
			return false;
		}
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
		return "sms";
	}

	public BasicDBObject sendAuth(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String c = UserCache.getCaptcha(context.cookieId);
		if (StringUtils.isEmpty(c) || !c.equalsIgnoreCase(oReq.getString("code"))) {
			return oResult.append("message", "机器验证码输入错误");
		}
		String mobile = oReq.getString("mobile");
		if (StringUtil.isEmpty(mobile) || !StringUtils.isNumeric(mobile)) {
			return oResult.append("message", "手机号必须是全数字。");
		}
		if (mobile.length() != 11) {
			return oResult.append("message", "手机号必须是11位。");
		}
		String sql = "select bind_mobile,sms_count,licence from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		if (rows.length == 0) {
			return oResult.append("message", "用户不存在");
		}
		if (rows[0][0].equalsIgnoreCase("1")) {
			return oResult.append("message", "该账号已经绑定了手机，请先取消绑定。");
		}
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Do, mobile));
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id from user where mobile=?", new String[] { values[0] }, 0, 0);
		if ((rows != null && rows.length >= 1) && (Long.parseLong(rows[0][0]) != context.user.getUserId())) {
			return oResult.append("message", "该手机号已经被使用，\r\n如果您的手机号被<a target='_blank' class='underline' href='/i/" + rows[0][0] + "'>该用户</a>使用了，请联系我们。");
		}
		String code = RandomStringUtils.randomNumeric(6);
		oResult.putAll((Map<String, Object>) send(String.valueOf(context.user.getUserId()), new String[] { mobile }, "感谢您选择一起嗨(www.yiqihi.com),您的手机激活码是:"
				+ code, 100));
		if (oResult.getBoolean("xeach")) {
			sql = "update user set mobile=? where user_id=?";
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Object[] { values[0], context.user.getUserId() });
			sql = "insert into user_bind(user_id,mobile_key)values(?,?) ON DUPLICATE KEY UPDATE mobile_key=values(mobile_key)";
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()), code });

			UserCache.setCaptcha(context.cookieId, CompressTool.createPassword(6));
		}
		return oResult;
	}

	public BasicDBObject checkAuth(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String sql = "select bind_mobile,password,call_server,licence,mobile from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		if (rows.length == 0) {
			return oResult.append("message", "用户不存在");
		}
		if (rows[0][0].equalsIgnoreCase("1")) {
			return oResult.append("message", "该账号已经绑定了手机!");
		}
		String mobile = rows[0][4];
		String reqMobile = oReq.getString("mobile");
		sql = "select mobile_key from user_bind where user_id=?";
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		if (rows.length == 0 || StringUtils.isEmpty(rows[0][0])) {
			return oResult.append("message", "请先发送激活码到手机");
		}
		if (!rows[0][0].equalsIgnoreCase(oReq.getString("code"))) {
			return oResult.append("message", "6位数字激活码输入错误");
		}
		if (StringUtils.isEmpty(reqMobile)) {
			return oResult.append("message", "手机号码不能为空");
		}
		String vosPasswd = CompressTool.createPassword(30);
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.UserPassword, SecurityMode.Do, vosPasswd),
				SecurityObject.create(SecurityType.Email, SecurityMode.Undo, mobile));
		if (!reqMobile.equalsIgnoreCase(values[1])) {
			return oResult.append("message", "手机号码不一致.");
		}
		sql = "update user_bind set mobile_key='' where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) });
		sql = "update user set bind_mobile=1 where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) });
		context.user.setBindMobile(1);
		context.user.updateUserCache();
		return oResult.append("xeach", true);
	}

	public BasicDBObject sendUnbindAuth(Context context) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String sql = "select bind_mobile,mobile from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Long[] { context.user.getUserId() }, 0, 0);
		if (rows.length <= 0) {
			return oResult.append("message", "用户不存在");
		}
		if (rows[0][0].equalsIgnoreCase("0")) {
			return oResult.append("message", "该账号未绑定手机!");
		}
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Undo, rows[0][1]));
		if (values == null) {
			return oResult.append("message", "服务器出现问题");
		}
		String code = RandomStringUtils.randomNumeric(6);
		oResult.putAll((Map<String, Object>) send(String.valueOf(context.user.getUserId()), values, "感谢您选择一起嗨(www.yiqihi.com),您的手机验证码是:" + code, 100));
		if (oResult.getBoolean("xeach")) {
			sql = "update user_bind set mobile_key=? where user_id=?";
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Object[] { code, context.user.getUserId() });
		}
		return oResult;
	}

	public BasicDBObject unbindAuth(Context context) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String sql = "select mobile_key from user_bind where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) }, 0, 0);
		if (rows.length == 0 || StringUtils.isEmpty(rows[0][0])) {
			return oResult.append("message", "请先发送激活码到手机");
		}
		if (!rows[0][0].equalsIgnoreCase(context.request.getString("code"))) {
			return oResult.append("message", "6位数字激活码输入错误");
		}
		sql = "update user_bind set mobile_key='' where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) });
		sql = "update user set bind_mobile=0,mobile=? where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { "", String.valueOf(context.user.getUserId()) });
		context.user.setBindMobile(0);
		context.user.updateUserCache();
		return oResult.append("xeach", true);
	}

	public BasicDBObject send(String userId, String[] mobiles, String content, int priority) {
		BasicDBObject oResult = new BasicDBObject();
		String sql = "select role from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId }, 0, 0);
		if (rows.length <= 0) {
			return oResult.append("xeach", false).append("message", "用户不存在");
		}
		UserRole role = new UserRole(Long.parseLong(rows[0][0]));
		boolean flag = role.isAccounter() || role.isCustomerService() || role.isRoot();
		if (!flag) {
			DateTime now = new DateTime();
			String time = now.toString("yyyy-MM-dd");
			DateTime startTime = JodaUtil.toDateTime(time, "yyyy-MM-dd");
			DateTime endTime = JodaUtil.toDateTime(time + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
			sql = "select timestamp from sms_log where timestamp>=? and timestamp<=? and user_id=? order by timestamp desc";
			rows = DataSet
					.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Object[] { startTime.getMillis(), endTime.getMillis(), userId }, 0, 0);
			if (rows.length >= 5) {
				return oResult.append("xeach", false).append("message", "对不起，每个用户每天只能够发送5次短信");
			}
			long currentTime = System.currentTimeMillis();
			long timestamp = Long.parseLong(rows[0][0]);
			if (timestamp + 60000 > currentTime) {
				return oResult.append("xeach", false).append("message", "对不起，每条短信的发送时间必须大于1分钟");
			}
		}
		try {
			sql = "insert into sms_log(user_id,mobile,content,priority,timestamp)values(?,?,?,?,?)";
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < mobiles.length; i++) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append(mobiles[i]);
			}
			content += "【一起嗨】";
			DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId, buffer.toString(), content, String.valueOf(priority),
					String.valueOf(System.currentTimeMillis()) });
			oResult.append("state", client.sendSMS(mobiles, content, "", priority));
		} catch (Exception e) {
			logger.error("send", e);
		}
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
		/** 发送手机验证码 **/
		case 33:
			oResult = sendAuth(ctx, ctx.request);
			break;
		/** 校验并绑定手机 **/
		case 35:
			oResult = checkAuth(ctx, ctx.request);
			break;
		/** 取消绑定手机 **/
		case 36:
			oResult = unbindAuth(ctx);
			break;
		}
		return oResult;
	}

	public static void main(String[] args) {
		ModuleSMS sms = new ModuleSMS();
		sms.init(false);
		int mode = 1;
		switch (mode) {
		/** 激活账号 **/
		case 0:
			int state = sms.client.registEx(sms.smsPassword);
			System.out.println("注册结果:" + state);
			break;
		/** 公司地址 **/
		case 1:
			state = sms.client.sendSMS(new String[] { "13439758492" }, "公司地址:丰台区公益西桥地铁C口东亚三环3号楼1212室,网站地址:www.yiqihi.com,电话:010-60934633【一起嗨】", "", 5);
			System.out.println("发送结果:" + state);
			break;
		/** 系统已经恢复 **/
		case 2:
			state = sms.client.sendSMS(new String[] { "13718821128" }, "感谢您使用一起嗨海外旅行服务，您申报的手机激活及发布需求故障已经于8.30日更新上线，现已能正常使用，再次抱歉给您带来不便！【一起嗨】", "", 5);
			System.out.println("发送结果:" + state);
			break;
		}
	}
}
