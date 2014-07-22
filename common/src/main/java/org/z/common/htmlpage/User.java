package org.z.common.htmlpage;


import org.z.global.dict.Global;
import org.z.global.dict.Global.IllegalType;
import org.z.global.interfaces.UserIntf;
import org.z.global.object.RemoteResult;
import org.z.global.object.UserRole;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class User implements UserIntf {
	public long userId = 0;
	public String userEmail = null;
	public String userName = null;
	public String server = null;
	public UserRole role = null;
	public Context ctx = null;

	public User(Context ctx) {
		this.ctx = ctx;
	}

	public long getUserId() {
		return this.userId;
	}

	public String getUserName() {
		if (StringUtil.isEmpty(this.userName)) {
			return ctx.cityName + "访客";
		} else {
			return this.userName;
		}
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public String getServer() {
		return this.server;
	}

	public String getServerIP() {
		return Global.DevelopMode ? Global.ServerIP : ServerDict.self.serverIPBy(this.server);
	}

	@Override
	public String getServerId() {
		return null;
	}

	@Override
	public String getBalanceId() {
		return null;
	}

	@Override
	public String getBelongCity() {
		return null;
	}

	@Override
	public int getBindWeixin() {
		return 0;
	}

	@Override
	public int getBindMail() {
		return 0;
	}

	@Override
	public int getBindMobile() {
		return 0;
	}

	@Override
	public int getBindQQ() {
		return 0;
	}

	@Override
	public int getBindSina() {
		return 0;
	}

	@Override
	public String getCallServer() {
		return null;
	}

	@Override
	public String getLastUri() {
		return null;
	}

	@Override
	public long getLicence() {
		return 0;
	}

	@Override
	public String getPeerApiKey() {
		return null;
	}

	@Override
	public String getPeerSessionKey() {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public int getReqCount() {
		return 0;
	}

	@Override
	public int getServiceCount() {
		return 0;
	}

	@Override
	public int getTripCount() {
		return 0;
	}

	@Override
	public int getTopicCount() {
		return 0;
	}

	@Override
	public int getUserCredit() {
		return 0;
	}

	@Override
	public void addUserCredit(IllegalType type, String msg) {
	}

	@Override
	public BasicDBObject bindAccount(BasicDBObject oValue) {
		return null;
	}

	@Override
	public BasicDBObject changePassword(String oldPassword, String newPassword) {
		return null;
	}

	@Override
	public BasicDBObject changeEmail(String oldPassword, String newEmail) {
		return null;
	}

	@Override
	public BasicDBObject checkEmailService() {
		return null;
	}

	@Override
	public boolean isOwner() {
		return false;
	}

	public boolean completeProfile() {
		return false;
	}

	@Override
	public boolean checkFollow(long id) {
		return false;
	}

	@Override
	public boolean checkFollow(String id) {
		return false;
	}

	@Override
	public BasicDBObject readContact() {
		return null;
	}

	@Override
	public void updateTopicCount(int topicCount) {
	}

	@Override
	public void updateRequireCount(int reqCount) {
	}

	@Override
	public void updateServiceCount(int topicCount) {
	}

	@Override
	public void updateTripCount(int tripCount) {
	}

	@Override
	public UserRole getRole() {
		return null;
	}

	@Override
	public boolean isLogined() {
		return false;
	}

	@Override
	public int getBindCount() {
		return 0;
	}

	@Override
	public boolean licenceIsValid() {
		return false;
	}

	@Override
	public int readCheckAppointCount() {
		return 0;
	}

	@Override
	public BasicDBObject readFollowCount() {
		return null;
	}

	@Override
	public String readFollowCountBy(String name) {
		return null;
	}

	@Override
	public String readPhoneAccount() {
		return null;
	}

	@Override
	public int readPhoneAccountByInt() {
		return 0;
	}

	@Override
	public int readPhoneAccountByInt(long id) {
		return 0;
	}

	@Override
	public void updateLastUri(String uri) {
	}

	@Override
	public RemoteResult existUser(String email) {
		return null;
	}

	@Override
	public BasicDBObject forgetPassword(BasicDBObject oReq) {
		return null;
	}

	@Override
	public BasicDBObject register( String password, String email, String code, boolean checkCode) {
		return null;
	}

	@Override
	public BasicDBObject registerBy(String authMode, String site, String peerId, String name, String city, int sex, String face, String authToken, String authSecret) {
		return null;
	}

	@Override
	public void initBill() {
	}

	@Override
	public BasicDBObject login(BasicDBObject oUser) {
		return null;
	}

	@Override
	public void deleteOldCookie(String id) {
	}

	@Override
	public int readBalance() {
		return 0;
	}

	@Override
	public void updateUserCache() {
	}

	@Override
	public BasicDBObject logout() {
		return null;
	}

	@Override
	public BasicDBObject getGroups() {
		return null;
	}

	@Override
	public BasicDBObject readMsgBox(int pageNum, int pageSize) {
		return null;
	}

	@Override
	public BasicDBObject updatePicture(BasicDBObject oReq) {
		return null;
	}

	@Override
	public int newMessageCount() {
		return 0;
	}

	@Override
	public BasicDBObject readUserCounter(long userId) {
		return null;
	}

	@Override
	public BasicDBList readServices(String userId) {
		return null;
	}

	@Override
	public BasicDBObject readContact(long uId) {
		return null;
	}

	@Override
	public BasicDBObject readBy(String... fieldNames) {
		return null;
	}

	@Override
	public BasicDBObject readProfile(String id, boolean includeDesc) {
		return null;
	}

	@Override
	public BasicDBObject readProfile(long userId, boolean includeDesc) {
		return null;
	}

	@Override
	public BasicDBObject readAccountMoney(String userId) {
		return null;
	}

	@Override
	public BasicDBObject updateProfile(BasicDBObject oReq) {
		return null;
	}

	@Override
	public void setUserId(long value) {
		this.userId = value;
	}

	@Override
	public void setUserName(String value) {
		this.userName = value;
	}

	@Override
	public void setBindMobile(int value) {
	}

	@Override
	public void setLicence(long value) {
	}

	@Override
	public String getSocketService() {
		return null;
	}

	@Override
	public void setSocketService(String value) {
	}

	@Override
	public BasicDBObject cropFace(BasicDBObject oReq) {
		return null;
	}

	@Override
	public BasicDBObject readLoginAccount(String userId) {
		return null;
	}

	@Override
	public boolean checkBindMail() {
		return false;
	}

	@Override
	public BasicDBList sendMessage(String action, long contactId, BasicDBList sockets, BasicDBObject oMsgHeader, BasicDBObject oMsg) {
		return null;
	}

	@Override
	public BasicDBList readBills() {
		return null;
	}

	@Override
	public BasicDBObject readBillByDay(int day, int pageNumber) {
		return null;
	}

	@Override
	public BasicDBList readMessageHeaders() {
		return null;
	}

	@Override
	public BasicDBObject readMessageHeaderByDay(int day, int pageNumber) {
		return null;
	}

	@Override
	public BasicDBObject resetPassword(BasicDBObject req) {
		return null;
	}

	@Override
	public BasicDBObject bindUser(BasicDBObject request) {
		return null;
	}

	@Override
	public void setQuickLogin(String from) {

	}

	@Override
	public String getQuickLogin() {
		return null;
	}

	@Override
	public BasicDBObject queryUser(BasicDBObject request) {
		return null;
	}

}
