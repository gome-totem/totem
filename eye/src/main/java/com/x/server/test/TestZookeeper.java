package com.x.server.test;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class TestZookeeper implements Watcher {

	public static Log log = LogFactory.getLog(TestZookeeper.class) ;

	public static void main(String args[]) throws IOException, KeeperException, InterruptedException{

		TestZookeeper watch = new TestZookeeper() ;
		
		String connectString = "10.58.50.12:19750" ;
		String routerPath = "/777" ;
		String appPath = routerPath + "/888" ;
		
		ZooKeeper zooKeeper = new ZooKeeper(connectString, 30000, watch) ;

		//创建router目录
		zooKeeper.exists(routerPath,true);
		zooKeeper.create(routerPath, "0".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT) ;
		System.in.read();
		//判断节点是否存在
		log.info("is " + routerPath +"alive: " + zooKeeper.exists(routerPath, watch) );
		
		//创建app目录
		zooKeeper.create(appPath, "1".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL) ;
		System.in.read();

		//判断app是否存在
		log.info("is " + appPath +"alive: " + zooKeeper.exists(appPath, watch)) ;
		
		//取出router目录信息
		log.info(zooKeeper.getData(routerPath, watch, null)) ;
		
		//取出app目录信息
		log.info(zooKeeper.getData(appPath, watch, null)) ;
		
		//取出router下面的app
		log.info(zooKeeper.getChildren(routerPath, watch)) ;
		
		//修改app目录下的数据
		zooKeeper.setData(appPath, "2".getBytes(), -1) ;
		System.in.read();
		
		//取出app目录信息
		log.info(zooKeeper.getData(appPath, watch, null)) ;
				
		//删除app目录
		zooKeeper.delete(appPath, -1) ;
		System.in.read();
		
		//判断app是否存在
		log.info("is " + appPath +"alive: " + zooKeeper.exists(appPath, watch) );
		
		//删除router
		zooKeeper.delete(routerPath, -1) ;
		System.in.read();
		
		//判断节点是否存在
		log.info("is " + routerPath +"alive: " + zooKeeper.exists(routerPath, watch) );
		
		//关闭连接
		zooKeeper.close() ;
	}

	public void process(WatchedEvent event) {
		
	}


}
