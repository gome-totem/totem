package org.z.global.zk;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.util.StringUtil;
import org.z.global.zk.ServerDict.NodeAction;
import org.z.global.zk.ServerDict.NodeType;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class ZooListener implements Watcher {
	protected static Logger logger = LoggerFactory.getLogger(ZooListener.class);
	protected String prefixPath = "";
	protected ServerNodes nodes = new ServerNodes();
	protected ArrayList<ZooKeeperWatchIntf> watchers = new ArrayList<ZooKeeperWatchIntf>();
	protected ZooConnect zooConnect = null;
	protected Integer LOCK = 0;
	protected NodeType nodeType = null;

	public ZooListener(NodeType nodeType, String prefixPath, ZooConnect zooConnect) {
		this.prefixPath = prefixPath;
		this.zooConnect = zooConnect;
		this.nodeType = nodeType;
	}

	public void fireNodeChange(NodeType type, NodeAction action, String id, String ip) {
		for (int i = 0; i < watchers.size(); i++) {
			watchers.get(i).zooNodeChange(type, action, id, ip);
		}
	}

	public void fireReconnect() {
		for (int i = 0; i < watchers.size(); i++) {
			watchers.get(i).reconnect();
		}
	}

	public BasicDBList records() {
		return nodes.records;
	}

	public BasicDBObject byId(String id) {
		return nodes.recordsById.get(id);
	}

	public BasicDBObject byIP(String ip) {
		return nodes.recordsByIP.get(ip);
	}

	public BasicDBList byTag(String tag) {
		BasicDBList servers = nodes.byTag(tag);
		return servers;
	}

	public BasicDBList byRole(String role) {
		BasicDBList servers = nodes.byRole(role);
		return servers;
	}

	public BasicDBObject hashBy(NodeType type, String key) {
		return this.nodes.hashServer(type, key);
	}

	public void removeRecord(String id) {
		this.nodes.remove(id);
	}

	public void addWather(ZooKeeperWatchIntf watcher) {
		watchers.add(watcher);
	}

	public void removeWatcher(ZooKeeperWatchIntf watcher) {
		watchers.remove(watcher);
	}

	public void addRecord(String id) {
		if (zooConnect.checkAlive() == false) {
			return;
		}
		try {
			String c = zooConnect.getData(this.prefixPath + "/" + id);
			if (c == null) {
				return;
			}
			BasicDBObject record = (BasicDBObject) JSON.parse(c);
			nodes.add(record);
		} catch (Exception e) {
			logger.error("ServerNodeListener", e);
		}
	}

	public void reload() {
		if (zooConnect.checkAlive() == false) {
			return;
		}
		if (zooConnect.exists(prefixPath) == false)
			return;
		try {
			String timestamp = StringUtil.currentTime();
			List<String> items = zooConnect.getChildrens(this.prefixPath);
			for (int i = 0; items != null && i < items.size(); i++) {
				String id = items.get(i);
				String c = zooConnect.getData(this.prefixPath + "/" + id);
				if (c == null) {
					continue;
				}
				BasicDBObject record = (BasicDBObject) JSON.parse(c);
				ServerDict.ipTable.add(record.getString("ip"));
				record.append("timestamp", timestamp);
				nodes.add(record);
			}
			ServerDict.ipTable.remove("127.0.0.1");
			int index = 0;
			while (index < nodes.records.size()) {
				BasicDBObject record = (BasicDBObject) nodes.records.get(index);
				if (record.getString("timestamp", "xiaoming@yiqihi.com").equalsIgnoreCase(timestamp)) {
					index++;
					continue;
				}
				if (nodes.remove(record.getString("id")) == false) {
					index++;
				}
			}
		} catch (Exception e) {
			logger.error("ServerListener", e);
		}
	}

	@Override
	public void process(WatchedEvent event) {
		if (event == null) {
			return;
		}
		String path = event.getPath();
		if ((path == null) || !path.startsWith(prefixPath)) {
			return;
		}
		synchronized (LOCK) {
			try {
				String[] values = StringUtils.split(path, '/');
				String id = values.length >= 2 ? values[1] : "";
				BasicDBObject oServer = null;
				NodeAction action = null;
				if (event.getType() == EventType.NodeCreated) {
					action = NodeAction.add;
					this.addRecord(id);
					oServer = this.byId(id);
				} else if (event.getType() == EventType.NodeDeleted) {
					action = NodeAction.delete;
					oServer = this.byId(id);
					this.removeRecord(id);
				} else if (event.getType() == EventType.NodeDataChanged) {
					action = NodeAction.datachange;
					this.addRecord(id);
					oServer = this.byId(id);
				} else if (event.getType() == EventType.NodeChildrenChanged) {
					action = NodeAction.childchange;
					this.reload();
					this.fireNodeChange(this.nodeType, action, id, "");
				}
				if (oServer != null) {
					this.fireNodeChange(this.nodeType, action, id, oServer.getString("ip"));
				}
			} catch (Exception e) {
				logger.info("process", e);
			} finally {
			}
		}
	}
}
