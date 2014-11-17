package org.z.global.factory;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.dict.Global;
import org.z.global.interfaces.ModuleHtmlPageIntf;
import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.ModuleProcessorIntf;
import org.z.global.interfaces.ModuleQueueIntf;
import org.z.global.interfaces.ModuleSocketIntf;
import org.z.global.util.EmptyUtil;

import com.mongodb.BasicDBList;

class ClassInfo {
	public Class<?> cls = null;
	public Object instance = null;
	public Object[] params = null;
	public boolean keepInstance = true;

	public ClassInfo(Class<?> cls) {
		this(cls, null, true);
	}

	public ClassInfo(Class<?> cls, Object[] params, boolean keepInstance) {
		this.cls = cls;
		this.params = params;
		this.keepInstance = keepInstance;
	}

	public ModuleIntf createModule() {
		try {
			instance = cls.newInstance();
			if (params != null) {
				((ModuleIntf) instance).afterCreate(params);
			}
		} catch (Exception e) {
			ModuleFactory.LOG.error("create Module", e);
			instance = null;
		}
		return (ModuleIntf) instance;
	}
}

public class ModuleFactory {

	public static final Logger LOG = LoggerFactory.getLogger(ModuleFactory.class);
	public static BasicDBList loadedModules = new BasicDBList();
	private static HashMap<String, ClassInfo> moduleInfos = new HashMap<String, ClassInfo>();
	private static ModuleIntf mq = null;
	private static ModuleIntf processor = null;
	private static ModuleIntf socket = null;
	private static ModuleIntf htmlpage = null;
	private static ModuleIntf queue = null;
	private static ModuleIntf productIndex = null;
	public static void registerModule(String moduleName, Class<?> moduleClass) {
		moduleInfos.put(moduleNameBy(moduleName), new ClassInfo(moduleClass));
	}

	public static void registerModule(String moduleName, Class<?> moduleClass, Object[] params) {
		moduleInfos.put(moduleNameBy(moduleName), new ClassInfo(moduleClass, params, true));
	}
	public static ModuleProcessorIntf processor() {
		if (processor != null)
			return (ModuleProcessorIntf) processor;
		ModuleIntf instance = moduleInstanceBy("processor");
		return (ModuleProcessorIntf) instance;
	}
	public static ModuleSocketIntf socket() {
		if (socket != null)
			return (ModuleSocketIntf) socket;
		ModuleIntf instance = moduleInstanceBy("socket");
		return (ModuleSocketIntf) instance;
	}
	
	public static String moduleNameBy(String name) {
		if (Global.DevelopMode == true) {
			return name.toLowerCase() + "@" + Global.DevelopName;
		} else {
			return name.toLowerCase();
		}
	}

	public static void registerModule(String moduleName, String moduleClass) {
		try {
			Class<?> cls = Class.forName(moduleClass);
			if (cls == null) {
				return;
			}
			moduleInfos.put(moduleNameBy(moduleName), new ClassInfo(cls));
		} catch (Exception e) {
		}
	}

	public static ModuleIntf moduleBy(String name) {
		ClassInfo info = moduleInfos.get(name);
		if(name.equalsIgnoreCase("edm"))
		System.out.println("dem test 109 line" +info);
		if (info == null) {
			return null;
		}
		return info.createModule();
	}

	public static ModuleIntf moduleInstanceBy(String name) {
		ClassInfo info = moduleInfos.get(moduleNameBy(name));
		if (info == null) {
			return null;
		}
		return (ModuleIntf) info.instance;
	}



	public static ModuleIntf mq() {
		if (mq != null)
			return mq;
		mq = moduleInstanceBy("mq");
		return mq;
	}
	public static ModuleIntf productIndex() {
		if (productIndex != null)
			return productIndex;
		productIndex = moduleInstanceBy("productindex");
		return productIndex;
	}
	public static ModuleHtmlPageIntf htmlPage() {
		if (htmlpage != null)
			return (ModuleHtmlPageIntf) htmlpage;
		ModuleIntf instance = moduleInstanceBy("htmlpage");
		return (ModuleHtmlPageIntf) instance;
	}
	public static ModuleQueueIntf queue() {
		if (queue != null)
			return (ModuleQueueIntf) queue;
		queue = moduleInstanceBy("queue");
		return (ModuleQueueIntf) queue;
	}

	public static void loadModules() {
		for (int i = 0; i < loadedModules.size(); i++) {
			String moduleName = String.valueOf(loadedModules.get(i));
			ModuleIntf instance = ModuleFactory.moduleBy(moduleName);
			if (instance == null) {
				LOG.error("module[{}] not be registered.", new String[] { moduleName });
				System.exit(-1);
			}
			LOG.info("===>Load Module[{}] Start.", new String[] { moduleName });
			if (instance.init(false) == false) {
				LOG.error("module[{}]  init fail", new String[] { moduleName });
				System.exit(-1);
			}
			instance.start(false);
			if (instance.isAlive() == false) {
				LOG.error("module[{}]  start fail", new String[] { moduleName });
				System.exit(-1);
			}
			LOG.info("===>Load module[{}] Ok.", new String[] { moduleName });
		}
	}

	public static void registerLoadModule(String module) {
		registerLoadModule(module, new String[0]);
	}

	public static void registerLoadModule(String module, String[] ips) {
		if (EmptyUtil.isEmpty(ips) && containsModule(module)) {
			return;
		}
		loadedModules.remove(moduleNameBy(module));
		if (EmptyUtil.notEmpty(ips)) {
			for (String ip : ips) {
				if (Global.localIP.equalsIgnoreCase(ip)) {
					LOG.info("module [{}] register ip [{}]", new String[] { module, ip });
					loadedModules.add(moduleNameBy(module));
				}
			}
			return;
		}
		loadedModules.add(moduleNameBy(module));
	}

	public static boolean containsModule(String module) {
		return loadedModules.contains(moduleNameBy(module));
	}
	public static void loadModules(BasicDBList values) {
		for (int i = 0; i < values.size(); i++) {
			String name = values.get(i).toString().toLowerCase();
			if (Global.DevelopMode == true) {
				loadedModules.add(name + "@" + Global.DevelopName);
			} else {
				loadedModules.add(name);
			}
		}
		for (int i = 0; i < values.size(); i++) {
			String moduleName = String.valueOf(values.get(i));
			ModuleIntf instance = ModuleFactory.moduleBy(moduleName);
			if (instance == null) {
				LOG.error("module[{}] not be registered.", new String[] { moduleName });
				System.exit(-1);
			}
			LOG.info("===>Load Module[{}] Start.", new String[] { moduleName });
			if (instance.init(false) == false) {
				LOG.error("module[{}]  init fail", new String[] { moduleName });
				System.exit(-1);
			}
			instance.start(false);
			if (instance.isAlive() == false) {
				LOG.error("module[{}]  start fail", new String[] { moduleName });
				System.exit(-1);
			}
			LOG.info("===>Load module[{}] Ok.", new String[] { moduleName });
		}
	}

}
