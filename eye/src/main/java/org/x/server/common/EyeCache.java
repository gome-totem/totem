package org.x.server.common;

import com.mongodb.BasicDBList;

public class EyeCache {
	public static BasicDBList appsDoc = new BasicDBList();   //ZooKeeper 中正在运行的各App中，索引Doc数
}
