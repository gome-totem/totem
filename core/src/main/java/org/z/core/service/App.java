package org.z.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleJobPull;
import org.z.core.app.ModuleZeromq;
import org.z.core.common.ModuleProcessor;
import org.z.core.module.ModuleAppoint;
import org.z.core.module.ModuleBill;
import org.z.core.module.ModuleUser;
import org.z.core.queue.ModuleQueue;
import org.z.core.stocket.ModuleSocket;
import org.z.global.dict.Global;
import org.z.global.environment.Config;
import org.z.global.factory.ModuleFactory;
import org.z.global.util.StringUtil;

public class App {
	protected static Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void init(){
		ModuleFactory.registerModule("mq", ModuleZeromq.class);
		ModuleFactory.registerModule("jobpull", ModuleJobPull.class);
		ModuleFactory.registerModule("socket",ModuleSocket.class,new Object[] { Global.SocketAppPort } );
		ModuleFactory.registerModule("queue", ModuleQueue.class);
		ModuleFactory.registerModule("processor", ModuleProcessor.class);
		ModuleFactory.registerModule("appoint", ModuleAppoint.class);
		ModuleFactory.registerModule("bill", ModuleBill.class);
		ModuleFactory.registerModule("user", ModuleUser.class);
	}
	
	public static void main(String[] args) {
		long start  =System.currentTimeMillis();
		init();
		logger.info("java.library.path[{}]",new Object[]{System.getProperty("java.library.path")});
		ModuleFactory.registerLoadModule("");
		ModuleFactory.registerLoadModule("");
		ModuleFactory.registerLoadModule("");
		ModuleFactory.registerLoadModule("");
		ModuleFactory.registerLoadModule("");
		String value = Config.rock().getItem("AppServices", "").toLowerCase();
		String[] values = value.split("\\,");
		for (int i = 0; i < values.length; i++) {
			if (StringUtil.isEmpty(values[i])) {
				continue;
			}
			String[] split = values[i].split(":");
			int length = split.length;
			if (length == 1) {
				ModuleFactory.registerLoadModule(split[0]);
			} else {
				String[] ips = new String[length - 1];
				System.arraycopy(split, 1, ips, 0, ips.length);
				ModuleFactory.registerLoadModule(split[0], ips);
			}
		}
		logger.info("AppServer starting success in {} ms.", System.currentTimeMillis() - start);
	}

}
