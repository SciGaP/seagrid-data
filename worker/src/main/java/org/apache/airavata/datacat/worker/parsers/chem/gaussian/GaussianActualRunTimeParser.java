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
package org.apache.airavata.datacat.worker.parsers.chem.gaussian;

import org.apache.airavata.datacat.worker.parsers.IParser;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class GaussianActualRunTimeParser implements IParser{
    private final static Logger logger = LoggerFactory.getLogger(GaussianActualRunTimeParser.class);

    /**
     * Input File should exist within the specified working directory
     *
     * @param dir
     * @param inputMetadata
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject parse(JSONObject finalObj, String dir, Map<String, Object> inputMetadata) throws Exception {
        if(!dir.endsWith(File.separator)){
            dir += File.separator;
        }
        String gaussianOutputFile = null;
        for(File file : (new File(dir).listFiles())){
            if(file.getName().endsWith(".out") || file.getName().endsWith(".log")){
                gaussianOutputFile = file.getAbsolutePath();
            }
        }
        if(gaussianOutputFile == null){
            logger.warn("Could not find the gaussian output file");
            return finalObj;
        }

        try{
            //Extracting actual job runtime
            //    * Leave Link    1 at Thu Sep 10 04:38:06 2015, MaxMem=    65536000 cpu:         0.0
            //    * Normal termination of Gaussian 09 at Thu Sep 10 04:38:09 2015.
            BufferedReader reader = new BufferedReader(new FileReader(gaussianOutputFile));
            String line = reader.readLine();
            Date startingTime = null, endingTime = null;
            while(line != null){
                if(line.startsWith(" Leave Link    1 at") && startingTime == null){
                    String startString = line;
                    startString = startString.replaceAll(" Leave Link    1 at ", "");
                    startString = startString.split(",")[0];
                    startString = startString.substring(4);
                    startingTime = getDate(startString.trim());
                }else if(line.startsWith(" Normal termination of Gaussian 09 at")){
                    String endString = line;
                    endString = endString.replaceAll(" Normal termination of Gaussian 09 at", "");
                    endString = endString.split(",")[0];
                    endString = endString.substring(4);
                    endingTime = getDate(endString.trim());
                    break;
                }
                line = reader.readLine();
            }

            if(endingTime != null && startingTime != null){
                long seconds = (endingTime.getTime()-startingTime.getTime())/1000;
                if(finalObj.has("ExecutionEnvironment")){
                    ((JSONObject)finalObj.get("ExecutionEnvironment")).put("ActualJobRunTime", seconds);
                }else{
                    finalObj.put("ExecutionEnvironment",new JSONObject().put("ActualJobRunTime", seconds));
                }
            }

        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            return finalObj;
        }
        return finalObj;
    }

    private Date getDate(String dateString) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");
        Date date = formatter.parse(dateString);
        return date;
    }
}