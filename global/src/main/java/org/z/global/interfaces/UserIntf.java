package org.z.global.interfaces;


import org.z.global.dict.Global.IllegalType;
import org.z.global.object.RemoteResult;
import org.z.global.object.UserRole;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public interface UserIntf {
	public long getUserId();

	public void setUserId(long value);

	public void setUserName(String value);

	public String getBalanceId();

	public String getBelongCity();

	public int getBindWeixin();

	public int getBindMail();

	public int getBindMobile();

	public void setBindMobile(int value);

	public int getBindQQ();

	public int getBindSina();

	public String getCallServer();

	public String getLastUri();

	public long getLicence();

	public void setLicence(long value);

	public String getPeerApiKey();

	public String getPeerSessionKey();

	public String getRemoteAddr();

	public int getReqCount();

	public UserRole getRole();

	public boolean isLogined();

	public int getServiceCount();
	
	public int getTripCount();

	public int getTopicCount();

	public int getUserCredit();

	public String getSocketService();

	public void setSocketService(String value);

	public void addUserCredit(IllegalType type, String msg);

	public BasicDBObject bindAccount(BasicDBObject oValue);

	public BasicDBObject readLoginAccount(String userId);

	public BasicDBObject changePassword(String oldPassword, String newPassword);

	public BasicDBObject changeEmail(String oldPassword, String newEmail);

	public boolean checkBindMail();

	public int getBindCount();

	public BasicDBObject checkEmailService();

	public boolean isOwner();

	public boolean checkFollow(long id);

	public boolean checkFollow(String id);

	public String getUserName();

	public String getUserEmail();

	public String getServer();

	public String getServerId();

	public BasicDBObject readContact();

	public void updateRequireCount(int reqCount);

	public void updateServiceCount(int topicCount);
	
	public void updateTripCount(int tripCount);

	public boolean licenceIsValid();

	public int readCheckAppointCount();

	public BasicDBObject readFollowCount();

	public String readFollowCountBy(String name);

	public String readPhoneAccount();

	public int readPhoneAccountByInt();

	public int readPhoneAccountByInt(long id);

	public void updateLastUri(String uri);

	public RemoteResult existUser(String email);

	public BasicDBObject forgetPassword(BasicDBObject oReq);

	public BasicDBObject register(String name, String password, String email, String code, boolean checkCode);

	public BasicDBObject registerBy(String authMode, String site, String peerId, String name, String city, int sex, String face, String authToken, String authSecret);

	public void updateTopicCount(int topicCount);

	public void initBill();

	public BasicDBObject login(BasicDBObject oUser);

	public void deleteOldCookie(String id);

	public int readBalance();

	public void updateUserCache();

	public BasicDBObject logout();

	public BasicDBObject getGroups();

	public BasicDBObject readMsgBox(int pageNum, int pageSize);

	public BasicDBObject updatePicture(BasicDBObject oReq);

	public int newMessageCount();

	public BasicDBObject readUserCounter(long userId);

	public BasicDBList readServices(String userId);

	public BasicDBObject readContact(long uId);

	public BasicDBObject readBy(String... fieldNames);

	public BasicDBObject readProfile(String id, boolean includeDesc);

	public BasicDBObject readProfile(long userId, boolean includeDesc);

	public BasicDBObject readAccountMoney(String userId);

	public BasicDBObject updateProfile(BasicDBObject oReq);

	public BasicDBObject cropFace(BasicDBObject oReq);

	public BasicDBList sendMessage(String action, long contactId, BasicDBList sockets, BasicDBObject oMsgHeader, BasicDBObject oMsg);

	public BasicDBList readMessageHeaders();

	public BasicDBObject readMessageHeaderByDay(int day, int pageNumber);

	public BasicDBList readBills();

	public BasicDBObject readBillByDay(int day, int pageNumber);

	public BasicDBObject resetPassword(BasicDBObject req);

	public BasicDBObject bindUser(BasicDBObject request);

	public void setQuickLogin(String from);

	public String getQuickLogin();

	public BasicDBObject queryUser(BasicDBObject request);
	

}
