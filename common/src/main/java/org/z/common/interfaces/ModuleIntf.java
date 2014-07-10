package org.z.common.interfaces;

public interface ModuleIntf {
	
	public boolean init(boolean isReload);

	public void afterCreate(Object[] params);

	public void start(boolean isReload);

	public void stop();

	public boolean isAlive();
	
	public String getId();
}
