package org.z.core.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.Context;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.dict.Global.RecordState;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ModuleComment implements ServiceIntf {
	protected static final Logger logger = LoggerFactory.getLogger(ModuleComment.class);
	public static ModuleComment self = null;

	public ModuleComment() {
	}

	/** 用户总评分: 0用户评分-1活动评分 **/
	public void addScore(long bizUserId, long shortId, int score) {
		int[] types = new int[] { 0, 1 };
		for (int i = 0; i < types.length; i++) {
			int type = types[i];
			long id = bizUserId;
			if (type == 1) {
				id = shortId;
			}
			BasicDBObject oReturn = new BasicDBObject().append("score", 1).append("count", 1);
			BasicDBObject qField = new BasicDBObject().append("type", type).append("id", id);
			BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "comment", qField, oReturn);
			if (one == null) {
				one = new BasicDBObject();
				one.append("userId", bizUserId);
				one.append("score", score);
				one.append("count", 1);
				DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, "comment", one);
			} else {
				one.append("score", one.getInt("score") + score);
				one.append("count", one.getInt("count") + 1);
				DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, "comment", qField, one, false);
			}
		}
	}

	/** 活动评分详情 **/
	public BasicDBObject addComment(Context context, long bizUserId, int bizType, int catalog, String bizTitle, long shortId, int score, String content) {
		BasicDBObject qField = new BasicDBObject().append("shortId", shortId).append("userId", context.user.getUserId());
		BasicDBObject oReturn = new BasicDBObject().append("score", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField, oReturn);
		int updateMode = 0;
		if (one == null) {
			one = new BasicDBObject();
			one.append("shortId", shortId);
			one.append("userId", context.user.getUserId());
		} else {
			updateMode = 1;
			one.removeField("_id");
		}
		one.append("bizType", bizType);
		one.append("catalog", catalog);
		one.append("bizUserId", bizUserId);
		one.append("bizTitle", bizTitle);
		one.append("userName", context.user.getUserName());
		one.append("score", score);
		one.append("content", content);
		one.append("timestamp", System.currentTimeMillis());
		one.append("state", RecordState.New.ordinal());
		if (updateMode == 0) {
			DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, "comments", one);
		} else {
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField, new BasicDBObject().append("$set", one));
		}
		return new BasicDBObject().append("xeach", true);
	}

	public BasicDBObject readScore(int type, long id) {
		BasicDBObject qField = new BasicDBObject().append("type", type).append("id", id);
		BasicDBObject oReturn = new BasicDBObject().append("score", 1).append("count", 1);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "comment", qField, oReturn);
		if (one != null) {
			one.append("rank", Math.round(((float) one.getInt("score") / one.getInt("count"))));
			return one;
		} else {
			return new BasicDBObject().append("rank", 2).append("score", 0).append("count", 0);
		}
	}

	public BasicDBObject readCommentBy(Context context, String fieldName, long id, int pageNumber, int pageSize) {
		pageNumber = pageNumber - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		pageSize = pageSize > 10 ? 10 : pageSize;
		BasicDBObject oResult = new BasicDBObject();
		BasicDBObject qField = new BasicDBObject();
		if (fieldName.equalsIgnoreCase("shortId")) {
			BasicDBList items = new BasicDBList();
			items.add(new BasicDBObject().append(fieldName, id).append("state", RecordState.Open.ordinal()));
			items.add(new BasicDBObject().append("userId", context.user.getUserId()).append("shortId", id));
			qField.append("$or", items);
		} else if (id == context.user.getUserId()) {
			qField.append(fieldName, id);
		} else {
			qField.append(fieldName, id).append("state", RecordState.Open.ordinal());
		}
		BasicDBObject sortField = new BasicDBObject().append("timestamp", -1);
		DataResult dr = DataCollection.find(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField, sortField, pageNumber, pageSize);
		oResult.append("totalCount", dr.totalCount);
		for (int i = 0; i < dr.list.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) dr.list.get(i);
			oItem.append("timestamp", context.tool.dateDiff(oItem.getLong("timestamp")));
		}
		oResult.append("items", dr.list);
		return oResult.append("xeach", true);
	}

	public BasicDBObject readCommentByBiz(Context context, long shortId, int pageNumber, int pageSize) {
		return this.readCommentBy(context, "shortId", shortId, pageNumber, pageSize);
	}

	public BasicDBObject readCommentByUser(Context context, long userId, int pageNumber, int pageSize) {
		return this.readCommentBy(context, "userId", userId, pageNumber, pageSize);
	}

	public int readToCheckCount() {
		BasicDBObject qField = new BasicDBObject().append("state", RecordState.New.ordinal());
		BasicDBObject sortField = new BasicDBObject().append("timestamp", -1);
		DataResult dr = DataCollection.find(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField, sortField, 1, 2);
		return dr.totalCount;
	}

	public BasicDBObject readCommentByCheck(Context context, int pageNumber, int pageSize) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		if (context.user.getRole().isCustomerService() == false) {
			return oResult;
		}
		pageNumber--;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		pageSize = pageSize > 10 ? 10 : pageSize;
		BasicDBObject qField = new BasicDBObject().append("state", RecordState.New.ordinal());
		BasicDBObject sortField = new BasicDBObject().append("timestamp", -1);
		DataResult dr = DataCollection.find(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField, sortField, pageNumber, pageSize);
		oResult.append("totalCount", dr.totalCount);
		oResult.append("items", dr.list);
		return oResult.append("xeach", true);
	}

	/** 活动评分详情 **/
	public BasicDBObject updateState(Context context, long userId, long shortId, RecordState state) {
		BasicDBObject qField = new BasicDBObject().append("shortId", shortId).append("userId", userId);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField, null);
		if (one != null) {
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, "comments", qField,
					new BasicDBObject("$set", new BasicDBObject().append("state", state.ordinal())));
		}
		return one;
	}

	@SuppressWarnings("unused")
	public BasicDBObject checkComment(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		if (context.user.getRole().isCustomerService() == false) {
			return oResult;
		}
		long shortId = oReq.getLong("shortId");
		long userId = oReq.getLong("userId");
		long bizUserId = oReq.getLong("bizUserId");
		String bizTitle = oReq.getString("bizTitle");
		int bizType = oReq.getInt("bizType");
		int catalog = oReq.getInt("catalog");
		int score = oReq.getInt("score");
		RecordState state = RecordState.values()[oReq.getInt("state")];
		String message = oReq.getString("message");
		BasicDBObject one = updateState(context, userId, shortId, state);
		if (one == null) {
			return oResult;
		}
		StringBuilder content = new StringBuilder();
		String url = context.tool.bizUrl(bizType, catalog, shortId);
		switch (state) {
		case Delete:
			String subject = one.getString("userName") + ",一起嗨有新消息通知您";
			content.append("<p>您在");
			content.append(context.tool.dateDiff(one.getLong("timestamp")));
			content.append(",发布消息:</p><p style='margin-left:20px'>");
			content.append(one.getString("content"));
			content.append("</p><p>以下是您的最新回复:</p><p style='margin-left:20px'>");
			content.append(message);
			content.append("</p>");
			content.append("<a  style='border-bottom: 1px dotted rgb(237, 92, 40); text-decoration: none; color: rgb(237, 92, 40);' target='_blank' href='http://www.yiqihi.com");
			content.append(url);
			content.append("&action=call");
			content.append("'>点击此处立即咨询");
			content.append("</a>");
			String msg = ServiceFactory.mail().createMessageDoc(one.getString("userName"), content.toString());
//			ServiceFactory.mail().sendMail(userId, subject, msg);
			break;
		case Open:
			this.addScore(bizUserId, shortId, score);
			subject = "一起嗨有新消息通知您-" + bizTitle;
			content.setLength(0);
			content.append("<p>" + one.getString("userName") + "在");
			content.append(context.tool.dateDiff(one.getLong("timestamp")));
			content.append(", 对您的服务:");
			content.append(bizTitle);
			content.append("</p><p>做出了如下评价:</p><p style='margin-left:20px'>");
			content.append(oReq.getString("content"));
			content.append("</p><p>一起嗨客服的评语:</p><p style='margin-left:20px'>");
			content.append(message);
			content.append("</p>");
			content.append("<p>如有评价不符或其他问题，</p><a  style='border-bottom: 1px dotted rgb(237, 92, 40); text-decoration: none; color: rgb(237, 92, 40);' target='_blank' href='http://www.yiqihi.com");
			content.append(url);
			content.append("&action=call");
			content.append("'>点击此处立即反馈您的意见");
			content.append("</a>");
			msg = ServiceFactory.mail().createMessageDoc("hi", content.toString());
//			ServiceFactory.mail().sendMail(bizUserId, subject, msg);
			break;
		}
		return oResult.append("xeach", true);
	}

	@Override
	public boolean init(boolean isReload) {
		return true;
	}

	@Override
	public void afterCreate(Object[] params) {
	}

	@Override
	public void start(boolean isReload) {
		self = this;
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
		return "comment";
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		BasicDBObject oResult = null;
		BasicDBObject o = (BasicDBObject) oReq;
		int funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		switch (funcIndex) {
		/** 读取评论 **/
		case 0:
			long shortId = o.getLong("shortId");
			int pageNumber = o.getInt("pageNumber");
			int pageSize = o.getInt("pageSize");
			oResult = this.readCommentByBiz(ctx, shortId, pageNumber, pageSize);
			break;
		/* 发布评论 */
		case 1:
			int bizType = o.getInt("bizType");
			long bizUserId = o.getLong("bizUserId");
			shortId = o.getLong("shortId");
			int score = o.getInt("score");
			int catalog = o.getInt("catalog");
			String content = o.getString("content");
			String bizTitle = o.getString("bizTitle");
			oResult = this.addComment(ctx, bizUserId, bizType, catalog, bizTitle, shortId, score, content);
			break;
		/* 读取审核 */
		case 2:
			pageNumber = o.getInt("pageNumber");
			pageSize = o.getInt("pageSize");
			oResult = this.readCommentByCheck(ctx, pageNumber, pageSize);
			break;
		/* 审核 */
		case 3:
			oResult = this.checkComment(ctx, o);
			break;
		}
		return oResult;
	}

}
