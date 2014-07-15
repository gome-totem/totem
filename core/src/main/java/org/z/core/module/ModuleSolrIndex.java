package org.z.core.module;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleZeromq;
import org.z.core.interfaces.IndexServiceIntf;
import org.z.data.analysis.LevelAttribute;
import org.z.data.analysis.SmartRepeatFilter;
import org.z.data.analysis.SmartTokenizer;
import org.z.data.analysis.SmartTokenizerFactory.TokenMode;
import org.z.data.analysis.SmartWordFilter;
import org.z.global.dict.Global.SearchMode;
import org.z.global.environment.Const;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.ModuleMsgIntf;
import org.z.global.util.StringUtil;
import org.z.global.util.TextUtil;
import org.z.store.leveldb.LevelDB;
import org.z.store.mongdb.DataSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public abstract class ModuleSolrIndex implements IndexServiceIntf {
	protected static Logger logger = LoggerFactory.getLogger(ModuleSolrIndex.class);
	public String indexName = null;
	public LevelDB dynamicDB = null;
	public EmbeddedSolrServer solrServer = null;
	public SolrCore solrCore = null;
	public ModuleZeromq mq = null;
	protected String SolrUniqueField = null;
	protected HashSet<String> dynamicFields = new HashSet<String>();

	public void addDynamic(String docId, BasicDBObject oDoc) {
		BasicDBObject oDyn = new BasicDBObject();
		for (Iterator<Entry<String, Object>> i = oDoc.entrySet().iterator(); i.hasNext();) {
			Entry<String, Object> entry = i.next();
			String fieldName = entry.getKey();
			if (this.dynamicFields.contains(fieldName) == false)
				continue;
			Object fieldValue = entry.getValue();
			if (fieldName.equalsIgnoreCase("description")) {
				fieldValue = TextUtil.extractText(fieldValue.toString());
			}
			oDyn.append(fieldName, fieldValue);
		}
		this.dynamicDB.put(docId, oDyn.toString());
		this.dynamicDB.save();
	}

	@Override
	public void attachMQ(ModuleIntf mq) {
		this.mq = (ModuleZeromq) mq;
	}

	@Override
	public void afterCreate(Object[] params) {
		if (params[0] instanceof String) {
			this.indexName = String.valueOf(params[0]);
		}
	}

	public void deleteLockFile(String indexName) {
		File lck = new File(Const.SolrHome + indexName + "/data/index/write.lock");
		try {
			if (lck.exists())
				FileUtils.forceDelete(lck);
		} catch (Exception e) {
		}
	}

	@Override
	public boolean init(boolean isReload) {
		this.deleteLockFile(this.indexName);
		dynamicDB = new LevelDB(Const.SolrHome, indexName + "/dynamic");
		if (dynamicDB.init() == false) {
			logger.error("init Index dynamicDB[{}] Fail.", new String[] { Const.SolrHome + indexName + "/dynamic" });
			return false;
		}
		return true;
	}

	public int toInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return 0;
		}
	}

	public long readCount(HashMap<String, Long> map, String key) {
		if (map.containsKey(key) == false) {
			return 0;
		}
		return map.get(key);
	}

	public BasicDBObject readUser(long userId, String... fieldNames) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < fieldNames.length; i++) {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append(fieldNames[i]);
		}
		buffer.insert(0, "select ");
		buffer.append(" from user where user_id=?");
		return DataSet.queryDBObject(Const.defaultMysqlServer, Const.defaultMysqlDB, buffer.toString(), new String[] { String.valueOf(userId) }, 0, 0);
	}

	public void addFacetFields(BasicDBObject oDoc, String fieldName, SolrInputDocument solrDoc, String solrFieldName) {
		Object o = oDoc.get(fieldName);
		if (o == null || (o instanceof BasicDBList) == false) {
			logger.error("addFacetField fail,check [" + fieldName + "]");
			return;
		}
		BasicDBList oList = (BasicDBList) o;
		for (int i = 0; i < oList.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) oList.get(i);
			if (oItem.containsField("id") == false) {
				continue;
			}
			solrDoc.addField(solrFieldName, oItem.getString("id"));
		}
	}

	public void addListToDoc(BasicDBObject oDoc, String fieldName, SolrInputDocument solrDoc, String solrFieldName) {
		Object oList = oDoc.get(fieldName);
		if (oList instanceof BasicDBList) {
			BasicDBList list = (BasicDBList) oList;
			for (int i = 0; i < list.size(); i++) {
				solrDoc.addField(solrFieldName, list.get(i));
			}
		} else if (oList instanceof String[]) {
			String[] list = (String[]) oList;
			for (int i = 0; i < list.length; i++) {
				solrDoc.addField(solrFieldName, list[i]);
			}
		}
	}

	public String parseSqlByArray(BasicDBObject oReq, String fieldName, String logical) {
		if (oReq.containsField(fieldName) == false) {
			return null;
		}
		BasicDBList values = (BasicDBList) oReq.get(fieldName);
		String sql = StringUtil.join(values, logical);
		if (StringUtil.isEmpty(sql))
			return null;
		return sql.toString();
	}

	public void addDBList(BasicDBObject oReq, String fieldName, String fieldValue) {
		if (oReq.containsField(fieldName) == false) {
			return;
		}
		BasicDBList items = (BasicDBList) oReq.get(fieldName);
		items.add(fieldValue);
	}

	public ArrayList<BasicDBObject> parseTokens(String content, TokenMode mode, boolean enableGram) {
		ArrayList<BasicDBObject> results = new ArrayList<BasicDBObject>();
		if (StringUtil.isEmpty(content)) {
			return results;
		}
		SmartTokenizer src = new SmartTokenizer(new StringReader(content));
		src.init();
		TokenStream ngr = src;
		if (enableGram == true) {
			ngr = new SmartWordFilter(ngr, 3, 5,TokenMode.search);
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

	public ArrayList<String> parseTokens(String content, boolean enableGram, int minTextLength) {
		ArrayList<BasicDBObject> tokens = parseTokens(content, TokenMode.search, enableGram);
		StringBuilder keys = new StringBuilder();
		StringBuilder words = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			BasicDBObject oToken = tokens.get(i);
			String text = oToken.getString("text");
			if (minTextLength != 0 && text.length() < minTextLength) {
				continue;
			}
			switch (oToken.getInt("level")) {
			case 1:
			case 2:
				if (keys.length() > 0) {
					keys.append(" ");
				}
				keys.append(text);
				break;
			case 3:
				if (words.length() > 0) {
					words.append(" ");
				}
				words.append(text);
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

	@Override
	public void start(boolean isReload) {
		try {
			solrServer = new EmbeddedSolrServer(ModuleAppDict.self.cores, indexName);
			solrCore = solrServer.getCoreContainer().getCore(indexName);
			long count = solrServer.query(new SolrQuery("*:*")).getResults().getNumFound();
			logger.info("IndexName[{}] has  Document Count=[{}]", new Object[] { indexName, count });
		} catch (Exception e) {
			logger.error("start[" + indexName + "]", e);
		}
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
		return indexName;
	}

	@Override
	public void remove(DBObject oRequire) {
		try {
			BasicDBList docs = null;
			if (oRequire instanceof BasicDBObject) {
				docs = new BasicDBList();
				docs.add(oRequire);
			} else {
				docs = (BasicDBList) oRequire;
			}
			for (int i = 0; docs != null && i < docs.size(); i++) {
				BasicDBObject oItem = (BasicDBObject) docs.get(i);
				String id = null;
				if (oItem.containsField("shortId")) {
					id = oItem.getString("shortId");
				} else if (oItem.containsField("id")) {
					id = oItem.getString("id");
				}
				if (StringUtil.isEmpty(id))
					continue;
				solrServer.deleteById(id);
				logger.info("Index[{}]Remove Doc[{}]", new String[] { this.indexName, id });
			}
		} catch (Exception e) {
			logger.error("SolrIndex Remvoe", e);
		}
	}

	@Override
	public void commit() {
		try {
			solrServer.commit(false, false);
			this.dynamicDB.save();
		} catch (Exception e) {
		}
	}

	protected abstract void addDoc(BasicDBObject o);

	@Override
	public int add(DBObject o) {
		int count = 0;
		if (o instanceof BasicDBObject) {
			this.addDoc((BasicDBObject) o);
			count = 1;
		} else if (o instanceof BasicDBList) {
			BasicDBList oDocs = (BasicDBList) o;
			for (int i = 0; i < oDocs.size(); i++) {
				BasicDBObject oDoc = (BasicDBObject) oDocs.get(i);
				this.addDoc(oDoc);
			}
			count = oDocs.size();
			oDocs.clear();
		}
		return count;
	}
	protected abstract void updateFields(BasicDBObject o);
	
	public void updateField(DBObject o){
		if (o instanceof BasicDBObject) {
			this.updateFields((BasicDBObject) o);
		} else if (o instanceof BasicDBList) {
			BasicDBList oDocs = (BasicDBList) o;
			for (int i = 0; i < oDocs.size(); i++) {
				BasicDBObject oDoc = (BasicDBObject) oDocs.get(i);
				this.updateFields(oDoc);
			}
			oDocs.clear();
		}
	}

	@Override
	public abstract boolean handleMsg(ModuleMsgIntf msg);

	@Override
	public abstract BasicDBObject search(DBObject oRequire, BasicDBObject oUser, SearchMode searchMode);

	protected SolrInputField fieldBy(String action, String name, Object value, float boost) {
		Hashtable<String, Object> oValue = new Hashtable<String, Object>();
		oValue.put(action, value);
		SolrInputField input = new SolrInputField(name);
		input.setValue(oValue, boost);
		return input;
	}

	public SolrInputField fieldBySet(String name, Object value, float boost) {
		return this.fieldBy("set", name, value, boost);
	}

	public SolrInputField fieldBySet(String name, Object value) {
		return this.fieldBy("set", name, value, 1);
	}

	public SolrInputField fieldByAdd(String name, Object value, float boost) {
		return this.fieldBy("add", name, value, boost);
	}

	public SolrInputField fieldByAdd(String name, Object value) {
		return this.fieldBy("add", name, value, 1);
	}

	public long termCountBy(String name, String value) {
		SolrQuery query = new SolrQuery();
		query.setQuery(name + ":(" + value + ")");
		query.setStart(0);
		query.setRows(1);
		try {
			QueryResponse rsp = solrServer.query(query);
			SolrDocumentList docs = rsp.getResults();
			return docs.getNumFound();
		} catch (Exception e) {
			logger.error("termCountBy", e);
			return 0;
		}
	}

	protected QueryResponse search(boolean debugMode, SolrQuery query, String sql, int pageNumber, int pageSize, BasicDBObject oResult) {
		query.setQuery(sql);
		query.setStart(pageNumber * pageSize);
		query.setRows(pageSize);
		SolrDocumentList docs = null;
		try {
			QueryResponse rsp = solrServer.query(query);
			docs = rsp.getResults();
			oResult.append("totalCount", docs.getNumFound());
			oResult.append("remianCount", docs.getNumFound()-(pageNumber+1 * pageSize));
			oResult.append("searchTime", rsp.getQTime());
			BasicDBList items = new BasicDBList();
			oResult.append("items", items);
			if (docs == null || docs.size() == 0) {
				return rsp;
			}
			for (SolrDocument doc : docs) {
				BasicDBObject oItem = new BasicDBObject();
				Collection<String> fieldNames = doc.getFieldNames();
				for (String fieldName : fieldNames) {
					oItem.append(fieldName, doc.get(fieldName));
				}
				if (debugMode == true) {
					String keyId = doc.getFieldValue(SolrUniqueField).toString();
					oItem.append("explain", rsp.getExplainMap().get(keyId));
				}
				items.add(oItem);
			}
			return rsp;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

}
