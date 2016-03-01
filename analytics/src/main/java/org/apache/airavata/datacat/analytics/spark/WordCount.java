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
package org.apache.airavata.datacat.analytics.spark;

import org.apache.airavata.datacat.analytics.spark.util.AnalyticsConstants;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.io.File;
import java.util.Arrays;

public class WordCount {

    private static String sparkMasterUrl = AnalyticsConstants.SPARK_MASTER_URL;
    private static String analyticsOutputDir = AnalyticsConstants.ANALYTICS_OUTPUT_DIR;
    private static String projectDir = AnalyticsConstants.PROJECT_DIR;


    private static final FlatMapFunction<String, String> WORDS_EXTRACTOR =
            new FlatMapFunction<String, String>() {

                public Iterable<String> call(String s) throws Exception {
                    return Arrays.asList(s.split(" "));
                }
            };

    private static final PairFunction<String, String, Integer> WORDS_MAPPER =
            new PairFunction<String, String, Integer>() {

                public Tuple2<String, Integer> call(String s) throws Exception {
                    return new Tuple2<String, Integer>(s, 1);
                }
            };

    private static final Function2<Integer, Integer, Integer> WORDS_REDUCER =
            new Function2<Integer, Integer, Integer>() {

                public Integer call(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            };

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("WordCount-Test")
                .setMaster(sparkMasterUrl);
//                .set("spark.cores.max","4")
//                .set("spark.executor.memory", "256M");

        JavaSparkContext jsc = new JavaSparkContext(conf);
        jsc.addJar("file://" + projectDir + "/analytics/target/analytics-0.1-SNAPSHOT-jar-with-dependencies.jar");
        JavaRDD<String> file = jsc.textFile(analyticsOutputDir + "/word-count/loremipsum.txt");
        JavaRDD<String> words = file.flatMap(WORDS_EXTRACTOR);
        JavaPairRDD<String, Integer> pairs = words.mapToPair(WORDS_MAPPER);
        JavaPairRDD<String, Integer> counter = pairs.reduceByKey(WORDS_REDUCER);

        if((new File(analyticsOutputDir + "/word-count").exists())){
            deleteDirectory(new File(analyticsOutputDir + "/word-count"));
        }
        counter.saveAsTextFile(analyticsOutputDir + "/word-count/output-"+System.currentTimeMillis());
        System.out.println("Total number of records : " + counter.count());

        jsc.stop();
    }

}