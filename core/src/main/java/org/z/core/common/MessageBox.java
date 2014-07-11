package org.z.core.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bson.types.ObjectId;
import org.z.global.dict.Global;
import org.z.global.dict.Global.ActionType;
import org.z.global.environment.Const;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * msgheader:每个用户 contact:每个用户 msgsocket:公用 msgbox:消息公用
 */

public class MessageBox {

	public static void normalizeMessage(BasicDBObject oMsg) {
		oMsg.append("id", oMsg.getString("_id"));
		oMsg.append("ago", StringUtil.timeDiff(oMsg.getLong("timestamp")));
		oMsg.append("timestamp", StringUtil.formatDateTime(oMsg.getLong("timestamp")));
		DBObjectUtil.removeBy(oMsg, "_id", "msgId");
	}

	public static BasicDBObject readMessages(String server, long userId, String msgId, int pageNumber, int pageSize) {
		ObjectId id = new ObjectId(msgId);
		BasicDBObject qField = new BasicDBObject().append("_id", id);
		BasicDBObject oHeader = (BasicDBObject) DataCollection.findOne(server, Const.defaultMongoDB, "msgheader_" + userId, qField);
		if (oHeader == null) {
			return null;
		}
		String boxServer = oHeader.getString("boxServer");
		long boxId = oHeader.getLong("boxId");
		BasicDBObject record = readSocketRecord(userId, boxServer, boxId, id, "timestamp");
		if (record == null) {
			return null;
		}
		int level = record.getInt("r" + userId);
		BasicDBObject sortField = new BasicDBObject().append("timestamp", -1);
		qField.clear();
		qField.append("msgId", msgId);
		BasicDBList items = new BasicDBList();
		items.add(new BasicDBObject().append("userId", userId));
		items.add(new BasicDBObject().append("level", new BasicDBObject().append("$lte", level)));
		qField.append("$or", items);
		int offset = pageNumber * pageSize;
		DataResult dr = DataCollection.find(boxServer, Const.defaultMongoDB, "msgbox_" + boxId, qField, sortField, offset, pageSize);
		items.clear();
		StringBuilder buffer = new StringBuilder();
		BasicDBObject oItem = null;
		BasicDBObject lastItem = null;
		for (int i = dr.list.size() - 1; i >= 0; i--) {
			oItem = (BasicDBObject) dr.list.get(i);
			if (lastItem == null) {
				lastItem = oItem;
				buffer.append(oItem.getString("content"));
				continue;
			}
			if (lastItem.getLong("userId") != oItem.getLong("userId") && buffer.length() > 0) {
				normalizeMessage(lastItem);
				lastItem.append("content", buffer.toString());
				buffer.setLength(0);
				items.add(lastItem);
			}
			String content = oItem.getString("content");
			if (buffer.length() > 0) {
				buffer.append("\n");
			}
			buffer.append(content);
			lastItem = oItem;
		}
		if (buffer.length() > 0) {
			normalizeMessage(lastItem);
			lastItem.append("content", buffer.toString());
			items.add(lastItem);
		}
		BasicDBObject oResult = new BasicDBObject();
		oResult.append("boxServer", boxServer);
		oResult.append("boxId", boxId);
		oResult.append("totalCount", dr.totalCount);
		oResult.append("items", items);
		return oResult;
	}

	public static BasicDBObject readMessages(String boxServer, long boxId, String msgId, long userId, int userLevel, int scrollMode, long timestamp) {
		BasicDBObject qField = new BasicDBObject().append("msgId", msgId);
		BasicDBList items = new BasicDBList();
		items.add(new BasicDBObject().append("userId", userId));
		items.add(new BasicDBObject().append("level", new BasicDBObject().append("$lte", userLevel)));
		qField.append("$or", items);
		BasicDBObject oSortField = new BasicDBObject().append("timestamp", -1);
		switch (scrollMode) {
		case 0:
			qField.append("timestamp", new BasicDBObject("$lt", timestamp));
			break;
		case 1:
			qField.append("timestamp", new BasicDBObject("$gt", timestamp));
			break;
		}
		int pageSize = 30;
		DataResult dr = DataCollection.find(boxServer, Const.defaultMongoDB, "msgbox_" + boxId, qField, oSortField, 0, pageSize);
		BasicDBList list = dr.list;
		if (timestamp == 0) {
			list = new BasicDBList();
			for (int i = dr.list.size() - 1; i >= 0; i--) {
				list.add(dr.list.get(i));
			}
		}
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		oResult.append("scrollMode", scrollMode);
		oResult.append("msgId", msgId);
		oResult.append("hasMore", pageSize < dr.totalCount);
		oResult.append("items", list);
		return oResult;
	}

	public static BasicDBObject readHeader(String userServer, long userId, String objectId) {
		BasicDBObject oReq = new BasicDBObject().append("_id", new ObjectId(objectId));
		BasicDBObject oResult = (BasicDBObject) DataCollection.findOne(userServer, Const.defaultMongoDB, "msgheader_" + userId, oReq);
		return oResult;
	}

	public static BasicDBObject readHeader(String userServer, long userId, int catalog, int bizType, long shortId) {
		BasicDBObject oReq = new BasicDBObject().append("catalog", catalog).append("bizType", bizType).append("shortId", shortId);
		BasicDBObject oResult = (BasicDBObject) DataCollection.findOne(userServer, Const.defaultMongoDB, "msgheader_" + userId, oReq);
		return oResult;
	}

	/*根据R-W过滤读写权限*/
	public static BasicDBList doFilterSockets(BasicDBObject oRecord, long userId) {
		BasicDBList results = new BasicDBList();
		int w = oRecord.getInt("w" + userId, 0);
		if (w == 0) {
			return results;
		}
		BasicDBList sockets = (BasicDBList) oRecord.get("sockets");
		for (int i = 0; sockets != null && i < sockets.size(); i++) {
			BasicDBObject oSocket = (BasicDBObject) sockets.get(i);
			if (w > oSocket.getInt("r")) {
				continue;
			}
			results.add(oSocket);
		}
		return results;

	}

	/* 返回Sockets 
	 * Customer    Guider        yiqihi
	 * Read=1       Read=2       Read=3 
	 * Write=3       Write=2      Write=1
	 * */
	public static BasicDBList createSockets(String sessionServer, long sessionUserId, String sessionUserName, long bizUserId) {
		BasicDBList sockets = new BasicDBList();
		HashSet<Long> users = new HashSet<Long>();
		BasicDBObject oSocket = new BasicDBObject().append("userId", sessionUserId);
		oSocket.append("userName", sessionUserName);
		oSocket.append("appserver", sessionServer);
		oSocket.append("r", 1);
		oSocket.append("w", 3);
		sockets.add(oSocket);
		users.add(sessionUserId);
		if (users.contains(bizUserId) == false) {
			String sql = "select userId,userName,appserver from user_socket where userId=?";
			oSocket = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Long[] { bizUserId }, 0, 0);
			if (oSocket == null) {
				String[][] rows = DataSet.query(Const.defaultDictMysql, Const.defaultDictMysqlDB, "select user_name,server from user where user_id=?",
						new Long[] { bizUserId }, 0, 0);
				if (rows != null && rows.length != 0) {
					oSocket = new BasicDBObject().append("userId", bizUserId);
					oSocket.append("userName", rows[0][0]);
					oSocket.append("appserver", rows[0][1]);
					oSocket.append("r", 2);
					oSocket.append("w", 2);
					sockets.add(oSocket);
					users.add(bizUserId);
				}
			}
		}
		String sql = null;
		if (Global.DevelopMode == true) {
			sql = "select userId,userName,appserver from user_socket where userId<6";
		} else {
			sql = "select userId,userName,appserver from user_socket where userId>=6 and  userId<=100  order by online desc,serviceCount asc limit 5";
		}
		ArrayList<BasicDBObject> records = DataSet.queryDBObjects(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		for (int i = 0; records != null && i < records.size(); i++) {
			oSocket = records.get(i);
			if (users.contains(oSocket.getLong("userId")))
				continue;
			oSocket.append("r", 3);
			oSocket.append("w", 1);
			sockets.add(oSocket);
			users.add(oSocket.getLong("userId"));
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update user_socket set serviceCount=serviceCount+1 where userId=?",
					new String[] { oSocket.getString("userId") });
			break;
		}
		return sockets;
	}

	public static BasicDBObject writeSockets(String boxServer, long boxId, Object msgId, long timestamp, BasicDBList sockets) {
		BasicDBObject one = new BasicDBObject();
		if (msgId instanceof String) {
			one.append("_id", new ObjectId(String.valueOf(msgId)));
		} else {
			one.append("_id", msgId);
		}
		one.append("sockets", sockets);
		for (int i = 0; i < sockets.size(); i++) {
			BasicDBObject oSocket = (BasicDBObject) sockets.get(i);
			int r = oSocket.getInt("r");
			int w = oSocket.getInt("w");
			String userId = oSocket.getString("userId");
			one.append("r" + userId, r);
			one.append("w" + userId, w);
		}
		one.append("timestamp", timestamp);
		DataCollection.insert(boxServer, Const.defaultMongoDB, "msgsocket_" + boxId, one);
		return one;
	}

	public static void updateSockets(String boxServer, long boxId, Object msgId, BasicDBList sockets) {
		BasicDBObject oReq = new BasicDBObject();
		if (msgId instanceof String) {
			oReq.append("_id", new ObjectId(String.valueOf(msgId)));
		} else {
			oReq.append("_id", msgId);
		}
		BasicDBObject oUpdate = new BasicDBObject("sockets", sockets);
		for (int i = 0; i < sockets.size(); i++) {
			BasicDBObject oSocket = (BasicDBObject) sockets.get(i);
			int r = oSocket.getInt("r", 1);
			int w = oSocket.getInt("w", 3);
			if (oSocket.getBoolean("state", false) == false)
				continue;
			oSocket.removeField("state");
			String userId = oSocket.getString("userId");
			oUpdate.append("r" + userId, r);
			oUpdate.append("w" + userId, w);
		}
		DataCollection.update(boxServer, Const.defaultMongoDB, "msgsocket_" + boxId, oReq, new BasicDBObject().append("$set", oUpdate), false);
	}

	public static void updateSockets(String boxServer, long boxId, String msgId, long timestamp) {
		BasicDBObject oReq = new BasicDBObject().append("_id", new ObjectId(msgId));
		DataCollection.update(boxServer, Const.defaultMongoDB, "msgsocket_" + boxId, oReq, new BasicDBObject().append("$inc", new BasicDBObject("msgCount", 1))
				.append("$set", new BasicDBObject("timestamp", timestamp)), false);
	}

	public static BasicDBObject readSocketRecord(long currentUserId, String boxServer, long boxId, Object msgId, String... fields) {
		BasicDBObject oReq = new BasicDBObject();
		if (msgId instanceof String) {
			oReq.append("_id", new ObjectId(String.valueOf(msgId)));
		} else {
			oReq.append("_id", msgId);
		}
		BasicDBObject oReturn = null;
		if (fields.length > 0) {
			oReturn = new BasicDBObject();
			oReturn.append("r" + currentUserId, 1).append("w" + currentUserId, 1);
			for (int i = 0; i < fields.length; i++) {
				oReturn.append(fields[i], 1);
			}
		}
		BasicDBObject one = null;
		if (oReturn == null) {
			one = (BasicDBObject) DataCollection.findOne(boxServer, Const.defaultMongoDB, "msgsocket_" + boxId, oReq);
		} else {
			one = (BasicDBObject) DataCollection.findOne(boxServer, Const.defaultMongoDB, "msgsocket_" + boxId, oReq, oReturn);
		}
		if (one == null || one.containsField("r" + currentUserId) == false) {
			return null;
		}
		return one;
	}

	public static void clearMsgHeader(String server, long userId, String msgId) {
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(msgId));
		BasicDBObject oSet = new BasicDBObject().append("unReadMsg", 0);
		BasicDBObject oUpdate = new BasicDBObject().append("$set", oSet);
		DataCollection.update(server, Const.defaultMongoDB, "msgheader_" + userId, qField, oUpdate, false);
	}

	public static void unReadMsg(String userServer, long userId, String msgId, long contactId, long timestamp, String content) {
		BasicDBObject oReq = new BasicDBObject().append("userId", contactId);
		BasicDBObject qField = new BasicDBObject().append("_id", new ObjectId(msgId));
		BasicDBObject oSet = new BasicDBObject().append("timestamp", timestamp);
		BasicDBObject oUpdate = new BasicDBObject().append("$inc", new BasicDBObject("unReadMsg", 1)).append("$set", oSet);
		DataCollection.update(userServer, Const.defaultMongoDB, "contact_" + userId, oReq, oUpdate, false);
		if (StringUtil.isEmpty(content) == false) {
			oSet.append("content", content);
		}
		DataCollection.update(userServer, Const.defaultMongoDB, "msgheader_" + userId, qField, oUpdate, false);
	}

	public static void removeHeader(String headerServer, long headerUserId, String msgId) {
		BasicDBObject oReq = new BasicDBObject();
		oReq.append("_id", new ObjectId(msgId));
		DataCollection.remove(headerServer, Const.defaultMongoDB, "msgheader_" + headerUserId, oReq);
	}

	public static BasicDBObject writeHeader(String headerServer, long headerUserId, String boxServer, long boxId, String msgId, long customerId,
			String customerName, long bizUserId, int catalog, int bizType, String bizTitle, String bizName, long shortId) {
		BasicDBObject one = new BasicDBObject();
		one.append("boxServer", boxServer);
		one.append("boxId", boxId);
		one.append("customerId", customerId);
		one.append("customerName", customerName);
		one.append("bizUserId", bizUserId);
		one.append("catalog", catalog);
		one.append("bizType", bizType);
		one.append("bizTitle", bizTitle);
		one.append("bizName", bizName);
		one.append("shortId", shortId);
		one.append("action", ActionType.inquiry.ordinal());
		one.append("content", "");
		one.append("unReadMsg", 0);
		one.append("timestamp", System.currentTimeMillis());
		if (msgId == null) {
			one.append("_id", new ObjectId());
		} else {
			one.append("_id", new ObjectId(msgId));
		}
		DataCollection.insert(headerServer, Const.defaultMongoDB, "msgheader_" + headerUserId, one);
		return one;
	}

	public static long writeBody(String boxServer, long boxId, String msgId, int level, long userId, String userName, String content) {
		long timestamp = System.currentTimeMillis();
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("msgId", msgId);
		oRecord.append("level", level);
		oRecord.append("userId", userId);
		oRecord.append("userName", userName);
		oRecord.append("content", content);
		oRecord.append("timestamp", timestamp);
		DataCollection.insert(boxServer, Const.defaultMongoDB, "msgbox_" + boxId, oRecord);
		return timestamp;
	}

	public static void main(String[] args) {
		int mode = 0;
		switch (mode) {
		case 0:
			String[] servers = new String[] { "x1", "x2", "dict" };
			for (int i = 0; i < servers.length; i++) {
				String server = servers[i];
				Set<String> names = DataCollection.getCollections(server, Const.defaultMongoDB);
				for (Iterator<String> t = names.iterator(); t.hasNext();) {
					String name = t.next();
					if (name.startsWith("msg")) {
						DataCollection.dropCollection(server, Const.defaultMongoDB, name);
						System.out.println(server + "===" + name);
					}
				}
			}
			break;
		/*清除消息*/
		case 1:
			long userId = 32543;
			String sql = "select server from user where user_id=?";
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(userId) }, 0, 0);
			if (rows == null || rows.length == 0)
				return;
			String server = rows[0][0];
			BasicDBObject oReturn = new BasicDBObject().append("boxId", 1).append("boxServer", 1);
			BasicDBList records = DataCollection.findAll(server, Const.defaultMongoDB, "msgheader_" + userId, oReturn);
			for (int i = 0; i < records.size(); i++) {
				BasicDBObject oItem = (BasicDBObject) records.get(i);
				String msgId = oItem.getString("_id");
				String boxServer = oItem.getString("boxServer");
				String boxId = oItem.getString("boxId");
				BasicDBObject oField = new BasicDBObject().append("msgId", msgId);
				DataCollection.remove(boxServer, Const.defaultMongoDB, "msgbox_" + boxId, oField);
			}
			break;
		}
		System.out.println("done.");
	}
}
