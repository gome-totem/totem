package org.x.server.email;


import com.dmdelivery.webservice.ArrayOfIntType;
import com.dmdelivery.webservice.DMdeliveryLoginType;
import com.dmdelivery.webservice.DMdeliverySoapAPI;
import com.dmdelivery.webservice.DMdeliverySoapAPIPort;
import com.dmdelivery.webservice.NewRecipientType;
import com.mongodb.BasicDBObject;

public interface WebPowerIntf {
	public final static int campaignId = EdmGlobal.campaignId;
	public final static int mailingId = EdmGlobal.mailingId;
	public final static String username = EdmGlobal.wusername;
	public final static String password = EdmGlobal.wpassword;
	public final static boolean addDuplisToGroups = true;
	public final static boolean overwrite = true;
	public final static DMdeliverySoapAPI client = new DMdeliverySoapAPI();
	public final static DMdeliverySoapAPIPort service = client.getDMdeliverySoapAPIPort();
	
	
	public DMdeliveryLoginType login();
	public ArrayOfIntType groupIds(String groupname,boolean istest,String remarks);
	public NewRecipientType getRecipientData(BasicDBObject object);
	public int getRecipientId(ArrayOfIntType groupIDs,NewRecipientType rdata);
	public void senSingleEmail(int recipientId);

}
