package org.z.global.interfaces;

import org.zeromq.ZContext;

public interface ModuleZeromqIntf extends ModuleIntf {

	public String allocateJobNo();

	public String name();

	public String getRouterIP();

	public ZContext ctx();

}
