package org.z.core.module;

import java.util.ArrayList;

import org.bson.BSONObject;
import org.z.common.htmlpage.Tool;
import org.z.core.common.Context;
import org.z.core.interfaces.ServiceIntf;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ModuleRecommend implements ServiceIntf {

	public ModuleRecommend() {

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
		return "recommend";
	}

	public BasicDBList sliders() {
		String sql = "select title,content,picture,url from slider order by `level` desc";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		return items;
	}

	public BasicDBObject read(String city, int bizType, String position, int pageNumber, int pageSize) {
		BasicDBObject oResult = new BasicDBObject();
		pageSize = pageSize > 12 ? 12 : pageSize;
		StringBuilder sql = new StringBuilder();
		BasicDBList items = null;
		sql.append("select * from recommend where type=?");
		ArrayList<String> params = new ArrayList<String>();
		params.add(String.valueOf(bizType));
		if (!StringUtil.isEmpty(position)) {
			sql.append(" and (position=?)");
			params.add(position);
		}
		if (!StringUtil.isEmpty(city)) {
			sql.append(" and (city=?)");
			params.add(city);
		}
		sql.append(" order by timestamp desc");
		pageNumber--;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), DataSet.toParameters(params), pageNumber * pageSize,
				pageSize);
		if (items == null) {
			items = new BasicDBList();
		} else {
			for (int i = 0; i < items.size(); i++) {
				BasicDBObject oItem = (BasicDBObject) items.get(i);
				BasicDBObject o = (BasicDBObject) JSON.parse(oItem.getString("metaContent"));
				oItem.putAll((BSONObject) o);
			}
		}
		oResult.append("items", items);
		return oResult;
	}

	public BasicDBList read(int bizType, String contient, int pageNumber, int pageSize) {
		StringBuilder sql = new StringBuilder();
		BasicDBList items = null;
		sql.append("select * from recommend where position in (select distinct country from country_dict where continent=?)");
		sql.append(" order by timestamp desc");
		pageNumber--;
		pageNumber = pageNumber < 0 ? 0 : pageNumber;
		items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql.toString(), new String[] { contient }, pageNumber * pageSize, pageSize);
		if (items == null) {
			items = new BasicDBList();
		} else {
			for (int i = 0; i < items.size(); i++) {
				BasicDBObject oItem = (BasicDBObject) items.get(i);
				BasicDBObject o = (BasicDBObject) JSON.parse(oItem.getString("metaContent"));
				oItem.putAll((BSONObject) o);
			}
		}
		return items;

	}

	public ArrayList<BasicDBObject> readTrips(int size) {
		ArrayList<BasicDBObject> items = DataSet.queryDBObjects(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select short_id,title,price,currency,country,snapshot  from activity_trip where catalog=1 and state=2 order by rand() limit " + size, 0, 0);
		return items;
	}

	public BasicDBObject readPerson(String userId) {
		BasicDBObject one = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB,
				"select sex,birthday,city as belong_city,age_work,age_drive,height,blood,build,marriage from user where user_id=?", new String[] { userId }, 0,
				0);
		one.append("age", Tool.yearDiff(one.getLong("birthday")));
		return one;
	}

	protected BasicDBObject readBlocks(BasicDBObject oReq) {
		BasicDBObject oResult = new BasicDBObject();
		String sql = "select * from recommendblock";
		BasicDBList items = DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		oResult.append("data", items);
		return oResult;
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		int funcIndex = 0;
		if (oReq.containsField("funcIndex")) {
			funcIndex = Integer.parseInt(String.valueOf(oReq.get("funcIndex")));
		} else {
			funcIndex = ctx.getParamByInt("funcIndex");
		}
		BasicDBObject oResult = null;
		switch (funcIndex) {
		case 0:
			oResult = this.readBlocks(ctx.pageParams);
			break;
		case 1:
			break;
		}
		return oResult;
	}
}
