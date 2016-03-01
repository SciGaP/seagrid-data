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
package org.apache.airavata.datacat.analytics.spark.chem;

import com.mongodb.DBCursor;
import com.mongodb.MongoException;
import com.mongodb.hadoop.input.MongoInputSplit;
import com.mongodb.hadoop.util.MongoConfigUtil;
import com.mongodb.hadoop.util.MongoPathRetriever;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.bson.BSONObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChemRecordReader extends RecordReader<String, ChemObject> {
    private final static Logger logger = LoggerFactory.getLogger(ChemRecordReader.class);
    private BSONObject currentBson;
    private ChemObject currentChemObject;
    private final MongoInputSplit split;
    private final DBCursor cursor;
    private float seen = 0.0F;
    private float total;

    public ChemRecordReader(MongoInputSplit split) {
        this.split = split;
        this.cursor = split.getCursor();
    }

    @Override
    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        this.total = 1.0F;
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        try {
            if (!this.cursor.hasNext()) {
                logger.info("Read " + this.seen + " documents from:");
                logger.info(this.split.toString());
                return false;
            } else {
                this.currentBson = this.cursor.next();
                String sdfString = this.currentBson.get("SDF").toString();
                InputStream is = new ByteArrayInputStream(sdfString.getBytes());
                MDLReader mdlReader = new MDLReader(is);
                IAtomContainer atomContainer = mdlReader.read(SilentChemObjectBuilder.getInstance()
                        .newInstance(IAtomContainer.class));
                this.currentChemObject = new ChemObject(atomContainer);
                ++this.seen;
                return true;
            }
        } catch (Exception var2) {
            logger.error("Exception reading next key/val from mongo: " + var2.getMessage());
            return false;
        }
    }

    @Override
    public String getCurrentKey() throws IOException, InterruptedException {
        Object key = MongoPathRetriever.get(this.currentBson, this.split.getKeyField());
        if (key != null) {
            return key.toString();
        } else {
            return null;
        }
    }

    @Override
    public ChemObject getCurrentValue() throws IOException, InterruptedException {
        return this.currentChemObject;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        try {
            return this.cursor.hasNext() ? 0.0F : 1.0F;
        } catch (MongoException var2) {
            return 1.0F;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.cursor != null) {
            this.cursor.close();
            MongoConfigUtil.close(this.cursor.getCollection().getDB().getMongo());
        }
    }
}