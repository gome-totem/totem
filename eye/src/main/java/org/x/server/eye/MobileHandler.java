package org.x.server.eye;

import java.net.URL;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.mobile.QgService;
import org.x.server.mobile.QgServiceSoap;
import org.x.server.service.MobileIntf;
import org.z.global.util.StringUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class MobileHandler implements MobileIntf{

	protected Logger logger = LoggerFactory.getLogger(MobileHandler.class);
	private static final QName SERVICE_NAME = new QName("http://tempuri.org/",	"QgService");
	
	public MobileHandler(){}
	
	public boolean sendTextMessage(BasicDBObject msg) {
		URL wsdlURL = QgService.WSDL_LOCATION;

		QgService ss = new QgService(wsdlURL, SERVICE_NAME);
		QgServiceSoap port = ss.getQgServiceSoap();

		BasicDBObject content = (BasicDBObject) msg.get("content");
		if (content == null) {
			return false;
		}

		Object info = content.get("info");
		if (info instanceof BasicDBObject) {
			this.sendMsg((BasicDBObject) info, port);
		} else if (info instanceof BasicDBList) {
			BasicDBList infos = (BasicDBList) info;
			for (int i = 0; infos != null && i < infos.size(); i++) {
				this.sendMsg((BasicDBObject) (infos.get(i)), port);
			}
		}
		return true;
	}

	public boolean sendHtmlMessage(BasicDBObject msg) {
		return sendTextMessage(msg);
	}
	
	private void sendMsg(BasicDBObject info, QgServiceSoap port) {
		if (info == null) {
			return;
		}

		long phoneNumber = info.getLong("phoneNumber", 0l);
		String context = info.getString("context");

		if (phoneNumber == 0l || StringUtil.isEmpty(context)) {
			return;
		}
		port.ecPhoneSend(phoneNumber, context);

		logger.info("send mobile message phone number [{}] and context [{}]",
				new Object[] { phoneNumber, context });

	}
	
	public static void main(String[] args) {
		MobileIntf MMMIntf = new MobileHandler();
		BasicDBObject msg = new BasicDBObject();
		BasicDBObject content = new BasicDBObject() ;
		
		content.append("id", System.currentTimeMillis()) ;
		BasicDBList info = new BasicDBList() ;
		
		info.add(new BasicDBObject().append("phoneNumber", 18810028676l).append("context", "test!")) ;
		content.append("info", info) ;
		msg.append("content", content);
		
		MMMIntf.sendTextMessage(msg);
	}
}
