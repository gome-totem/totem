package org.z.core.module;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.internet.MimeUtility;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.Context;
import org.z.core.common.MailRecord;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global.SecurityMode;
import org.z.global.dict.Global.SecurityType;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.object.SecurityObject;
import org.z.global.util.DateUtil;
import org.z.global.util.StringUtil;
import org.z.global.util.TextUtil;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ModuleMail implements ServiceIntf {
	public static int currentIndex = -1;
	protected static final Logger logger = LoggerFactory.getLogger(ModuleMail.class);
	protected static final String key = "xeach.yiqihi.com";
	protected static HashMap<String, MailRecord> mailAccounts = new HashMap<String, MailRecord>();

	@Override
	public boolean init(boolean isReload) {
		Properties props = System.getProperties();
		props.put("mail.smtp.localhost", "localhost");
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
		return "mail";
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		int funcIndex = 0;
		if (oReq.containsField("funcIndex")) {
			funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		}
		BasicDBObject oResult = null;
		switch (funcIndex) {
		/** 验证邮箱 **/
		case 32:
//			oResult = sendAuthMail(ctx);
			break;
		}
		return oResult;
	}

	public MailRecord readMailAccount(String domain) {
		if (mailAccounts.containsKey(domain)) {
			return mailAccounts.get(domain);
		}
		String sql = "select * from mail_dict where domain=?";
		BasicDBObject result = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { domain }, 0, 0);
		MailRecord record = new MailRecord();
		record.hostName = result.getString("host");
		record.userId = result.getString("userId");
		record.userName = result.getString("userName");
		record.password = TextUtil.decrypt(key, result.getString("password"));
		mailAccounts.put(domain, record);
		return record;
	}

	public String createProfileDoc(String userName, String loginId, String loginPassword) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailProfile");
		if (t == null)
			return "emailProfile Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("login", loginId);
		vContext.put("password", loginPassword);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			String content = writer.toString();
			return content;
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public String createInviteDoc(String userName, String title, String subTitle) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailInvite");
		if (t == null)
			return "emailInvite Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("title", title);
		vContext.put("subTitle", subTitle);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			String content = writer.toString();
			return content;
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public String createCheckOfferDoc(String loginId, String loginPassword, String userName, String title, String subTitle, String url) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailCheckOffer");
		if (t == null)
			return "emailCheckOffer Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("loginId", loginId);
		vContext.put("userName", userName);
		vContext.put("title", title);
		vContext.put("subTitle", subTitle);
		vContext.put("url", url);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			String content = writer.toString();
			return content;
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public String createMessageDoc(String userName, String content) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailMessage");
		if (t == null)
			return "emailMessage Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("content", content);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public String createAppointDoc(String userName, String title, String url, String content) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailAppoint");
		if (t == null)
			return "emailAppoint Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("title", title);
		vContext.put("url", url);
		vContext.put("content", content);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public String createRegisterDoc(String userName, String login, String password) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailRegister");
		if (t == null)
			return "emailOrder Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("login", login);
		vContext.put("password", password);
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public String createOrderDoc(String userName, String title, String url, String content) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailOrder");
		if (t == null)
			return "emailOrder Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("title", title);
		vContext.put("url", url);
		vContext.put("content", content);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}
	public boolean sendMail(String addr, String subject, String content) {
		return sendMail(addr, subject, content, null);
	}

	public boolean sendMail(String addr, String subject, String content, String fileName) {
		try {
			HtmlEmail email = new HtmlEmail();
			MailRecord rec = readMailAccount(Const.defaultMysqlDB);
			email.setHostName(rec.hostName);
			email.setAuthentication(rec.userId, rec.password);
			email.setFrom(rec.userId, rec.userName);
			email.setCharset("utf-8");
			email.setSubject(subject);
			email.setHtmlMsg(content);
			email.getToAddresses().clear();
			email.addTo(addr);
			if (StringUtil.isEmpty(fileName) == false) {
				File f = new File(fileName);
				if (f.exists() == true) {
					EmailAttachment attachment = new EmailAttachment();
					attachment.setPath(fileName);
					attachment.setName(MimeUtility.encodeText(f.getName()));
					email.attach(attachment);
				}
			}
			email.send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String createTripDoc(String userName, String title, String url, String content) {
		Template t = ModuleFactory.htmlPage().getTemplate("emailTrip");
		if (t == null)
			return "emailTrip Template is null";
		VelocityContext vContext = new VelocityContext();
		vContext.put("userName", userName);
		vContext.put("title", title);
		vContext.put("url", url);
		vContext.put("content", content);
		vContext.put("year", DateUtil.getCurrentYear());
		vContext.put("sendTime", StringUtil.formatDateTime(new Date()));
		StringWriter writer = new StringWriter();
		try {
			t.merge(vContext, writer);
			return writer.toString();
		} catch (Exception e) {
			logger.error("MailService", e);
			return "";
		}
	}

	public void sendGuideMail(Context context) {
		String sql = "select user_id,user_name,title,short_id from activity_trip where mailing=0";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
		while (rows != null && rows.length > 0) {
			for (int i = 0; i < rows.length; i++) {
				String[][] users = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select email,qq,password from user where user_id=?", new String[] { rows[i][0] }, 0, 0);
				if (users != null && users.length > 0) {
					String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Undo, users[0][0]),
							SecurityObject.create(SecurityType.Name, SecurityMode.Undo, users[0][1]), SecurityObject.create(SecurityType.UserPassword, SecurityMode.Undo, users[0][2]));
					String email = null;
					String content = null;
					if (values != null) {
						if (values[1].length() > 0 && StringUtil.isAlphanumeric(values[1])) {
							email = values[1] + "@qq.com";
							content = ServiceFactory.mail().createProfileDoc(rows[i][1], email, values[2]);
						}
						if (values[0].indexOf("@") > 1) {
							email = values[0];
							content = ServiceFactory.mail().createProfileDoc(rows[i][1], email, values[2]);
						}
					}
					if (StringUtil.isEmpty(content)) {
						DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_trip set mailing=2 where short_id=?", new String[] { rows[i][3] });
						continue;
					}
					DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into mail_job(user_id,email,title,content)values(?,?,?,?)", new String[] { rows[i][0], email,
							"能预约您做我们的私人导游吗?", content });
					DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_trip set mailing=1 where short_id=?", new String[] { rows[i][3] });
					logger.info("email=" + email + "&shortId=" + rows[i][3]);
				}
			}
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
		}
	}

//	protected boolean sendMailJob() throws EmailException, MalformedURLException {
//		String sql = "select id,email,title,content from mail_job";
//		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
//		while (rows != null && rows.length > 0) {
//			for (int i = 0; i < rows.length; i++) {
//				try {
//					HtmlEmail email = new HtmlEmail();
//					MailRecord rec = readMailAccount(Const.defaultMysqlDB);
//					email.setAuthentication(rec.userId, rec.password);
//					email.setCharset("utf-8");
//					email.setHostName(rec.hostName);
//					email.setFrom(rec.userId, rec.userName);
//					email.setSubject(rows[i][2] + ",我想预定您的服务");
//					String content = rows[i][3];
//					content = content.replaceAll("http://www.yiqihi.com/mail=" + rows[i][0], "http://www.yiqihi.com");
//					email.setHtmlMsg(content);
//					email.setTextMsg("您的邮箱不支持网页邮件，详情浏览http://www.yiqihi.com");
//					email.getToAddresses().clear();
//					email.addTo(rows[i][1]);
//					email.send();
//					logger.info("SendMail=" + rows[i][1] + "&id=" + rows[i][0]);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update mail_trace set send_time=?,send_count=send_count+1 where mailId=?",
//						new String[] { StringUtil.formatDateTime(new Date()), rows[i][0] });
//				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "delete from mail_job where id=?", new String[] { rows[i][0] });
//			}
//			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
//		}
//		return true;
//	}

//	public BasicDBObject sendAuthMail(Context context) {
//		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
//		String sql = "insert into user_bind(user_id,mail_key)values(?,?) ON DUPLICATE KEY UPDATE mail_key=values(mail_key)";
//		String code = RandomStringUtils.randomNumeric(10);
//		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()), code });
//		try {
//			HtmlEmail email = new HtmlEmail();
//			MailRecord rec = readMailAccount(Const.defaultMysqlDB);
//			email.setHostName(rec.hostName);
//			email.setAuthentication(rec.userId, rec.password);
//			email.setFrom(rec.userId, rec.userName);
//			email.setCharset("utf-8");
//			email.setSubject("一起嗨通知您,验证您的邮箱!");
//			String url = "http://www.yiqihi.com/checkmail/id=" + context.user.getUserId() + "&code=" + code;
//			StringBuilder buffer = new StringBuilder();
//			buffer.append("验证邮箱地址:<a href='" + url + "'>" + url + "</a>");
//			buffer.append(",请点击该链接，如不能点击，请复制该链接直接在浏览器输入进行验证！");
//			email.setHtmlMsg(buffer.toString());
//			email.getToAddresses().clear();
//			if (context.user.getUserEmail().endsWith("@yiqihi.com")) {
//				email.addTo("xiaoming@yiqihi.com");
//			} else {
//				email.addTo(context.user.getUserEmail());
//			}
//			email.send();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return oResult.append("message", e.getMessage());
//		}
//		return oResult.append("xeach", true);
//	}

//	public boolean sendMail(long userId, String subject, String content) {
//		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select email,qq from user where user_id=?", new String[] { String.valueOf(userId) }, 0, 0);
//		if (rows == null || rows.length == 0)
//			return false;
//		String[] values = ZeroConnect.doSecurity(SecurityObject.create(SecurityType.Email, SecurityMode.Undo, rows[0][0]), SecurityObject.create(SecurityType.Name, SecurityMode.Undo, rows[0][1]));
//		if (values == null)
//			return false;
//		String addr = values[0];
//		if (addr.indexOf("@") > 0) {
//			if (addr.endsWith("@yiqihi.com")) {
//				addr = "xiaoming@yiqihi.com";
//			}
//		} else if (!StringUtil.isEmpty(values[1])) {
//			addr = values[1] + "qq.com";
//		}
//		try {
//			HtmlEmail email = new HtmlEmail();
//			MailRecord rec = readMailAccount(Const.defaultMysqlDB);
//			email.setHostName(rec.hostName);
//			email.setAuthentication(rec.userId, rec.password);
//			email.setFrom(rec.userId, rec.userName);
//			email.setCharset("utf-8");
//			email.setSubject(subject);
//			email.setHtmlMsg(content);
//			email.getToAddresses().clear();
//			email.addTo(addr);
//			email.send();
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

//	public boolean sendMail(String addr, String subject, String content) {
//		return sendMail(addr, subject, content, null);
//	}

//	public boolean sendMail(String addr, String subject, String content, String fileName) {
//		try {
//			HtmlEmail email = new HtmlEmail();
//			MailRecord rec = readMailAccount(Const.defaultMysqlDB);
//			email.setHostName(rec.hostName);
//			email.setAuthentication(rec.userId, rec.password);
//			email.setFrom(rec.userId, rec.userName);
//			email.setCharset("utf-8");
//			email.setSubject(subject);
//			email.setHtmlMsg(content);
//			email.getToAddresses().clear();
//			email.addTo(addr);
//			if (StringUtil.isEmpty(fileName) == false) {
//				File f = new File(fileName);
//				if (f.exists() == true) {
//					EmailAttachment attachment = new EmailAttachment();
//					attachment.setPath(fileName);
//					attachment.setName(MimeUtility.encodeText(f.getName()));
//					email.attach(attachment);
//				}
//			}
//			email.send();
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

//	public boolean sendMails(String[] emails, String[] userNames, String subject, String content) {
//		return sendMails(emails, userNames, subject, content, null);
//	}

//	public boolean sendMails(String[] emails, String[] userNames, String subject, String content, String fileName) {
//		try {
//			HtmlEmail email = new HtmlEmail();
//			MailRecord rec = readMailAccount(Const.defaultMysqlDB);
//			email.setHostName(rec.hostName);
//			email.setAuthentication(rec.userId, rec.password);
//			email.setFrom(rec.userId, rec.userName);
//			email.setCharset("utf-8");
//			email.setSubject(subject);
//			email.setHtmlMsg(content);
//			email.getToAddresses().clear();
//			for (int i = 0; i < emails.length; i++) {
//				if (StringUtil.isEmpty(emails[i]))
//					continue;
//				email.addTo(emails[i], userNames[i]);
//			}
//			if (email.getToAddresses().size() == 0) {
//				return false;
//			}
//			if (StringUtil.isEmpty(fileName) == false) {
//				File f = new File(fileName);
//				if (f.exists() == true) {
//					EmailAttachment attachment = new EmailAttachment();
//					attachment.setPath(fileName);
//					attachment.setName(MimeUtility.encodeText(f.getName()));
//					email.attach(attachment);
//				}
//			}
//			email.send();
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	public static void main(String[] args) throws  IOException {}

}
