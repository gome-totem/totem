package org.x.server.email;


import com.dmdelivery.webservice.ArrayOfIntType;
import com.dmdelivery.webservice.NewRecipientType;
import com.mongodb.BasicDBObject;

public  class SendEmail {
	
	private static UseWebPower wpInstance = null;
	private static Object synObj = new Object();
	
	public static UseWebPower getWPInstance(){
		if(wpInstance == null){
			synchronized(synObj){
				if(wpInstance == null)
				{
					wpInstance = new UseWebPower();
				}
			}
		}
		return wpInstance;
	}
	
	public static void useWebPowerSendEmail(BasicDBObject info){
		wpInstance = getWPInstance();
		ArrayOfIntType groupIDs = wpInstance.groupIds("ATG", true, "ATG send email");
		NewRecipientType rdata = wpInstance.getRecipientData(info);
		int rid = wpInstance.getRecipientId(groupIDs, rdata);
		wpInstance.senSingleEmail(rid);	
	}
	
	public static void main(String args[]){
		BasicDBObject info = new BasicDBObject();
		info.append("email", "549710156@qq.com").append("subject", "youjianbiaoti").append("content", "email content");
		SendEmail.useWebPowerSendEmail(info);
	}

}
