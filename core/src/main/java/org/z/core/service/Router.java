package org.z.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleJobPull;
import org.z.core.common.ModuleBroadcast;
import org.z.core.router.ModuleZeromq;
import org.z.core.stocket.ModuleSocket;
import org.z.global.dict.Global;
import org.z.global.environment.ConfigFile;
import org.z.global.factory.ModuleFactory;
import com.mongodb.BasicDBList;

public class Router {
	protected static Logger logger = LoggerFactory.getLogger(Router.class);
	public static boolean verbose = false;
	public static String RouterModules = ConfigFile.rock().getItem("RouterModules", "mq,broadcast,jobpull,socket").toLowerCase();

	public static void init() {
		ModuleFactory.registerModule("mq", ModuleZeromq.class);
		ModuleFactory.registerModule("broadcast", ModuleBroadcast.class, new Object[] { "router" });
		ModuleFactory.registerModule("jobpull", ModuleJobPull.class, new Object[] { 1 });
		ModuleFactory.registerModule("socket", ModuleSocket.class, new Object[] { Global.SocketRouterPort });
	}

	public static void main(String[] args) {
		init();
		logger.info("java.library.path={}", new Object[] { System.getProperty("java.library.path") });
		String[] values = RouterModules.split("\\,");
		BasicDBList modules = new BasicDBList();
		for (int i = 0; i < values.length; i++) {
			if (modules.contains(values[i])) {
				continue;
			}
			modules.add(values[i]);
		}
		ModuleFactory.loadModules(modules);
		logger.info("RouterServer starting success.");
	}
}
