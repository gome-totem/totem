package org.z.store.totem;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

public class StoreBuilder {
	public static enum StoreAction {
		usr, enviroment
    }

	private final ConcurrentMap<String, String> settings = new MapMaker().makeMap();
	public static StoreBuilder Builder() {
		return new StoreBuilder();
	}

	public ConcurrentMap<String, String> settings() {
		return settings;
	}

	public StoreBuilder settings(ConcurrentMap<String, String> settings) {
		settings.putAll(settings);
		return this;
	}

	public StoreBuilder put(String key, String value) {
		settings.put(key, value);
		return this;
	}
	public TotemDB build(){
		TotemDB totemDB = new TotemDB(settings);
		totemDB.initDBFactory();
		return totemDB;
	 }

}
