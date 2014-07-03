package org.z.global.plugins;

import org.z.global.plugin.AbstractPlugin;

import com.google.common.collect.ImmutableMap;

public class TestPlugin extends AbstractPlugin {
	private ImmutableMap<String,Object> settings;
	public TestPlugin(ImmutableMap<String,Object> settings){
		this.settings =settings;
	}
	public TestPlugin(){
	}
	@Override
	public String name() {
		return "name"+settings.size();
	}
	@Override
	public String description() {
		return "test";
	}

}
