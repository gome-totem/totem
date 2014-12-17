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

import com.mongodb.*;
import com.mongodb.MongoClientOptions.Builder;
import org.apache.log4j.Logger;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
/**
 * 
 * @author dinghb
 *
 */
public class MongodbUtils extends AbstractComponent{
    
    private static Logger logger=Logger.getLogger(MongodbUtils.class.getName());
    
    public MongodbUtils(Settings settings) {
        super(settings);
    }
    private static MongoClient mongo=null;
    private static DB db=null;
     
     static{
         try {
             mongo=new MongoClient(new ServerAddress(Config.getField("db.ip","10.58.50.137")
                     ,Integer.parseInt(Config.getField("db.port","19753"))) , getClientOptions());
             db=mongo.getDB(Config.getField("db.name","dragon"));
        } catch (Exception e) {
            logger.error("connect to mongodb fail");
        }
        
     }
    @SuppressWarnings("deprecation")
	private static MongoClientOptions getClientOptions(){
        MongoClientOptions mongoClientOptions = null;
        try {
            Builder builder = null;
           
            builder = MongoClientOptions.builder();
            builder.autoConnectRetry(true);
            builder.connectionsPerHost(100);
            builder.threadsAllowedToBlockForConnectionMultiplier(50);
            builder.maxWaitTime(10000);
            builder.connectTimeout(3000);
            mongoClientOptions = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mongoClientOptions;
    }
    
    public static DBCollection getColl(String collName){
        return db.getCollection(collName);
    } 

}
