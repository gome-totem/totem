package org.z.core.es;


import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * embed start es
 * @author dinghb
 *
 */
public class ESEmbededServer {
	
	protected static Logger logger = LoggerFactory.getLogger(ESEmbededServer.class);
	private String ES_HOME,nodeName;
	private Node node;
	
	public ESEmbededServer(String esHome,String nodeName){
		
		this.ES_HOME=esHome;
		this.nodeName=nodeName;
	}
	
	public boolean start(){
		
		node=buildNode();
		return true;
	}
	
	public void close(){
		
		if(!node.isClosed())
			node.close();
	}
	
	private Node buildNode() {
		
        Settings settings = buildNodeSettings();

        Node node = NodeBuilder.nodeBuilder()
		        .settings(settings)
		        
		        .node();
        return node;
    }

    /**
     * Build node settings
     */
    private Settings buildNodeSettings() {

        Builder settingsBuilder = ImmutableSettings.settingsBuilder()
                .put("path.data", ES_HOME + "/data")
                .put("path.work", ES_HOME + "/work")
                .put("path.logs", ES_HOME + "/logs")
                .put("path.logs", ES_HOME + "/plugins")
//                .put("index.number_of_shards", "4")
//                .put("index.number_of_replicas", "0");
//        			 .put("index.analysis.analyzer.default.type", "keyword");
                		;
        if(!StringUtils.isEmpty(this.nodeName)){
        		settingsBuilder.put("node.name",this.nodeName);
        	}

        Settings configSettings = ImmutableSettings.settingsBuilder().loadFromClasspath("elasticsearch.yml").build();
        settingsBuilder.put(configSettings);
        logger.info("node settings:[{}]", new Object[] { settingsBuilder.internalMap() } );
        return settingsBuilder.build();
    }
    
    public static void main(String[] args) {
    	
	 ESEmbededServer server=new ESEmbededServer("/server/es1/","215"+new Date().getTime());
	 
    	server.start();
        while(true){
        	try {
				Thread.sleep(1000000);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        }
	}
	

}
