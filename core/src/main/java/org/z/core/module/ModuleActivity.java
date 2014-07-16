package org.z.core.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.common.Context;
import org.z.core.common.ServiceFactory;
import org.z.core.interfaces.ActivityIntf;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.RecordState;
import org.z.global.environment.Const;
import org.z.global.factory.ModuleFactory;
import org.z.global.object.DBListObject;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.DateUtil;
import org.z.global.util.HtmlDom;
import org.z.global.util.PdfTool;
import org.z.global.util.StringUtil;
import org.z.global.util.TextUtil;
import org.z.global.zk.ServiceName;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;
import org.z.store.mongdb.DataSet;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ModuleActivity implements ActivityIntf {
	protected static final Logger logger = LoggerFactory.getLogger(ModuleActivity.class);
	public static ConcurrentHashMap<String, DBListObject> guideByHomePage = new ConcurrentHashMap<String, DBListObject>();
	public static String InsertActivityTripSql = "insert into activity_trip(id,catalog,type,level,title,country,city,snapshot,create_date,guide,policyCancel,securityDeposit,currency,chargeWay,price,tip,meal,perdiem,overtime,maxmile,trans,prices,priceTags,photos,tags,locations,schedules,description,user_id,user_name,last_modified,policy,contact_person,contact_mobile,contact_qq,contact_msn,short_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static String UpdateActivityTripSql = "update activity_trip set state=0,type=?,level=?, title=?,country=?,city=?,snapshot=?,guide=?,policyCancel=?,securityDeposit=?,currency=?,chargeWay=?,price=?,tip=?,meal=?,perdiem=?,overtime=?,maxmile=?,trans=?,prices=?,priceTags=?,photos=?,locations=?,schedules=?,tags=?,description=?,contact_person=?,contact_mobile=?,contact_qq=?,contact_msn=?,last_modified=?,policy=? where short_id=?";
	public static String InsertTripSql = "insert into trip(id,catalog,type,level,title,country,city,snapshot,create_date,guide,policyCancel,securityDeposit,currency,chargeWay,price,tip,meal,perdiem,overtime,maxmile,trans,prices,priceTags,photos,tags,locations,schedules,description,user_id,user_name,last_modified,policy,contact_person,contact_mobile,contact_qq,contact_msn,short_id,customer_date,customer_name,customer_userId,customer_userName,customer_sex,customer_age,customer_city,customer_email,customer_mobile,customer_qq,customer_msn) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static String UpdateTripSql = "update trip set state=0,type=?,level=?, title=?,country=?,city=?,snapshot=?,guide=?,policyCancel=?,securityDeposit=?,currency=?,chargeWay=?,price=?,tip=?,meal=?,perdiem=?,overtime=?,maxmile=?,trans=?,prices=?,priceTags=?,photos=?,locations=?,schedules=?,tags=?,description=?,contact_person=?,contact_mobile=?,contact_qq=?,contact_msn=?,last_modified=?,policy=?,customer_date=?,customer_name=?,customer_userId=?,customer_userName=?,customer_sex=?,customer_age=?,customer_city=?,customer_email=?,customer_mobile=?,customer_qq=?,customer_msn=? where short_id=?";

	public ModuleActivity() {
	}

	@Override
	public void afterCreate(Object[] params) {
	}

	public String tableBy(BizType type) {
		String name = "";
		if (type == BizType.car) {
			name = "activity_car";
		} else if (type == BizType.trip) {
			name = "activity_trip";
		} else if (type == BizType.room) {
			name = "activity_room";
		} else if (type == BizType.require) {
			name = "activity_req";
		} else if (type == BizType.product) {
			name = "activity_product";
		}
		return name;
	}

	public static String urlBy(int catalog, int type, long shortId) {
		return urlBy(catalog, BizType.values()[type], String.valueOf(shortId));
	}

	public static String urlBy(int catalog, BizType type, String shortId) {
		String url = "";
		switch (type) {
		case car:
			url = "http://www.yiqihi.com/car/" + shortId;
			break;
		case trip:
//			TripCatalog c = TripCatalog.values()[catalog];
//			switch (c) {
//			case guide:
//				url = "http://www.yiqihi.com/guide/" + shortId;
//				break;
//			case trip:
//				url = "http://www.yiqihi.com/trip/" + shortId;
//			case plan:
//				url = "http://www.yiqihi.com/trip/" + shortId + "&name=plan";
//			}
			break;
		case room:
			url = "http://www.yiqihi.com/room/" + shortId;
			break;
		case require:
			url = "http://www.yiqihi.com/require/" + shortId;
			break;
		case product:
			url = "http://www.yiqihi.com/product/" + shortId;
		}
		return url;
	}

	public void updateTripCount(String userId, int value) {
		String sql = "update user set trip_count=trip_count +? where user_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new Object[] { value, userId });
	}

	public void updateUserCache(String userId) {
		String sql = "select cookie_id,req_count,service_count,topic_count,trip_count from user where user_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId }, 0, 0);
		if (rows == null || rows.length == 0) {
			return;
		}
		String cookieId = rows[0][0];
		if (StringUtil.isEmpty(cookieId))
			return;
		try {
			String userContent = RedisPool.use().get(cookieId);
			if (StringUtil.isEmpty(userContent)) {
				return;
			}
			BasicDBObject oUser = (BasicDBObject) JSON.parse(userContent);
			oUser.append("reqCount", Integer.parseInt(rows[0][1]));
			oUser.append("serviceCount", Integer.parseInt(rows[0][2]));
			oUser.append("topicCount", Integer.parseInt(rows[0][3]));
			oUser.append("tripCount", Integer.parseInt(rows[0][4]));
			userContent = oUser.toString();
			RedisPool.use().set(cookieId, userContent);
		} catch (Exception e) {
			logger.error("updateUserCache", e);
		}
	}

	public BasicDBObject readCommentScore(long shortId) {
		ModuleComment comment = (ModuleComment) ModuleFactory.moduleInstanceBy("comment");
		return comment.readScore(1, shortId);
	}

	public BasicDBObject readComments(Context context, long shortId, int pageNumber, int pageSize) {
		ModuleComment comment = (ModuleComment) ModuleFactory.moduleInstanceBy("comment");
		return comment.readCommentByBiz(context, shortId, pageNumber, pageSize);
	}

	public boolean isOwner(Context context, BizType type, String activityId) {
		String tableName = this.tableBy(type);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from " + tableName + " where  id=? and user_id=?",
				new String[] { activityId, String.valueOf(context.user.getUserId()) }, 0, 0);
		return Integer.parseInt(rows[0][0]) == 1;
	}

	public boolean isOwner(Context context, BizType type, Long shortId) {
		String tableName = this.tableBy(type);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select count(*) from " + tableName + " where short_id=? and user_id=?",
				new String[] { String.valueOf(shortId), String.valueOf(context.user.getUserId()) }, 0, 0);
		return Integer.parseInt(rows[0][0]) == 1;
	}

	public BasicDBObject fieldBy(Context context, BizType type, Long shortId, String... fieldNames) {
		String tableName = this.tableBy(type);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; fieldNames != null && i < fieldNames.length; i++) {
			if (buffer.length() > 0)
				buffer.append(",");
			buffer.append(fieldNames[i]);
		}
		buffer.insert(0, "select ");
		buffer.append(" from " + tableName + " where short_id=? and user_id=? ");
		return DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, buffer.toString(),
				new String[] { String.valueOf(shortId), String.valueOf(context.user.getUserId()) }, 0, 0);
	}

	/* 检查ShortId是否存在 */
	public boolean exists(BizType type, Long shortId) {
		String tableName = this.tableBy(type);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from " + tableName + " where short_id=?",
				new String[] { String.valueOf(shortId) }, 0, 0);
		return Integer.parseInt(rows[0][0]) == 1;
	}

	public BasicDBObject readBy(BizType type, long shortId) {
		return readBy(type, 0, shortId);
	}

	public BasicDBObject readComments(BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select count(*) from `order` where shortId=? and state=8",
				new String[] { oReq.getString("shortId") }, 0, 0);
		oResult.append("totalCount", Integer.parseInt(rows[0][0]));
		int pageNumber = oReq.getInt("pageNumber") - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		int pageSize = oReq.getInt("pageSize");
		BasicDBList items = DataSet
				.queryDBList(
						Const.defaultMysqlServer,
						Const.defaultMysqlDB,
						"select customer_id,confirm_date,customer_name,score_person,score_activity,comment from `order` where shortId=?  and state=8 order by create_date",
						new String[] { oReq.getString("shortId") }, pageNumber, pageSize);
		oResult.append("items", items);
		return oResult;
	}

	public BasicDBObject readRecommends(BasicDBObject oReq) {
		int bizType = oReq.getInt("bizType");
		String city = oReq.getString("city");
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		oResult.append("sliders", new ModuleRecommend().read(city, bizType, oReq.getString("position"), 0, 12));
		return oResult;
	}

	public BasicDBObject changePoints(BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		String[][] rows;
		String sql = null;
		BizType type = BizType.values()[oReq.getInt("bizType")];
		switch (type) {
		case car:
			sql = "select locations,pending from activity_car where short_id=?";
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { oReq.getString("shortId") }, 0, 0);
			if (rows == null || rows.length == 0)
				return oResult;
			if (Integer.parseInt(rows[0][1]) >= 1)
				return oResult;
			BasicDBList locations = (BasicDBList) JSON.parse(rows[0][0]);
			BasicDBList points = (BasicDBList) oReq.get("points");
			if (locations.size() != points.size())
				return oResult;
			for (int i = 0; i < locations.size(); i++) {
				BasicDBObject oLoc = (BasicDBObject) locations.get(i);
				BasicDBObject oPoint = (BasicDBObject) points.get(i);
				if (!oLoc.getString("key").equalsIgnoreCase(oPoint.getString("key")))
					return oResult;
			}
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_car set locations=?,pending=1 where short_id=?", new String[] {
					points.toString(), oReq.getString("shortId") });
			break;
		}
		return oResult;

	}

	/* 为浏览使用 */
	public BasicDBObject readContactBy(long shortId, BizType type) {
		if (shortId == 0) {
			return null;
		}
		StringBuffer sql = new StringBuffer();
		switch (type) {
		case car:
			sql.append(" from activity_car where short_id=?");
			break;
		case trip:
			sql.append(" from activity_trip where short_id=?");
			break;
		case product:
			sql.append(" from activity_product where short_id=?");
			break;
		}
		BasicDBObject oResult = null;
		if (sql.length() != 0) {
			sql.insert(0, "select contact_person,contact_qq,contact_msn,contact_mobile");
			oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), new String[] { String.valueOf(shortId) }, 0, 0);
			String[] values = null;
			//			ServiceFactory.appDict().decode(oResult.getString("contact_person"), oResult.getString("contact_mobile"),
//					oResult.getString("contact_qq"), oResult.getString("contact_msn"));
			oResult.clear();
			int index = 0;
			oResult.append("name", values[index++]);
			oResult.append("mobile", values[index++]);
			oResult.append("qq", values[index++]);
			oResult.append("msn", values[index++]);
		}
		return oResult;
	}

	public BasicDBList normalizePhotos(BasicDBObject oResult) {
		Object content = oResult.get("photos");
		if (content == null) {
			return null;
		}
		BasicDBList photos = null;
		if (content instanceof String) {
			photos = (BasicDBList) JSON.parse(String.valueOf(content));
			oResult.append("photos", photos);
		} else {
			photos = (BasicDBList) content;
		}
		int index = 0;
		while (index < photos.size()) {
			BasicDBObject oPhoto = (BasicDBObject) photos.get(index);
			if (oPhoto == null || oPhoto.containsField("picture") == false) {
				photos.remove(index);
				continue;
			}
			index++;
		}
		return photos;
	}

	/* 为浏览使用 */
	public BasicDBObject readBy(BizType type, int catalog, long shortId) {
		if (shortId == 0) {
			return null;
		}
		String sql = null;
		switch (type) {
		case car:
			sql = "select rank,priceTags,locations,currency,chargeWay,driver,catalog,user_id,user_name,person_count,"
					+ "country,city,id,map_id,title,description,car_count,car_brand,car_logo,car_model,"
					+ "currency,tags,photos,state,securityDeposit,policyCancel,price,prices," + "pending from activity_car where short_id=?";
			break;
		case room:
			sql = "select rank,currency,chargeWay,maxDay,minDay,checkIn,checkOut,securityDeposit,policyCancel,catalog,"
					+ "user_id,user_name,country,city,id,map_id,title,description,currency,tags,priceTags,photos,prices,"
					+ "location,state,price,lodge_type,lease_type,toilet_type,pending  from activity_room where short_id=?";
			break;
		case trip:
//			TripCatalog c = TripCatalog.values()[catalog];
//			switch (c) {
//			case plan:
//				sql = "select rank,securityDeposit,policy,type,policyCancel,catalog,user_id,user_name,id,title,description,currency,guide,schedules,"
//						+ "country,city,level,tags,photos,locations,guide,state,chargeWay,price,prices,priceTags,pending,customer_date,customer_userId,customer_name,customer_age,customer_city,customer_qq,customer_msn,customer_mobile,customer_email,customer_sex  from trip where short_id=?";
//				break;
//			default:
//				sql = "select rank,securityDeposit,policy,type,policyCancel,catalog,user_id,user_name,id,title,description,currency,guide,schedules,"
//						+ "country,city,level,tags,photos,locations,guide,state,chargeWay,price,prices,priceTags,pending  from activity_trip where short_id=?";
//				break;
//			}
			break;
		case require:
			sql = "select rank,id,short_id,catalog,user_id,title,location,language,service,currency,price,chargeWay,"
					+ "contact_person,contact_mobile,contact_qq,contact_msn,car_count,car_model,"
					+ "person,description,picture from activity_req where short_id=?";
			break;
		case product:
			sql = "select rank,id,short_id,category,user_id,user_name,title,currency,price,chargeWay,state,description,designer_url,designer_face,designer_say,"
					+ "contact_person,contact_mobile,contact_qq,contact_msn,country,brand,model,color,designer,post,post_time,"
					+ "photos,recommend_userId,recommend_userName from activity_product where short_id=?";
			break;
		}
		if (sql == null)
			return null;
		BasicDBObject oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(shortId) }, 0, 0);
		if (oResult == null)
			return null;
		oResult.append("bizType", type.ordinal());
		switch (type) {
		case trip:
//			TripCatalog c = TripCatalog.values()[catalog];
//			switch (c) {
////			case guide:
//				oResult.append("guide", JSON.parse(oResult.getString("guide")));
//				break;
////			case trip:
////			case plan:
//				String v = oResult.getString("schedules");
//				if (StringUtil.isEmpty(v) || v.equalsIgnoreCase("[]")) {
//					oResult.append("schedules", new BasicDBList());
//				} else {
//					oResult.append("schedules", JSON.parse(v));
//				}
//				oResult.append("guide", JSON.parse(oResult.getString("guide")));
//				break;
//			}
			break;
		}
		oResult.append("prices", JSON.parse(oResult.getString("prices", "[]")));
		return oResult;
	}

	public void decodeCustomer(Context ctx, BasicDBObject oItem) {
		if (oItem == null) {
			return;
		}
		if (ctx.user.getUserId() != oItem.getLong("customer_userId", 0) && ctx.user.getUserId() != oItem.getLong("user_id", 0)) {
			return;
		}
		String[] values = null;//ServiceFactory.appDict().decode(oItem.getString("customer_name"), oItem.getString("customer_mobile"), oItem.getString("customer_qq"),
				//oItem.getString("customer_msn"), oItem.getString("customer_email"));
		int index = 0;
		oItem.append("customer_name", values[index++]);
		oItem.append("customer_mobile", values[index++]);
		oItem.append("customer_msn", values[index++]);
		oItem.append("customer_email", values[index++]);
	}

	public void initDays(BasicDBObject oReq, int dayCount) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(java.util.Calendar.DAY_OF_YEAR, 0);
		for (int i = 0; i <= dayCount; i++) {
			BasicDBObject oDay = new BasicDBObject();
			c.add(java.util.Calendar.DAY_OF_YEAR, 1);
			int fullDay = c.get(java.util.Calendar.DAY_OF_YEAR);
			if (oReq.containsField("d" + fullDay)) {
				continue;
			}
			int day = c.get(java.util.Calendar.DAY_OF_MONTH);
			int month = c.get(java.util.Calendar.MONTH) + 1;
			oReq.append("d" + fullDay, oDay);
			oDay.append("state", 1);
			oDay.append("day", day);
			oDay.append("month", month);
			oDay.append("price", oReq.getInt("price"));
		}
	}

	/* 更新日历，获得最低价 */
	public int updateCalendar(Context context, long shortId, BizType type, int catalog, int currency, int chargeWay, BasicDBList oldList, BasicDBList newList) {
		if (newList == null)
			return 0;
		HashMap<String, BasicDBObject> table = new HashMap<String, BasicDBObject>();
		int miniPrice = Integer.MAX_VALUE;
		for (int i = 0; i < newList.size(); i++) {
			BasicDBObject oNew = (BasicDBObject) newList.get(i);
			table.put(oNew.getString("name"), oNew);
		}
		HashMap<String, BasicDBObject> oldTable = new HashMap<String, BasicDBObject>();
		for (int i = 0; i < oldList.size(); i++) {
			BasicDBObject old = (BasicDBObject) oldList.get(i);
			oldTable.put(old.getString("name"), old);
		}
		for (Iterator<Entry<String, BasicDBObject>> i = table.entrySet().iterator(); i.hasNext();) {
			Entry<String, BasicDBObject> entry = i.next();
			BasicDBObject oPrice = entry.getValue();
			int v = oPrice.getInt("value");
			if (v <= miniPrice) {
				miniPrice = v;
			}
			String name = oPrice.getString("name");
			oldTable.remove(name);
			BasicDBObject oCalendar = (BasicDBObject) oPrice.removeField("calendar");
			if (oCalendar == null) {
				continue;
			}
			oCalendar.append("currency", currency).append("chargeWay", chargeWay);
			oPrice.append("currency", currency).append("chargeWay", chargeWay);
			this.updateCalendarBy(context, shortId, type, catalog, name, oCalendar);
		}
		for (Iterator<Entry<String, BasicDBObject>> i = oldTable.entrySet().iterator(); i.hasNext();) {
			Entry<String, BasicDBObject> entry = i.next();
			String name = entry.getValue().getString("name");
			this.removeCalendar(shortId, type, name, catalog);
		}
		return miniPrice;
	}

	/* bizName为报价名称：值为不带车、5座车、房间名等 */
	protected void updateCalendarBy(Context context, long shortId, BizType bizType, int catalog, String bizName, BasicDBObject oCalendar) {
		BasicDBObject oRecord = new BasicDBObject();
		oRecord.append("userId", context.user.getUserId());
		oRecord.append("shortId", shortId);
		oRecord.append("bizType", bizType.ordinal());
		oRecord.append("bizName", bizName);
		oRecord.append("catalog", catalog);
		oRecord.putAll((BSONObject) oCalendar);
		BasicDBObject oReturn = new BasicDBObject().append("userId", 1);
		BasicDBObject qField = new BasicDBObject().append("shortId", shortId).append("bizName", bizName);
		BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField, oReturn);
		if (one == null) {
			DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", oRecord);
		} else {
			qField.clear();
			qField.append("_id", one.get("_id"));
			DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField, new BasicDBObject("$set", oRecord), false);
		}
	}

	protected void removeCalendar(long shortId, BizType bizType, String bizName, int catalog) {
		BasicDBObject qField = new BasicDBObject().append("shortId", shortId);
		qField.append("bizName", bizName);
		DataCollection.remove(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField);
	}

	protected BasicDBObject createPriceObject(String name) {
		return new BasicDBObject().append("name", name).append("value", 0).append("person", 0).append("active", true).append("calendar", new BasicDBObject());
	}

	protected BasicDBObject createPriceObject(String name, int value, int person) {
		return new BasicDBObject().append("name", name).append("value", value).append("person", person).append("active", true)
				.append("calendar", new BasicDBObject());
	}

	private BasicDBObject createPriceObject(BasicDBObject oSource, String displayName, String fieldName, int qty) {
		BasicDBObject oItem = new BasicDBObject();
		oItem.append("name", displayName);
		oItem.append("price", oSource.get(fieldName));
		oSource.removeField(fieldName);
		oItem.append("qty", qty);
		oItem.append("selected", true);
		return oItem;
	}

	/* 为网页异步，编辑使用 */
	public BasicDBObject readByEdit(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		BizType type = BizType.values()[oReq.getInt("bizType")];
		String sql = null;
		BasicDBObject oItem = null;
		switch (type) {
		case require:
			sql = "select * from activity_req where(user_id=?) and (short_id=?) ";
			oItem = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
					new String[] { String.valueOf(context.user.getUserId()), oReq.getString("shortId") }, 0, 0);
			if (oItem == null)
				return oResult;
			oItem.append("tags", JSON.parse(oItem.getString("tags")));
			oItem.append("locations", JSON.parse(oItem.getString("locations")));
			break;
		}
		oResult.append("xeach", true);
		String[] values = null;
//				ServiceFactory.appDict().decode(oItem.getString("contact_person"), oItem.getString("contact_mobile"), oItem.getString("contact_qq"),
//				oItem.getString("contact_msn"));
		if (values != null) {
			int index = 0;
			oItem.append("contact_person", values[index++]);
			oItem.append("contact_mobile", values[index++]);
			oItem.append("contact_qq", values[index++]);
			oItem.append("contact_msn", values[index++]);
		}
		oResult.putAll((BSONObject) oItem);
		return oResult;
	}

	/* 为网页同步，编辑使用 */
	public BasicDBObject readByEdit(Context context, BizType type, int catalog, String shortId) {
		BasicDBObject oResult = null;
		String sql = null;
		if (!StringUtils.isEmpty(shortId)) {
			switch (type) {
			case car:
				sql = "select catalog,locations,driver,person_count,policyCancel,securityDeposit,chargeWay,price,currency,prices,priceTags,tags,tip,meal,perdiem,overtime,maxmile,trans,hascar,title,car_count,car_brand,car_logo,car_model,description,contact_person,contact_qq,contact_mobile,contact_msn,photos,country,city,state from activity_car where short_id=? and user_id=?";
				oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
						new String[] { shortId, String.valueOf(context.user.getUserId()) }, 0, 0);
				break;
			case room:
				sql = "select catalog,title,location,country,city,lodge_type,lease_type,toilet_type,minDay,maxDay,checkIn,checkOut,currency,chargeWay,policyCancel,securityDeposit,prices,priceTags,tags,description,rooms,contact_person,contact_qq,contact_mobile,contact_msn,photos,state from activity_room where short_id=? and user_id=?";
				oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
						new String[] { shortId, String.valueOf(context.user.getUserId()) }, 0, 0);
				break;
			case trip:
//				TripCatalog c = TripCatalog.values()[catalog];
//				switch (c) {
//				case guide:
//				case trip:
//				case activity:
//					sql = "select title,catalog,type,photos,tags,description,policyCancel,policy,securityDeposit,currency,chargeWay,price,prices,priceTags,tip,meal,perdiem,overtime,maxmile,trans,hascar,guide,locations,schedules,contact_person,contact_qq,contact_mobile,contact_msn,state,country,city,hour,venue,user_id,user_name from activity_trip where short_id=? and user_id=?";
//					oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
//							new String[] { shortId, String.valueOf(context.user.getUserId()) }, 0, 0);
//					break;
//				case plan:
//					sql = "select title,catalog,type,photos,tags,description,policyCancel,policy,securityDeposit,currency,chargeWay,price,prices,priceTags,tip,meal,perdiem,overtime,maxmile,trans,hascar,guide,locations,schedules,contact_person,contact_qq,contact_mobile,contact_msn,state,country,city,customer_date,customer_userId,customer_userName,customer_name,customer_sex,customer_age,customer_city,customer_email,customer_mobile,customer_qq,customer_msn,user_id,user_name from trip where short_id=? and (user_id=? or customer_userId=?)";
//					oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
//							new String[] { shortId, String.valueOf(context.user.getUserId()), String.valueOf(context.user.getUserId()) }, 0, 0);
//					break;
//				}
				break;
			case require:
				sql = "select id,short_id,catalog,user_id,dept_date,day,hotel_star,hotel_price,person,baby,child,old,"
						+ "sex,car_enabled,car_model,car_count,visa,relax,currency,price,"
						+ "tags,locations,title,description,contact_person,contact_mobile,contact_qq,contact_msn from activity_req where short_id=? and user_id=?";
				oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
						new String[] { shortId, String.valueOf(context.user.getUserId()) }, 0, 0);
				break;
			case product:
				sql = "select id,short_id,category,user_id,title,currency,price,chargeWay,description,"
						+ "contact_person,contact_mobile,contact_qq,contact_msn,country,model,brand,color,"
						+ "designer,designer_say,designer_face,designer_face2,designer_url,post,source,"
						+ "photos from activity_product where short_id=? and user_id=?";
				oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
						new String[] { shortId, String.valueOf(context.user.getUserId()) }, 0, 0);
				break;
			}
		}
		if (oResult == null) {
			oResult = new BasicDBObject().append("exist", false);
			BasicDBObject oInfo = context.user.readContact();
			if (oInfo != null) {
				oResult.append("contact_person", oInfo.getString("name"));
				oResult.append("contact_qq", oInfo.getString("qq"));
				oResult.append("contact_mobile", oInfo.getString("mobile"));
				oResult.append("contact_msn", oInfo.getString("msn"));
				oResult.append("city", oInfo.getString("city"));
				oResult.append("country", oInfo.getString("country"));
			} else {
				oResult.append("contact_person", "");
				oResult.append("contact_qq", "");
				oResult.append("contact_mobile", "");
				oResult.append("contact_msn", "");
				oResult.append("city", "");
				oResult.append("country", "");
			}
			oResult.append("policyCancel", 0);
			oResult.append("securityDeposit", 0);
			oResult.append("currency", 0);
			oResult.append("chargeWay", 1);
			oResult.append("state", 0);
			oResult.append("shortId", "");
			oResult.append("tags", "[]");
			oResult.append("priceTags", "[]");
			oResult.append("schedules", "[]");
			BasicDBList prices = new BasicDBList();
			oResult.append("prices", prices);
			switch (type) {
			case trip:
//				TripCatalog c = TripCatalog.values()[catalog];
//				switch (c) {
////				case guide:
//					prices.add(createPriceObject("不带车"));
//					prices.add(createPriceObject("5座车"));
//					break;
////				case trip:
//					prices.add(createPriceObject("标准价"));
//					break;
////				case plan:
//					prices.add(createPriceObject("标准价"));
//					oResult.append("use_id", 0);
//					oResult.append("customer_userId", 0);
//					oResult.append("customer_userName", "");
//					oResult.append("customer_sex", "0");
//					oResult.append("customer_city", "");
//					oResult.append("customer_name", "");
//					oResult.append("customer_mobile", "");
//					oResult.append("customer_qq", "");
//					oResult.append("customer_msn", "");
//					oResult.append("customer_email", "");
//					break;
////				case activity:
//					BasicDBObject oPrice = createPriceObject("全年");
//					oPrice.append("age", "成人 (≥18)岁");
//					break;
//				}
				break;
			case car:
				prices.add(createPriceObject("5座车"));
				prices.add(createPriceObject("7座车"));
				prices.add(createPriceObject("9座车"));
				break;
			}
		} else {
			oResult.append("shortId", Long.parseLong(shortId));
			oResult.append("exist", true);
			String[] values = null;
//			ServiceFactory.appDict().decode(oResult.getString("contact_person"), oResult.getString("contact_mobile"),
//					oResult.getString("contact_qq"), oResult.getString("contact_msn"));
			oResult.append("contact_person", values[0]);
			oResult.append("contact_mobile", values[1]);
			oResult.append("contact_qq", values[2]);
			oResult.append("contact_msn", values[3]);
			BasicDBList prices = (BasicDBList) JSON.parse(oResult.getString("prices"));
			if (prices != null) {
				for (int i = 0; i < prices.size(); i++) {
					BasicDBObject oPrice = (BasicDBObject) prices.get(i);
					BasicDBObject qField = new BasicDBObject().append("shortId", Long.parseLong(shortId)).append("bizName", oPrice.getString("name"));
					BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField);
					if (one == null) {
						oPrice.append("calendar", qField.append("currency", oResult.getInt("currency")));
						continue;
					}
					DBObjectUtil.removeBy(one, "_id", "bizName", "bizType", "catalog", "currency");
					oPrice.append("calendar", one);
				}
				oResult.append("prices", prices);
			}
//			switch (type) {
//			case trip:
//				TripCatalog c = TripCatalog.values()[catalog];
//				switch (c) {
//				case guide:
//					break;
//				case trip:
//					oResult.append("schedules", base64(oResult.getString("schedules")));
//					oResult.append("title", base64(oResult.getString("title")));
//					oResult.append("tags", base64(oResult.getString("tags")));
//					break;
//				case plan:
//					oResult.append("schedules", base64(oResult.getString("schedules")));
//					oResult.append("title", base64(oResult.getString("title")));
//					oResult.append("tags", base64(oResult.getString("tags")));
//					String[] items = ServiceFactory.appDict().decode(oResult.getString("customer_name"), oResult.getString("customer_mobile"),
//							oResult.getString("customer_qq"), oResult.getString("customer_msn"), oResult.getString("customer_email"));
//					oResult.append("customer_name", items[0]);
//					oResult.append("customer_mobile", items[1]);
//					oResult.append("customer_qq", items[2]);
//					oResult.append("customer_msn", items[3]);
//					oResult.append("customer_email", items[4]);
//					break;
//				}
//				break;
//			}
		}
		oResult.append("bizType", type.ordinal());
		if (oResult.containsField("description")) {
			oResult.append("description", base64(oResult.getString("description")));
			oResult.append("base64", true);
		}
		return oResult;
	}

	public String base64(String content) {
		Base64 base64 = new Base64();
		try {
			byte[] bytes = base64.encode(content.getBytes("UTF-8"));
			content = new String(bytes, "UTF-8");
			content = content.replaceAll("\r|\n", "");
		} catch (Exception e) {
		}
		return content;
	}


	private BasicDBList normalizeDesc(BasicDBList items) {
		for (int i = 0; i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			oItem.append("description", StringUtils.abbreviate(TextUtil.extractText(oItem.getString("description")), 300));
			if (oItem.containsField("rooms")) {
				oItem.append("rooms", JSON.parse(oItem.getString("rooms")));
			}
		}
		return items;
	}

	public int readMyActivity(Context context, String sql, String name, int start, int readCount, int recordCount, BasicDBObject oResult) {
		BasicDBList items = null;
		if (readCount > recordCount) {
			readCount = recordCount;
		}
		items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) }, start,
				readCount);
		oResult.append(name, new BasicDBObject().append("items", normalizeDesc(items)));
		oResult.append("xeach", true);
		return readCount;
	}

	/* 按时间顺序读取发布的活动 */
	public ArrayList<BasicDBObject> readOffers(Context context) {
		String userId = context.getParam("u", "");
		if (StringUtil.isEmpty(userId)) {
			userId = context.getParam("id", "");
		}
		if (StringUtil.isEmpty(userId)) {
			userId = String.valueOf(context.user.getUserId());
		}
		ArrayList<BasicDBObject> results = new ArrayList<BasicDBObject>();
		String state = "2";
		String sql = "select 6 as bizType,catalog,short_id,snapshot,title,description,last_modified from activity_trip where (user_id=?) and (state=?)";
		ArrayList<BasicDBObject> items = DataSet.queryDBObjects(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId, state }, 0, 0);
		results.addAll(items);
		sql = "select  5 as bizType,catalog,short_id,snapshot,title,description,last_modified from activity_car where (user_id=?) and (state=?)";
		items = DataSet.queryDBObjects(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId, state }, 0, 0);
		results.addAll(items);
		sql = "select  10 as bizType, catalog,short_id,snapshot,title,description,last_modified from activity_req where (user_id=?) and (state=?)";
		items = DataSet.queryDBObjects(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { userId, state }, 0, 0);
		results.addAll(items);
		Collections.sort(results, new Comparator<BasicDBObject>() {
			@Override
			public int compare(BasicDBObject o1, BasicDBObject o2) {
				return (int) (o2.getLong("last_modified") - o1.getLong("last_modified"));
			}
		});
		return results;
	}

	@SuppressWarnings("unused")
	public BasicDBObject sendMailByTrip(Context ctx, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		if (ctx.user.getRole().isCustomerService() == false) {
			return oResult.append("message", "您没有权限");
		}
		String id = oReq.getString("shortId");
		BasicDBObject dbObject = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select customer_name,customer_email from trip where short_id=?", new String[] { id }, 0, 0);
		if (dbObject == null || StringUtil.isEmpty(dbObject.getString("customer_email"))) {
			return oResult.append("message", "行程单邮件地址不存在.");
		}
		String[] values = null;
//		ServiceFactory.appDict().decode(dbObject.getString("customer_name"), dbObject.getString("customer_email"));
		String fileName = "order-" + id + ".pdf";
		String pageUrl = "http://www.yiqihi.com/trip/" + id + "&pdf=true&name=plan&cookieId=" + ctx.cookieId;
		BasicDBObject oFile = PdfTool.createFor(pageUrl, fileName);
		if (oFile.getBoolean("xeach", false) == false) {
			return oResult.append("message", "生成行程单pdf失败.");
		}
		String url = "http://www.yiqihi.com/trip/" + id + "&name=plan";
		String html = null;
		StringBuilder subject = new StringBuilder();
		subject.append(values[0]);
		subject.append("您好,一起嗨行程单已送达，请尽快确认!");
		html = ServiceFactory.mail().createTripDoc(values[0] + ",您好", "", url, "请尽快确认!");
//		oResult.append("xeach", ServiceFactory.mail().sendMail(values[1], subject.toString(), html, oFile.getString("fileName")));
		return oResult;
	}

	/* 修改我的行程状态 */
	public BasicDBObject checkMyTrip(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", true);
		String sql = "update trip set last_modified=?,state=? where short_id=? and (user_id=? or customer_userId=?)";
		String userId = String.valueOf(context.user.getUserId());
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(System.currentTimeMillis()), oReq.getString("state"),
				oReq.getString("shortId"), userId, userId });
		return oResult;
	}

	/* 读取我的行程 */
	public BasicDBObject readMyTrips(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		int pageNumber = oReq.getInt("pageNumber") - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		int pageSize = oReq.getInt("pageSize");
		String[] params = new String[] { String.valueOf(context.user.getUserId()), String.valueOf(context.user.getUserId()) };
		int totalCount = Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select count(*) from trip where (user_id=? or customer_userId=?) ", params, 0, 0)[0][0]);
		oResult.append("totalCount", totalCount);
		int start = pageNumber * pageSize;
		String sql = "select catalog,short_id,state,snapshot,title,description,city,chargeWay,currency,price,locations,message,rank,customer_userName from trip where (user_id=? or customer_userId=?) order by last_modified desc";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, params, start, pageSize);
		oResult.append("items", items);
		return oResult.append("xeach", true);
	}

	/* 读取我发布的活动 */
	public BasicDBObject readMyActivities(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String sql = null;
		int tripCount = Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select count(*) from activity_trip where user_id=? and (state<=3) ", new String[] { String.valueOf(context.user.getUserId()) }, 0, 0)[0][0]);
		int carCount = Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select count(*) from activity_car where user_id=? and (state<=3) ", new String[] { String.valueOf(context.user.getUserId()) }, 0, 0)[0][0]);
		int productCount = Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select count(*) from activity_product where user_id=? and (state<=3)", new String[] { String.valueOf(context.user.getUserId()) }, 0, 0)[0][0]);
		int totalCount = tripCount + carCount + productCount;
		oResult.append("totalCount", totalCount);
		int pageNumber = oReq.getInt("pageNumber") - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		int pageSize = oReq.getInt("pageSize");
		int totalReadCount = pageSize;
		int start = pageNumber * pageSize;
		sql = "select catalog,short_id,state,snapshot,title,description,city,chargeWay,currency,price,locations,message,rank from activity_trip where (user_id=?) and (state<=3) order by last_modified desc";
		if (start < tripCount) {
			int readCount = readMyActivity(context, sql, "trip", start, totalReadCount, tripCount - start, oResult);
			start = 0;
			totalReadCount -= readCount;
		} else {
			start = tripCount;
		}

		sql = "select catalog,short_id,state,snapshot,title,map_id,description,city,car_brand,car_model,car_logo,chargeWay,rank,currency,price,locations,message from activity_car where (user_id=?) and (state<=3) order by last_modified desc";
		if (start < carCount) {
			int readCount = readMyActivity(context, sql, "car", start, totalReadCount, carCount - start, oResult);
			start = 0;
			totalReadCount -= readCount;
		} else {
			start = carCount;
		}

		sql = "select category,short_id,state,snapshot,title,description,country,chargeWay,currency,brand,price,model,color,message from activity_product where (user_id=?) and (state<=3) order by last_modified desc";
		if (start < productCount) {
			int readCount = readMyActivity(context, sql, "product", start, totalReadCount, productCount - start, oResult);
			start = 0;
			totalReadCount -= readCount;
		} else {
			start = productCount;
		}
		return oResult.append("xeach", true);
	}

	/* 读取我发布的需求 */
	public BasicDBObject readMyRequires(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		int pageNumber = oReq.getInt("pageNumber") - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		int pageSize = oReq.getInt("pageSize");
		String sql = "select id,short_id,catalog,user_id,title,locations,tags,dept_date,currency,price,chargeWay,"
				+ "contact_person,contact_mobile,contact_qq,contact_msn,car_count,car_model,"
				+ "person,description,message,state from activity_req where(user_id=?) and (state<=3) order by last_modified desc";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()) },
				pageNumber, pageSize);
		oResult.append("items", items);
		return oResult.append("xeach", true);
	}

	public BasicDBObject readToCheckCount() {
		BasicDBObject oResult = new BasicDBObject();
		String sql = "select count(*) from activity_car where state=0";
		oResult.append("carCount", Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0)[0][0]));
		sql = "select count(*) from activity_room where state=0";
		oResult.append("roomCount", Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0)[0][0]));
		sql = "select count(*) from activity_trip where state=0";
		oResult.append("tripCount", Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0)[0][0]));
		sql = "select count(*) from activity_req where state=0";
		oResult.append("reqCount", Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0)[0][0]));
		sql = "select count(*) from activity_product where state=0";
		oResult.append("productCount", Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0)[0][0]));
		DataResult dr = DataCollection.find(Const.defaultMongoServer, Const.defaultMongoDB, "topics", new BasicDBObject().append("state", 0), null, null, 0, 1);
		oResult.append("topicCount", dr.totalCount);
		return oResult;
	}

	/* 审核通过活动 */

	/* 读取要审核的服务 */
	public BasicDBObject readToCheckActivities(Context context, BasicDBObject oReq, BizType type) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		int pageNumber = oReq.getInt("pageNumber") - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		String sql = null;
		String totalSql = null;
		BasicDBList items = new BasicDBList();
		if (type == BizType.car && context.user.getRole().isCarAssistant()) {
			sql = "select catalog,short_id,state,snapshot,title,map_id,description,city,car_brand,car_model,car_logo,price,user_id,user_name,message,last_modified from activity_car where state=0 order by last_modified desc";
			totalSql = "select count(*) from activity_car where state=0";
		} else if (type == BizType.room && context.user.getRole().isRoomAssistant()) {
			sql = "select catalog,short_id,state,snapshot,title,description,city,price,lodge_type,lease_type,location,user_id,user_name,message,last_modified from activity_room where state=0 order by last_modified desc";
			totalSql = "select count(*) from activity_room where state=0";
		} else if (type == BizType.trip && context.user.getRole().isTripAssistant()) {
			sql = "select catalog,short_id,state,snapshot,title,description,city,price,locations,user_id,user_name,message,last_modified from activity_trip where state=0 order by last_modified desc";
			totalSql = "select count(*) from activity_trip where state=0";
		} else if (type == BizType.require && context.user.getRole().isRequireAssistant()) {
			sql = "select id,short_id,catalog,user_id,user_name,title,location,language,service,currency,price,chargeWay,"
					+ "contact_person,contact_mobile,contact_qq,contact_msn,car_count,car_model,"
					+ "person,description,picture from activity_req where short_id=?";
			totalSql = "select count(*) from activity_req where state=0";
		} else if (type == BizType.product && context.user.getRole().isProductAssistant()) {
			sql = "select category,short_id,user_id,user_name,state,snapshot,title,description,country,chargeWay,currency,price,model,color,last_modified,message from activity_product where state=0 order by last_modified desc";
			totalSql = "select count(*) from activity_product where state=0";
		}
		if (sql != null) {
			items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, pageNumber, oReq.getInt("pageSize"));
			int totalCount = Integer.parseInt(DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, totalSql, 0, 0)[0][0]);
			return oResult.append("xeach", true).append("items", this.normalizeDesc(items)).append("totalCount", totalCount);
		} else {
			return oResult.append("xeach", true).append("items", items).append("totalCount", 0);
		}
	}

	/* 读取相关的推荐活动 */
	public BasicDBList[] readRecommends(BizType type, String country, int pageNumber, int pageSize) {
		pageNumber = pageNumber - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		String sql = "select short_id,snapshot,title from " + tableBy(type) + " where country=? and state=2 order by rand()";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { country }, pageNumber, pageSize);
		BasicDBList[] result = new BasicDBList[2];
		result[0] = items;
		result[1] = new BasicDBList();
		pageSize = pageSize / 2;
		while (pageSize < items.size()) {
			result[1].add(items.get(pageSize));
			items.remove(pageSize);
		}
		return result;
	}

	/* 读取活动 */
	public BasicDBList readRandoms(int type, String country, int pageNumber, int pageSize) {
		pageNumber = pageNumber - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		String sql = "select short_id,snapshot,title,description,price,currency from " + tableBy(BizType.values()[type])
				+ " where country=? and state=2 order by rand()";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { country }, pageNumber, pageSize);
		for (int i = 0; i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			oItem.append("description", StringUtils.abbreviate(TextUtil.extractText(oItem.getString("description")), 30));
		}
		return items;
	}

	/* 最新更新 */
	public BasicDBList readUpdates(int type, String country, int pageNumber, int pageSize) {
		pageNumber = pageNumber - 1;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		String sql = "select short_id,snapshot,title,description,price,currency from " + tableBy(BizType.values()[type])
				+ " where country=? and state=2 order by create_date desc";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { country }, pageNumber, pageSize);
		for (int i = 0; i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			oItem.append("description", StringUtils.abbreviate(TextUtil.extractText(oItem.getString("description")), 50));
		}
		return items;
	}

	public ServiceName serviceIndexBy(BizType type) {
		switch (type) {
		case topic:
			return ServiceName.TopicIndex;
		case product:
			return ServiceName.ProductIndex;
		case trip:
		case car:
			return ServiceName.TripIndex;
		case require:
			return ServiceName.RequireIndex;
		default:
			return null;
		}
	}

	/* 修改活动的状体 */
	public BasicDBObject changeState(Context context, BasicDBObject oReq, BizType type) {
		String tableName = this.tableBy(type);
		BasicDBObject oResult = new BasicDBObject().append("xeach", false);
		String sql = null;
		String[][] rows = null;
		sql = "select state,id from " + tableName + " where user_id=? and short_id=?";
		rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql,
				new String[] { String.valueOf(context.user.getUserId()), oReq.getString("shortId") }, 0, 0);
		if (rows == null || rows.length == 0) {
			return oResult.append("message", "记录不存在");
		}
		int state = oReq.getInt("state");
		if (Integer.parseInt(rows[0][0]) != state) {
			return oResult.append("message", "状态和服务器的状态不一致,请与管理员联系.");
		}
		state = state == RecordState.Open.ordinal() ? RecordState.Close.ordinal() : RecordState.Open.ordinal();
		if (state == RecordState.Close.ordinal()) {
			ZeroConnect.removeIndex(serviceIndexBy(type), new BasicDBObject().append("id", oReq.getString("shortId")));
		}
		sql = "update " + tableName + " set state=? where short_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(state), oReq.getString("shortId") });
		return oResult.append("xeach", true).append("state", state);
	}

	/* 删除活动 */
	public BasicDBObject delete(Context context, BasicDBObject oReq, BizType type) {
		String tableName = this.tableBy(type);
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		if (oReq.containsField("mapId") && !oReq.getString("mapId").equals("0")) {
			DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "delete from map where user_id=? and map_id=?",
					new String[] { String.valueOf(context.user.getUserId()), oReq.getString("mapId") });
		}
		String sql = "select prices from " + tableName + " where short_id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { oReq.getString("shortId") }, 0, 0);
		if (rows.length < 0) {
			return result;
		}
		BasicDBList prices = (BasicDBList) JSON.parse(rows[0][0]);
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update " + tableName + " set state=? where user_id=? and short_id=?", new String[] {
				String.valueOf(RecordState.Delete.ordinal()), String.valueOf(context.user.getUserId()), oReq.getString("shortId") });
		if (type == BizType.require) {
			context.user.updateRequireCount(-1);
		} else {
			context.user.updateServiceCount(-1);
			for (int i = 0; i < prices.size(); i++) {
				BasicDBObject oPrice = (BasicDBObject) prices.get(i);
				DataCollection.remove(context.user.getServer(), Const.defaultMongoDB, "calendar", new BasicDBObject()
						.append("shortId", oReq.getLong("shortId")).append("bizName", oPrice.getString("name")));
			}
		}
		ZeroConnect.removeIndex(serviceIndexBy(type), new BasicDBObject().append("id", oReq.getString("shortId")));
		result.append("xeach", true);
		return result;
	}

	public void removeIndex(BizType type, BasicDBObject oReq) {
		switch (type) {
		case car:
		case trip:
			ZeroConnect.removeIndex(ServiceName.TripIndex, oReq);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unused")
	public BasicDBObject update(Context context, BasicDBObject oReq) {
		if (oReq.containsField("description")) {
			String html = oReq.getString("description");
			HtmlDom dom = new HtmlDom();
			dom.load(html, "utf-8");
			dom.clean(true);
			html = dom.getNodeHtml(dom.rootNode.domNode);
			oReq.append("description", html);
		}
		if (oReq.containsField("photos")) {
			this.normalizePhotos(oReq);
		}
		BizType type = BizType.values()[oReq.getInt("bizType", -1)];
//		switch (type) {
//		case trip:
//			TripCatalog c = TripCatalog.values()[oReq.getInt("catalog", -1)];
//			switch (c) {
//			case plan:
//				break;
//			default:
//				this.removeIndex(type, oReq);
//				break;
//			}
//			break;
//		default:
//			this.removeIndex(type, oReq);
//			break;
//		}
		return oReq;
	}

	public void readActivityComments(Context context, BasicDBObject oResult, BizType type, String activityId) {
		String tableName = this.tableBy(type);
		String sql = "select comment_id,short_id from " + tableName + " where user_id=? and id=?";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(context.user.getUserId()),
				activityId }, 0, 0);
		if (rows == null || rows.length == 0)
			return;
	}

	public BasicDBObject updateLastModified(Context context, BizType type, String activityId, BasicDBObject object) {
		String tableName = this.tableBy(type);
		BasicDBObject result = new BasicDBObject().append("xeach", false);
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select last_modified from " + tableName
				+ " where user_id=? and activity_id=?", new String[] { String.valueOf(context.user.getUserId()), activityId }, 0, 0);
		if (rows == null || rows.length == 0) {
			return result.append("message", "not exist.");
		}
		long newTime = System.currentTimeMillis();
		long time = Long.parseLong(rows[0][0]);
		if (Global.DevelopMode == false && (newTime - time <= 1800000)) {
			return result.append("message", "半小时内只能刷新一次！");
		}
		String sql = "update " + tableName + " set last_modified=? where user_id=? and activity_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(newTime), String.valueOf(context.user.getUserId()),
				activityId });
		return result.append("xeach", true);
	}

	public BasicDBObject readMap(BasicDBObject oReq) {
		String sql = "select map_id from map where map_type=0 order by length(content) ";
		return DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, oReq.getInt("offset"), 1);
	}

	public BasicDBObject deleteMap(BasicDBObject oReq) {
		String sql = "delete from map where map_id=?";
		DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { oReq.getString("mapId") });
		return readMap(oReq);
	}

	public HashSet<String> hashSetBy(BasicDBObject oActivity, String field) {
		Object o = oActivity.get(field);
		BasicDBList items = null;
		if (o instanceof String) {
			items = (BasicDBList) JSON.parse(String.valueOf(o));
		} else if (o instanceof BasicDBList) {
			items = (BasicDBList) oActivity.get(field);
		}
		HashSet<String> values = new HashSet<String>();
		for (int i = 0; items != null && i < items.size(); i++) {
			values.add(String.valueOf(items.get(i)));
		}
		return values;
	}

	public boolean hasTag(HashSet<String> tags, String v) {
		return tags.contains(v);
	}

	public boolean hasPriceTag(HashSet<String> priceTags, String v) {
		return priceTags.contains(v);
	}




	public BasicDBObject readByUrl(Context context, BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject().append("xeach", false).append("message", "地址不合法,请检查重新输入.");
		String url = oReq.getString("url").replaceAll("http://www.yiqihi.com/", "");
		String[] values = url.split("/");
		if (values.length < 2) {
			return oResult;
		}
		String pageName = values[0];
		String id = values[1];
		if (StringUtils.isNumeric(id) == false) {
			return oResult;
		}
		String fieldName = null;
		String tableName = null;
		switch (pageName) {
		case "guide":
			fieldName = "guide";
			tableName = "activity_trip";
			break;
		case "car":
			fieldName = "driver";
			tableName = "activity_car";
			break;
		}
		if (StringUtil.isEmpty(fieldName) == false) {
			StringBuilder sql = new StringBuilder();
			sql.append("select ");
			sql.append(fieldName);
			sql.append(",user_name,user_id,chargeWay,currency,price,tip,meal,perdiem,overtime,maxmile,trans from ");
			sql.append(tableName);
			sql.append(" where short_id=?");
			BasicDBObject object = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), new String[] { id }, 0, 0);
			if (object != null) {
				oResult.putAll((BSONObject) JSON.parse(object.getString(fieldName)));
				object.removeField(fieldName);
				oResult.removeField("message");
				oResult.append("xeach", true);
				oResult.putAll((BSONObject) object);
				oResult.append("chargeWay", context.tool.chargeWayBy(oResult.getInt("chargeWay")));
				oResult.append("currency", context.tool.currencyNameBy(oResult.getInt("currency")));
				int year = 0;
				if (oResult.containsField("birthday") == true) {
					year = DateUtil.diffYear(oResult.getString("birthday"));
				}
				if (year == 0) {
					oResult.append("age", "未填写");
				} else {
					oResult.append("age", year);
				}
				if (oResult.containsField("sex") == false) {
					oResult.append("sex", "未填写");
				}
				if (oResult.containsField("ethnicity") == false) {
					oResult.append("ethnicity", "未填写");
				}
				BasicDBList items = new BasicDBList();
				items.add(createPriceObject(oResult, "佣金", "price", 1));
				items.add(createPriceObject(oResult, "小费/每天", "tip", 1));
				items.add(createPriceObject(oResult, "餐补/每天", "meal", 0));
				items.add(createPriceObject(oResult, "住宿补助/每天", "perdiem", 0));
				items.add(createPriceObject(oResult, "超时服务计费/每小时", "overtime", 0));
				items.add(createPriceObject(oResult, "返程费/每50公里", "trans", 0));
				oResult.append("items", items);
				return oResult;
			}
		}
		return oResult;
	}

	@Override
	public DBObject handle(Context context, DBObject oReq) {
		BasicDBObject oResult = null;
		BasicDBObject oRequest = (BasicDBObject) oReq;
		int funcIndex = oRequest.getInt("funcIndex");
		BizType type = BizType.values()[oRequest.getInt("bizType", BizType.ask.ordinal())];
		switch (funcIndex) {
		/** 读取评价 **/
		case 0:
			oResult = this.readComments(context.request);
			break;
		/** 利用客户的机器,获得GOOGLE经纬度坐标 **/
		case 1:
			oResult = this.changePoints(context.request);
			break;
		/** 首页读取推荐数据 **/
		case 2:
			oResult = this.readRecommends(context.request);
			break;
		/** 从网页地址获得导游基本信息 **/
		case 3:
			oResult = this.readByUrl(context, context.request);
			break;
		/** 读取我的服务 */
		case 5:
			oResult = this.readByEdit(context, context.request);
			break;
		/** 发送行程单邮件给客人 */
		case 16:
			oResult = this.sendMailByTrip(context, context.request);
			/** 修改行程单状态 */
		case 17:
			oResult = this.checkMyTrip(context, context.request);
			break;
		/** 读取行程单 */
		case 18:
			oResult = this.readMyTrips(context, context.request);
			break;
		/** 更新服务数据 */
		case 30:
			oResult = this.update(context, context.request);
			break;
		/** 读取我的服务数据 */
		case 31:
			oResult = this.readMyActivities(context, context.request);
			break;
		/** 启用或停用服务 */
		case 32:
			oResult = this.changeState(context, context.request, type);
			break;
		/** 删除服务 */
		case 34:
			oResult = this.delete(context, context.request, type);
			break;
		/** 读取要审核的服务== */
		case 35:
			oResult = this.readToCheckActivities(context, context.request, type);
			break;
		/** 测试 --读取 */
		case 37:
			oResult = this.readMap(context.request);
			break;
		/** 测试-删除 */
		case 38:
			oResult = this.deleteMap(context.request);
			break;
		/** 读取我的发布的需求 */
		case 39:
			oResult = this.readMyRequires(context, context.request);
			break;
		}
		return oResult;
	}

	@Override
	public boolean init(boolean isReload) {
		return true;
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
		return "activity";
	}

	public static void main(String[] args) {
		ModuleActivity activity = new ModuleActivity();
		int mode = 1;
		switch (mode) {
		case 0:
			String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,short_id,price,hascar,catalog from activity_car",
					0, 0);
			for (int i = 0; i < rows.length; i++) {
				String[] row = rows[i];
				BasicDBObject oRecord = new BasicDBObject();
				oRecord.append("userId", Long.parseLong(row[0]));
				oRecord.append("shortId", Long.parseLong(row[1]));
				oRecord.append("bizType", BizType.car.ordinal());
				int catalog = Integer.parseInt(row[4]);
				BasicDBList prices = new BasicDBList();
				String bizName = null;
				bizName = "5座车";
				int price = Integer.parseInt(row[2]);
				prices.add(activity.createPriceObject(bizName, price, 5));
				oRecord.append("bizName", bizName);
				oRecord.append("catalog", catalog);
				oRecord.append("price", price);
				activity.initDays(oRecord, 360);
				BasicDBObject oReturn = new BasicDBObject().append("userId", 1);
				BasicDBObject qField = new BasicDBObject().append("shortId", Long.parseLong(row[1])).append("bizName", bizName);
				BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField, oReturn);
				if (one == null) {
					DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", oRecord);
				} else {
					qField.clear();
					qField.append("_id", one.get("_id"));
					DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField, new BasicDBObject("$set", oRecord), false);
				}
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_car set prices=? where short_id=?",
						new String[] { prices.toString(), row[1] });
				System.out.println(row[1]);
			}
			break;
		case 1:
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, "select user_id,short_id,price,hascar,catalog from activity_trip", 0, 0);
			for (int i = 0; i < rows.length; i++) {
				String[] row = rows[i];
				BasicDBObject oRecord = new BasicDBObject();
				oRecord.append("userId", Long.parseLong(row[0]));
				oRecord.append("shortId", Long.parseLong(row[1]));
				oRecord.append("bizType", BizType.trip.ordinal());
				int hasCar = Integer.parseInt(row[3]);
				int catalog = Integer.parseInt(row[4]);
				BasicDBList prices = new BasicDBList();
				int price = Integer.parseInt(row[2]);
				String bizName = null;
				if (hasCar == 0) {
					bizName = "不带车";
					prices.add(activity.createPriceObject(bizName, price, 5));
				} else {
					bizName = "5座车";
					prices.add(activity.createPriceObject(bizName, price, 5));
				}
				oRecord.append("bizName", bizName);
				oRecord.append("catalog", catalog);
				oRecord.append("price", Integer.parseInt(row[2]));
				activity.initDays(oRecord, 360);
				BasicDBObject oReturn = new BasicDBObject().append("userId", 1);
				BasicDBObject qField = new BasicDBObject().append("shortId", Long.parseLong(row[1])).append("bizName", bizName);
				BasicDBObject one = (BasicDBObject) DataCollection.findOne(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField, oReturn);
				if (one == null) {
					DataCollection.insert(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", oRecord);
				} else {
					qField.clear();
					qField.append("_id", one.get("_id"));
					DataCollection.update(Const.defaultMongoServer, Const.defaultMongoDB, "calendar", qField, new BasicDBObject("$set", oRecord), false);
				}
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_trip set prices=? where short_id=?",
						new String[] { prices.toString(), row[1] });
				System.out.println(row[1]);
			}
			break;
		}
		System.out.println("done!");
	}

}
