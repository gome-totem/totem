package org.z.global.zk;

import org.z.global.zk.ServerDict.NodeAction;
import org.z.global.zk.ServerDict.NodeType;

public interface ZooKeeperWatchIntf {

	public void zooNodeChange(NodeType type, NodeAction action, String id, String ip);
	
	public void reconnect();

}
