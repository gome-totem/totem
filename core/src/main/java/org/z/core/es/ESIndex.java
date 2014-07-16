package org.z.core.es;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.bson.BSONObject;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.module.ModuleAppDict;
import org.z.data.analysis.LevelAttribute;
import org.z.data.analysis.SmartNgramFilter;
import org.z.data.analysis.SmartRepeatFilter;
import org.z.data.analysis.SmartTokenizer;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;
import org.z.data.analysis.SmartWordFilter;
import org.z.global.dict.Global;
import org.z.global.environment.Const;
import org.z.global.interfaces.IndexServiceIntf;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.util.EmptyUtil;
import org.z.global.util.StringUtil;
import org.z.store.leveldb.LevelDB;

import redis.clients.jedis.ShardedJedis;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public abstract class ESIndex implements IndexServiceIntf {

	protected static Logger logger = LoggerFactory.getLogger(ESIndex.class);
	public LevelDB dynamicDB;
	private ESEmbededServer server;
	protected Client client;
	public String indexType = "productType";
	public String indexName = "product";
	protected long time = System.currentTimeMillis();
	private BulkRequestBuilder bulkRequest = null;
	private List<String> clearIds = new ArrayList<String>();
//	public static final String redisIp = Global.esRedis.trim();

	@Override
	public void afterCreate(Object[] params) {
		if (params[0] instanceof String) {

			this.indexName = String.valueOf(params[0]);
			this.indexType = String.valueOf(params[1]);

			if (StringUtils.isEmpty(Global.indexName_reIndex)) {

				this.indexName = Global.indexName_reIndex;
			}
		}
		logger.info("es indexName {} ", indexName);
	}

	@Override
	public boolean init(boolean isReload) {
		// Const.ESHome="/server/es1";
		dynamicDB = new LevelDB(Const.ESHome, Global.indexName + "/dynamic");
		if (dynamicDB.init() == false) {
			logger.error("init Index dynamicDB[{}] Fail.", new String[] { Const.ESHome + indexName + "/dynamic" });
			return false;
		}


			server = new ESEmbededServer(Const.ESHome + indexName, Global.ServerName);
			server.start();
			logger.info("es embeded server started!!");
		client = ESClientUtils.getTransportClient();
		// if(client==null||!createIndex()||!createMapping()){
		if (client == null) {
			logger.error("init es client or create indexName [{}] type [{}]  or createMapping Fail reason index already existed.", new String[] { indexName, indexType });
			return false;
		}
		return true;
	}

	@Override
	public void start(boolean isReload) {

		long count = client.prepareCount().setQuery(QueryBuilders.matchAllQuery()).execute().actionGet().getCount();
		logger.info("IndexName[{}] type [{}] has  Document Count=[{}]", new Object[] { indexName, indexType, count });

	}

	@Override
	public void clearDynamicDB(BasicDBObject oReq) {
//			ESDict.clearDynamicDB(oReq, jedis);
	}

	@Override
	public int add(DBObject oDocs) {
		
		ShardedJedis jedis = null;
		int count = 0;
		try {
			List<BasicDBObject> docData = Lists.newArrayList();
			List<String> clearData = Lists.newArrayList();
			if (oDocs instanceof BasicDBObject) {
				// logger.info("add one doc----------");
				count = 1;
				BasicDBObject result = this.getDocData((BasicDBObject) oDocs, jedis);
				if (result == null)
					return count;
				List<BasicDBObject> curData = (List<BasicDBObject>) result.get("docs");

				if (curData == null || curData.size() == 0)
					return count;

				List<String> curClearData = (List<String>) result.get("clearData");
				if (curClearData != null && curClearData.size() > 0)
					clearData.addAll(curClearData);
				docData.addAll(curData);

			} else if (oDocs instanceof BasicDBList) {

				BasicDBList oDocList = (BasicDBList) oDocs;
				// logger.info("add more doc----------num:"+oDocList.size());
				for (int i = 0; i < oDocList.size(); i++) {
					BasicDBObject oDoc = (BasicDBObject) oDocList.get(i);
					BasicDBObject result = this.getDocData((BasicDBObject) oDoc, jedis);
					if (result == null)
						continue;
					List<BasicDBObject> curData = (List<BasicDBObject>) result.get("docs");
					if (curData == null || curData.size() == 0)
						continue;
					List<String> curClearData = (List<String>) result.get("clearData");
					if (curClearData != null && curClearData.size() > 0)
						clearData.addAll(curClearData);

					docData.addAll(curData);
				}

				count = oDocList.size();
				oDocList.clear();

			}
			BasicDBObject buildDoc = this.buildBulkIndexParam(docData);
			docData.clear();
			if (buildDoc == null)
				return count;

			synchronized (clearIds) {

				if (bulkRequest == null) {
					bulkRequest = client.prepareBulk();
					clearIds.clear();
				}
				long end = System.currentTimeMillis();
				for (IndexRequestBuilder indexBuilder : (List<IndexRequestBuilder>) buildDoc.get("docs")) {
					bulkRequest.add(indexBuilder);
				}
				if (bulkRequest.numberOfActions() >= 1 || (end - time >= 60000 && bulkRequest.numberOfActions() > 0)) {
					// logger.info("bluk refresh!!!=================================111");
					bulkRequest.execute();
					clearIds.addAll((List<String>) buildDoc.get("clearCache"));
					clearIds.addAll(clearData);
					ModuleAppDict.self.dispatchClearCache("indexCache", new BasicDBObject("clearIds", clearIds).append("fromIp", Global.localIP));
					bulkRequest = null;
					time = end;
				}
			}
			docData = null;
		} catch (Exception e) {
			logger.error("add Bulk fail", e);
		} finally {
//			RedisPool.getESRedisPool().ret(jedis);
		}
		return count;
	}

	public ArrayList<BasicDBObject> parseTokens(String content, TokenMode mode, boolean enableGram) {
		ArrayList<BasicDBObject> results = new ArrayList<BasicDBObject>();
		if (StringUtil.isEmpty(content)) {
			return results;
		}
		StringReader str = new StringReader(content);
		SmartTokenizer src = new SmartTokenizer(str);
		src.init();
		TokenStream ngr = src;
		if (enableGram == true) {
			ngr = new SmartWordFilter(ngr, 2, 2, TokenMode.search);
		}
		ngr = new SmartRepeatFilter(ngr, mode);
		try {
			while (ngr.incrementToken()) {
				BasicDBObject oToken = new BasicDBObject();
				oToken.append("text", ngr.getAttribute(CharTermAttribute.class).toString());
				oToken.append("level", ngr.getAttribute(LevelAttribute.class).getLevel());
				oToken.append("pos", ngr.getAttribute(OffsetAttribute.class).startOffset());
				results.add(oToken);
			}
		} catch (IOException e) {
		}
		return results;
	}

	public ArrayList<BasicDBObject> defparseToken(String content, TokenMode mode, boolean enableGram) {
		ArrayList<BasicDBObject> results = new ArrayList<BasicDBObject>();
		if (StringUtil.isEmpty(content)) {
			return results;
		}
		StringReader str = new StringReader(content);
		SmartTokenizer src = new SmartTokenizer(str);
		src.init();
		TokenStream ngr = src;
		if (enableGram == true) {
			ngr = new SmartNgramFilter(ngr, 2, 2, TokenMode.search);
		}
		ngr = new SmartRepeatFilter(ngr, mode);
		try {
			while (ngr.incrementToken()) {
				BasicDBObject oToken = new BasicDBObject();
				oToken.append("text", ngr.getAttribute(CharTermAttribute.class).toString());
				oToken.append("level", ngr.getAttribute(LevelAttribute.class).getLevel());
				oToken.append("pos", ngr.getAttribute(OffsetAttribute.class).startOffset());
				results.add(oToken);
			}
		} catch (IOException e) {
		}
		return results;
	}

	public ArrayList<String> defparseToken(String content, boolean enableGram) {
		ArrayList<BasicDBObject> tokens = defparseToken(content, TokenMode.search, enableGram);
		StringBuilder keys = new StringBuilder();
		StringBuilder words = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			BasicDBObject oToken = tokens.get(i);
			String text = oToken.getString("text");
			switch (oToken.getInt("level")) {
			case 1:
			case 2:
				keys.append(text);
				keys.append(" ");
				break;
			case 3:
				words.append(text);
				words.append(" ");
				break;
			}
		}
		ArrayList<String> results = new ArrayList<String>();
		results.add(keys.toString());
		if (words.length() > 0) {
			results.add(words.toString());
		}
		return results;
	}

	public ArrayList<String> parseTokens(String content, boolean enableGram) {
		ArrayList<BasicDBObject> tokens = parseTokens(content, TokenMode.search, enableGram);
		StringBuilder keys = new StringBuilder();
		StringBuilder words = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			BasicDBObject oToken = tokens.get(i);
			String text = oToken.getString("text");
			switch (oToken.getInt("level")) {
			case 1:
			case 2:
				keys.append(text);
				keys.append(" ");
				break;
			case 3:
				words.append(text);
				words.append(" ");
				break;
			}
		}
		ArrayList<String> results = new ArrayList<String>();
		results.add(keys.toString());
		if (words.length() > 0) {
			results.add(words.toString());
		}
		return results;
	}

	public boolean updateFiledById(String id, String field, Object value) {

		try {
			StringBuffer script = new StringBuffer("ctx._source.").append(field).append("=");

			if (value instanceof String) {
				script.append("'").append(value).append("'");
			} else {
				script.append(value);
			}
			client.prepareUpdate(indexName, indexType, id).setScript(script.toString()).execute();

			// logger.info("update  indexName [{}] type[{}] id [{}] script [{}]",
			// new Object[] { indexName, indexType,id,script.toString()});
		} catch (DocumentMissingException mis) {
			logger.error("update  indexName [{}] type[{}] id [{}] for field [{}] fail,no such identifier.", new Object[] { indexName, indexType, id, field });
			return false;
		} catch (Exception e) {
			logger.error("update filed fail", e);
			return false;
		}

		return true;
	}

	public boolean updateFiledsById(String id, BasicDBObject values) {

		if (StringUtils.isEmpty(id) || values == null || values.size() == 0)
			return false;
		try {

			StringBuffer script = new StringBuffer();

			for (Entry<String, Object> entry : values.entrySet()) {
				Object value = entry.getValue();
				script.append("ctx._source.").append(entry.getKey()).append("=");
				if (value instanceof String) {
					script.append("'").append(value).append("'");
				} else {
					script.append(value);
				}
				script.append(";");
			}

			client.prepareUpdate(indexName, indexType, id).setScript(script.toString()).execute();
			// logger.info("update  indexName [{}] type[{}] id [{}] script [{}]",
			// new Object[] { indexName, indexType,id,script.toString()});
		} catch (DocumentMissingException mis) {
			logger.error("update  indexName [{}] type[{}] id [{}] for series fields [{}] fail,no such identifier.", new Object[] { indexName, indexType, id, values.toString() });
			return false;
		} catch (Exception e) {
			logger.error("update filed fail", e);
			return false;
		}

		return true;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	@Override
	public void commit() {
		client.admin().indices().prepareFlush(indexName).setFull(false).execute();
	}

	@Override
	public void commitDynamicDB() {

		this.dynamicDB.save();
	}

	@Override
	public BasicDBObject remove(DBObject oRequire) {
		BasicDBObject resp = new BasicDBObject();
		String fq = null;
		try {
			boolean flag = false;
			// SolrQuery query = new SolrQuery();
			QueryBuilder queryBuilder = new QueryBuilder();
			BasicDBObject dbObject = (BasicDBObject) oRequire;
			resp.putAll((BSONObject) dbObject);
			String catId = dbObject.getString("catId");
			BasicDBObject category = CategoryDict.getCategory(catId);
			if (EmptyUtil.notEmpty(category)) {
				flag = true;
				fq = null;
				fq = category.getString("catalog") + ":" + catId;
				logger.info("remove index,fq=" + fq);
				queryBuilder.addTermFilter(category.getString("catalog"), catId);
			}
			int skuState = dbObject.getInt("skuState", 0);
			if (skuState != 4) {
				flag = true;
				fq = null;
				fq = "skuState:" + skuState;
				logger.info("remove index,fq=" + fq);
				queryBuilder.addTermFilter("skuState", skuState);
			}
			int productState = dbObject.getInt("productState", 0);
			if (productState != 4) {
				flag = true;
				fq = null;
				fq = "productState:" + productState;
				logger.info("remove index,fq=" + fq);
				queryBuilder.addTermFilter("productState", productState);
			}
			String skuId = dbObject.getString("skuId");
			if (EmptyUtil.notEmpty(skuId)) {
				flag = true;
				fq = null;
				fq = "skuId:" + skuId;
				logger.info("remove index,fq=" + fq);
				// query.addFilterQuery(fq);
				queryBuilder.addTermFilter("skuId", skuId);
			}
			String productId = dbObject.getString("productId");
			if (EmptyUtil.notEmpty(productId)) {
				flag = true;
				fq = null;
				fq = "productId:" + productId;
				logger.info("remove index,fq=" + fq);
				// query.addFilterQuery(fq);
				queryBuilder.addTermFilter("productId", productId);
			}
			List<String> ids = new ArrayList<String>();
			if (flag) {

				SearchResponse resonse = queryBuilder.get();
				SearchHit[] hits = resonse.getHits().getHits();
				for (SearchHit hit : hits) {
					ids.add(hit.getId());
				}
				client.prepareDeleteByQuery(indexName).setTypes(indexType).setQuery(QueryBuilders.idsQuery(indexType).addIds(ids.toArray(new String[ids.size()])));
				commit();
			}
			resp.append("ids", ids);
		} catch (Exception e) {
			logger.error("emove index,fq=" + fq, e);
			resp.append("e", e);
		}
		return resp;
	}

	public Object getDynamicDB() {
		return this.dynamicDB;
	}

	@Override
	public void stop() {

		if (client != null)
			client.close();
		server.close();
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public String getId() {
		return indexName;
	}

	@Override
	public void attachMQ(ModuleIntf module) {

	}

	protected abstract boolean createMapping();

	protected abstract boolean createIndex();

	protected abstract BasicDBObject getDocData(BasicDBObject o, ShardedJedis jedis);

	protected abstract BasicDBObject buildBulkIndexParam(List<BasicDBObject> docs);

	protected abstract void addDoc(BasicDBObject docData, ShardedJedis jedis);

}
