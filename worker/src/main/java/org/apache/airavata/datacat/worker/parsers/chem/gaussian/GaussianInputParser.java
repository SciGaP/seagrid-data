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
import java.util.Map;

public class GaussianInputParser implements IParser{
    private final static Logger logger = LoggerFactory.getLogger(GaussianInputParser.class);

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
        String gaussianInputFile = null;
        for(File file : (new File(dir).listFiles())){
            if(file.getName().endsWith(".com") || file.getName().endsWith(".in")){
                gaussianInputFile = file.getAbsolutePath();
            }
        }
        if(gaussianInputFile == null){
            logger.warn("Could not find the gaussian input file");
            return finalObj;
        }

        String link0Commands = "";
        String routeCommands = "";

        try{
            //Extracting actual job runtime
            //    * %mem=4000mb
            //    * #p opt
            BufferedReader reader = new BufferedReader(new FileReader(gaussianInputFile));
            String line = reader.readLine();
            while(line != null){
                if(line.startsWith("%")){
                    link0Commands += (";" + line.trim());
                }else if(line.startsWith("#")){
                    routeCommands += (";" + line.trim());
                }
                line = reader.readLine();
            }
            if(finalObj.has("InputFileConfiguration")){
                ((JSONObject)finalObj.get("InputFileConfiguration")).put("Link0Commands", link0Commands.substring(1))
                        .put("RouteCommands", routeCommands.substring(1));
            }else{
                finalObj.put("InputFileConfiguration",new JSONObject().put("Link0Commands", link0Commands.substring(1))
                        .put("RouteCommands", routeCommands.substring(1)));
            }
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            return finalObj;
        }
        return finalObj;
    }
}