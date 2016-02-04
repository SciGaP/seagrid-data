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

import org.apache.airavata.datacat.analytics.input.DataCatChemInputFormat;
import org.apache.airavata.datacat.analytics.input.DataCatChemObject;
import org.apache.airavata.datacat.analytics.util.AnalyticsProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.openscience.cdk.interfaces.IAtomContainer;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class GridChemAnalysis {

    private static final String MONGO_INPUT_URI = "mongo.input.uri";
    private static final String SPARK_MASTER_URL = "spark.master.url";

    private static String mongoInputUri = AnalyticsProperties.getInstance().getProperty(MONGO_INPUT_URI, "");
    private static String sparkMasterUrl = AnalyticsProperties.getInstance().getProperty(SPARK_MASTER_URL, "");
    private static String projectDir = System.getProperty("user.dir");

    public static void main(String[] args) {

        Configuration config = new Configuration();
        config.set("org.apache.airavata.datacat.analytics.input.format",
                "org.apache.airavata.datacat.analytics.input.DataCatChemInputFormat");
        config.set("org.apache.airavata.datacat.analytics.input.mongo.uri", mongoInputUri);

        SparkConf sparkConf = new SparkConf()
                .setAppName("GridChem-Analysis")
                .setMaster(sparkMasterUrl)
                .set("spark.cores.max", "4")
                .set("spark.executor.memory", "1G");

        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.addJar("file://" + projectDir + "/analytics/target/analytics-0.1-SNAPSHOT-jar-with-dependencies.jar");

        JavaPairRDD<String, DataCatChemObject> parentDocuments = jsc.newAPIHadoopRDD(
                config,
                DataCatChemInputFormat.class,
                String.class,
                DataCatChemObject.class
        );

        parentDocuments.persist(org.apache.spark.storage.StorageLevel.MEMORY_ONLY_SER());

        countAtoms(parentDocuments);
        countNoOfAtoms(parentDocuments);
        countMolecularMass(parentDocuments);

        jsc.stop();
    }

    public static void countAtoms(JavaPairRDD<String, DataCatChemObject> parentDocuments) {
        JavaRDD<String> atoms = parentDocuments.flatMap(new FlatMapFunction<Tuple2<String, DataCatChemObject>, String>() {
            public Iterable<String> call(Tuple2<String, DataCatChemObject> objectTuple) throws Exception {
                ArrayList<String> atomList = new ArrayList();
                IAtomContainer atomContainer = objectTuple._2().getMolecule();
                for(int i=0;i<atomContainer.getAtomCount();i++){
                    atomList.add(atomContainer.getAtom(i).getSymbol());
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

    public static void countNoOfAtoms(JavaPairRDD<String, DataCatChemObject> parentDocuments) {
        JavaRDD<Integer> nAtoms = parentDocuments.flatMap(new FlatMapFunction<Tuple2<String, DataCatChemObject>, Integer>() {
            public Iterable<Integer> call(Tuple2<String, DataCatChemObject> objectTuple) throws Exception {
                ArrayList<Integer> atomCountList = new ArrayList();
                IAtomContainer atomContainer = objectTuple._2().getMolecule();
                atomCountList.add(atomContainer.getAtomCount());
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

    public static void countMolecularMass(JavaPairRDD<String, DataCatChemObject> parentDocuments) {
        JavaRDD<Integer> massNumbers = parentDocuments.flatMap(new FlatMapFunction<Tuple2<String,
                DataCatChemObject>, Integer>() {
            public Iterable<Integer> call(Tuple2<String, DataCatChemObject> objectTuple) throws Exception {
                ArrayList<Integer> massNumberCountList = new ArrayList();
                IAtomContainer atomContainer = objectTuple._2().getMolecule();
                int massNumber = 0;
                for(int i=0;i<atomContainer.getAtomCount();i++){
                    massNumber += atomContainer.getAtom(i).getMassNumber();
                }
                massNumberCountList.add(massNumber);
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
        System.out.println("\n-----------------Molecule Mass Number Counts-----------------");
        for (Tuple2<?,?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        System.out.println("\n--------End of Molecule Mass Number Counts-------------------");
    }
}