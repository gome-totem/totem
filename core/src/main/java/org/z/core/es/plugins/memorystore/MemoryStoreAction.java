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

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
/**
 * 
 * @author dinghb
 *
 */
public class MemoryStoreAction extends Action<MemoryStoreRequest, MemoryStoreResponse,MemoryStoreRequestBuilder,Client>{

    public static final MemoryStoreAction INSTANCE = new MemoryStoreAction();
    public static final String NAME = "memoryStore";
    
    private MemoryStoreAction() {
        super(NAME);
    }

    @Override
    public MemoryStoreRequestBuilder newRequestBuilder(Client client) {
        return new MemoryStoreRequestBuilder(client,null);
    }

    @Override
    public MemoryStoreResponse newResponse() {
        return new MemoryStoreResponse();
    }
    
    

  

}
