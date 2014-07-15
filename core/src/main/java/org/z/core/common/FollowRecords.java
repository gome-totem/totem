package org.z.core.common;

import java.util.HashSet;

import org.bson.BSONObject;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.MongoTable;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * userId:1, groups:["a","b","c","d"],group1:[2,4,5],group2:[1,2,5]
 * beFollows:[{userId:3,userName:'user2'}] -->被关注的列表
 * follows:[{userId:3,userName:'user3'}] -->我的关注 的列表
 * followCount:0,beFollowCount:0
 */

public class FollowRecords extends MongoTable {

	public String dbName = Const.defaultMysqlDB;
	public String collName = "follows";
	private long userId = 0;
	private String userName = null;
	private BasicDBObject qField = null;
	private String serverName = null;

	public FollowRecords(String serverName, long userId, String userName) {
		this.userId = userId;
		this.userName = userName;
		this.serverName = serverName;
		qField = new BasicDBObject("userId", userId);
		check();
	}

	public void check() {
		this.checkBy(this.serverName, this.userId);
	}

	public void checkBy(String server, long userId) {
		BasicDBObject oMatch = (BasicDBObject) DataCollection.findOne(server, dbName, collName, new BasicDBObject()
				.append("userId", userId), new BasicDBObject().append("userName", 1));
		if (oMatch == null) {
			oMatch = new BasicDBObject().append("userId", userId).append("groups", new String[] { "好友", "驴友", "游客" })
					.append("follows", new String[] {}).append("beFollows", new String[] {}).append("followCount", 0)
					.append("beFollowCount", 0);
			DataCollection.insert(serverName, dbName, collName, oMatch);
		}
	}

	public BasicDBObject readCount() {
		BasicDBObject oMatch = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, new BasicDBObject()
				.append("userId", userId), new BasicDBObject().append("followCount", 1).append("beFollowCount", 1));
		return oMatch;
	}

	public String toString() {
		BasicDBObject oMatch = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField, null);
		if (oMatch == null) {
			return "";
		}
		return oMatch.toString();
	}

	public BasicDBObject createGroup(String groupName) {
		DBObject returnField = BasicDBObjectBuilder.start("groups", 1).append(groupName, 1).get();
		BasicDBObject oMatch = (BasicDBObject) DataCollection
				.findOne(serverName, dbName, collName, qField, returnField);
		BasicDBObject oField = (BasicDBObject) BasicDBObjectBuilder.start().add("$addToSet",
				new BasicDBObject("groups", groupName)).get();
		if (!oMatch.containsField(groupName)) {
			oField.append("$set", new BasicDBObject(groupName, new BasicDBList()));
		}
		DataCollection.update(serverName, dbName, collName, qField, oField, false);
		BasicDBList groups = (BasicDBList) oMatch.get("groups");
		boolean exist = false;
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).toString().equals(groupName)) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			groups.add(groupName);
		}
		return oMatch;
	}

	public BasicDBObject readGroups() {
		DBObject returnField = BasicDBObjectBuilder.start("groups", 1).get();
		BasicDBObject oMatch = (BasicDBObject) DataCollection
				.findOne(serverName, dbName, collName, qField, returnField);
		return appendGroupCount(oMatch);
	}

	public HashSet<Long> readGroupUsers() {
		HashSet<Long> oResult = new HashSet<Long>();
		BasicDBObject oMatch = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField, null);
		if (oMatch == null) {
			return oResult;
		}
		BasicDBList groups = (BasicDBList) oMatch.get("groups");
		for (int i = 0; i < groups.size(); i++) {
			String groupName = groups.get(i).toString();
			BasicDBList groupUsers = (BasicDBList) oMatch.get(groupName);
			for (int t = 0; groupUsers != null && t < groupUsers.size(); t++) {
				oResult.add(Long.parseLong(groupUsers.get(t).toString()));
			}
		}
		return oResult;
	}

	public BasicDBObject appendGroupCount(BasicDBObject oMatch) {
		BasicDBList newGroups = new BasicDBList();
		BasicDBList groups = (BasicDBList) oMatch.get("groups");
		BasicDBObject oReturn = new BasicDBObject();
		for (int i = 0; i < groups.size(); i++) {
			String groupName = groups.get(i).toString();
			oReturn.append(groupName, 1);
		}
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField, oReturn);
		for (int i = 0; i < groups.size(); i++) {
			String groupName = groups.get(i).toString();
			BasicDBObject oGroup = new BasicDBObject();
			oGroup.append("name", groupName);
			if (one.containsField(groupName)) {
				oGroup.append("count", ((BasicDBList) one.get(groupName)).size());
			} else {
				oGroup.append("count", 0);
			}
			newGroups.add(oGroup);
		}
		oMatch.append("groups", newGroups);
		return oMatch;
	}

	public BasicDBObject removeGroup(String[] groups) {
		BasicDBObject returnField = new BasicDBObject().append("groups", 1);
		for (int i = 0; i < groups.length; i++) {
			String group = groups[i];
			returnField.append(group, 1);
		}
		BasicDBObject oMatch = (BasicDBObject) DataCollection
				.findOne(serverName, dbName, collName, qField, returnField);
		if (oMatch != null) {
			/* 删除这个组中，所有用户包含有该组的信息 */
			for (int i = 0; i < groups.length; i++) {
				String groupName = groups[i];
				/* 删除某个字段用unset--1* */
				DBObject oField = BasicDBObjectBuilder.start().append("$pull", new BasicDBObject("groups", groupName))
						.append("$unset", new BasicDBObject(groupName, 1)).get();
				DataCollection.update(serverName, dbName, collName, qField, oField, false);
				BasicDBList list = (BasicDBList) oMatch.get(groupName);
				for (int t = 0; list != null && t < list.size(); t++) {
					DataCollection.update(serverName, dbName, collName, qField, new BasicDBObject("$pull",
							new BasicDBObject("g_" + (Long) list.get(i), groupName)), false);
				}
			}
		}
		oMatch = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField, new BasicDBObject()
				.append("groups", 1));
		return oMatch;
	}

	public BasicDBList getGroups() {
		DBObject returnField = BasicDBObjectBuilder.start().add("groups", 1).get();
		DBObject oMatch = DataCollection.findOne(serverName, dbName, collName, qField, returnField);
		return oMatch == null ? null : (BasicDBList) oMatch.get("groups");
	}

	public BasicDBList getGroupUsers(String groupName) {
		DBObject returnField = BasicDBObjectBuilder.start().add(groupName, 1).get();
		DBObject oMatch = DataCollection.findOne(serverName, dbName, collName, qField, returnField);
		return oMatch == null ? null : (BasicDBList) oMatch.get(groupName);
	}

	public BasicDBObject addUserToGroup(String[] groups, long targetUserId, int pageNumber, int pageSize) {
		for (int i = 0; i < groups.length; i++) {
			String groupName = groups[i];
			BasicDBObject o1 = new BasicDBObject();
			BasicDBObject o2 = new BasicDBObject();
			o1.append("$addToSet", new BasicDBObject(groupName, targetUserId));
			o2.append("$addToSet", new BasicDBObject("g_" + targetUserId, groupName));
			DataCollection.update(serverName, dbName, collName, qField, o1, false);
			DataCollection.update(serverName, dbName, collName, qField, o2, false);
		}
		return readFollowsWithGroup(pageNumber, pageSize);
	}

	public BasicDBObject removeUserFromGroup(String groupName, long targetUserId, int pageNumber, int pageSize) {
		DataCollection.update(serverName, dbName, collName, qField, new BasicDBObject("$pull", new BasicDBObject(
				groupName, targetUserId)), false);
		DataCollection.update(serverName, dbName, collName, qField, new BasicDBObject("$pull", new BasicDBObject("g_"
				+ targetUserId, groupName)), false);
		return readFollowsWithGroup(pageNumber, pageSize);
	}

	public boolean followExist(long targetUserId) {
		BasicDBObject qField_ = new BasicDBObject();
		qField_.putAll((BSONObject) qField);
		qField_.append("follows", new BasicDBObject().append("$elemMatch", new BasicDBObject().append("userId",
				targetUserId)));
		DBObject returnField = BasicDBObjectBuilder.start().add("userId", 1).get();
		DBObject oMatch = DataCollection.findOne(serverName, dbName, collName, qField_, returnField);
		return oMatch != null;
	}

	public void follow(String targetServer, long targetUserId, String targetUserName) {
		if (followExist(targetUserId))
			return;
		/* 增加到自己的follows中 */
		DBObject oUser = BasicDBObjectBuilder.start().push("follows").append("userId", targetUserId).append("userName",
				targetUserName).get();
		DataCollection.update(serverName, dbName, collName, qField, new BasicDBObject("$addToSet", oUser).append(
				"$inc", new BasicDBObject().append("followCount", 1)), false);
		/* 增加对方的beFollows */
		checkBy(targetServer, targetUserId);
		oUser = BasicDBObjectBuilder.start().push("beFollows").append("userId", userId).append("userName", userName)
				.get();
		DataCollection.update(serverName, dbName, collName, new BasicDBObject().append("userId", targetUserId),
				new BasicDBObject("$addToSet", oUser).append("$inc", new BasicDBObject().append("beFollowCount", 1)),
				false);
	}

	public void unFollow(String targetServer, long targetUserId, String targetUserName) {
		/* 删除自己follows和该用户的生产的组别 */
		DBObject oUser = BasicDBObjectBuilder.start().push("follows").append("userId", targetUserId).append("userName",
				targetUserName).get();
		DataCollection.update(serverName, dbName, collName, qField, new BasicDBObject("$pull", oUser).append("$unset",
				new BasicDBObject("g_" + targetUserId, 1))
				.append("$inc", new BasicDBObject().append("followCount", -1)), false);
		/* 删除组中所有该用户id */
		DBObject returnField = BasicDBObjectBuilder.start().add("groups", 1).get();
		DBObject oMatch = DataCollection.findOne(serverName, dbName, collName, qField, returnField);
		BasicDBList list = (BasicDBList) oMatch.get("groups");
		for (int i = 0; i < list.size(); i++) {
			String gName = (String) list.get(i);
			DataCollection.update(serverName, dbName, collName, qField, new BasicDBObject("$pull", new BasicDBObject(
					gName, targetUserId)), false);
		}
		/* 删除对方的beFollows */
		oUser = BasicDBObjectBuilder.start().push("beFollows").append("userId", userId).append("userName", userName)
				.get();
		DataCollection.update(serverName, dbName, collName, new BasicDBObject().append("userId", targetUserId),
				new BasicDBObject("$pull", oUser).append("$unset", new BasicDBObject("g_" + userId, 1)).append("$inc",
						new BasicDBObject().append("beFollowCount", -1)), false);
	}

	/* 关注的列表 */
	private void readFollows(BasicDBObject oResult, int pageNumber, int pageSize) {
		BasicDBList result = new BasicDBList();
		if (oResult != null && oResult.containsField("follows")) {
			BasicDBList follows = (BasicDBList) oResult.get("follows");
			if (pageNumber == 0 && pageSize == 0) {
				return;
			}
			pageNumber--;
			pageNumber = pageNumber < 0 ? 0 : pageNumber;
			int offset = pageNumber * pageSize;
			while (pageSize > 0 && offset < follows.size()) {
				result.add(follows.get(offset));
				offset++;
				pageSize--;
			}
		}
		oResult.append("follows", result);
	}

	public BasicDBObject readFollows(int pageNumber, int pageSize) {
		BasicDBObject oResult = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField,
				new BasicDBObject("follows", 1).append("groups", 1).append("followCount", 1));
		readFollows(oResult, pageNumber, pageSize);
		return oResult;
	}

	public BasicDBObject readBeFollows(int pageNumber, int pageSize) {
		BasicDBObject oResult = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField,
				new BasicDBObject("beFollows", 1).append("beFollowCount", 1));
		BasicDBList result = new BasicDBList();
		if (oResult != null && oResult.containsField("beFollows")) {
			BasicDBList beFollows = (BasicDBList) oResult.get("beFollows");
			if (pageNumber == 0 && pageSize == 0) {
				return oResult;
			}
			pageNumber--;
			pageNumber = pageNumber < 0 ? 0 : pageNumber;
			int offset = pageNumber * pageSize;
			while (pageSize > 0 && offset < beFollows.size()) {
				result.add(beFollows.get(offset));
				offset++;
				pageSize--;
			}
		}
		oResult.append("beFollows", result);
		return oResult;
	}

	public BasicDBObject readFollowsByUserId(String server, long userId, int pageNumber, int pageSize) {
		BasicDBObject oResult = (BasicDBObject) DataCollection
				.findOne(server, dbName, collName, new BasicDBObject().append("userId", userId), new BasicDBObject(
						"follows", 1).append("followCount", 1).append("groups", 1));
		readFollows(oResult, pageNumber, pageSize);
		return oResult;
	}

	/* 关注的列表(带GROUP属性) */
	private BasicDBList appendGroup(BasicDBObject one) {
		BasicDBList follows = (BasicDBList) one.get("follows");
		for (int i = 0; i < follows.size(); i++) {
			BasicDBObject oFollow = (BasicDBObject) follows.get(i);
			long userId = oFollow.getInt("userId");
			if (!one.containsField("g_" + userId)) {
				oFollow.append("groups", new String[] {});
			} else {
				oFollow.append("groups", one.get("g_" + userId));
			}
			one.removeField("g_" + userId);
		}
		return follows;
	}

	public BasicDBObject readFollowsWithGroup(int pageNumber, int pageSize) {
		BasicDBObject oReturn = new BasicDBObject().append("beFollows", 0).append("beFollowCount", 0);
		BasicDBObject oResult = (BasicDBObject) DataCollection.findOne(serverName, dbName, collName, qField, oReturn);
		readFollows(oResult, pageNumber, pageSize);
		appendGroup(oResult);
		return oResult;
	}

	public static void main(String[] args) {
	}
}
