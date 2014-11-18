package org.x.server.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@Service
public class IJVMDetectImpl implements IJVMDetect{
	public BasicDBObject JVMDetect(){		
			String freeMem=formatSize(Long.valueOf(Runtime.getRuntime().freeMemory()), true);
			String totalMem=formatSize( Long.valueOf(Runtime.getRuntime().totalMemory()), true);
			String  maxMem=formatSize(Long.valueOf(Runtime.getRuntime().maxMemory()), true);
			SortedMap memoryPoolMBeans = new TreeMap();

		    for (MemoryPoolMXBean mbean : ManagementFactory.getMemoryPoolMXBeans()) {
		      String sortKey = new StringBuilder().append(mbean.getType()).append(":").append(mbean.getName()).toString();
		      memoryPoolMBeans.put(sortKey, mbean);
		    }
		    BasicDBList JVMList=new BasicDBList();
		    Collection con= memoryPoolMBeans.values();
		    Iterator it = con.iterator();  
		    while(it.hasNext()) {  
		    	 MemoryPoolMXBean memoryPoolMBean=(MemoryPoolMXBean)it.next();
		        BasicDBObject detailJVM=new BasicDBObject();
		        MemoryUsage usage = memoryPoolMBean.getUsage();
		        String name= memoryPoolMBean.getName();
		        String type=memoryPoolMBean.getType().toString();
		        String init=formatSize(Long.valueOf(usage.getInit()), true);
		        String commit=formatSize(Long.valueOf(usage.getCommitted()), true);
		        String max=formatSize(Long.valueOf(usage.getMax()), true);
		        String use=formatSize(Long.valueOf(usage.getUsed()), true);
		        if (usage.getMax() > 0L) {
		          use+=new StringBuilder().append(" (").append(usage.getUsed() * 100L / usage.getMax()).append("%)").toString();
		        }
		        detailJVM.append("name", name).append("type", type).append("init", init).append("commit", commit).append("max", max).append("use", use);
		        JVMList.add(detailJVM);
		    }  
			return new BasicDBObject("freeMem",freeMem).append("totalMem", totalMem).append("MaxMem", maxMem).append("detail", JVMList);
	}
	
	
	/**
	 * Display the given size in bytes, either as KB or MB.
	 * 
	 */
	public static String formatSize(Object obj, boolean mb) {
		long bytes = -1L;
		if (obj instanceof Long) {
			bytes = ((Long) obj).longValue();
		} else if (obj instanceof Integer) {
			bytes = ((Integer) obj).intValue();
		}
		if (mb) {
			StringBuilder buff = new StringBuilder();
			if (bytes < 0) {
				buff.append('-');
				bytes = -bytes;
			}
			long mbytes = bytes / (1024 * 1024);
			long rest = ((bytes - (mbytes * (1024 * 1024))) * 100)
					/ (1024 * 1024);
			buff.append(mbytes).append('.');
			if (rest < 10) {
				buff.append('0');
			}
			buff.append(rest).append(" MB");
			return buff.toString();
		} else {
			return ((bytes / 1024) + " KB");
		}
	}
}
