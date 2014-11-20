package org.z.global.zk;

import java.util.HashSet;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ServerDict {
	public static enum NodeAction {
		add, delete, datachange, childchange
	};

	public static enum NodeType {
		all, router, app, server, page, css, mongo, call, redis
	}

	public static ServerDict self = null;
	public static HashSet<String> ipTable = new HashSet<String>();
	protected static Logger logger = LoggerFactory.getLogger(ServerDict.class);
	protected ZooConnect zooConnect = null;
	public ZooListener routerListener = null;
	public ZooListener appListener = null;
	public ZooListener pageListener = null;
	public ZooListener scriptListener = null;
	public ZooListener styleListener = null;
	public ZooListener serverListener = null;
	public ZooListener redisPoolListener = null;

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
		routerListener = new ZooListener(NodeType.router, "/routers", zooConnect);
		routerListener.reload();
		zooConnect.add(routerListener);

		appListener = new ZooListener(NodeType.app, "/appservers", zooConnect);
		zooConnect.add(appListener);
		appListener.reload();

		pageListener = new ZooListener(NodeType.page, "/pages", zooConnect);
		zooConnect.add(pageListener);
		pageListener.reload();

		styleListener = new ZooListener(NodeType.page, "/styles", zooConnect);
		zooConnect.add(styleListener);
		styleListener.reload();

		scriptListener = new ZooListener(NodeType.page, "/scripts", zooConnect);
		zooConnect.add(scriptListener);
		scriptListener.reload();

		serverListener = new ZooListener(NodeType.server, "/servers", zooConnect);
		zooConnect.add(serverListener);
		serverListener.reload();

		redisPoolListener = new ZooListener(NodeType.redis, "/redispools", zooConnect);
		zooConnect.add(redisPoolListener);
		redisPoolListener.reload();
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

	public BasicDBList apps() {
		return appListener.records();
	}

	public BasicDBList servers() {
		return serverListener.records();
	}

	public BasicDBList pages() {
		return pageListener.records();
	}

	public BasicDBList redisPool() {
		return redisPoolListener.records();
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

	public BasicDBObject redisPoolBy(String id) {
		return redisPoolListener.byId(id);
	}

	public BasicDBObject serverBy(String id) {
		return serverListener.byId(id);
	}

	public BasicDBList serverByTag(String tag) {
		return serverListener.nodes.recordsByTag.get(tag);
	}

	public String serverIPBy(String id) {
		BasicDBObject oServer = this.serverBy(id);
		if (oServer == null) {
			return "www.yiqihi.com";
		}
		return oServer.getString("ip");
	}

	public boolean hasIP(String ip) {
		return ipTable.contains(ip);
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

	public BasicDBObject hashServer(NodeType type, ServiceName service, String hashKey,String remoteCallModuleName) {
		switch (type) {
		case router:
			return routerListener.nodes.hashServer(type, service.value, hashKey,remoteCallModuleName);
		case app:
			return appListener.nodes.hashServer(type, service.value, hashKey,remoteCallModuleName);
		case server:
			return serverListener.nodes.hashServer(type, service.value, hashKey,remoteCallModuleName);
		default:
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(ServerDict.self.routers());
	}

}
