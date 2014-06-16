package org.z.global.dict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.ServerDict.NodeType;
import org.z.global.util.HashTimes;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ServerNodes {
	protected HashMap<String, BasicDBObject> recordsById = new HashMap<String, BasicDBObject>();
	protected HashMap<String, BasicDBObject> recordsByIP = new HashMap<String, BasicDBObject>();
	protected HashMap<String, BasicDBList> recordsByTag = new HashMap<String, BasicDBList>();
	protected HashMap<String, BasicDBList> recordsBy = new HashMap<String, BasicDBList>();
	protected BasicDBList records = new BasicDBList();
	protected HashSet<String> tags = new HashSet<String>();
	protected ConcurrentHashMap<String, AtomicLong> counter = new ConcurrentHashMap<String, AtomicLong>();
	protected static Logger logger = LoggerFactory.getLogger(ServerNodes.class);

	public BasicDBList records() {
		return this.records;
	}

	public boolean contains(String id) {
		return recordsById.containsKey(id);
	}

	public BasicDBObject byId(String id) {
		return recordsById.get(id);
	}
	public BasicDBObject byIP(String ip) {
		return recordsByIP.get(ip);
	}

	public BasicDBList byTag(String tag) {
		BasicDBList servers = recordsByTag.get(tag);
		if (Global.DevelopMode == false || servers == null) {
			return servers;
		}
		BasicDBList items = new BasicDBList();
		items.addAll(servers);
		int index = 0;
		while (index < items.size()) {
			BasicDBObject oServer = (BasicDBObject) items.get(index);
			if (!oServer.getString("id").startsWith(Global.DevelopName)) {
				items.remove(index);
				continue;
			}
			index++;
		}
		return items;
	}

	public BasicDBList byRole(String role) {
		BasicDBList items = new BasicDBList();
		items.addAll(records);
		int index = 0;
		while (index < items.size()) {
			BasicDBObject oServer = (BasicDBObject) items.get(index);
			BasicDBList roles = (BasicDBList) oServer.get("role");
			boolean exist = false;
			for (int i = 0; roles != null && i < roles.size(); i++) {
				if (roles.get(i).toString().equalsIgnoreCase(role) == true) {
					exist = true;
					break;
				}
			}
			if (exist == false) {
				items.remove(index);
				continue;
			}
			index++;
		}
		return items;
	}

	public HashSet<String> tags() {
		return this.tags;
	}

	public void clear() {
		recordsById.clear();
		recordsByTag.clear();
		recordsBy.clear();
		recordsByIP.clear();
		records.clear();
		tags.clear();
	}

	public BasicDBObject findByIP(String ip) {
		for (int i = 0; i < records.size(); i++) {
			BasicDBObject oServer = (BasicDBObject) records.get(i);
			if (oServer.getString("ip").equalsIgnoreCase(ip)) {
				return oServer;
			}
		}
		return null;
	}

	public int find(String id, BasicDBList items) {
		int result = -1;
		for (int i = 0; i < items.size(); i++) {
			BasicDBObject oItem = (BasicDBObject) items.get(i);
			if (oItem.getString("id").equalsIgnoreCase(id)) {
				result = i;
			}
		}
		return result;
	}

	public synchronized void add(BasicDBObject record) {
		String id = record.getString("id");
		int index = this.find(id, records);
		if (index == -1) {
			records.add(record);
		} else {
			records.set(index, record);
		}
		recordsById.put(id, record);
		if (!record.containsField("tags")) {
			return;
		}
		if (record.containsField("ip")) {
			recordsByIP.put(record.getString("ip"), record);
		}
		BasicDBList recordTags = (BasicDBList) record.get("tags");
		for (int i = 0; recordTags != null && i < recordTags.size(); i++) {
			String tag = recordTags.get(i).toString();
			tags.add(tag);
			BasicDBList items = recordsByTag.get(tag);
			if (items == null) {
				items = new BasicDBList();
				recordsByTag.put(tag, items);
			}
			index = this.find(id, items);
			if (index == -1) {
				items.add(record);
			} else {
				items.set(index, record);
			}
		}
		if (record.getString("type", "none").equalsIgnoreCase("server") && record.getString("action", "add").equalsIgnoreCase("remove") == false) {
			if (record.containsField("mongo")) {
				addBy("mongo", record);
			} else if (record.containsField("call")) {
				addBy("call", record);
			} else if (record.containsField("redis")) {
				addBy("redis", record);
			}
		}
	}

	protected void addBy(String typeName, BasicDBObject record) {
		String id = record.getString("id");
		BasicDBList items = recordsBy.get(typeName);
		if (items == null) {
			items = new BasicDBList();
			recordsBy.put(typeName, items);
		}
		int index = this.find(id, items);
		if (index == -1) {
			items.add(record);
		} else {
			items.set(index, record);
		}
	}

	protected void removeBy(String typeName, BasicDBObject record) {
		String id = record.getString("id");
		BasicDBList items = recordsBy.get(typeName);
		if (items == null) {
			return;
		}
		int index = this.find(id, items);
		if (index != -1) {
			items.remove(index);
		}
		if (items.size() == 0) {
			recordsBy.remove(typeName);
		}
	}

	public synchronized boolean remove(String id) {
		BasicDBObject oRemove = recordsById.remove(id);
		if (oRemove != null && oRemove.containsField("ip")) {
			recordsByIP.remove(oRemove.getString("ip"));
		}
		int index = this.find(id, records);
		boolean tag = index != -1;
		BasicDBObject record = null;
		if (tag) {
			record = (BasicDBObject) records.remove(index);
		}
		for (Iterator<Entry<String, BasicDBList>> i = recordsByTag.entrySet().iterator(); i.hasNext();) {
			Entry<String, BasicDBList> entry = i.next();
			BasicDBList items = entry.getValue();
			index = this.find(id, items);
			if (index != -1) {
				items.remove(index);
			}
			if (items.size() == 0) {
				tags.remove(entry.getKey());
			}
		}
		if (tag == true && record.getString("type", "none").equalsIgnoreCase("server") && record.getString("action", "add").equalsIgnoreCase("remove") == false) {
			if (record.containsField("mongo")) {
				removeBy("mongo", record);
			} else if (record.containsField("call")) {
				removeBy("call", record);
			} else if (record.containsField("redis")) {
				removeBy("redis", record);
			}
		}
		return tag;
	}

	protected String allocateNo(NodeType type) {
		AtomicLong count = counter.get(type.name());
		if (count == null) {
			count = new AtomicLong(0);
			counter.put(type.name(), count);
		}
		if (count.get() == Long.MAX_VALUE) {
			count.set(0);
		}
		return String.valueOf(count.addAndGet(1));
	}

	public BasicDBObject hashServer(String hashKey) {
		return hashServer(NodeType.all, null, hashKey, null);
	}

	public BasicDBObject hashServer(NodeType type, String hashKey) {
		return hashServer(type, null, hashKey, null);
	}

	public BasicDBObject hashServer(NodeType type, String tag, String hashKey, String remoteCallModuleName) {
		BasicDBList items = null;
		switch (type) {
		case all:
			items = records;
			break;
		case mongo:
		case call:
		case redis:
			items = this.recordsBy.get(type.name());
			break;
		default:
			items = this.byTag(tag);
			break;
		}
		if (items == null || items.size() == 0) {
			return null;
		}
		BasicDBList newItems = null;
		if (remoteCallModuleName != null && !remoteCallModuleName.equals("")) {
			if (Global.DevelopMode) {
				remoteCallModuleName= remoteCallModuleName.toLowerCase();
				remoteCallModuleName += "@" + Global.DevelopName;
			}
			newItems = new BasicDBList();
			for (int i = 0; i < items.size(); i++) {
				BasicDBObject item = (BasicDBObject) items.get(i);
				BasicDBList tags = (BasicDBList) item.get("tags");
				if (tags != null && tags.contains(remoteCallModuleName)) {
					newItems.add(item);
				}
			}
			items = newItems;
		}
		if (items == null || items.size() == 0) {
			return null;
		}
		if (Global.DevelopMode == true) {
			for (int i = 0; i < items.size(); i++) {
				BasicDBObject oRouter = (BasicDBObject) items.get(i);
				if (oRouter.getString("id").startsWith(Global.DevelopName)) {
					return oRouter;
				}
			}
			logger.warn("can't find {}  by prefix name[{}]", new Object[] { type.name(), Global.DevelopName });
			return null;
		}
		if (StringUtils.isEmpty(hashKey)) {
			hashKey = allocateNo(type);
		}
		int index = HashTimes.use33(hashKey) % items.size();
		return (BasicDBObject) items.get(index);
	}
}
