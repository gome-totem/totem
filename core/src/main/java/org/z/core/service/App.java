package org.z.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.core.app.ModuleJobPull;
import org.z.core.app.ModuleZeromq;
import org.z.core.queue.ModuleQueue;
import org.z.core.stocket.ModuleSocket;
import org.z.global.dict.Global;
import org.z.global.factory.ModuleFactory;

public class App {
	protected static Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void init(){
		ModuleFactory.registerModule("mq", ModuleZeromq.class);
		ModuleFactory.registerModule("jobpull", ModuleJobPull.class);
		ModuleFactory.registerModule("socket",ModuleSocket.class,new Object[] { Global.SocketAppPort } );
		ModuleFactory.registerModule("queue", ModuleQueue.class);
//		ModuleFactory.registerModule("processor", ModuleProcessor.class);
		
		
	}

}
