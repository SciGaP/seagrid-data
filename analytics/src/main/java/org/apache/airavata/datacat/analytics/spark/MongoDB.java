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

import com.mongodb.hadoop.MongoInputFormat;
import org.apache.airavata.datacat.analytics.spark.util.AnalyticsConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.BSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MongoDB {
    private static String mongoInputUri = AnalyticsConstants.MONGO_INPUT_URI;
    private static String sparkMasterUrl = AnalyticsConstants.SPARK_MASTER_URL;
    private static String projectDir = AnalyticsConstants.PROJECT_DIR;

    public static void main(String[] args) throws IOException {
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
                .setAppName("MongoDB-Test")
                .setMaster(sparkMasterUrl);

        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.addJar("file://" + projectDir + "/analytics/target/analytics-0.1-SNAPSHOT-jar-with-dependencies.jar");

        // Create an RDD backed by the MongoDB collection.
        JavaPairRDD<Object, BSONObject> parentDocuments = jsc.newAPIHadoopRDD(
                mongodbConfig,            // Configuration
                MongoInputFormat.class,   // InputFormat: read from a live cluster.
                Object.class,             // Key class
                BSONObject.class          // Value class
        );

        //Extracting the SDFs
        JavaRDD<String> sdfStructures = parentDocuments.map(d->d._2().get("SDF").toString());
        List<String> sdfList = sdfStructures.collect();
        BufferedWriter writer = new BufferedWriter(new FileWriter("gridchem.sdf"));
        for(String s:sdfList){
            writer.write(s);
        }
        writer.flush();
        writer.close();
        jsc.stop();
    }
}