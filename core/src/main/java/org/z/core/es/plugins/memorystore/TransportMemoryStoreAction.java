/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.z.core.es.plugins.memorystore;



import com.mongodb.BasicDBList;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.*;

import java.util.ArrayList;
import java.util.Map;
/**
 * 
 * @author dinghb
 *
 */
public class TransportMemoryStoreAction extends TransportAction<MemoryStoreRequest, MemoryStoreResponse> {

    private final TransportService transportService;

    private final ClusterService clusterService;
    
    
    @Inject
    protected TransportMemoryStoreAction(Settings settings,
            ThreadPool threadPool,
            TransportService transportService,
            ClusterService clusterService) {
        super(settings,MemoryStoreAction.NAME, threadPool);
        this.transportService=transportService;
        this.clusterService=clusterService;
          
        transportService.registerHandler(MemoryStoreAction.NAME, new TransportHandler());
        transportService.registerHandler(MemoryStoreAction.NAME+"/otherNodes", new TransportNodeMemoryStoreHandler());
    }

    @Override
    protected void doExecute(MemoryStoreRequest request, ActionListener<MemoryStoreResponse> listener) {
   
        this.saveLocal(request);
       
        DiscoveryNodes nodes = clusterService.state().nodes();
           for(DiscoveryNode node:nodes){
               if(node.id().equals(nodes.localNodeId()))
                  continue;
               
               transportService.sendRequest(node, MemoryStoreAction.NAME+"/otherNodes", request,  new BaseTransportResponseHandler<MemoryStoreResponse>() {

                @Override
                public MemoryStoreResponse newInstance() {
                    return new MemoryStoreResponse();
                }

                @Override
                public void handleResponse(MemoryStoreResponse response) {
                }

                @Override
                public void handleException(TransportException exp) {
                }

                @Override
                public String executor() {
                    return ThreadPool.Names.SAME;
                }
                   
               });
           }
        
        MemoryStoreResponse response =new MemoryStoreResponse();
        response.setBackData(new StringBuffer().append("NodeId:").append(nodes.localNodeId())
        		.append(" dragonMap Size： ").append(newDragonDict.dragonMap.size())
        		.append("dragonCMap size:").append(newDragonDict.dragonCMap.size()).toString());
        listener.onResponse(response);
    }
    
    private class TransportHandler extends BaseTransportRequestHandler<MemoryStoreRequest> {

        public MemoryStoreRequest newInstance() {
            return new MemoryStoreRequest();
        }

        public String executor() {
            return ThreadPool.Names.SAME;
        }

        public void messageReceived(MemoryStoreRequest request, final TransportChannel channel) throws Exception {
            // no need to have a threaded listener since we just send back a response
//            request.listenerThreaded(false);
            doExecute(request, new ActionListener<MemoryStoreResponse>() {

                public void onResponse(MemoryStoreResponse result) {
                    try {
                        channel.sendResponse(result);
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }
                public void onFailure(Throwable e) {
                    try {
                        channel.sendResponse(e);
                    } catch (Exception e1) {
                        logger.warn("Failed to send response for get", e1);
                    }
                }
            });
        }
    }
    

    private class TransportNodeMemoryStoreHandler extends BaseTransportRequestHandler<MemoryStoreRequest> {

        public MemoryStoreRequest newInstance() {
            return new MemoryStoreRequest();
        }

        public String executor() {
            return ThreadPool.Names.SAME;
        }

        public void messageReceived(MemoryStoreRequest request, final TransportChannel channel) throws Exception {
            
            saveLocal(request);
            channel.sendResponse(new MemoryStoreResponse());
        }
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void  saveLocal(MemoryStoreRequest request){
    	   try {
               XContentParser parser = XContentHelper.createParser(request.source);
               Map m=parser.map();
               Map<String,String> dragonMapSrc=(Map<String,String>)m.get("dragonMap");
               if(dragonMapSrc.size()>0){
                   for(Map.Entry<String, String> entry:dragonMapSrc.entrySet()){
                       String key =entry.getKey();
                       if(key.lastIndexOf("n")==-1){
                           newDragonDict.dragonMap.remove(key);
                           newDragonDict.dragonCMap.remove(key);
                       }else{
                           newDragonDict.dragonMap.remove(key.substring(0,key.length()-2));
                             }
//                       logger.info("put dragonMap size {}",new Object[]{dragonMapSrc.size()});
                       newDragonDict.dragonMap.putAll(dragonMapSrc);
                            
                        }
                   }
               Map<String,ArrayList> dragonCMapSrc=(Map<String,ArrayList>)m.get("dragonCMap");
               if(dragonCMapSrc.size()>0){
            	   for(Map.Entry<String, ArrayList> entry:dragonCMapSrc.entrySet()){
                       BasicDBList values=new BasicDBList();
                       values.addAll(entry.getValue());
                       newDragonDict.dragonCMap.put(entry.getKey(),values);
                         }
//                   logger.info("put dragonCMap size {}",new Object[]{dragonCMapSrc.size()});
                   }
                  

           } catch (Exception e) {
               logger.warn("parse source or save to memeory fail",e);
           }
    }

}
