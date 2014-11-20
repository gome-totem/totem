package org.z.core.interfaces;

import org.z.global.interfaces.ModuleIntf;
import org.z.global.interfaces.ModuleZeromqIntf;
import org.zeromq.ZMsg;

public interface ModuleBroadcastIntf extends ModuleIntf {

	public void reconnect();

	public ModuleZeromqIntf mq();

	public void send(ZMsg msg);

}
