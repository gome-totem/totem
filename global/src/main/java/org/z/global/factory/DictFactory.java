package org.z.global.factory;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z.global.interfaces.DateInfo;
import org.z.global.interfaces.DictInfo;

import com.mongodb.BasicDBList;

class DictClassInfo{
	public Class<?> cls = null;
	public Object instance = null;
	public Object[] params = null;
	public boolean keepInstance = true;

	public DictClassInfo(Class<?> cls) {
		this(cls, null, true);
	}

	public DictClassInfo(Class<?> cls, Object[] params, boolean keepInstance) {
		this.cls = cls;
		this.params = params;
		this.keepInstance = keepInstance;
	}

	public DictInfo createDict() {
		try {
			instance = cls.newInstance();
			((DictInfo) instance).init();
		} catch (Exception e) {
			ModuleFactory.LOG.error("create Module", e);
			instance = null;
		}
		return (DictInfo) instance;
	}
	public String getSimpleName(){
		return cls.getSimpleName();
	}
}

public class DictFactory {
	
	public static HashMap<String, DictClassInfo> dictInfos = new HashMap<String, DictClassInfo>();
	public static final Logger LOG = LoggerFactory.getLogger(ModuleFactory.class);
	public static BasicDBList loadeddict = new BasicDBList();
	public static DateInfo date = null;
	
	public static void registerModule(Object dictClass) {
		DictClassInfo dict =new DictClassInfo((Class<?>)dictClass);
		dictInfos.put(dictByName(dict.getSimpleName()),dict);
		dict.createDict();
	}
	
	private static String dictByName(String name) {
			return name.toLowerCase();
	   }
	
	
	public static DictInfo getDictByName(String name){
		DictClassInfo info =dictInfos.get(name.toLowerCase());
		if (info == null) {
			return null;
		}
		return (DictInfo)info.instance ;
	 }

	public static DateInfo getDateDict() {
		DictClassInfo info = dictInfos.get("datedict");
		if (info == null) {
			return null;
		}
		return (DateInfo) info.instance;
	}

	}


