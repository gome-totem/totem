package org.x.server.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.server.common.ZExcel;
import org.z.global.environment.Const;
import org.z.global.util.StringUtil;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataResult;

import com.gome.totem.sniper.util.FileUtil;
import com.gome.totem.sniper.util.TimeUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class SearchMapHandler {
	
	private static Logger logger = LoggerFactory.getLogger(SearchMapHandler.class);
	private static final String qLogs = "searchlogs";
	private static final String MAPREDUCE = "zdx_mapReduce_searchmap";//mapreduce output的表
	private static final String CITY_100 = "zdx_mapReduce_city100";//mapreduce output的表
	private static final String Question_100 = "zdx_mapReduce_question100";//mapreduce output的表
	private static final String CatId_100 = "zdx_mapReduce_catId100";//mapreduce output的表
	private static final String TOP_100 = "eye_top_100_data";//存放淘宝数据的中间表，servlet查询的表，由job每天维护一次
	public static final String TOP_data_100 = "eye_top_100_map";//存放搜索地图数据中间表，当内存中没有数据时，从此表读数据
	public static final String CAT_KEYS_tmp = "eye_cat_keys_tmp";
	public static final String CAT_KEYS_data = "eye_cat_keys_data";
	private static final String TAOBAO_100 = "gomeTaobaoMap";
	public static BasicDBList city_100 = new BasicDBList();
	public static BasicDBList keys_100 = new BasicDBList();
	public static HashMap<String, Object> city_keys = new HashMap<String, Object>();
	public static HashMap<String, Object> catId_keys = new HashMap<String, Object>();
	
	private static final int howmany = 9;
	static String taobaoName = null;
	
	/**
	 * 统计9天之内城市搜索排行榜100
	 * 根据城市搜索关键词搜索次数之和
	 * **/
	public static void City_Top_100(){
		String map = "function(){var cityName = this.cityName; " +
				"cityName = cityName.replace(new RegExp('\\\\\\.','g'),'_'); " +
				"cityName = cityName.replace(new RegExp('\\\\\\$','g'),'_');" +
				"emit(cityName,1);}";
		String reduce = "function(key, values){return values.length;}";
		mapReduce(map, reduce, CITY_100, city_100, null, 1);
		logger.info("Top city 100 deal finish!");
	}
	
	/**
	 * 统计9天之内搜索关键词排行榜100
	 * 根据搜索关键词搜索次数
	 * **/
	public static void Question_Top_100(){
		String map = "function(){var question = this.question; " +
				"question = question.replace(new RegExp('\\\\\\.','g'),'_'); " +
				"question = question.replace(new RegExp('\\\\\\$','g'),'_');" +
				"emit(question,1);}";
		String reduce = "function(key, values){return values.length;}";
		mapReduce(map, reduce, Question_100, keys_100, null, 2);
		logger.info("Top question 100 deal finish!");
	}
	
	/**
	 * 统计9天之内分类搜索排行榜100
	 * 根据分类下搜索关键词搜索次数之和
	 * **/
	public static void CatId_Top_100() {
		String map = 
			"function(){ " +
			"	var catId = this.catId;"+
			"	catId = catId.replace(new RegExp('\\\\\\.','g'),'_');" +
			"	catId = catId.replace(new RegExp('\\\\\\$','g'),'_');"+
			"	if(catId != '' && catId != null){ " +
			"		emit(catId, {'keys':[this.question]}); "+
			"	}"+
			"};";
		String reduce = 
			"function(key, values){ "+
			"	var result = {'keys':[]},tmp = []; "+
			"	for ( var i in values) {"+
			"		values[i].keys.forEach(function(value){ "+
			"			result.keys.push(value); "+
			"		});"+
			"	}"+
			"	return result; "+
			"};";
		
		mapReduce(map, reduce, CatId_100, null, catId_keys, 3);
		logger.info("Top catId 100 deal finish!");
	}
	
	/**
	 * 统计城市搜索的关键词
	 * 
	 * **/
	public static void All(){
		String map = 
		"function(){ "+
		"	var cityName = this.cityName; "+
		"	cityName = cityName.replace(new RegExp('\\\\\\.','g'),'_');" +
		"	cityName = cityName.replace(new RegExp('\\\\\\$','g'),'_'); "+
		"	if(cityName != '' && cityName != null){ "+
		"		emit(cityName, {'keys':[this.question]}); "+
		"	} "+
		"}";
		String reduce = 
		"function(key, values){ "+
		"	var tmp = [], put = true, result = {'keys':[]},len = 0;"+
		"	for ( var i in values) {"+
		"		values[i].keys.forEach(function(question){ "+
		"			put = true; "+
		"			tmp = result.keys.concat();"+
		"			len = tmp.length; "+
		"			for ( var i = 0; i < len; i++) { "+
		"			if (tmp[i] == question) { put = false; } "+
		"			} "+
		"			if(put){ "+
		"				result.keys.push(question); "+
		"			}"+
		"			tmp = []; "+
		"		}); "+
		"	} "+
		"	return result; "+
		"}";
		mapReduce(map, reduce, MAPREDUCE, null, city_keys, 0);
		logger.info("MapReduce search deal finish!");
	}
	
	/**
	 * 统计一个月之内搜索关键词和次数
	 * 
	 * **/
	public static void allQuestionByMonth(){
		String map = 
			"function(){"+
			"	var keyword = this.question;"+
			"	keyword = keyword.replace(new RegExp('\\\\\\.','g'),'_');"+
			"	keyword = keyword.replace(new RegExp('\\\\\\$','g'),'_');"+
			"	if(keyword != '' && keyword != null){"+
			"		emit(keyword, 1);"+
			"	}"+
			"}";
		String reduce = 
			"function(key, values){"+
			"	return values.length;"+
			"}";
		
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, qLogs);
//		BasicDBObject oReq = new BasicDBObject().append("time", new BasicDBObject().append("$gte", 0l));
		coll.mapReduce(map, reduce, "temp_keys", null);
		DataResult dataResult = DataCollection.find(Const.defaultLogServer, Const.defaultLogDB, "temp_keys",null,null);
		BasicDBList results = dataResult.getList();
		HashMap<String, Integer> after = new HashMap<String, Integer>();
				
		for (int i = 0, len = results.size(); i < len; i++) {
			BasicDBObject ob = (BasicDBObject) results.get(i);
			String key = ob.getString("_id");
			key.replace("\t", " ");
			
			Pattern p = Pattern.compile("[a-zA-Z]{8}");
			Matcher m = p.matcher(key);
			boolean find = false;
			while (m.find()) {
				find = true;
			}
			
			if(find){
				Pattern c = Pattern.compile("[^a-zA-Z]");
				Matcher h = c.matcher(key);
				
				while (h.find()) {
					key = key.replace(h.group(), " ");
				}
				
				String[] strs = key.split(" ");
				
				for (int j = 0, slen = strs.length; j < slen; j++) {
					
					String str = strs[j];
					if(str.length() >= 8){
						int count = ob.getInt("value",0);
						System.out.println(str + "--->" + ob.getInt("value",0));
						if(after.get(str) != null) {
							count += after.get(str);
						}
						after.put(str, count);
					}
				}
			}
		}
		
		for (String strKey : after.keySet()) {
			System.out.println(strKey + "	" + after.get(strKey));
			FileUtil.writeFile("/server/1111.txt", strKey + "	" + after.get(strKey), true);
		}
//		StringUtil.writeToFile(after.toString(), "/server/1111.txt", "utf-8");
	}
	
	public static void allQuestion(){
		String excelName = "/doc/keywords.xls";
//		String map = "function(){" +
//				"	var keyword = this.question;" +
//				"	keyword = keyword.replace(new RegExp('\\\\\\.','g'),'_');"+
//				"	keyword = keyword.replace(new RegExp('\\\\\\$','g'),'_');"+
//				"	if(keyword != '' && keyword != null){"+
//				"		emit(keyword, 1);"+
//				"	}"+
//				"}";
//		String reduce = 
//				"function(key, values){"+
//				"	return values.length;"+
//				"}";
//		
//		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, qLogs);
//		coll.mapReduce(map, reduce, "allquestionbyzdx", null);
		
		BasicDBObject oSort = new BasicDBObject().append("value", -1);
		
		DataResult dataResult = DataCollection.find(Const.defaultLogServer, Const.defaultLogDB, "allquestionbyzdx", null, oSort);
		
		long count = dataResult.getTotalCount();
		int total = (int) (count / 10000 == 0 ? count / 10000 : (count / 10000 + 1));
		
		WritableWorkbook wwb = ZExcel.createExcel(excelName);
		WritableSheet ws = null;
		
		for (int i = 0; i < total; i++) {
			ws = wwb.createSheet("关键词" + i, i);
			DataResult dResult = DataCollection.find(Const.defaultLogServer, Const.defaultLogDB, "allquestionbyzdx", null, oSort, 10000 * i, 10000);
			if(dResult == null) continue;
			BasicDBList list = dResult.getList();
			for (int j = 0, len = list.size(); j < len; j++) {
				BasicDBObject dbObject = (BasicDBObject) list.get(j);
				
				Label cell = new Label(0, j, dbObject.getString("_id"));
				try {
					ws.addCell(cell);
					cell = new Label(1, j, dbObject.getString("value"));
					ws.addCell(cell);
				} catch (RowsExceededException e) {
					e.printStackTrace();
				} catch (WriteException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			wwb.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			wwb.close();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static BasicDBList dealCategoryKeys(BasicDBObject value) {
		BasicDBList keys = (BasicDBList) value.get("keys");
		BasicDBList zkeys = new BasicDBList();
		HashMap<String, Integer> keyNumMap = new HashMap<String, Integer>();

		for (int i = 0; keys != null && i < keys.size(); i++) {
			String key = (String) keys.get(i);
			if(StringUtils.isEmpty(key)) continue;
			keyNumMap.put(key, 1);
		}
		
		Iterator<String> it = keyNumMap.keySet().iterator();
		while (it.hasNext()) {
			int count = 0;
			String key = it.next();
			for (int i = 0; keys != null && i < keys.size(); i++) {
			 	if(key.equals(keys.get(i))){
			 		count++;
			    }
			}
			keyNumMap.put(key, count);
		}
		List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>(keyNumMap.entrySet());
		Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
				return obj2.getValue() - obj1.getValue();
			}
		});
		for (int j = 0, len = info.size(); j < len;j++) {
//			if(j < 100){
				zkeys.add(new BasicDBObject().append("key", info.get(j).getKey()).append("count", info.get(j).getValue()));
//			}
		}
		return zkeys;
	}
	
	public static void mapReduce(String map, String reduce, String CollName, BasicDBList topMap,HashMap<String, Object> hMap, int msgTag){
		BasicDBObject qField = new BasicDBObject().append("time", new BasicDBObject().append("$gte", TimeUtil.getTime(howmany)));
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, qLogs);
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, CollName, qField);
		
		BasicDBObject oSort = null;
		if(msgTag == 0 || msgTag == 3){
		}else{
			oSort = new BasicDBObject().append("value", -1);
		}
		DataResult dataResult = DataCollection.find(Const.defaultLogServer, Const.defaultLogDB, CollName, null, oSort);
		BasicDBList results = dataResult.getList();
		
		if(results == null) return;
		if(msgTag == 0 || msgTag == 3){ hMap.clear();
		}else{ topMap.clear(); }
		
		BasicDBObject oFiled = null;
		switch(msgTag){
		case 0:
			for (int i = 0, len = results.size(); i < len; i++) {
				BasicDBObject result = (BasicDBObject) results.get(i);
				String id = result.getString("_id","");
				if(id == null || StringUtils.isEmpty(id)) continue;
				BasicDBObject value = (BasicDBObject) result.get("value");
				if(value == null) continue;
				BasicDBList keys = (BasicDBList) value.get("keys");
				BasicDBList tmp = new BasicDBList();
				for (int j = 0, klen = keys == null ? 0 : keys.size(); j < klen; j++) {
					tmp.add(StringUtil.XSSFilter(keys.get(j).toString()));
				}
				hMap.put(id, new BasicDBObject().append("value", tmp));
			}
			
			if(city_100.size() > 0){
				oFiled = new BasicDBObject().append("_top", "city_keys");
				String cityName = "";
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				for (int i = 0, len = city_100.size(); i < len; i++) {
					Object res = city_100.get(i);
					if(res instanceof BasicDBObject){
						cityName = null;
					}else{
						cityName = (String) city_100.get(i);
					}
					if(cityName != null && hMap.containsKey(cityName)){
						resultMap.put(cityName, hMap.get(cityName));
					}
				}
				if(resultMap.size() > 0){
					DataCollection.remove(Const.defaultLogServer, Const.defaultLogDB, TOP_data_100, oFiled);
					DataCollection.insert(Const.defaultLogServer, Const.defaultLogDB, TOP_data_100, oFiled.append("value", resultMap));
				}
			}
			break;
		case 1:
		case 2:
			for (int i = 0, len = results.size(); i < len; i++) {
				BasicDBObject result = (BasicDBObject) results.get(i);
				String id = result.getString("_id","");

				if(id == null || StringUtils.isEmpty(id)) continue;
				if(topMap.size() > 100){
					break;
				}
				topMap.add(result);
			}
			if(msgTag == 1){
				oFiled = new BasicDBObject().append("_top", "city_100");
				DataCollection.remove(Const.defaultLogServer, Const.defaultLogDB, TOP_data_100, oFiled);
				DataCollection.insert(Const.defaultLogServer, Const.defaultLogDB, TOP_data_100, oFiled.append("value", topMap));
			}else if(msgTag == 2){
				oFiled = new BasicDBObject().append("_top", "keys_100");
				DataCollection.remove(Const.defaultLogServer, Const.defaultLogDB, TOP_data_100, oFiled);
				DataCollection.insert(Const.defaultLogServer, Const.defaultLogDB, TOP_data_100, oFiled.append("value", topMap));
			}
			break;
		case 3:
			DataCollection.dropCollection(Const.defaultLogServer, Const.defaultLogDB, CAT_KEYS_data);
			BasicDBList oValue = null;
			for (int i = 0, len = results.size(); i < len; i++) {
				BasicDBObject result = (BasicDBObject) results.get(i);
				String id = result.getString("_id","");
				if(id == null || StringUtils.isEmpty(id) || id.length() > 18) continue;
				BasicDBObject value = (BasicDBObject) result.get("value");
				if(value == null) continue;
				oValue = dealCategoryKeys(value);
				hMap.put(id, oValue);
				DataCollection.insert(Const.defaultLogServer, Const.defaultLogDB, CAT_KEYS_data, new BasicDBObject().append("catId", id).append("keys", oValue));
			}
			DBCollection table = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, CAT_KEYS_data);
			table.ensureIndex("catId");
			break;
		default:
			break;
		}
	}
	
	/**
	 * 备份淘宝数据
	 *淘宝数据每天跟新一次，若数据更新则同步至mongo库，若无则使用mongo库作为数据查询
	 * **/
	public static void copyTable(){
		if(taobaoName == null)
			taobaoName = TAOBAO_100 + TimeUtil.getStringDate(new Date().getTime());
		
		DataResult dataResult = DataCollection.find(Const.defaultDictMongo, Const.defaultDictMongoDB, taobaoName, null, null);
		long count = dataResult.getTotalCount();
		if(count == 0) {
			return;
		}
		BasicDBList oResults = dataResult.getList();
		if(oResults == null) {
			return;
		}
		DataCollection.dropCollection(Const.defaultDictMongo, Const.defaultDictMongoDB, TOP_100);
		DBCollection table = DataCollection.getCollection(Const.defaultDictMongo, Const.defaultDictMongoDB, TOP_100);
		if(table == null){
			return;
		}
		List<DBObject> listDB = new ArrayList<DBObject>();
		for (int i = 0; i < oResults.size(); i++) {
			BasicDBObject result = (BasicDBObject)oResults.get(i);
			result.removeField("_id");
			listDB.add(result);
		}
		table.insert(listDB);
		logger.info("TaoBao top 100 data update!");
	}
	
	public static BasicDBList taobaoKeys(String gomeCatId, String returnType){
		BasicDBObject qField = new BasicDBObject().append("gomeCatId", gomeCatId);
		DBObject dataResult = DataCollection.findOne(Const.defaultDictMongo, Const.defaultDictMongoDB, TOP_100, qField);
		if(dataResult == null) return null;
		return (BasicDBList) dataResult.get(returnType);
	}
	
	/***
	 * 统计关键词点击在第2页的数据
	 * 
	 * **/
	public static void keysInSecondPage(){
		String map = 
"function(){" +
"	var keyword = this.question;"+
"	if(keyword != '' && keyword != null){"+
"		var num = this.pageNumber, num = Number(num);"+
"		if(num == 2){"+
"			emit(this._id, {'keys':[keyword]});"+
"		}" +
"	}"+
"};";
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "usrsactionlogs");
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, "keysInSecondPage", null);
	}
	
	/**
	 * 搜索关键词点击在第6行以下的
	 * **/
	public static void keysHigherLine6(){
		String map = 
"function(){" +
"	var keyword = this.question;"+
"	if(keyword != '' && keyword != null){"+
"		var num = this.postion, num = Number(num);"+
"		if(num > 24){"+
"			emit(this._id, {'keys':[keyword]});"+
"		}" +
"	}"+
"};";
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "usrsactionlogs");
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, "keysHigherLine6", null);
	}
	
	/**
	 * 搜索关键词无结果的
	 * **/
	public static void keysWithNoResult(){
		String map = 
"function(){" +
"	var keyword = this.question;"+
"	if(keyword != '' && keyword != null){" +
"		var facets = this.facets;"+
"		if(facets == 'null' || facets == '' || facets == null){"+
"			emit(keyword, {keys:[this.ResultCount]});"+
"		}" +
"	}"+
"};";
		String reduce = 
" function(key, values){ "+
"	var tmp = [], put = true, result = {'keys':[]}; "+
"	for ( var i in values) {"+
"		values[i].keys.forEach(function(key){ "+
"			tmp = result.keys.concat();"+
"			result.keys.push(key); "+
"			tmp = []; "+
"		});"+
"	} "+
"	return result; "+
"};";
		BasicDBObject qField = new BasicDBObject().append("time", new BasicDBObject().append("$gte", TimeUtil.getTime(howmany)));
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "searchlogs");
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, "keysWithNoResult", qField);
	}
	
	/**
	 * 搜索的关键词但没有点击过的
	 * 
	 * */
	public static void keysWithNoClick(){
		String map = 
"	function(){" +
"		var keyword = this.question;" +
"		if(keyword != '' && keyword != null){" +
"			emit(keyword, 1);" +
"		}" +
"	}";
		String reduce = 
"	function(key, values){" +
"		return values.length;" +
"	}";
		BasicDBObject qField = new BasicDBObject().append("time", new BasicDBObject().append("$gte", TimeUtil.getTime(howmany)));
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "searchlogs");
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, "keysWithNoClickShard1", qField);
		
		coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "usrsactionlogs");
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, "keysWithNoClickShard2", qField);
	}
	
	public static void cat500(){
		String map = 
"function(){"+
"	var catId = this.catId;" +
"	if(catId != '' && catId != null ){" +
"		emit(catId, 1);" +
"	}" +
"}";
		
		String reduce = 
"function(key, values){" +
"	return values.length;" +
"}";
		
		DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "searchlogs");
		
		if(coll == null) return ;
		coll.mapReduce(map, reduce, "cat500", null);
		
		List<ServerAddress> address = new ArrayList<ServerAddress>();
		List<MongoCredential> credent = new ArrayList<MongoCredential>();
		
		try {
			address.add(new ServerAddress("10.58.50.55", 19753));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		credent.add(MongoCredential.createMongoCRCredential("gome", "logs", "totem".toCharArray()));
		MongoClient mongoClient = new MongoClient(address, credent);
		DB db = mongoClient.getDB("logs");
		
		DBCollection dbc = db.getCollection("cat500");
		dbc.addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		DBCursor curs = null;
		try {
			curs = dbc.find(null, null);
			
			while (curs.hasNext()) {
				BasicDBObject dObj = (BasicDBObject) curs.next();
				String key = dObj.getString("_id");
				int count = ((Double) dObj.get("value")).intValue();
				m.put(key, count);
			}
		} finally {
			curs.close();
		}
		
		List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>(m.entrySet());
		Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
				return obj2.getValue() - obj1.getValue();
			}
		});
		
		for (int i = 0; i < info.size(); i++) {
			Map.Entry<String, Integer> ss = info.get(i);
			FileUtil.writeFile("/server/1234.txt", ss.getKey() + "	" + ss.getValue(), true);
		}
	}
	
	private static String reduce = 
"function(key, values){ "+
"	var tmp = [], put = true, result = {'keys':[]}; "+
"	for ( var i in values) { "+
"		values[i].keys.forEach(function(key){ "+
"			put = true; "+
"			tmp = result.keys.concat();"+
"			for ( var j = 0; j < tmp.length; j++){"+
"				if (tmp[j] == key) {"+
"					put = false;"+
"				}"+
"			} "+
"			if(put){ "+
"				result.keys.push(key); "+
"			}  "+
"			tmp = []; "+
"		});"+
"	} "+
"	return result; "+
"};";
	
	
	
	public static void main(String[] args) {
		int mode = 2;
		BasicDBObject qField = null;
		switch(mode){
			case 0:
				//获取当日搜索无结果关键词
				qField = new BasicDBObject().append("time", new BasicDBObject().append("$gte", TimeUtil.getTime(2))).append("ResultCount", 0).append("catId", "");
				DataResult s = DataCollection.find(Const.defaultLogServer, Const.defaultLogDB, "searchlogs", qField,null);
				if(s == null) return;
				BasicDBList list = s.getList();
				BasicDBList result = new BasicDBList();
				for (int i = 0,len = list.size(); i < len; i++) {
					String q = ((BasicDBObject)list.get(i)).getString("question","");
					if(!result.contains(q)){
						result.add(q);
					}
				}
				StringUtil.writeToFile(result.toString(), "/server/solr/无结果页.txt", "utf-8");
				break;
			case 1:
				//删除9天之前的日志
				qField = new BasicDBObject().append("time", new BasicDBObject().append("$lt", TimeUtil.getTime(howmany)));
				DataCollection.remove(Const.defaultLogServer, Const.defaultLogDB, "searchlogs", qField);
				break;
			case 2:
//				System.out.println(TimeUtil.firstDayOfMonth());
//				allQuestionByMonth();
//				keysInSecondPage();
//				keysHigherLine6();
//				keysWithNoResult();
//				keysWithNoClick();
				cat500();
				break;
			case 3:
//				City_Top_100();
//				Question_Top_100();
//				CatId_Top_100();
//				All();
				allQuestion();
//				copyTable();
//				categoryKeys();
//				CatId_Top_100();
//				DataCollection.dropCollection(Const.defaultLogServer, Const.defaultLogDB, CAT_KEYS_data);
				break;
			case 4:
				testStringFilter();
			case 5:
//				String map = "function(){" +
//						"	var keyword = this.question;" +
//						"	if(keyword.length > 22){" +
//						"		keyword = keyword.replace(new RegExp('\\\\\\.','g'),'_');"+
//						"		keyword = keyword.replace(new RegExp('\\\\\\$','g'),'_');" +
//						"		emit(this.time, {'keys':[keyword]});"+
//						"	}" +
//						"}";
//				String reduce = 
//						"function(key, values){ "+
//						"	var tmp = [], put = true, result = {'keys':[]},len = 0;"+
//						"	for ( var i in values) {"+
//						"		values[i].keys.forEach(function(question){ "+
//						"			put = true; "+
//						"			tmp = result.keys.concat();"+
//						"			len = tmp.length; "+
//						"			for ( var i = 0; i < len; i++) { "+
//						"			if (tmp[i] == question) { put = false; } "+
//						"			} "+
//						"			if(put){ "+
//						"				result.keys.push(question); "+
//						"			}"+
//						"			tmp = []; "+
//						"		}); "+
//						"	} "+
//						"	return result; "+
//						"}";
//				DataCollection.mapReduce(Const.defaultLogServer, Const.defaultLogDB, qLogs, map, reduce,"errorKeys", null, null);
				
				DBCollection con = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "errorKeys");
				DBCursor cur = con.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				
				int totalCount = cur.count();
				int count = 0;
				BasicDBList results = new BasicDBList();
				BasicDBObject _cur = null, value = null;
				while (cur.hasNext()) {
					_cur = (BasicDBObject) cur.next();
					++ count;
					if(_cur != null){
						value = (BasicDBObject) _cur.get("value");
						if(value != null){
							BasicDBList keys = (BasicDBList) value.get("keys");
							
							if(keys != null){
								for (int i = 0, len = keys.size(); i < len; i++) {
									results.add(keys.get(i));
								}
							}
						}
					}
					if((count % 10000) == 0){
						System.out.println("index ====>" + count);
						StringUtil.writeToFile(results.toString(), "/server/solr/error_"+count+".txt", "utf-8");
						results = new BasicDBList();
					}
				}
				System.out.println(totalCount);
				break;
			case 6:
				int all = 2801500, index = 0;
				while(index <= all){
					index ++;
					if(index % 10000 == 0){
						System.out.println(index);
					}
				}
				break;
			case 7:
				String map = "function(){emit(this.question, 1)}";
				String reduce = "function(key, values){return values.length;}";
				DBCollection coll = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, qLogs);
				
				if(coll == null) return ;
				coll.mapReduce(map, reduce, "tmp_zdx_del_later", qField);
				
				DBCollection conect = DataCollection.getCollection(Const.defaultLogServer, Const.defaultLogDB, "tmp_zdx_del_later");
				DBCursor zCur = conect.find().addOption(Bytes.QUERYOPTION_NOTIMEOUT);
				
				while(zCur.hasNext()){
					BasicDBObject r = (BasicDBObject) zCur.next();
					String key = r.getString("_id","");
					if(key.equalsIgnoreCase("SAMSUNG")){
						FileUtil.writeFile("/server/countresult.txt", key + "-" + r.getDouble("value"), true);
					}else if(key.equalsIgnoreCase("I9500")){
						FileUtil.writeFile("/server/countresult.txt", key + "-" + r.getDouble("value"), true);
					}
				}
				break;
			default:
				break;
		}
		System.out.println("done!");
	}
	
	/**
	 *过滤关键词中的特殊字符
	 *
	 * **/
	public static String StringFilter(String str) throws PatternSyntaxException {
		String regEx = "▲";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	public static void testStringFilter() throws PatternSyntaxException {
		String str = "▼南京座機電話號碼☉〇▲Q97809598▲套┄├DF8/";
		System.out.println(str);
		System.out.println(StringFilter(str));
	}
}
