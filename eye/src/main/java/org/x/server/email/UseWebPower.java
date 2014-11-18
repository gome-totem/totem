package org.x.server.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.dmdelivery.webservice.ArrayOfIntType;
import com.dmdelivery.webservice.DMdeliveryLoginType;
import com.dmdelivery.webservice.NewGroupType;
import com.dmdelivery.webservice.NewRecipientType;
import com.dmdelivery.webservice.RecipientNameValuePairType;
import com.dmdelivery.webservice.RecordResultType;
import com.mongodb.BasicDBObject;

public class UseWebPower implements WebPowerIntf {
	
	protected static Logger logger = LoggerFactory.getLogger(UseWebPower.class);
	private RecordResultType result = null;
	private DMdeliveryLoginType login = null;

	
	public UseWebPower(){	
		login = login();
	}
	
	public DMdeliveryLoginType login(){
		DMdeliveryLoginType login = new DMdeliveryLoginType();
		login.setUsername(username);
		login.setPassword(password);
		return login;	
	}
	
	public ArrayOfIntType groupIds(String groupname,boolean istest,String remarks){
		NewGroupType group = new NewGroupType();
		ArrayOfIntType groupIDs = new ArrayOfIntType();
		int groupId = 0;
		group.setName(groupname);
		group.setIsTest(istest);
		group.setRemarks(remarks);
		result = service.addGroup(login, campaignId, group);
		if(result.getStatus().equals("ERROR")){
			logger.error("Error creating group [{}]"+new Object[] { result.getStatusMsg() });
			return groupIDs;
		}
		groupId = result.getId();
		groupIDs.getInt().add(groupId);
		return groupIDs;	
	}
	
	public NewRecipientType getRecipientData(BasicDBObject object){
		NewRecipientType recipientData = new NewRecipientType();
		List<RecipientNameValuePairType> list = new ArrayList<RecipientNameValuePairType>();
		Set<String> keyset = object.keySet();
		for(String str:keyset){
			RecipientNameValuePairType fields = new RecipientNameValuePairType();
			fields.setName(str);
			fields.setValue(object.getString(str));
			list.add(fields);	
		}
		recipientData.getFields().addAll(list);
		return recipientData;
	}
	
	public int getRecipientId(ArrayOfIntType groupIDs,NewRecipientType rdata){
		
		if(rdata == null || groupIDs == null)
			return -1;
		int rId = 0;
		result = service.addRecipient(login, campaignId, groupIDs, rdata, addDuplisToGroups, overwrite);
		if(result.getStatus().equals("ERROR")){
			logger.info("got recipient Id status<<<<<<<"+result.getStatusMsg());
			logger.error("创建邮件错误:"+result.getStatusMsg());
			return -1;
		}
		rId = result.getId();
		return rId;
		
	}

	public void senSingleEmail(int recipientId) {
		if(recipientId == -1)
			return;
		boolean flag = false;
		try
		{
			flag = service.sendSingleMailing(login, campaignId, mailingId, recipientId);
			if(flag == false)
				logger.error("Error sending single mailing.");
			logger.info("邮件发送成功 收件人的ID为:"+  recipientId);
		}
		catch(Exception e)
		{
			logger.error("邮件发送失败 失败的Id为: "+  recipientId);
		}
		
	}

	
	
	
}
