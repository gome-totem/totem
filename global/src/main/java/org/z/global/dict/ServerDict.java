package org.z.global.dict;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.util.ZooConnect;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ServerDict {
	public static enum NodeAction {
		add, delete, datachange, childchange
	};

	public static enum NodeType {
		all, router, app, server, page, css, mongo, call, redis, scheduler;
	}

	public static ServerDict self = null;
	protected static Logger logger = LoggerFactory.getLogger(ServerDict.class);
	protected ZooConnect zooConnect = null;
	public ServerListener routerListener = null;
	public ServerListener appListener = null;
	public ServerListener pageListener = null;
	public ServerListener scriptListener = null;
	public ServerListener styleListener = null;
	public ServerListener serverListener = null;
	public ServerListener schedulerListener = null;

	static {
		self = new ServerDict();
	}

	public ServerDict() {
		this.init();
	}

	public boolean init() {
		logger.info("ServerDict init zookeeper...");
		zooConnect = new ZooConnect();
		if (zooConnect == null || zooConnect.start() == false) {
			return false;
		}
		routerListener = new ServerListener(NodeType.router, "/routers", zooConnect);
		routerListener.reload();
		zooConnect.add(routerListener);

		appListener = new ServerListener(NodeType.app, "/appservers", zooConnect);
		zooConnect.add(appListener);
		appListener.reload();

		pageListener = new ServerListener(NodeType.page, "/pages", zooConnect);
		zooConnect.add(pageListener);
		pageListener.reload();

		styleListener = new ServerListener(NodeType.page, "/styles", zooConnect);
		zooConnect.add(styleListener);
		styleListener.reload();

		scriptListener = new ServerListener(NodeType.page, "/scripts", zooConnect);
		zooConnect.add(scriptListener);
		scriptListener.reload();

		serverListener = new ServerListener(NodeType.server, "/servers", zooConnect);
		zooConnect.add(serverListener);
		serverListener.reload();

		schedulerListener = new ServerListener(NodeType.scheduler, "/schedulers", zooConnect);
		zooConnect.add(schedulerListener);
		schedulerListener.reload();
		return true;
	}

	public CuratorFramework zoo() {
		return this.zooConnect.instance;
	}

	public void removeRouter(String id) {
		this.routerListener.removeRecord(id);
	}

	public BasicDBList routers() {
		return routerListener.records();
	}

	public BasicDBList schedulers() {
		return schedulerListener.records();
	}

	public BasicDBList apps() {
		return appListener.records();
	}

	public BasicDBList servers() {
		return serverListener.records();
	}

	public BasicDBList pages() {
		return pageListener.records();
	}

	public BasicDBList styles() {
		return styleListener.records();
	}

	public BasicDBList scripts() {
		return scriptListener.records();
	}

	public BasicDBObject scriptBy(String id) {
		return scriptListener.byId(id);
	}

	public BasicDBObject styleBy(String id) {
		return styleListener.byId(id);
	}

	public BasicDBObject pageBy(String id) {
		return pageListener.byId(id);
	}

	public BasicDBList routersByTag(String tag) {
		return routerListener.byTag(tag);
	}

	public BasicDBList routersByRole(String role) {
		return routerListener.byRole(role);
	}

	public BasicDBList appsByTag(String tag) {
		return appListener.byTag(tag);
	}

	public BasicDBObject serverBy(String id) {
		return serverListener.byId(id);
	}

	public String serverIPBy(String id) {
		BasicDBObject oServer = this.serverBy(id);
		if (oServer == null) {

			return null;
		}
		return oServer.getString("ip");
	}

	public BasicDBObject routerBy(String id) {
		return routerListener.byId(id);
	}

	public BasicDBObject hashServer(NodeType type, String hashKey) {
		switch (type) {
		case router:
			return routerListener.nodes.hashServer(hashKey);
		case app:
			return appListener.nodes.hashServer(hashKey);
		case server:
			return serverListener.nodes.hashServer(hashKey);
		case mongo:
			return null;
		default:
			return null;
		}
	}

	public BasicDBObject hashServer(NodeType type, ServiceName service) {
		switch (type) {
		case router:
			return routerListener.nodes.hashServer(type, service.value);
		case app:
			return appListener.nodes.hashServer(type, service.value);
		default:
			return null;
		}
	}

	public BasicDBObject hashServer(NodeType type, String service, String hashKey, String remoteCallModuleName) {
		switch (type) {
		case router:
			return routerListener.nodes.hashServer(type, service, hashKey, remoteCallModuleName);
		case app:
			return appListener.nodes.hashServer(type, service, hashKey, remoteCallModuleName);
		case server:
			return serverListener.nodes.hashServer(type, service, hashKey, remoteCallModuleName);
		case scheduler:
			return schedulerListener.nodes.hashServer(type, remoteCallModuleName, hashKey, remoteCallModuleName);
		default:
			return null;
		}
	}

	public BasicDBObject hashServer(NodeType type, ServiceName service, String hashKey, String remoteCallModuleName) {
		return hashServer(type, service.value, hashKey, remoteCallModuleName);
	}

	public static void main(String[] args) {
		System.out.println(ServerDict.self.schedulers().toString());
	}

}
