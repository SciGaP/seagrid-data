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
package org.apache.airavata.datacat.analytics.input.chem;

import com.mongodb.hadoop.input.MongoInputSplit;
import com.mongodb.hadoop.splitter.MongoSplitter;
import com.mongodb.hadoop.splitter.MongoSplitterFactory;
import com.mongodb.hadoop.splitter.SplitFailedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ChemInputFormat extends InputFormat<String, ChemObject> {
    private final static Logger logger = LoggerFactory.getLogger(ChemInputFormat.class);

    @Override
    public List<InputSplit> getSplits(JobContext jobContext) throws IOException, InterruptedException {
        Configuration conf = jobContext.getConfiguration();
        Configuration mongoConfig = new Configuration();
        mongoConfig.set("mongo.job.input.format", conf.get("org.apache.airavata.datacat.analytics.input.format"));
        mongoConfig.set("mongo.input.uri", conf.get("org.apache.airavata.datacat.analytics.input.mongo.uri"));
        try {
            MongoSplitter spfe = MongoSplitterFactory.getSplitter(mongoConfig);
            if(logger.isDebugEnabled()) {
                logger.debug("Using " + spfe.toString() + " to calculate splits.");
            }

            return spfe.calculateSplits();
        } catch (SplitFailedException var4) {
            throw new IOException(var4);
        }
    }

    @Override
    public RecordReader<String, ChemObject> createRecordReader(InputSplit inputSplit
            , TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        if(!(inputSplit instanceof MongoInputSplit)) {
            throw new IllegalStateException("Creation of a new RecordReader requires a MongoInputSplit instance.");
        } else {
            MongoInputSplit mis = (MongoInputSplit)inputSplit;
            return new ChemRecordReader(mis);
        }
    }
}