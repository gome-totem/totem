package org.z.core.common;

import java.util.concurrent.ConcurrentHashMap;

public class FacetInfo {

	public int id = 0;
	public String code = null;
	public String name = null;
	public String label = null;

	public static ConcurrentHashMap<Integer, FacetInfo> byId = new ConcurrentHashMap<Integer, FacetInfo>();
	public static ConcurrentHashMap<String, FacetInfo> byName = new ConcurrentHashMap<String, FacetInfo>();

	public static FacetInfo register(int id, String code, String name, String label) {
		FacetInfo info = new FacetInfo(id, code, name, label);
		byId.put(id, info);
		byName.put(name, info);
		return info;
	}

	public static FacetInfo nameBy(String name) {
		return byName.get(name);
	}

	public static FacetInfo idBy(int id) {
		return byId.get(id);
	}

	public FacetInfo(int id, String code, String name, String label) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.label = label;
	}

}
