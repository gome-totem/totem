package org.z.core.es.plugins.memorystore;


import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
/**
 * 
 * @author dinghb
 *
 */
public class MemoryStorePlugin extends AbstractPlugin{

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "memoryStore";
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "memoryStore";
	}
	@Override public void processModule(Module module) {
        if (module instanceof ActionModule) {
        	
          	String isClient=Config.getField("isClient" ,"false");
          	if(isClient.equals("false")) 
          		newDragonDict.init();
          	else{
          		System.out.println(newDragonDict.dragonMap.size());
          	}
            ActionModule actionModule = (ActionModule) module;
            actionModule.registerAction(MemoryStoreAction.INSTANCE, TransportMemoryStoreAction.class);
//            try {
//            	   Class clasz=Class.forName("org.x.server.es.plugins.memorystore.MemoryStoreAction");
//            	   Action obj=(Action)clasz.newInstance();
//            	   Class clasz1=Class.forName("org.x.server.es.plugins.memorystore.TransportMemoryStoreAction");
//            	   actionModule.registerAction(obj, clasz1);
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
             
           }
    }
}
