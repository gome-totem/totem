package org.z.global.dict;

import org.z.global.dict.ServerDict.NodeAction;
import org.z.global.dict.ServerDict.NodeType;

public interface ZooKeeperWatchIntf {

	public void zooNodeChange(NodeType type, NodeAction action, String id, String ip);
	
	public void reconnect();

}
