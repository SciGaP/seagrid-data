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
package org.apache.airavata.datacat.worker.parsers.chem;

import org.apache.airavata.datacat.worker.parsers.IParser;
import org.apache.airavata.datacat.worker.parsers.ParserException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Map;

public class GamessParser implements IParser {
    private final static Logger logger = LoggerFactory.getLogger(GamessParser.class);

    private final String outputFileName = "gamess-output.json";

    @SuppressWarnings("unchecked")
    public JSONObject parse(String dir, Map<String, Object> inputMetadata) throws Exception {
        try{
            if(!dir.endsWith(File.separator)){
                dir += File.separator;
            }
            String inputFileName = dir + ".out";
            //FIXME Move the hardcoded script to some kind of configuration
            Process proc = Runtime.getRuntime().exec(
                    "docker run -t --env LD_LIBRARY_PATH=/usr/local/lib -v " +
                            dir +":/datacat/working-dir scnakandala/datacat-chem python " +
                            "/datacat/gamess.py /datacat/working-dir/"
                            + inputFileName +" /datacat/working-dir/" + outputFileName);


            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s;
            // read any errors from the attempted command
            String error = "";
            while ((s = stdError.readLine()) != null) {
                error += s;
            }
            if(error == null || !error.isEmpty()){
                logger.warn(error);
            }

            File outputFile = new File(dir + outputFileName);
            if(outputFile.exists()){
                JSONObject jsonObject = new JSONObject(new JSONTokener(new FileReader(dir + outputFileName)));

                inputMetadata.keySet().stream().forEach(key->{
                    jsonObject.put(key, inputMetadata.get(key));
                });

                return jsonObject;
            }
            throw new Exception("Could not parse data");
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new ParserException(ex);
        }finally {
            File outputFile = new File(dir + outputFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }
        }
    }
}