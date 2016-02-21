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

    private static final String MONGO_SERVER_URL = "mongo.server.url";
    private static final String MONGO_DB_NAME = "mongo.db.name";
    private static final String MONGO_COLLECTION_NAME = "mongo.collection.name";
    private static final String MONGO_PRIMARY_FIELD = "mongo.primary.field";

    private String primaryKey = RegistryProperties.getInstance().getProperty(MONGO_PRIMARY_FIELD,"id");
    private String mongoServerUrl = RegistryProperties.getInstance().getProperty(MONGO_SERVER_URL, "");
    private String mongoDBName = RegistryProperties.getInstance().getProperty(MONGO_DB_NAME, "datacat-db");
    private String mongoCollectionName = RegistryProperties.getInstance().getProperty(MONGO_COLLECTION_NAME, "datacat-collection");

    private MongoClient mongoClient = null;
    private DB mongoDB;
    private DBCollection collection;

    public MongoRegistryImpl(){
        mongoClient = new MongoClient(new MongoClientURI(mongoServerUrl));
        mongoDB = mongoClient.getDB(mongoDBName);
        collection = mongoDB.getCollection(mongoCollectionName);
//        collection.remove(new BasicDBObject());
        collection.dropIndexes();
        initIndexes();
        logger.debug("New Mongo Client created with [" + mongoServerUrl + "]");
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
        //Default mongodb primary key
        jsonObject.put("_id", jsonObject.get(primaryKey));
        try {
            collection.save((DBObject) JSON.parse(jsonObject.toString()));
            logger.debug("Created new record " + primaryKey + ":" + jsonObject.get(primaryKey));
        } catch (Exception e) {
            throw new RegistryException(e);
        }
        return true;
    }

    @Override
    public List<JSONObject> select(String q) throws RegistryException {
        List<JSONObject> result = new ArrayList<>();
        DBObject allQuery = new BasicDBObject();
        DBObject removeIdProjection = new BasicDBObject("_id", 0);
        //FIXME Limit
        DBCursor cursor = collection.find(allQuery, removeIdProjection).limit(10);
        for(DBObject document: cursor){
            result.add(new JSONObject(document.toString()));
        }
        return result;
    }

    @Override
    public JSONObject get(String id) throws RegistryException {
        DBObject document = collection.findOne(new BasicDBObject(primaryKey, id));
        return new JSONObject(document.toMap());
    }
}