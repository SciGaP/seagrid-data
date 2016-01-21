/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.metcat.registry;

import org.apache.airavata.metcat.registry.util.RegistryProperties;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class SolrRegistryImpl implements IRegistry {
    private final static Logger logger = LoggerFactory.getLogger(SolrRegistryImpl.class);

    private static final String SOLR_SERVER_URL = "solr.server.url";

    private static final String UNIQUE_ID_FIELD = "id";

    public boolean publish(JSONObject jsonObject) throws RegistryException {
        if(jsonObject.get(UNIQUE_ID_FIELD) == null || jsonObject.get(UNIQUE_ID_FIELD).toString().isEmpty()){
            throw new RegistryException("Unique ID " + UNIQUE_ID_FIELD + " not set");
        }
        CloseableHttpClient httpClient = null;
        try {
            String solrServerPubUrl = RegistryProperties.getInstance().getProperty(SOLR_SERVER_URL, "");
            solrServerPubUrl += "/update/json?wt=json";
            httpClient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(solrServerPubUrl);
            StringEntity entity  = new StringEntity("{add: {doc:" + jsonObject.toJSONString() + ",boost:1.0,overwrite:true," +
                    "commitWithin:1000}}", "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = null;
            response = httpClient.execute(post);

            HttpEntity httpEntity = response.getEntity();
            InputStream in = httpEntity.getContent();
            String encoding = httpEntity.getContentEncoding() == null ? "UTF-8" : httpEntity.getContentEncoding().getName();
            encoding = encoding == null ? "UTF-8" : encoding;
            String responseText = IOUtils.toString(in, encoding);
            if(response.getStatusLine().getStatusCode() == 200) {
                logger.info("Published metadata to Solr. Response Text is " + responseText.replaceAll("\n",""));
            }else{
                throw new Exception("Failed to publish data to solr. Response Text is " + responseText);
            }
        } catch (Exception e) {
            new RegistryException(e);
        }finally {
            if(httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
        return true;
    }
}