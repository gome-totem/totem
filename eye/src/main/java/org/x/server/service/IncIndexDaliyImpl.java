package org.x.server.service;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.x.server.controller.IncIndexDaliyController;
import org.z.global.environment.Const;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;

import com.gome.totem.sniper.util.StringUtil;
import com.gome.totem.sniper.util.TimeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MapReduceOutput;
@Service
public class IncIndexDaliyImpl implements IIncIndexDaliy {
	public static Logger log = LoggerFactory.getLogger(IncIndexDaliyController.class);
	private static final String INDEXLOGS = "indexlogs";
	private static final String MAPREDUCE = "zdx_mapReduce_daliy";

	public BasicDBObject findOneInMongo(String indexlogs2, String id) {
		long start;
		try {
			start = TimeUtil.getTimestampByDay(0);
		} catch (ParseException e) {
			start = 0l;
		}
		BasicDBObject qField = new BasicDBObject("id", id).append("time",
				new BasicDBObject("$gte", start));
		DataResult dataResult = DataCollection.find(Const.defaultLogServer,
				Const.defaultLogDB, indexlogs2, qField, new BasicDBObject(
						"_id", 0));

		if (dataResult == null)
			return null;
		BasicDBList allResults = dataResult.getList();

		if (dataResult.getTotalCount() == 0)
			return null;
		StringUtil.quickSortByName("time", allResults);

		BasicDBObject result = (BasicDBObject) allResults.get(0);
		return result;
	}

	public  BasicDBObject dealDaliyInMongo(String collName, int dateIndex) {
		long start = 0l, end = 0l;
		int productSize = 0, skuSize = 0, coo8Size = 0, gomeSize = 0;
		BasicDBObject qField = null;
		BasicDBObject oResult = null;
		BasicDBObject result = null;

		try {
			start = TimeUtil.getTimestampByDay(dateIndex);
			end = TimeUtil.getTimestampByDay(dateIndex - 1);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		qField = new BasicDBObject("time", new BasicDBObject().append("$gte",
				start).append("$lt", end));
		long begin = StringUtil.nowTime();

		DBCollection coll = DataCollection.getCollection(
				Const.defaultLogServer, Const.defaultLogDB, INDEXLOGS);

		String map = "function() {"
				+ "var s = 0; for ( var i in this.skus) {s ++ ;}"
				+ "if (this.isCoo8Product == true) {emit('coo8', {p : 1,	s : s});}"
				+ "else {emit('gome', {p : 1,	s : s});}" + "};";

		String reduce = "function(key, values) {"
				+ "var pCount = 0, sCount = 0;"
				+ "for ( var i in values) { pCount += values[i].p; sCount += values[i].s;}"
				+ "return {p : pCount,s : sCount};" + "};";

		MapReduceOutput meop = coll.mapReduce(map, reduce, MAPREDUCE, qField);
		DBCollection resultColl = meop.getOutputCollection();
		DBCursor cursor = resultColl.find();

		while (cursor.hasNext()) {
			oResult = (BasicDBObject) cursor.next();
			if (oResult == null)
				break;
			String id = oResult.getString("_id", "");
			result = (BasicDBObject) oResult.get("value");
			if (result == null)
				break;
			if (id.equals("coo8")) {
				coo8Size = result.getInt("p", 0);
			} else if (id.equals("gome")) {
				gomeSize = result.getInt("p", 0);
			}

			skuSize += result.getInt("s", 0);
		}
		productSize = coo8Size + gomeSize;

		result = new BasicDBObject().append("productSize", productSize)
				.append("skuSize", skuSize).append("coo8Size", coo8Size)
				.append("gomeSize", gomeSize);

		log.info(
				"It (timestamp: {}) takes {} ms to get logs from mongo! Result:[{}]",
				new Object[] { start, StringUtil.decNow(begin), result });

		if (oResult == null) {
			return new BasicDBObject();
		}
		return result;
	}

}
