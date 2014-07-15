package org.z.core.common;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.z.global.util.StringUtil;
import org.z.store.redis.RedisPool;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class Facet {
	private FacetInfo info = null;
	private ConcurrentHashMap<String, BasicDBObject> byValue = new ConcurrentHashMap<String, BasicDBObject>();
	private ConcurrentHashMap<String, BasicDBObject> byId = new ConcurrentHashMap<String, BasicDBObject>();
	private ConcurrentHashMap<String, LinkedHashMap<String, BasicDBObject>> byChild = new ConcurrentHashMap<String, LinkedHashMap<String, BasicDBObject>>();
	public BasicDBList records = new BasicDBList();

	public Facet(FacetInfo info) {
		this.info = info;
	}

	public FacetInfo info() {
		return info;
	}

	public boolean exists(String value) {
		return byValue.containsKey(value);
	}

	public BasicDBObject valueBy(String value) {
		return this.byValue.get(value);
	}

	public BasicDBObject idBy(String id) {
		return this.byId.get(id);
	}

	public BasicDBObject addValue(String value, int order) {
		BasicDBObject oItem = byValue.get(value);
		if (oItem != null) {
			oItem.append("o", order);
			return oItem;
		}
		String id = ShortId.create(info, value);
		oItem = new BasicDBObject();
		oItem.append("t", this.info.id);
		oItem.append("i", id);
		oItem.append("v", value);
		oItem.append("o", order);
		byId.put(id, oItem);
		byValue.put(value, oItem);
		records.add(oItem);
		return oItem;
	}

	public void write() {
		Collections.sort(records, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				BasicDBObject item1 = (BasicDBObject) o1;
				BasicDBObject item2 = (BasicDBObject) o2;
				int result = item2.getInt("o") - item1.getInt("o");
				if (result != 0)
					return result;
				result = item1.getString("v").compareToIgnoreCase(item2.getString("v"));
				return 0;
			}
		});
		RedisPool.use("dict").set(info.name, records.toString());
	}

	public boolean read() {
		records.clear();
		String content = RedisPool.use("dict").get(info.name);
		if (StringUtil.isEmpty(content) == true)
			return false;
		records = (BasicDBList) JSON.parse(content);
		for (int i = 0; i < records.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) records.get(i);
			String id = oItem.getString("i");
			String value = oItem.getString("v");
			byId.put(id, oItem);
			byValue.put(value, oItem);
		}
		return true;
	}

	public void addChild(String parentValue, BasicDBObject record) {
		if (StringUtil.isEmpty(parentValue))
			return;
		LinkedHashMap<String, BasicDBObject> v = byChild.get(parentValue);
		if (v == null) {
			v = new LinkedHashMap<String, BasicDBObject>();
			byChild.put(parentValue, v);
		}
		v.put(record.getString("v"), record);
	}

	public void addChilds(BasicDBList records) {
		for (int t = 0; t < records.size(); t++) {
			BasicDBObject o = (BasicDBObject) records.get(t);
			addChild(o.getString("p"), o);
		}
	}

	public BasicDBList childs(String parentValue) {
		LinkedHashMap<String, BasicDBObject> v = this.byChild.get(parentValue);
		if (v == null)
			return null;
		BasicDBList results = new BasicDBList();
		for (Iterator<Entry<String, BasicDBObject>> i = v.entrySet().iterator(); i.hasNext();) {
			Entry<String, BasicDBObject> entry = i.next();
			results.add(entry.getValue());
		}
		return results;
	}

	public boolean containChild(String parentValue, String childValue) {
		LinkedHashMap<String, BasicDBObject> v = this.byChild.get(parentValue);
		if (v == null)
			return false;
		return v.containsKey(childValue);
	}

	@Override
	public String toString() {
		return this.records.toString();
	}

	public int size() {
		return this.records.size();
	}

}
