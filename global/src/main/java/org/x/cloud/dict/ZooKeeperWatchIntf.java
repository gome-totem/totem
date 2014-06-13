package org.x.cloud.dict;

import org.x.cloud.dict.ServerDict.NodeAction;
import org.x.cloud.dict.ServerDict.NodeType;

public interface ZooKeeperWatchIntf {

	public void zooNodeChange(NodeType type, NodeAction action, String id, String ip);
	
	public void reconnect();

}
