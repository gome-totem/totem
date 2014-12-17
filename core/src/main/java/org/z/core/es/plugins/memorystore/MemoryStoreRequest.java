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

import org.elasticsearch.ElasticsearchGenerationException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.replication.IndicesReplicationOperationRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author dinghb
 *
 */
public class MemoryStoreRequest extends IndicesReplicationOperationRequest<MemoryStoreRequest>{

    private static final XContentType contentType = Requests.CONTENT_TYPE;
    public BytesReference source;
    private boolean sourceUnsafe;
    
    
    public MemoryStoreRequest source(Map source) throws ElasticsearchGenerationException {
        return source(source, contentType);
    }
    
    public MemoryStoreRequest source(Map source, XContentType contentType) throws ElasticsearchGenerationException {
        try {
            XContentBuilder builder = XContentFactory.contentBuilder(contentType);
            builder.map(source);
            return source(builder);
        } catch (IOException e) {
            throw new ElasticsearchGenerationException("Failed to generate [" + source + "]", e);
        }
    }
    
    public MemoryStoreRequest source(XContentBuilder sourceBuilder) {
        source = sourceBuilder.bytes();
        sourceUnsafe = false;
        return this;
    }
    @Override
    public ActionRequestValidationException validate() {
        return null;
    }
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        source = in.readBytesReference();
        sourceUnsafe = false;
    }
    
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBytesReference(source);
    }
    public static void main(String[] args) {
        MemoryStoreRequest request=new MemoryStoreRequest();
        Map map=new HashMap();
        List<String>  list=new ArrayList<String>();list.add("333");
        map .put("1", "2222");
        map.put("2", list);
        request.source(map);
        System.out.println(request.source);
        
        try {
            XContentParser parser = XContentHelper.createParser(request.source);
            Map m=parser.map();
               System.out.println(m.get("2"));
//            for (XContentParser.Token token = parser.nextToken(); token != XContentParser.Token.END_OBJECT; token = parser.nextToken()) {
//                System.out.println(token.toString());
//            }
        } catch (Exception e) {
           e.printStackTrace();
        }
       
         
    }
    
    
}
