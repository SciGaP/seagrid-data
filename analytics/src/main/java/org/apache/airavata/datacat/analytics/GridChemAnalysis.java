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

import com.mongodb.hadoop.MongoInputFormat;
import org.apache.airavata.datacat.analytics.util.AnalyticsProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.bson.BSONObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import scala.Tuple2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GridChemAnalysis {

    private static final String MONGO_INPUT_URI = "mongo.input.uri";
    private static final String SPARK_MASTER_URL = "spark.master.url";

    private static String mongoInputUri = AnalyticsProperties.getInstance().getProperty(MONGO_INPUT_URI, "");
    private static String sparkMasterUrl = AnalyticsProperties.getInstance().getProperty(SPARK_MASTER_URL, "");
    private static String projectDir = System.getProperty("user.dir");

    public static void main(String[] args) {
        // Set configuration options for the MongoDB Hadoop Connector.
        Configuration mongodbConfig = new Configuration();
        // MongoInputFormat allows us to read from a live MongoDB instance.
        // We could also use BSONFileInputFormat to read BSON snapshots.
        mongodbConfig.set("mongo.job.input.format", "com.mongodb.hadoop.MongoInputFormat");
        // MongoDB connection string naming a collection to use.
        // If using BSON, use "mapred.input.dir" to configure the directory
        // where BSON files are located instead.
        mongodbConfig.set("mongo.input.uri", mongoInputUri);

        //Creating the Spark Context
        SparkConf sparkConf = new SparkConf()
                .setAppName("GridChem-Analysis")
                .setMaster(sparkMasterUrl)
                .set("spark.cores.max", "4")
                .set("spark.executor.memory", "1G");

        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.addJar("file://" + projectDir + "/analytics/target/analytics-0.1-SNAPSHOT-jar-with-dependencies.jar");

        // Create an RDD backed by the MongoDB collection.
        JavaPairRDD<Object, BSONObject> parentDocuments = jsc.newAPIHadoopRDD(
                mongodbConfig,            // Configuration
                MongoInputFormat.class,   // InputFormat: read from a live cluster.
                Object.class,             // Key class
                BSONObject.class          // Value class
        );

//        parentDocuments.cache();

//        countAtoms(parentDocuments);
//        countJobStatuses(parentDocuments);
//        countNoOfAtoms(parentDocuments);
        countMolecularMass(parentDocuments);

        jsc.stop();
    }

    public static void countAtoms(JavaPairRDD<Object, BSONObject> parentDocuments) {
        //Extracting Atoms from SDF structure
        JavaRDD<String> atoms = parentDocuments.flatMap(new FlatMapFunction<Tuple2<Object, BSONObject>, String>() {
            public Iterable<String> call(Tuple2<Object, BSONObject> objectBSONObjectTuple2) throws Exception {
                ArrayList<String> atomList = new ArrayList();
                try{
                    String sdfString = objectBSONObjectTuple2._2().get("SDF").toString();
                    if(sdfString!= null && !sdfString.isEmpty()){
                        InputStream is = new ByteArrayInputStream(sdfString.getBytes());
                        MDLReader mdlReader = new MDLReader(is);
                        IAtomContainer atomContainer = mdlReader.read(SilentChemObjectBuilder.getInstance()
                                .newInstance(IAtomContainer.class));
                        for(int i=0;i<atomContainer.getAtomCount();i++){
                            atomList.add(atomContainer.getAtom(i).getSymbol());
                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                return atomList;
            }
        });

        JavaPairRDD<String, Integer> ones = atoms.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = counts.collect();
        System.out.println("\n-------------------Individual Atom Counts--------------------");
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        System.out.println("\n------------End of Individual Atom Counts--------------------");
    }

    public static void countJobStatuses(JavaPairRDD<Object, BSONObject> parentDocuments) {
        //Extracting Atoms from SDF structure
        JavaRDD<String> jobStatuses = parentDocuments.flatMap(new FlatMapFunction<Tuple2<Object, BSONObject>, String>() {
            public Iterable<String> call(Tuple2<Object, BSONObject> objectBSONObjectTuple2) throws Exception {
                ArrayList<String> statusList = new ArrayList();
                String jobStatus = objectBSONObjectTuple2._2().get("JobStatus").toString();
                if(jobStatus!= null && !jobStatus.isEmpty()){
                    statusList.add(jobStatus);
                }
                return statusList;
            }
        });

        JavaPairRDD<String, Integer> ones = jobStatuses.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = counts.collect();
        System.out.println("\n----------------------Job Status Counts----------------------");
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        System.out.println("\n---------------End of Job Status Counts----------------------");
    }

    public static void countNoOfAtoms(JavaPairRDD<Object, BSONObject> parentDocuments) {
        //Extracting Atoms from SDF structure
        JavaRDD<Integer> nAtoms = parentDocuments.flatMap(new FlatMapFunction<Tuple2<Object, BSONObject>, Integer>() {
            public Iterable<Integer> call(Tuple2<Object, BSONObject> objectBSONObjectTuple2) throws Exception {
                ArrayList<Integer> atomCountList = new ArrayList();
                try{
                    String sdfString = objectBSONObjectTuple2._2().get("SDF").toString();
                    if(sdfString!= null && !sdfString.isEmpty()){
                        InputStream is = new ByteArrayInputStream(sdfString.getBytes());
                        MDLReader mdlReader = new MDLReader(is);
                        IAtomContainer atomContainer = mdlReader.read(SilentChemObjectBuilder.getInstance()
                                .newInstance(IAtomContainer.class));
                        atomCountList.add(atomContainer.getAtomCount());
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                return atomCountList;
            }
        });

        JavaPairRDD<Integer, Integer> ones = nAtoms.mapToPair(new PairFunction<Integer, Integer, Integer>() {
            public Tuple2<Integer, Integer> call(Integer i) {
                return new Tuple2<Integer, Integer>(i, 1);
            }
        });

        JavaPairRDD<Integer, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        List<Tuple2<Integer, Integer>> output = counts.sortByKey(true).collect();
        System.out.println("\n-----------------No of Atoms in Molecule Counts--------------");
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        System.out.println("\n--------End of No of Atoms in Molecule Counts----------------");
    }

    public static void countMolecularMass(JavaPairRDD<Object, BSONObject> parentDocuments) {
        //Extracting Atoms from SDF structure
        JavaRDD<Integer> massNumbers = parentDocuments.flatMap(new FlatMapFunction<Tuple2<Object, BSONObject>, Integer>() {
            public Iterable<Integer> call(Tuple2<Object, BSONObject> objectBSONObjectTuple2) throws Exception {
                ArrayList<Integer> massNumberCountList = new ArrayList();
                try{
                    String sdfString = objectBSONObjectTuple2._2().get("SDF").toString();
                    if(sdfString!= null && !sdfString.isEmpty()){
                        InputStream is = new ByteArrayInputStream(sdfString.getBytes());
                        MDLReader mdlReader = new MDLReader(is);
                        IAtomContainer atomContainer = mdlReader.read(SilentChemObjectBuilder.getInstance()
                                .newInstance(IAtomContainer.class));
                        int massNumber = 0;
                        for(int i=0;i<atomContainer.getAtomCount();i++){
                           massNumber += atomContainer.getAtom(i).getMassNumber();
                        }
                        massNumberCountList.add(massNumber);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                return massNumberCountList;
            }
        });

        JavaPairRDD<Integer, Integer> ones = massNumbers.mapToPair(new PairFunction<Integer, Integer, Integer>() {
            public Tuple2<Integer, Integer> call(Integer i) {
                return new Tuple2<Integer, Integer>(i, 1);
            }
        });

        JavaPairRDD<Integer, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        List<Tuple2<Integer, Integer>> output = counts.sortByKey(true).collect();
        System.out.println("\n-----------------Molecule Mass Number Counts--------------");
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        System.out.println("\n--------End of Molecule Mass Number Counts----------------");
    }
}