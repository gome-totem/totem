package org.z.core.module;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.CoreContainer;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleZeromq;
import org.z.global.dict.MessageScope;
import org.z.global.dict.MessageType;
import org.z.global.dict.MessageVersion;
import org.z.global.environment.Config;
import org.z.global.environment.Const;
import org.z.global.factory.DictFactory;
import org.z.global.interfaces.ModuleAppDictIntf;
import org.z.global.util.EmptyUtil;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServiceName;
import org.z.store.mongdb.DataCollection;
import org.z.store.redis.RedisFactory;
import org.z.store.redis.RedisSocket;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

public class ModuleAppDict implements ModuleAppDictIntf {
	protected static Logger logger = LoggerFactory.getLogger(ModuleAppDict.class);
	public String dictIP = null;
	protected ModuleZeromq mq = null;
	public CoreContainer coreContainer = null;
	public int ProcessThreadCount = 0;
	public String scheduleIP = null;
	public static ModuleAppDict self = null;
	public Set<Object> set = new HashSet<Object>();
	private HashMap<String, HashMap<String, Integer>> facetWeights = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, BasicDBList> categoryOrder = new HashMap<String, BasicDBList>();
	private HashMap<String, String[]> pinyins = new HashMap<String, String[]>();
	private Map<String, Float> promoScores = new ConcurrentHashMap<String, Float>();
	private Map<String, String> recommendMap = new ConcurrentHashMap<String, String>();
	private Map<String, Map<String, Integer>> recommendIndexs = new ConcurrentHashMap<String, Map<String, Integer>>();
	private List<String> bigImgCatetory = new ArrayList<String>();

	public Object coreContainer() {
		return this.coreContainer;
	}

	public Object mq() {
		return this.mq;
	}

	public void mq(Object value) {
		this.mq = (ModuleZeromq) value;
	}

	@Override
	public boolean init(boolean isReload) {
		self = this;
		ProcessThreadCount = Config.rock().getIntItem("AppProcessThreadCount", "1");
		scheduleIP = Config.rock().getItem("scheduleIP", "10.58.50.24");
		BasicDBObject oItem = null;
		BasicDBList records = DataCollection.findAll(Const.defaultDictMongo, Const.defaultDictMongoDB, "pinyin", null);
		for (int i = 0; i < records.size(); i++) {
			oItem = (BasicDBObject) records.get(i);
			pinyins.put(oItem.getString("chinese"), new String[] { oItem.getString("short"), oItem.getString("long") });
		}
		for (Object info : set) {
			DictFactory.registerModule(info);
		}
		return true;
	}


	public void dispatchClearCache(String cacheName, BasicDBObject oReq) {
		mq.dispatch(ServiceName.ProductIndex, MessageScope.ALLROUTER, MessageType.CLEARCACHE, cacheName, MessageVersion.MQ, System.currentTimeMillis(), oReq.toString());
	}


	public HashMap<String, Integer> readFacetWeight(BasicDBObject oItem) {
		HashMap<String, Integer> table = new HashMap<String, Integer>();
		int index = 100;
		BasicDBList items = (BasicDBList) oItem.get("values");
		for (int i = 0; i < items.size(); i++) {
			table.put(items.get(i).toString(), index--);
		}
		return table;
	}


	public void putFacetWeight(BasicDBObject oItem) {
		this.facetWeights.put(oItem.getString("id") + "_" + oItem.getString("catId", ""), readFacetWeight(oItem));
	}

	public HashMap<String, Integer> getFacetWeight(String facetInfoId, String catId) {
		return this.facetWeights.get(facetInfoId + "_" + catId);
	}

	public void putCategoryOrder(BasicDBObject oReq) {
		this.categoryOrder.put(oReq.getString("catId"), (BasicDBList) oReq.get("categories"));
	}

	public BasicDBList getCategoryOrder(String catId) {
		return categoryOrder.get(catId);
	}

	public void putPinyin(BasicDBObject oReq) {
		pinyins.put(oReq.getString("chinese").trim(), new String[] { oReq.getString("short"), oReq.getString("long") });
	}

	public String[] getPinyin(String chinese) {
		return pinyins.get(chinese);
	}

	public Float getPromoScore(String skuId) {
		return promoScores.get(skuId);
	}

	public void putPromoScore(String skuId, Float promoScore) {
		promoScores.put(skuId, promoScore);
	}

	public String getRecommend(String catId) {
		return recommendMap.get(catId);
	}

	public Map<String, Integer> getRecommendIndex(String catId) {
		return recommendIndexs.get(catId);
	}

	public void putRecommend(BasicDBObject oReq) {
		String catId = oReq.getString("catId");
		if (oReq.getInt("flag", 0) == 0) {
			recommendMap.remove(catId);
			recommendIndexs.remove(catId);
			return;
		}
		BasicDBList skus = (BasicDBList) oReq.get("skus");
		if (EmptyUtil.notEmpty(skus)) {
			StringBuilder skuIds = new StringBuilder();
			for (Object object : skus) {
				if (skuIds.length() > 0) {
					skuIds.append(" ");
				}
				Map<String, Integer> map = recommendIndexs.get(catId);
				if (map == null) {
					map = new ConcurrentHashMap<String, Integer>();
					recommendIndexs.put(catId, map);
				}
				BasicDBObject dbObject = (BasicDBObject) object;
				String skuId = dbObject.getString("skuId");
				map.put(skuId, 11 - Integer.parseInt(dbObject.getString("pos")));
				skuIds.append(skuId);
			}
			recommendMap.put(catId, skuIds.toString());
		}
	}

	public boolean containRecommend(String catId) {
		return recommendMap.containsKey(catId);
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

	public Set<String> keys(String pattern) {
		RedisSocket redis = null;
		Set<String> keys = null;
		try {
			redis = RedisFactory.get(dictIP);
			keys = redis.keys(pattern);
		} catch (Exception e) {
			redis.isAlive = false;
			logger.error("keys[{}],exception[{}]", new Object[] { dictIP, e.getLocalizedMessage() });
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
		return keys;
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

	public void delete(String prefix, String key) {
		RedisSocket redis = null;
		try {
			redis = RedisFactory.get(dictIP);
			redis.delete(StringUtil.append(prefix, "@", key));
		} catch (Exception e) {
			redis.isAlive = false;
			logger.error("write[{}],exception[{}]", new Object[] { dictIP, e.getLocalizedMessage() });
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
	}

	public void write(String prefix, String key, String content) {
		RedisSocket redis = null;
		try {
			redis = RedisFactory.get(dictIP);
			content = redis.set(StringUtil.append(prefix, "@", key), content);
		} catch (Exception e) {
			redis.isAlive = false;
			logger.error("write[{}],exception[{}]", new Object[] { dictIP, e.getLocalizedMessage() });
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
	}

	public Boolean exists(String key) {
		RedisSocket redis = null;
		try {
			redis = RedisFactory.get(dictIP);
			return redis.exists(key);
		} catch (Exception e) {
			logger.error("exists[{}],exception[{}]", new Object[] { dictIP, e.getLocalizedMessage() });
			redis.isAlive = false;
			return false;
		} finally {
			RedisFactory.ret(dictIP, redis);
		}
	}

	public void expire(String key, int seconds) {
		RedisSocket redis = null;
		try {
			redis = RedisFactory.get(dictIP);
			redis.expire(key, seconds);
		} catch (Exception e) {
			logger.error("expire[{}],[{}],exception[{}]", new Object[] { dictIP, seconds, e.getLocalizedMessage() });
			redis.isAlive = false;
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

	@Override
	public void afterCreate(Object[] params) {
		for (int i = 0; i < params.length; i++) {
			Object dict = params[i];
			set.add(dict);
		}
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


	public List<String> getBigImgCatetory() {
		return bigImgCatetory;
	}

	public void setBigImgCatetory(List<String> bigImgCatetory) {
		this.bigImgCatetory = bigImgCatetory;
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
	public void creatFile(Object obj) {
		BasicDBList list = (BasicDBList) obj;
		for (Object ob : list) {
			BasicDBObject file = (BasicDBObject) ob;
			StringUtil.deleteFile(file.getString("filename"));
			StringUtil.writeToFile(file.getString("content"), file.getString("filename"), "utf-8");
		}
	}


	public static void buildBtree() {
		long time = System.currentTimeMillis();
		try {
			// BTreeInterface b = BTree.openExistingTree(CompareMode.textbyte,
			// "/server/btree", 512, 512);
			for (long i = 0; i < 1000000000; i++) {
				// b.put(String.valueOf(i), String.valueOf(i));
				if (i % 10000 == 0) {
					System.out.println(i);
				}
			}
		} catch (Exception e) {
		}
		System.out.println("used=" + (System.currentTimeMillis() - time));
	}

	public static void main(String[] args) throws UnknownHostException, MongoException {
		int mode = 1;
		ModuleAppDict module = new ModuleAppDict();
		switch (mode) {
		case 1:
			BasicDBList obl = new BasicDBList();
			BasicDBObject oReq1 = new BasicDBObject().append("filename", "/server/test/test").append("content", "test");
			obl.add(oReq1);
			module.creatFile(obl);
			break;
		}
		System.out.println("done");

	}

}
