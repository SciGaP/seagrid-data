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
package org.apache.airavata.datacat.analytics;

import com.mongodb.*;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SEAGridRunTimeAnalyserV3 {
    private final static Logger logger = LoggerFactory.getLogger(SEAGridRunTimeAnalyserV3.class);

    public static void main(String[] args) throws JSONException, IOException {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://gw62.iu.xsede.org:27017"));
        DB mongoDB = mongoClient.getDB("datacat");
        DBCollection collection = mongoDB.getCollection("gridchem");

        int documentCount = 0;
        DBObject allQuery = new BasicDBObject();
        DBObject removeIdProjection = new BasicDBObject("_id", 0);
        DBCursor cursor = collection.find(allQuery, removeIdProjection)
                .limit(1000);

        //generating feature matrix
        BufferedWriter writer = new BufferedWriter(new FileWriter("gaussian-runtime-v3.csv"));
        writer.write("id,calc-types,methods,basis-sets,basis-functions,link-0-commands,number-of-atoms,machine,nproc,memory,runtime\n");
        while (cursor.size() > 0) {
            documentCount += cursor.size();
            for (DBObject document : cursor) {
                JSONObject object = new JSONObject(document.toString());
                writeLine(writer, object);
            }
            cursor = collection.find(allQuery, removeIdProjection)
                    .skip(documentCount)
                    .limit(1000);
        }
        writer.close();
    }

    public static void writeLine(BufferedWriter writer, JSONObject object) throws JSONException, IOException {
        writer.write(object.get("Id").toString()+",");
        if(object.has("Calculation") && ((JSONObject)object.get("Calculation")).has("CalcType")){
            writer.write("\"" + ((JSONObject) object.get("Calculation")).get("CalcType").toString().trim().toLowerCase() + "\"");
        }
        writer.write(",");
        if(object.has("Calculation") && ((JSONObject)object.get("Calculation")).has("Methods")){
            writer.write("\"" + ((JSONObject) object.get("Calculation")).get("Methods").toString().trim().toLowerCase() + "\"");
        }
        writer.write(",");
        if(object.has("Calculation") && ((JSONObject)object.get("Calculation")).has("Basis")){
            writer.write("\"" + ((JSONObject) object.get("Calculation")).get("Basis").toString().trim().toLowerCase() + "\"");
        }
        writer.write(",");
        if(object.has("Calculation") && ((JSONObject)object.get("Calculation")).has("NBasis")){
            writer.write(((JSONObject) object.get("Calculation")).get("NBasis").toString().trim());
        }
        writer.write(",");
        if(object.has("InputFileConfiguration") && ((JSONObject)object.get("InputFileConfiguration")).has("RouteCommands")){
            writer.write("\"" + ((JSONObject) object.get("InputFileConfiguration")).get("RouteCommands").toString().trim().toLowerCase() + "\"");
        }
        writer.write(",");
        if(object.has("Molecule") && ((JSONObject)object.get("Molecule")).has("NAtom")){
            writer.write(((JSONObject) object.get("Molecule")).get("NAtom").toString().trim());
        }
        writer.write(",");
        if(object.has("ExecutionEnvironment") && ((JSONObject)object.get("ExecutionEnvironment")).has("CalcMachine")){
            writer.write("\"" + ((JSONObject) object.get("ExecutionEnvironment")).get("CalcMachine").toString().trim()
                    .toLowerCase()+ "\"");
        }
        writer.write(",");
        if(object.has("ExecutionEnvironment") && ((JSONObject)object.get("ExecutionEnvironment")).has("NProcShared")){
            writer.write(((JSONObject) object.get("ExecutionEnvironment")).get("NProcShared").toString().trim()
                    .toLowerCase());
        }
        writer.write(",");
        if(object.has("ExecutionEnvironment") && ((JSONObject)object.get("ExecutionEnvironment")).has("Memory")){
            writer.write(((JSONObject) object.get("ExecutionEnvironment")).get("Memory").toString().trim()
                    .toLowerCase());
        }
        writer.write(",");
        if(object.has("ExecutionEnvironment") && ((JSONObject)object.get("ExecutionEnvironment")).has("ActualJobRunTime")){
            writer.write(((JSONObject) object.get("ExecutionEnvironment")).get("ActualJobRunTime").toString().trim()
                    .toLowerCase());
        }
        writer.write("\n");
    }
}