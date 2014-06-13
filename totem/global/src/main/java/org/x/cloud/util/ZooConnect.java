package org.x.cloud.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.x.cloud.dict.ServerListener;

/**
 * @author xiaoming@yundiz.com
 * @version 创建时间：2013-4-3 上午10:25:24
 */

public class ZooConnect implements CuratorWatcher, CuratorListener, ConnectionStateListener {
	public CuratorFramework instance = null;
	public ArrayList<ServerListener> listeners = new ArrayList<ServerListener>();
	protected static Logger logger = LoggerFactory.getLogger(ZooConnect.class);

	public ZooConnect() {
		this.instance = ZooUtil.create();
	}

	public boolean start() {
		try {
			this.instance.getConnectionStateListenable().addListener(this);
			this.instance.getCuratorListenable().addListener(this);
			this.instance.start();
			this.instance.getZookeeperClient().blockUntilConnectedOrTimedOut();
			return true;
		} catch (Exception e) {
			logger.error("start", e);
			return false;
		}
	}

	public void add(ServerListener listener) {
		listeners.add(listener);
	}

	public void remove(ServerListener listener) {
		listeners.remove(listener);
	}

	public boolean checkAlive() {
		return this.instance.getZookeeperClient().isConnected();
	}

	public String getData(String path) throws Exception {
		return ZooUtil.getData(instance, path, this);
	}

	public boolean exists(String path) {
		return ZooUtil.exists(instance, path, this);
	}

	public List<String> getChildrens(String path) throws Exception {
		if (ZooUtil.exists(instance, path, this) == false) {
			return null;
		}
		return ZooUtil.getChilds(instance, path, this);
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		if (newState == ConnectionState.RECONNECTED) {
			logger.info("Zookeeper Reconnect [{}]", new String[] { client.toString() });
			for (int i = 0; i < listeners.size(); i++) {
				ServerListener w = listeners.get(i);
				w.fireReconnect();
			}
		} else if (newState == ConnectionState.LOST || newState == ConnectionState.SUSPENDED) {
			logger.info("Zookeeper Lost connection");
		}
	}

	private void fireEvents(WatchedEvent e) {
		for (int i = 0; i < listeners.size(); i++) {
			ServerListener w = listeners.get(i);
			w.process(e);
		}
	}

	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
		if (event == null || event.getPath() == null) {
			return;
		}
		fireEvents(event.getWatchedEvent());
	}

	@Override
	public void process(WatchedEvent event) {
		if (event == null || event.getPath() == null) {
			return;
		}
		fireEvents(event);
	}

}