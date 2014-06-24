package org.z.global.evernbus;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import com.google.common.collect.MapMaker;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class EverntBusPool {
	
	private   ConcurrentMap<String, EventBus> busMap =new MapMaker().makeMap();
	private   ConcurrentMap<String, AsyncEventBus> synBusMap =new MapMaker().makeMap();
	public void buderBus(String busName){
		EventBus evernt=new EventBus();
		busMap.put(busName, evernt);
	}
	public static EverntBusPool Build(){
		EverntBusPool pool = new EverntBusPool();
		return pool;
	}
	public void buderSynBus(String busName,int ThreadNumber){
		AsyncEventBus bus = new AsyncEventBus(Executors.newFixedThreadPool(ThreadNumber));
		synBusMap.put(busName, bus);
	}
	
	public boolean  regester(String busName,Object obj){
		EventBus bus = busMap.get(busName);
		if(bus==null)return false;
		bus.register(obj);
		return true;
	}
	public boolean regesterSyn(String busName,Object obj){
		AsyncEventBus bus = synBusMap.get(busName);
		if(bus==null)return false;
		bus.register(obj);
		return true;
	}
}
