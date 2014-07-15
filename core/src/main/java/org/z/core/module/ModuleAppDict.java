package org.z.core.module;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.segment.dict.Dictionary;
import org.z.common.tree.RadixTreeImpl;
import org.z.core.common.Context;
import org.z.core.common.Facet;
import org.z.core.common.Facets;
import org.z.core.interfaces.ServiceDictIntf;
import org.z.core.suggest.Reader;
import org.z.data.analysis.SmartTokenizerFactory;
import org.z.global.connect.ZeroConnect;
import org.z.global.dict.Global;
import org.z.global.dict.Global.BizType;
import org.z.global.dict.Global.CodeType;
import org.z.global.environment.Config;
import org.z.global.environment.Const;
import org.z.global.interfaces.ModuleMsgIntf;
import org.z.global.interfaces.OnModuleMsgIntf;
import org.z.global.ip.IPSeeker;
import org.z.global.object.SecurityObject;
import org.z.global.util.DBObjectUtil;
import org.z.global.util.StringUtil;
import org.z.global.util.TextUtil;
import org.z.global.zk.ServerDict;
import org.z.global.zk.ServerDict.NodeType;
import org.z.store.mongdb.DBFileSystem;
import org.z.store.mongdb.DataCollection;
import org.z.store.mongdb.DataSet;
import org.z.store.redis.RedisFactory;
import org.z.store.redis.RedisPool;
import org.z.store.redis.RedisSocket;

import com.google.common.collect.MapMaker;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ModuleAppDict implements ServiceDictIntf, OnModuleMsgIntf {
	protected static final Logger logger = LoggerFactory.getLogger(ModuleAppDict.class);
	public static ModuleAppDict self = null;
	public String PHONE_YUNDIZ = "4008382003";
	public String PHONE_YIQIHI = "4008382003";
	public String[] ReceiveMobiles = new String[] { "13911813380", "13911813380" };
	public double SMSFee = Double.parseDouble(Config.rock().getItem("SMSFee", "0.2"));
	public String rootPath = null;
	public String[] cacheServers = null;
	public Timer messageTimer = null;
	public byte[] defaultFaceImage = null;
	public byte[] NoPictrue = null;
	/* 格式 */
	public BasicDBList hotelChains = null;
	public HashMap<String, BasicDBObject> hotelChainByCity = new HashMap<String, BasicDBObject>();
	public RadixTreeImpl<BasicDBObject> hotelChainByArea = new RadixTreeImpl<BasicDBObject>(true);
	public String dictIP = null;
	public CoreContainer cores = null;
	public int ProcessThreadCount = 0;
	public String scheduleIP = null;
	public float UsdRate = (float) 6.3;
	public float EurRate = (float) 8.3;
	public BasicDBList countries = new BasicDBList();
	public DBFileSystem shots = null;
	public DBFileSystem pictures = null;
	public DBFileSystem receipts = null;
	public Facets facets = null;
	public ConcurrentMap<String, BasicDBList> WeathMap = new MapMaker().makeMap();

	@Override
	public boolean init(boolean isReload) {
		System.setProperty("jmagick.systemclassloader", "no");
		self = this;
		scheduleIP = Config.rock().getItem("scheduleIP", "10.58.50.24");
		BasicDBObject dictNode = ServerDict.self.serverBy(Const.defaultDictMysql);
		if (dictNode == null) {
			logger.error("ServerNode[{}] not be registered in zookeeper, please run registerServerNodes", new Object[] { Const.defaultDictMysql });
			System.exit(-1);
			return false;
		}
		String dict = Config.rock().getItem("AppDictInit", "");
		dictIP = dictNode.getString("ip");
		if (dict.indexOf("tokenizer") >= 0) {
			SmartTokenizerFactory.init();
		}
		if (dict.indexOf("segment") >= 0) {
			Dictionary.init();
		}
		if (dict.indexOf("solr") >= 0) {
			File solrFile = new File(Const.SolrHome + "solr.xml");
			deleteLockFiles();
			System.setProperty("solr.solr.home", Const.SolrHome);
			cores = CoreContainer.createAndLoad(Const.SolrHome, solrFile);
		}
		if (dict.indexOf("ipaddr") >= 0) {
			initIPAddress();
		}
		if (dict.indexOf("country") >= 0) {
			initEntityDB();
			initContries();
		}
		RedisPool.use();
		logger.info("init Image MongoDB");
		shots = new DBFileSystem("image", "shots");
		pictures = new DBFileSystem("image", "pictures");
		receipts = new DBFileSystem("image", "receipts");
		facets = new Facets();
		facets.init();
		logger.info("WeatherData is start");
		return true;
	}

	public Object coreContainer() {
		return this.cores;
	}


	public BasicDBList getWeatherMap(String city) {
		return self.WeathMap.get(city);
	}

	public void setWeatherMap(String city, BasicDBList weather) {
		self.WeathMap.put(city, weather);
	}

	public String read(String prefix, String key) {
		String content = null;
		RedisSocket redis = null;
		try {
			redis = RedisFactory.get(dictIP);
			content = redis.get(prefix + "@" + key);
		} catch (Exception e) {
			redis.isAlive = false;
			logger.error("read[{}],exception[{}]", new Object[] { dictIP, e.getLocalizedMessage() });
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
		return content;
	}

	public BasicDBObject readDBObject(String prefix, String key) {
		String content = read(prefix, key);
		if (StringUtils.isEmpty(content)) {
			return null;
		}
		return (BasicDBObject) JSON.parse(content);
	}

	public BasicDBList readDBList(String prefix, String key) {
		String content = read(prefix, key);
		if (StringUtils.isEmpty(content)) {
			return null;
		}
		try {
			return (BasicDBList) JSON.parse(content);
		} catch (Exception e) {
			logger.error("readDBList[{}],error[{}]", new Object[] { content, e });
			return null;
		}
	}

	public void write(String prefix, String key, String content) {
		RedisSocket redis = null;
		try {
			redis = RedisFactory.get(dictIP);
			content = redis.set(prefix + "@" + key, content);
		} catch (Exception e) {
			redis.isAlive = false;
			logger.error("write[{}],exception[{}]", new Object[] { dictIP, e.getLocalizedMessage() });
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
	}

	public synchronized int allocateUniqueId(String prefix) {
		RedisSocket redis = null;
		int count = 1;
		try {
			redis = RedisFactory.get(dictIP);
			String value = redis.get(prefix);
			if (!StringUtils.isEmpty(value)) {
				count = Integer.parseInt(value) + 1;
			}
			redis.set(prefix, String.valueOf(count));
		} catch (Exception e) {
			redis.isAlive = false;
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
		return count;
	}

	public void deleteLockFiles() {
		File[] files = new File(Const.SolrHome).listFiles();
		int index = 0;
		while (index < files.length) {
			File f = files[index];
			if (f.isDirectory() == false) {
				index++;
				continue;
			}
			File lockFile = null;
			try {
				lockFile = new File(f.getAbsolutePath() + "/data/index/write.lock");
				if (lockFile.exists() == true) {
					FileUtils.forceDelete(lockFile);
				}
			} catch (Exception e) {
				logger.error("deleteLockFile", e);
			}
			if (lockFile.exists() == false) {
				index++;
			}
		}
		File dir = new File(Const.SolrHome + "/data/tlog");
		if (dir.exists()) {
			try {
				FileUtils.deleteDirectory(dir);
			} catch (Exception e) {
				logger.error("deleteDirectory", e);
			}
		}
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
		return "appdict";
	}

	@Override
	public boolean onFacetChange(CodeType type, BasicDBObject oRecord) {
		String value = oRecord.getString("v");
		int order = oRecord.getInt("o");
		facets.add(type, value, order);
		switch (type) {
		case country:
		case location:
			Facet facet = facets.facetBy(type);
			facet.addChild(oRecord.getString("p"), oRecord);
			break;
		}
		return true;
	}

	public void initTopicDoc(String server, BasicDBObject oDoc) {
		oDoc.append("server", server);
		oDoc.append("continent", oDoc.getString("contient"));
		if (oDoc.containsField("timestamp") && oDoc.containsField("last_timestamp")) {
			oDoc.append("last_modified", Math.max(oDoc.getLong("timestamp"), oDoc.getLong("last_timestamp")));
		} else if (oDoc.containsField("timestamp")) {
			oDoc.append("last_modified", oDoc.getLong("timestamp"));
		} else if (oDoc.containsField("last_timestamp")) {
			oDoc.append("last_modified", oDoc.getLong("last_timestamp"));
		}
		String desc = null;
		if (oDoc.containsField("html")) {
			desc = oDoc.getString("html");
			desc = TextUtil.extractText(desc);
			oDoc.removeField("html");
		} else {
			desc = oDoc.getString("content");
		}
		oDoc.append("content", desc);
	}

	public void initTripDoc(BasicDBObject oDoc, BizType bizType) {
		oDoc.append("bizType", bizType.ordinal());
		String shortId = oDoc.getString("short_id");
		oDoc.append("id", shortId);
		BasicDBList locations = DBObjectUtil.toList(oDoc.getString("locations"));
		locations = facets.normalizeLocations(shortId, locations);
		oDoc.append("locations", locations);
		BasicDBList countires = facets.normalizeCountries(oDoc.getString("country"), locations);
		oDoc.append("countries", countires);
		BasicDBList contients = facets.normalizeContients(countires);
		oDoc.append("contients", contients);

		BasicDBList priceTags = DBObjectUtil.toList(oDoc.getString("priceTags"));
		CodeType[] types = new CodeType[] { CodeType.priceTag };
		BasicDBList[] results = facets.normalize(types, priceTags);
		oDoc.append("priceTags", results[0]);

		BasicDBList tags = DBObjectUtil.toList(oDoc.getString("tags"));
		types = new CodeType[] { CodeType.serviceTag, CodeType.language };
		results = facets.normalize(types, tags);
		oDoc.append("tags", results[0]);
		results[0].addAll(facets.parse(CodeType.serviceTag, tags));
		oDoc.append("languages", results[1]);

		BasicDBList categories = DBObjectUtil.toList(oDoc.getString("categories"));
		String category = facets.categoryBy(bizType, oDoc.getInt("catalog"));
		categories.add(category);
		types = new CodeType[] { CodeType.category };
		oDoc.append("categories", facets.normalize(types, categories)[0]);
		BasicDBObject oCity = facets.valueBy(CodeType.location, oDoc.getString("city"));
		if (oCity != null) {
			oDoc.append("belongCity", oCity.getString("v"));
		}
		String v = oDoc.getString("guide");
		if (StringUtil.isEmpty(v)) {
			oDoc.append("guide", new BasicDBObject());
		} else {
			oDoc.append("guide", JSON.parse(v));
		}
		BasicDBObject oUser = this.readUserBy(oDoc.getLong("user_id"), "user_name", "sex", "height", "birthday");
		if (oUser != null) {
			oDoc.append("user", oUser);
		}
	}

	public BasicDBObject readUserBy(long userId, String... fieldNames) {
		StringBuilder sql = new StringBuilder();
		for (int i = 0; i < fieldNames.length; i++) {
			if (sql.length() > 0)
				sql.append(",");
			sql.append(fieldNames[i]);
		}
		BasicDBObject oResult = DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, "select " + sql.toString() + " from user where user_id=?",
				new String[] { String.valueOf(userId) }, 0, 0);
		return oResult;
	}

	public String[] decode(String name, String mobile, String qq, String msn) {
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, name),
				SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, mobile), SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, qq),
				SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, msn));
		return values;
	}

	public String[] encode(String name, String mobile, String qq, String msn) {
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, name),
				SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, mobile), SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, qq),
				SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, msn));
		return values;
	}

	public String[] decode(String name, String mobile, String qq, String msn, String email) {
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, name),
				SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, mobile), SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, qq),
				SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, msn), SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, email));
		return values;
	}

	public String[] encode(String name, String mobile, String qq, String msn, String email) {
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, name),
				SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, mobile), SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, qq),
				SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, msn), SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email));
		return values;
	}

	public String[] decode(String name, String email) {
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Undo, name),

		SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Undo, email));
		return values;
	}

	public String[] encode(String name, String email) {
		String[] values = ZeroConnect.doSecurity(SecurityObject.create(Global.SecurityType.Name, Global.SecurityMode.Do, name),
				SecurityObject.create(Global.SecurityType.Email, Global.SecurityMode.Do, email));
		return values;
	}

	public String add(Context context, long userId, String title, double amount, double balance) {
		String collName = "account_" + userId;
		BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, String.valueOf(userId));
		BasicDBObject oField = new BasicDBObject();
		oField.append("operator", context.user.getUserName());
		oField.append("title", title);
		oField.append("amount", amount);
		oField.append("balance", balance);
		oField.append("createDate", System.currentTimeMillis());
		DataCollection.insert(rec.getString("id"), Const.defaultMongoDB, collName, oField);
		return oField.get("_id").toString();
	}

	public String rootAdd(int userId, String title, double amount, double balance) {
		String collName = "account_" + userId;
		BasicDBObject rec = ServerDict.self.serverListener.hashBy(NodeType.mongo, String.valueOf(userId));
		BasicDBObject oField = new BasicDBObject();
		oField.append("operator", "system");
		oField.append("title", title);
		oField.append("amount", amount);
		oField.append("balance", balance);
		oField.append("createDate", System.currentTimeMillis());
		DataCollection.insert(rec.getString("id"), Const.defaultMongoDB, collName, oField);
		return oField.get("_id").toString();
	}

	@Override
	public DBObject handle(Context ctx, DBObject oReq) {
		return null;
	}

	public BasicDBObject defaultFilterObject() {
		return (BasicDBObject) BasicDBObjectBuilder.start().add("_ns", 0).get();
	}

	protected void initMongoDB() {
		DataCollection.createIndex(Const.defaultMongoServer, Const.defaultMongoDB, "conversations", "fromToIndex", new BasicDBObject().append("fromUserId", 1).append("toUserId", 1), true);
		BasicDBList servers = (BasicDBList) ServerDict.self.servers();
		for (int i = 0; servers != null && i < servers.size(); i++) {
			BasicDBObject rec = (BasicDBObject) servers.get(i);
			DataCollection.createIndex(rec.getString("name"), Const.defaultMongoDB, "messages", "keyIndex", new BasicDBObject().append("keyId", 1), false);
		}
		logger.info("build MongoDB indexes");
	}

	private void initIPAddress() {
		logger.info("init IPSeeker");
		IPSeeker.init();
	}

	private void initEntityDB() {
		logger.info("init EntityDB");
		Reader.init();
	}

	private void initContries() {
		logger.info("init countries");
		String sql = "select country,coordinate from country_dict where country!='all'";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		for (int i = 0; i < rows.length; i++) {
			String[] row = rows[i];
			BasicDBObject oResult = new BasicDBObject();
			oResult.append("country", row[0]);
			String[] values = row[1].split("\\,");
			if (values.length <= 1)
				continue;
			oResult.append("lat", Float.parseFloat(values[0]));
			oResult.append("lng", Float.parseFloat(values[1]));
			BasicDBObject oData = new BasicDBObject().append("country", row[0]);
			oResult.append("data", oData);
			countries.add(oResult);
		}
	}

	private long toShortId(DBObject rec, BizType type) {
		return DataSet.insert(Const.defaultMysqlServer, Const.defaultMysqlDB, "insert into short_mapping(mapping,bizType)values(?,?)", new String[] { rec.toString(), String.valueOf(type.ordinal()) });
	}

	public long shortTrip(String activityId, String mapId) {
		return toShortId(BasicDBObjectBuilder.start().add("id", activityId).add("mapid", mapId).get(), BizType.trip);
	}

	public long shortProduct(String activityId) {
		return toShortId(BasicDBObjectBuilder.start().add("id", activityId).get(), BizType.product);
	}

	public long shortCar(String activityId, String mapId) {
		return toShortId(BasicDBObjectBuilder.start().add("id", activityId).add("mapid", mapId).get(), BizType.car);
	}

	public long shortRoom(String activityId, String mapId) {
		return toShortId(BasicDBObjectBuilder.start().add("id", activityId).add("mapid", mapId).get(), BizType.room);
	}

	public long shortRequire(String id, int catalog) {
		return toShortId(BasicDBObjectBuilder.start().add("id", id).add("catalog", catalog).get(), BizType.require);
	}

	public long shortTopic(int termId, String id) {
		return toShortId(BasicDBObjectBuilder.start().add("teamid", termId).add("id", id).get(), BizType.topic);
	}

	public void initTrip() {
		String sql = "select activity_id,map_id from activity_trip where isnull(short_id)";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		while (rows != null && rows.length > 0) {
			for (int i = 0; i < rows.length; i++) {
				String[] values = rows[i];
				int index = 0;
				String id = values[index++];
				String mapId = values[index++];
				long shortId = shortTrip(id, mapId);
				System.out.println(shortId);
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_trip set short_id=? where activity_id=?", new String[] { String.valueOf(shortId), id });
			}
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 0);
		}
	}

	public void initCar() {
		String sql = "select activity_id,map_id from activity_Car where isnull(short_id)";
		String[][] rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
		while (rows != null && rows.length > 0) {
			for (int i = 0; i < rows.length; i++) {
				String[] values = rows[i];
				int index = 0;
				String id = values[index++];
				String mapId = values[index++];
				long shortId = shortCar(id, mapId);
				DataSet.update(Const.defaultMysqlServer, Const.defaultMysqlDB, "update activity_Car set short_id=? where activity_id=?", new String[] { String.valueOf(shortId), id });
			}
			rows = DataSet.query(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, 0, 20);
		}
	}

	public BasicDBList catalogs(String groupName) {
		String sql = "select id,name from catalog where parent=-1 a and group=? order by score";
		return DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { groupName }, 0, 0);
	}

	public BasicDBList catalogsBy(String groupName, String parentId) {
		String sql = "select id,name from catalog where parentId=? and group=? order by score";
		return DataSet.queryDBList(Const.defaultMysqlServer, Const.defaultMysqlDB, sql, new String[] { String.valueOf(parentId) }, 0, 0);
	}

	@Override
	public boolean handleMsg(ModuleMsgIntf event) {
		return false;
	}

	public static void main(String[] args) {
		int mode = 0;
		ModuleAppDict module = new ModuleAppDict();
		switch (mode) {
		/* reload */
		case 0:
			module.init(false);
			Facet facet = module.facets.facetBy(CodeType.priceRange);
			System.out.println(facet.toString());
			break;
		case 1:
			File solrFile = new File(Const.SolrHome + "solr.xml");
			CoreContainer cores = CoreContainer.createAndLoad(Const.SolrHome, solrFile);
			System.out.println(cores.toString());
			break;
		case 10:
			break;
		}
		System.out.println("done");
	}
}
