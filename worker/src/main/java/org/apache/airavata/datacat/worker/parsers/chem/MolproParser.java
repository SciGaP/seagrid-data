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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.UUID;

public class MolproParser implements IParser {

    private final static Logger logger = LoggerFactory.getLogger(MolproParser.class);

    private final String outputFileName = "molpro-output.json";

    @SuppressWarnings("unchecked")
    public JSONObject parse(String inputFileName, String workingDir, Map<String, Object> inputMetadata) throws Exception {
        try{
            if(!workingDir.endsWith(File.separator)){
                workingDir += File.separator;
            }

            //FIXME Move the hardcoded script to some kind of configuration
            Process proc = Runtime.getRuntime().exec(
                    "docker run -t --env LD_LIBRARY_PATH=/usr/local/lib -v " +
                            workingDir +":/datacat/working-dir scnakandala/datacat-chem python" +
                            " /datacat/molpro.py /datacat/working-dir/"
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

            File outputFile = new File(workingDir + outputFileName);
            if(outputFile.exists()){
                JSONParser jsonParser = new JSONParser();
                Object obj = jsonParser.parse(new FileReader(workingDir + outputFileName));
                JSONObject jsonObject = (JSONObject) obj;

                //TODO populate other fields
                if(inputMetadata != null && inputMetadata.get("experimentId") != null) {
                    //For MongoDB
                    jsonObject.put("_id", inputMetadata.get("experimentId"));
                    //For Solr
                    jsonObject.put("id", inputMetadata.get("experimentId"));
                    jsonObject.put("experimentId", inputMetadata.get("experimentId"));
                }else{
                    jsonObject.put("id", UUID.randomUUID().toString());
                }

                return jsonObject;
            }
            throw new Exception("Could not parse data");
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new ParserException(ex);
        }finally {
            File outputFile = new File(workingDir+ outputFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }
        }
    }
}