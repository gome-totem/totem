package org.z.global.util;

import org.apache.zookeeper.ZooKeeper;

/**
 * @author xiaoming@yundiz.com
 * @version 创建时间：2013-4-4 下午4:18:19
 */

public interface ZooEvent {
	public void onReady(ZooKeeper instance);
}
