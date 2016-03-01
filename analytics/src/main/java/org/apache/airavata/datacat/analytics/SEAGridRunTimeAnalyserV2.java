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
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SEAGridRunTimeAnalyserV2 {
    private final static Logger logger = LoggerFactory.getLogger(SEAGridRunTimeAnalyserV2.class);

    public static void main(String[] args) throws JSONException, IOException {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://gw62.iu.xsede.org:27017"));
        DB mongoDB = mongoClient.getDB("datacat");
        DBCollection collection = mongoDB.getCollection("gridchem");

        HashMap<String, Map.Entry<String, Integer>> uniqueKeywordMap = new HashMap<>();
        int documentCount = 0;
        DBObject allQuery = new BasicDBObject();
        DBObject removeIdProjection = new BasicDBObject("_id", 0);
        DBCursor cursor = collection.find(allQuery, removeIdProjection)
                .limit(1000);

        while (cursor.size() > 0) {
            documentCount += cursor.size();
            for (DBObject document : cursor) {
                JSONObject object = new JSONObject(document.toString());
                uniqueKeywordMap.putAll(getKeywords(object));
            }
            cursor = collection.find(allQuery, removeIdProjection)
                    .skip(documentCount)
                    .limit(1000);
        }

        System.out.println("number of documents : " + documentCount);
        System.out.println("number of unique keywords : " + uniqueKeywordMap.size());
        Set<Map.Entry<String, Map.Entry<String, Integer>>> sortedSet = new TreeSet<>((o1, o2) -> {
            return o1.getKey().toString().compareTo(o2.getKey().toString());
        });
        sortedSet.addAll(uniqueKeywordMap.entrySet());
        int i = 1;
        for (Map.Entry<String,Map.Entry<String,Integer>> entry : sortedSet) {
            if(!entry.getKey().toString().contains(".")) {
                System.out.println(i + " " + entry.getKey() + " : " + entry.getValue().getValue() + " : " + entry.getValue().getKey());
                i++;
            }
        }

        //generating feature matrix
        BufferedWriter writer = new BufferedWriter(new FileWriter("gaussian-runtime-v2.csv"));
        ArrayList<String> keywordOrderList = new ArrayList<>();
        for (Map.Entry<String, Map.Entry<String, Integer>> entry : sortedSet) {
            if (!entry.getKey().contains("runtime") && !entry.getKey().contains(".")) {
                keywordOrderList.add(entry.getKey());
            }
        }
        keywordOrderList.add("runtime");

        documentCount = 0;
        writer.write("id," + StringUtils.join(keywordOrderList, ','));
        cursor = collection.find(allQuery, removeIdProjection)
                .limit(1000);
        while (cursor.size() > 0) {
            documentCount += cursor.size();
            for (DBObject document : cursor) {
                JSONObject object = new JSONObject(document.toString());
                ArrayList<String> featureVector = getFeatureVector(keywordOrderList, object);
                //Skip records which does not have the runtime associated
                if(!featureVector.get(featureVector.size()-1).equalsIgnoreCase("0")){
                    writer.write("\n"+object.get("Id")+ "," + StringUtils.join(featureVector, ','));
                }
            }
            cursor = collection.find(allQuery, removeIdProjection)
                    .skip(documentCount)
                    .limit(1000);
        }
        writer.close();
    }

    public static ArrayList<String> getFeatureVector(ArrayList<String> keywordsOrder, JSONObject object) throws JSONException {
        HashMap<String, Map.Entry<String, Integer>> keywords = getKeywords(object);
        ArrayList<String> returnList = new ArrayList<>();
        for (String k : keywordsOrder) {
            if (keywords.get(k) != null) {
                returnList.add(keywords.get(k).getValue() + "");
            } else {
                returnList.add("0");
            }
        }
        return returnList;
    }

    public static HashMap<String, Map.Entry<String, Integer>> getKeywords(JSONObject object) throws JSONException {
        HashMap<String, Map.Entry<String, Integer>> uniqueKeywordMap = new HashMap<>();
        if (object.has("Calculation") && ((JSONObject) object.get("Calculation")).has("CalcType")) {
            String calcTypeString = ((JSONObject) object.get("Calculation")).get("CalcType").toString();
            if (calcTypeString != null && !calcTypeString.isEmpty()) {
                calcTypeString = calcTypeString.toLowerCase().trim();
                String[] t1 = calcTypeString.split(";");
                for (String t2 : t1) {
                    String[] t3 = t2.split(" ");
                    for (String t4 : t3) {
                        if(!t4.trim().isEmpty()) {
                            if (uniqueKeywordMap.get(t4) != null) {
                                uniqueKeywordMap.put(t4,
                                        new AbstractMap.SimpleEntry<>(object.get("Id").toString(), uniqueKeywordMap.get(t4).getValue() + 1));
                            } else {
                                uniqueKeywordMap.put(t4, new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 1));
                            }
                        }
                    }
                }
            }
        }
//
//        if (object.has("Calculation") && ((JSONObject) object.get("Calculation")).has("Methods")) {
//            String methodsString = ((JSONObject) object.get("Calculation")).get("Methods").toString();
//            if (methodsString != null && !methodsString.isEmpty()) {
//                methodsString = methodsString.toLowerCase().trim();
//                String[] t1 = methodsString.split(";");
//                for (String t2 : t1) {
//                    String[] t3 = t2.split(" ");
//                    for (String t4 : t3) {
//                        if(!t4.trim().isEmpty()) {
//                            if (uniqueKeywordMap.get(t4) != null) {
//                                uniqueKeywordMap.put(t4,
//                                        new AbstractMap.SimpleEntry<>(object.get("Id").toString(), uniqueKeywordMap.get(t4).getValue() + 1));
//                            } else {
//                                uniqueKeywordMap.put(t4, new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 1));
//                            }
//                        }
//                    }
//                }
//            }
//        }

//        if (object.has("Calculation") && ((JSONObject) object.get("Calculation")).has("Basis")) {
//            String basisSetString = ((JSONObject) object.get("Calculation")).get("Basis").toString();
//            if (basisSetString != null && !basisSetString.isEmpty()) {
//                basisSetString = basisSetString.toLowerCase().trim();
//                String[] t1 = basisSetString.split(";");
//                for (String t2 : t1) {
//                    String[] t3 = t2.split(" ");
//                    for (String t4 : t3) {
//                        if(!t4.trim().isEmpty()) {
//                            if (uniqueKeywordMap.get(t4) != null) {
//                                uniqueKeywordMap.put(t4,
//                                        new AbstractMap.SimpleEntry<>(object.get("Id").toString(), uniqueKeywordMap.get(t4).getValue() + 1));
//                            } else {
//                                uniqueKeywordMap.put(t4, new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 1));
//                            }
//                        }
//                    }
//                }
//            }
//        }

        if (object.has("Calculation") && ((JSONObject) object.get("Calculation")).has("NBasis")) {
            String nBasisString = ((JSONObject) object.get("Calculation")).get("NBasis").toString();
            if (nBasisString != null && !nBasisString.isEmpty()) {
                Integer nbasis = Integer.parseInt(nBasisString.trim());
                uniqueKeywordMap.put("nbasis", new AbstractMap.SimpleEntry<>(object.get("Id").toString(), nbasis));
            }
        }

        //Additional parameters from the molecule, execution environment and the target value job runtime
        if (object.has("Molecule") && ((JSONObject) object.get("Molecule")).has("NAtom")) {
            String nAtomsString = ((JSONObject) object.get("Molecule")).get("NAtom").toString();
            if (nAtomsString != null && !nAtomsString.isEmpty()) {
                uniqueKeywordMap.put("natoms", new AbstractMap.SimpleEntry<>(object.get("Id").toString(),
                        Integer.parseInt(nAtomsString.trim())));
            }
        }

        if (object.has("ExecutionEnvironment") && ((JSONObject) object.get("ExecutionEnvironment")).has("CalcMachine")) {
            String comMachString = ((JSONObject) object.get("ExecutionEnvironment")).get("CalcMachine").toString();
            if (comMachString != null && !comMachString.isEmpty()) {
                if (comMachString.toLowerCase().contains("trestles")) {
                    uniqueKeywordMap.put("machine",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 1));
                } else if (comMachString.toLowerCase().contains("gcn")) {
                    uniqueKeywordMap.put("machine",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 2));
                } else if (comMachString.toLowerCase().contains("comet")) {
                    uniqueKeywordMap.put("machine",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 3));
                }
            }
        }

        if (object.has("ExecutionEnvironment") && ((JSONObject) object.get("ExecutionEnvironment")).has("Memory")) {
            String memory = ((JSONObject) object.get("ExecutionEnvironment")).get("Memory").toString();
            if (memory != null && !memory.isEmpty()) {
                memory = memory.toLowerCase();
                if (memory.endsWith("gw")) {
                    uniqueKeywordMap.put("memory",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(),
                                    Integer.parseInt(memory.substring(0, memory.length() - 2)) * 1000 * 8));
                } else if (memory.endsWith("gb")) {
                    uniqueKeywordMap.put("memory",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(),
                                    Integer.parseInt(memory.substring(0, memory.length() - 2)) * 1000));
                } else if (memory.endsWith("mw")) {
                    uniqueKeywordMap.put("memory",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(),
                                    Integer.parseInt(memory.substring(0, memory.length() - 2)) * 8));
                } else if (memory.endsWith("mb")) {
                    uniqueKeywordMap.put("memory",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(),
                                    Integer.parseInt(memory.substring(0, memory.length() - 2))));
                }else{
                    uniqueKeywordMap.put("memory",
                            new AbstractMap.SimpleEntry<>(object.get("Id").toString(), 256));
                }
            }
        }

        if (object.has("ExecutionEnvironment") && ((JSONObject) object.get("ExecutionEnvironment")).has("NProcShared")) {
            String processCount = ((JSONObject) object.get("ExecutionEnvironment")).get("NProcShared").toString();
            if (processCount != null && !processCount.isEmpty()) {
                uniqueKeywordMap.put("nproc",
                        new AbstractMap.SimpleEntry<>(object.get("Id").toString(), Integer.parseInt(processCount.trim())));
            }
        }

        if (object.has("ExecutionEnvironment") && ((JSONObject) object.get("ExecutionEnvironment")).has("JobCPURunTime")) {
            String jobRunTime = ((JSONObject) object.get("ExecutionEnvironment")).get("JobCPURunTime").toString();
            if (jobRunTime != null && !jobRunTime.isEmpty()) {
                uniqueKeywordMap.put("runtime",
                        new AbstractMap.SimpleEntry<>(object.get("Id").toString(), (int) Double.parseDouble(jobRunTime.trim())));
            }
        }

        return uniqueKeywordMap;
    }
}