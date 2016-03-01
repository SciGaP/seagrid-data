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

public class SEAGridRunTimeAnalyserV1 {
    private final static Logger logger = LoggerFactory.getLogger(SEAGridRunTimeAnalyserV1.class);

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

//        System.out.println("number of documents : " + documentCount);
//        System.out.println("number of unique keywords : " + uniqueKeywordMap.size());
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
        BufferedWriter writer = new BufferedWriter(new FileWriter("gaussian-runtime.csv"));
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
        String routeString = ((JSONObject) object.get("Calculation")).get("Keywords").toString();
        if (routeString != null && !routeString.isEmpty()) {
            routeString = preProcessString(routeString);
            routeString = unifyDifferentForms(routeString);
            routeString = reduceKeywordsToBaseForm(routeString);
            routeString = removeInBetweenCommas(routeString);
            routeString = routeString.trim();
            String[] keywords = routeString.split(" ");
            for (String keyword : keywords) {
                String[] explodedKeys = explode(keyword.trim());
                for (String expldKey : explodedKeys) {
                    if (uniqueKeywordMap.get(expldKey) != null) {
                        uniqueKeywordMap.put(expldKey,
                                new AbstractMap.SimpleEntry<>(routeString, uniqueKeywordMap.get(expldKey).getValue() + 1));
                    } else {
                        uniqueKeywordMap.put(expldKey, new AbstractMap.SimpleEntry<>(routeString, 1));
                    }
                    //System.out.print(expldKey + " ");
                }
            }
            //System.out.print(" -> " + routeString);
            //System.out.println();
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

    public static String preProcessString(String routeString) {
        //Various pre processing steps
        if (routeString.contains(";")) {
            routeString = routeString.split(";")[0];
        }
        // a\nb -> ab
        routeString = routeString.replaceAll("\n", "");
        // AbC -> abc
        routeString = routeString.toLowerCase();
        routeString = routeString.replaceAll("#", "");
        // #p -> p
        routeString = routeString.replaceAll(" +", " ");
        // a  b -> a b
        routeString = routeString.replaceAll(" , ", ",");
        // , a -> ,a
        routeString = routeString.replaceAll(", ", ",");
        // a , -> a,
        routeString = routeString.replaceAll(" ,", ",");
        //a ) -> a)
        routeString = routeString.replaceAll(" \\)", ")");
        //( a -> (a
        routeString = routeString.replaceAll("\\( ", "(");
        //very tight -> very_tight
        routeString = routeString.replaceAll("very tight", "verytight");
        //...noramanscf...
        routeString = routeString.replaceAll("noramanscf", "noraman scf");
        //...noramantd...
        routeString = routeString.replaceAll("noramantd", "noraman td");
        //...noramanmp2...
        routeString = routeString.replaceAll("noramanump2", "noraman mp2");
        //noramanump2 -> noraman ump2
        routeString = routeString.replaceAll("noramanump2", "noraman ump2");
        //numerscf -> numer scf
        routeString = routeString.replaceAll("numerscf", "numer scf");
        //ramanscf -> raman scf
        routeString = routeString.replaceAll("ramanscf", "raman scf");
        //readscf -> read scf
        routeString = routeString.replaceAll("readscf", "read scf");
        //aug-cc-pvdzscf -> aug-cc-pvdz scf
        routeString = routeString.replaceAll("aug-cc-pvdzscf", "aug-cc-pvdz scf");
        //aug-cc-pvtzscf -> aug-cc-pvtz scf
        routeString = routeString.replaceAll("aug-cc-pvtzscf", "aug-cc-pvtz scf");
        //)a -> ) a
        routeString = routeString.replaceAll("(\\))([a-z])", "$1 $2");

        return routeString;
    }

    public static String unifyDifferentForms(String routeString) {
        //Unifying different forms of the same keyword
        //maxcyle,maxcycl -> maxcyc
        routeString = routeString.replaceAll("maxcyc?[cl|cle]", "maxcyc");
        //anharmonic -> anharm
        routeString = routeString.replaceAll("anharmonic", "anharm");
        //maxcyce->maxcyc
        routeString = routeString.replaceAll("maxcyce", "maxcyc");
        //symm -> sym
        routeString = routeString.replaceAll("symm", "sym");
        //modredundant -> modredun
        routeString = routeString.replaceAll("modredundant", "modredun");
        return routeString;
    }

    public static String removeInBetweenCommas(String routeString){
        routeString = routeString.replaceAll("6-311g\\(d,p\\)", "6-311(dp)");
        routeString = routeString.replaceAll("6-311\\+\\+g\\(d,p\\)", "6-311++g(dp)");
        routeString = routeString.replaceAll("6-311\\+g\\(2d,p\\)", "6-311+g(2dp)");
        routeString = routeString.replaceAll("6-311\\+g\\(3df,p\\)", "6-311+g(3dfp)");
        routeString = routeString.replaceAll("6-311\\+g\\(d,p\\)", "6-311+g(dp)");
        routeString = routeString.replaceAll("6-31g\\(d,p\\)", "6-31g(dp)");
        routeString = routeString.replaceAll("6-31\\+\\+g\\(d,p\\)", "6-31++g(dp)");
        routeString = routeString.replaceAll("6-31\\+g\\(d,p\\)", "6-31+g(dp)");
        routeString = routeString.replaceAll("6-31\\(d,p\\)", "6-31(dp)");

        return routeString;
    }

    public static String reduceKeywordsToBaseForm(String routeString) {
        //Shrinking the keyword space by reducing keywords to their base form
        routeString = routeString.replaceAll("3-21g", "3-21");
        routeString = routeString.replaceAll("4-31g", "4-31");
        routeString = routeString.replaceAll("6-311g\\(d,p\\)", "6-311");
        routeString = routeString.replaceAll("6-311g\\(d\\)", "6-311");
        routeString = routeString.replaceAll("6-311g\\*\\*", "6-311");
        routeString = routeString.replaceAll("6-311\\+\\+g\\(d,p\\)", "6-311");
        routeString = routeString.replaceAll("6-311\\+g\\(2d,p\\)", "6-311");
        routeString = routeString.replaceAll("6-311\\+g\\(3df,p\\)", "6-311");
        routeString = routeString.replaceAll("6-311\\+g\\(2df\\)", "6-311");
        routeString = routeString.replaceAll("6-311\\+g\\(2d\\)", "6-311");
        routeString = routeString.replaceAll("6-311\\+g\\(d\\)", "6-311");
        routeString = routeString.replaceAll("6-311\\+g\\(d,p\\)", "6-311");
        routeString = routeString.replaceAll("6-31g\\(d,p\\)", "6-31");
        routeString = routeString.replaceAll("6-31g\\(d\\)", "6-31");
        routeString = routeString.replaceAll("6-31g\\*", "6-31");
        routeString = routeString.replaceAll("6-31g", "6-31");
        routeString = routeString.replaceAll("6-31\\+\\+g\\(d,p\\)", "6-31");
        routeString = routeString.replaceAll("6-31\\+\\+g\\(d\\)", "6-31");
        routeString = routeString.replaceAll("6-31\\+g\\(d,p\\)", "6-31");
        routeString = routeString.replaceAll("6-31\\+g\\(d\\)", "6-31");
        routeString = routeString.replaceAll("6-31\\(d,p\\)", "6-31");
        routeString = routeString.replaceAll("6-31\\(d\\)", "6-31");
        routeString = routeString.replaceAll("6-31\\*", "6-31");

        //aug-cc-pv5z -> aug cc-pv5z
        routeString = routeString.replaceAll("aug-cc", "aug cc");
        //cam-b3lyp -> cam b3lyp
        routeString = routeString.replaceAll("cam-b3lyp", "cam b3lyp");

        //cc-pv5z -> cc-pv
        routeString = routeString.replaceAll("cc-pv5z", "cc-pv");
        routeString = routeString.replaceAll("cc-pvdz", "cc-pv");
        routeString = routeString.replaceAll("cc-pvqz", "cc-pv");
        routeString = routeString.replaceAll("cc-pvtz", "cc-pv");
        //ccsd(t) -> ccsd
        routeString = routeString.replaceAll("ccsd\\(t\\)", "ccsd");
        //qcisd(t) -> qcisd
        routeString = routeString.replaceAll("qcisd\\(t\\)", "qcisd");
        //mp2(fc) -> mp2
        routeString = routeString.replaceAll("mp2\\(fc\\)", "mp2");
        //integral->int
        routeString = routeString.replaceAll("integral", "int");
        //m062x -> m06
        routeString = routeString.replaceAll("m062x", "m06");
        //genecp -> gen
        routeString = routeString.replaceAll("genecp", "gen");
        //(lanl2dz,lanl2mb) -> lanl2
        routeString = routeString.replaceAll("lanl2dz", "landl2");
        routeString = routeString.replaceAll("lanl2mb", "landl2");
        //b2plypd->b2
        routeString = routeString.replaceAll("b2plypd", "b2");
        //(b3lyp,b3p86,b3pw91) ->b3
        routeString = routeString.replaceAll("b3lyp", "b3");
        routeString = routeString.replaceAll("b3p86", "b3");
        routeString = routeString.replaceAll("b3pw91", "b3");
        //(mpw1pw91,mpwb95) -> mpw
        routeString = routeString.replaceAll("mpw1pw91", "mpw");
        routeString = routeString.replaceAll("mpwb95", "mpw");

        //ub3lyp -> u b3lyp
        routeString = routeString.replaceAll("ub3lyp", "u b3lyp");
        routeString = routeString.replaceAll("uhf", "u hf");
        routeString = routeString.replaceAll("um062x", "u m062x");
        routeString = routeString.replaceAll("um06", "u m06");
        routeString = routeString.replaceAll("ump2", "u mp2");
        routeString = routeString.replaceAll("uqcisd", "u qcisd");

        return routeString;
    }

    public static String[] explode(String keyword) {

        ArrayList<String> explodedKeywords = new ArrayList<>();
        //Todo if else if ladder (core feature extraction logic)

        //#p or #mpwb95/6-31g(d) form
        if (keyword.startsWith("#")) {
            if (keyword.contains("/")) {
                for (String t : keyword.split("/"))
                    explodedKeywords.add(t);
            } else {
                explodedKeywords.add(keyword);
            }
        }//oniom(b3lyp/6-31g(d,p):b3lyp/sto-3g)
        else if (keyword.startsWith("oniom")) {
            String t1 = keyword.replaceAll("\\(.+\\)", "");
            explodedKeywords.add(t1);
            String t2 = keyword.replaceAll("^" + t1, "");
            t2 = t2.substring(1, t2.length() - 1);
            String[] t3 = t2.split(":");
            for (String t4 : t3) {
                explodedKeywords.add(t1);
                explodedKeywords.add(t4.split("/")[0]);
                explodedKeywords.add(t4.split("/")[1]);
                explodedKeywords.add(t1 + "." + t4.split("/")[0]);
                explodedKeywords.add(t1 + "." + t4.split("/")[0] + "." + t4.split("/")[1]);
            }
        }//scf=(xqc,maxcycl=2000) and iop(3/76=0560004400) but not b3lyp/6-31g(d) or 6-31+g(d,p)
        else if (keyword.matches(".+\\(.+\\)") && !keyword.matches(".+/.+\\(.+\\)") && !keyword.startsWith("6-31")) {
            //scf=(xqc,maxcycl=2000)
            if (keyword.matches(".+=\\(.+\\)")) {
                String t1 = keyword.replaceAll("=\\(.+\\)", "");
                explodedKeywords.add(t1);
                String t2 = keyword.replaceAll("^" + t1 + "=", "");
                t2 = t2.substring(1, t2.length() - 1);
                String[] t3 = t2.split(",");
                for (String t4 : t3) {
                    //FIXME currently omitting the numeric or string values. e.g scf.maxcycle=200, scrf.solvent=p-xylene
                    if (t4.matches(".+=.+")) {
                        t4 = t4.replaceAll("=.+", "");
                    }
                    explodedKeywords.add(t1 + "." + t4);
                }
            }//iop(3/76=0560004400)
            else {
                String t1 = keyword.replaceAll("\\(.+\\)", "");
                explodedKeywords.add(t1);
                String t2 = keyword.replaceAll("^" + t1, "");
                t2 = t2.substring(1, t2.length() - 1);
                String[] t3 = t2.split(",");
                for (String t4 : t3) {
                    //FIXME currently omitting the numeric or string values. e.g scf.maxcycle=200, scrf.solvent=p-xylene
                    if (t4.matches(".+=.+")) {
                        t4 = t4.replaceAll("=.+", "");
                    }
                    explodedKeywords.add(t1 + "." + t4);

                }
            }
        }//freq=noraman or counterpoise=2
        else if (keyword.matches(".+=.+")) {
            //ccsd(t)=rw/6-311+g(2d),mp2=fc/aug-cc-pvdz
            if (keyword.matches("ccsd.*=.+/.+") || keyword.matches("mp2.*=.+/.+") || keyword.matches("freq.*=.+/.+")) {
                String t1 = keyword.split("=")[0];
                String t2 = keyword.split("=")[1];
                explodedKeywords.add(t1);
                explodedKeywords.add(t2.split("/")[0]);
                explodedKeywords.add(t2.split("/")[1]);
                explodedKeywords.add(t1 + "." + t2.split("/")[0]);
                explodedKeywords.add(t1 + "." + t2.split("/")[0] + "." + t2.split("/")[1]);
            } else {
                String t1 = keyword.replaceAll("=.+", "");
                String t2 = keyword.replaceAll(".+=", "");

                explodedKeywords.add(t1);
                //FIXME Numeric parameters of the form counterpoise=2 and maxdisk=3gb are not considered
                if (!StringUtils.isNumeric(t2) && !t1.contains("maxdisk") && !t1.contains("scale")) {
                    explodedKeywords.add(t1 + "." + t2);
                }
            }
        }//b3lyp/6-31g(d)
        else if (keyword.matches(".+/.+")) {
            String[] t1 = keyword.split("/");
            explodedKeywords.add(t1[0]);
            explodedKeywords.add(t1[1]);
            explodedKeywords.add(t1[0] + "." + t1[1]);
        }//default
        else {
            explodedKeywords.add(keyword);
        }
        return explodedKeywords.toArray(new String[explodedKeywords.size()]);
    }
}