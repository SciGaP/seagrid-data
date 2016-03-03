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

import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.worker.parsers.IDataDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GaussianDataDetector implements IDataDetector {
    private final static Logger logger = LoggerFactory.getLogger(GaussianDataDetector.class);

    @Override
    public boolean detectData(String localDirPath, CatalogFileRequest catalogFileRequest) throws Exception {
        File expDir = new File(localDirPath);
        for(File outputFile : expDir.listFiles()){
            if(outputFile.getName().endsWith(".out")){
                BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                String temp = reader.readLine();
                if(temp!=null && !temp.isEmpty() && temp.toLowerCase().contains("gaussian")){
                    boolean failed = true;
                    temp = reader.readLine();
                    while (temp != null){
                        //Omitting failed experiments
                        if(temp.contains("Normal termination")){
                            failed = false;
                            break;
                        }
                        temp = reader.readLine();
                    }
                    if(!failed) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}