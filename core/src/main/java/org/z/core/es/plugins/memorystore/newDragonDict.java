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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
/**
 * @author dinghb
 */
public class newDragonDict {
    static ESLogger logger = ESLoggerFactory.getLogger("[dragon dict]","memeory.store");
    public static ConcurrentMap<String, String> dragonMap = new ConcurrentHashMap<String, String>();
    public static ConcurrentMap<String, BasicDBList> dragonCMap = new ConcurrentHashMap<String, BasicDBList>();
    
    public static void init(){
        logger.info(" init ...");
        DBCursor stock=MongodbUtils.getColl(Config.getField("db.dragon.coll","stock")).find(new BasicDBObject().append("c", "all"));
        
        BasicDBObject st = null;
        while (stock.hasNext()) {
            st = (BasicDBObject) stock.next();
            dragonMap.put(st.getString("s").trim(), "1");
        }
        DBCursor stockn=MongodbUtils.getColl(Config.getField("db.dragon.coll","stock"))
                .find(new BasicDBObject().append("c", new BasicDBObject().append("$ne", "all")),new BasicDBObject().append("s", 1));
        while (stockn.hasNext()) {
            st = (BasicDBObject) stockn.next();
            dragonMap.put(st.getString("s").trim()+"n", "1");
        }
       
        logger.info(" init done! size: "+dragonMap.size()+" collection "+Config.getField("db.dragon.coll","stock"));
    }
    public static int getStock(String sku, String city) {
        Object c = null;
        BasicDBList citys = null;
        DBObject obj =MongodbUtils.getColl(Config.getField("db.dragon.coll","stock")).findOne(new BasicDBObject().append("s", sku));
        if (obj == null) {
            dragonMap.put(sku, "0");
            return 0;
        }
        c = obj.get("c");
        if (c instanceof String) {
            dragonMap.put(sku, "1");
            return 1;
        } else {
            citys = (BasicDBList) c;
            dragonCMap.put(sku, citys);
            dragonMap.put(new StringBuffer(sku).append("n").toString() ,"1");
        }
        return citys.contains(city) ? 1 : 0;
    }
    
    public static void main(String[] args) {
    }

}
