package org.z.global.plugin;

import java.util.Collection;
import java.util.HashMap;

import com.google.common.collect.ImmutableList;

public abstract class AbstractPlugin implements Plugin {


	@Override
	public Collection<Class<? extends Module>> modules() {
		return ImmutableList.of();
	}

	@Override
	public Collection<? extends Module> modules(HashMap<String, Object> settings) {
		return ImmutableList.of();
	}

	@Override
	public Collection<Class<? extends Module>> indexModules() {
		return ImmutableList.of();
	}

	@Override
	public Collection<? extends Module> indexModules(
			HashMap<String, Object> settings) {
		return ImmutableList.of();
	}

	@Override
	public Collection<Class<? extends Module>> shardModules() {
		return ImmutableList.of();
	}

	@Override
	public Collection<? extends Module> shardModules(
			HashMap<String, Object> settings) {
		return ImmutableList.of();
	}

	@Override
	public void processModule(Module module) {

	}

	@Override
	public HashMap<String, Object> additionalSettings() {
		return null;
	}

}
