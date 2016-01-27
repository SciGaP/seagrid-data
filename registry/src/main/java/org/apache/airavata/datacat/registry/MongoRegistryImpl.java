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
package org.apache.airavata.datacat.registry;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.airavata.datacat.registry.util.RegistryProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoRegistryImpl implements IRegistry {
    private final static Logger logger = LoggerFactory.getLogger(MongoRegistryImpl.class);

    private static final String MONGO_SERVER_HOST = "mongo.server.host";
    private static final String MONGO_SERVER_PORT = "mongo.server.port";
    private static final String MONGO_DB_NAME = "mongo.db.name";
    private static final String MONGO_COLLECTION_NAME = "mongo.collection.name";
    private static final String MONGO_PRIMARY_FIELD = "mongo.primary.field";

    private String primaryKey = RegistryProperties.getInstance().getProperty(MONGO_PRIMARY_FIELD,"id");
    private String mongoHost = RegistryProperties.getInstance().getProperty(MONGO_SERVER_HOST, "localhost");
    private Integer mongoPort = Integer.parseInt(RegistryProperties.getInstance().getProperty(MONGO_SERVER_PORT, "27017"));
    private String mongoDBName = RegistryProperties.getInstance().getProperty(MONGO_DB_NAME, "datacat-db");
    private String mongoCollectionName = RegistryProperties.getInstance().getProperty(MONGO_COLLECTION_NAME, "datacat-collection");

    private MongoClient mongoClient = null;
    private DB mongoDB;
    private DBCollection collection;

    public MongoRegistryImpl(){
        mongoClient = new MongoClient(mongoHost, mongoPort);
        logger.debug("New Mongo Client created with [" + mongoHost + "] and [" + mongoPort + "]");
        mongoDB = mongoClient.getDB(mongoDBName);
        collection = mongoDB.getCollection(mongoCollectionName);
        collection.dropIndexes();
        initIndexes();
    }

    /**
     * If indexes are already defined this will simply ignore them
     */
    private void initIndexes() {
        if(!primaryKey.equals("_id")) { //Not using the default MongoDB primary key
            collection.createIndex(new BasicDBObject(primaryKey, 1), new BasicDBObject("unique", true));
        }
    }

    @Override
    public boolean create(JSONObject jsonObject) throws RegistryException {
        if(jsonObject.get(primaryKey) == null || jsonObject.get(primaryKey).toString().isEmpty()){
            throw new RegistryException("Primary Key " + primaryKey + " not set");
        }
        try {
            DBObject criteria = new BasicDBObject(primaryKey, jsonObject.get(primaryKey));
            DBObject doc = collection.findOne(criteria);
            if (doc != null) {
                DBObject query = BasicDBObjectBuilder.start().add(primaryKey, jsonObject.get(primaryKey)).get();
                collection.update(query, (DBObject) JSON.parse(jsonObject.toString()));
                logger.debug("Updated existing record " + primaryKey + ":" + jsonObject.get(primaryKey));
            }else{
                collection.insert((DBObject) JSON.parse(jsonObject.toString()));
                logger.debug("Created new record " + primaryKey + ":" + jsonObject.get(primaryKey));
            }
        } catch (Exception e) {
            throw new RegistryException(e);
        }
        return true;
    }

    @Override
    public List<JSONObject> select(String q) throws RegistryException {
        List<JSONObject> result = new ArrayList<>();
        DBCursor cursor = collection.find();
        for(DBObject document: cursor){
            result.add(new JSONObject(document.toString()));
        }
        return result;
    }
}