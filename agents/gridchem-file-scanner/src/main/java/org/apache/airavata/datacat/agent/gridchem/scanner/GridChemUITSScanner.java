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
package org.apache.airavata.datacat.agent.gridchem.scanner;

import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.commons.FileTypes;
import org.apache.airavata.datacat.commons.messaging.WorkQueuePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;

public class GridChemUITSScanner {
    private final static Logger logger = LoggerFactory.getLogger(GridChemUITSScanner.class);

    public static void main(String[] args) {
        int totalGaussianExpCount = 0;
        WorkQueuePublisher workQueuePublisher = new WorkQueuePublisher("amqp://airavata:airavata@gw56.iu.xsede.org:5672/development",
                "datacat_parse_data_work_queue");
        String dataRootPath = "/home/ccguser/mss/internal";
        String scpFilePathBase = "scp://ccguser@gridchem.uits.iu.edu:";
        logger.info("Starting File System Scanning");
        logger.info("Entering " + dataRootPath);
        File dataRoot = new File(dataRootPath);
        for(File userDir : dataRoot.listFiles()){
            if(userDir.getName().equals("..") || userDir.getName().equals(".") || !userDir.isDirectory())
                continue;
            String username = userDir.getName();
            logger.info("Entering user dir" + username);

            for(File projDir : userDir.listFiles()){
                if(projDir.getName().equals("..") || projDir.getName().equals(".") || !projDir.isDirectory())
                    continue;
                String projDirName = projDir.getName();
                logger.info("Entering project dir " + projDirName);

                for(File expDir : projDir.listFiles()){
                    if(expDir.getName().equals("..") || expDir.getName().equals(".") || !expDir.isDirectory())
                        continue;
                    String experimentDirName = expDir.getName();
                    logger.info("Entering experiment dir " + experimentDirName);

                    for(File outputFile : expDir.listFiles()){
                        if(outputFile.getName().endsWith(".out")){
                            try {
                                BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                                String temp = reader.readLine();
                                if(temp.toLowerCase().contains("gaussian")){
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
                                        logger.info("Gaussian experiment data found in exp-dir " + username + File.separator +
                                                projDirName + File.separator + experimentDirName);
                                        CatalogFileRequest catalogFileRequest = new CatalogFileRequest();
                                        catalogFileRequest.setFileUri(new URI(scpFilePathBase + outputFile.getAbsolutePath()));
                                        HashMap<String, Object> inputMetadata = new HashMap<>();
                                        inputMetadata.put("Id", experimentDirName);
                                        inputMetadata.put("Username", username);
                                        inputMetadata.put("GatewayId", "GridChem");
                                        inputMetadata.put("FullPath", scpFilePathBase + outputFile.getAbsolutePath());
                                        catalogFileRequest.setIngestMetadata(inputMetadata);
                                        catalogFileRequest.setMimeType(FileTypes.APPLICATION_GAUSSIAN_STDOUT);
                                        workQueuePublisher.publishMessage(catalogFileRequest);
                                        totalGaussianExpCount++;
                                        logger.info("Published catalog file request to RabbitMQ. Total Gaussian output count : "
                                                + totalGaussianExpCount);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(),e);
                            }
                        }
                    }
                }
            }
        }

    }

}