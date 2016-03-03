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
package org.apache.airavata.datacat.seagrid;

import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.commons.FileTypes;
import org.apache.airavata.datacat.commons.messaging.WorkQueuePublisher;
import org.apache.airavata.datacat.seagrid.util.SEAGridFileScanerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;

public class SEAGridFileScanner {

    private final static Logger logger = LoggerFactory.getLogger(SEAGridFileScanner.class);

    private static final String fileName = "output-files.txt";

    public static final String DATACAT_RABBITMQ_BROKER_URL = "datacat.rabbitmq.broker.url";
    public static final String DATACAT_RABBITMQ_WORK_QUEUE_NAME = "datacat.rabbitmq.work.queue.name";

    private static final String datacatBrokerUrl = SEAGridFileScanerProperties.getInstance().getProperty(DATACAT_RABBITMQ_BROKER_URL, "");
    private static final String datacatWorkQueueName = SEAGridFileScanerProperties.getInstance().getProperty(DATACAT_RABBITMQ_WORK_QUEUE_NAME, "");

    public static void main(String[] args) throws Exception {
        int skipLinesCount = 0;
        String filePathProtocol = "file://";
        String dataRootPath = "/home/datacat/data";

        WorkQueuePublisher workQueuePublisher = new WorkQueuePublisher(datacatBrokerUrl, datacatWorkQueueName);
        if(args.length >0 ) {
            String arg0 = args[0];
            if (arg0 != null && !arg0.isEmpty()) {
                skipLinesCount = Integer.parseInt(arg0);
            }
            if(args.length > 1){
                dataRootPath = args[1];
            }
        }

        File file = new File(fileName);
        if(!file.exists()){
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            int totalGaussianExpCount = 0;
            File dataRoot = new File(dataRootPath);

            for(File userDir : dataRoot.listFiles()){
                if(userDir.getName().equals("..") || userDir.getName().equals(".") || !userDir.isDirectory())
                    continue;
                String username = userDir.getName();

                for(File projDir : userDir.listFiles()){
                    if(projDir.getName().equals("..") || projDir.getName().equals(".") || !projDir.isDirectory())
                        continue;
                    String projDirName = projDir.getName();

                    for(File expDir : projDir.listFiles()){
                        if(expDir.getName().equals("..") || expDir.getName().equals(".") || !expDir.isDirectory())
                            continue;
                        String experimentDirName = expDir.getName();

                        for(File outputFile : expDir.listFiles()){
                            if(outputFile.getName().endsWith(".out")){
                                try {
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
                                            totalGaussianExpCount++;
                                            writer.write(totalGaussianExpCount + " " + filePathProtocol
                                                    + dataRootPath + File.separator + username + File.separator
                                                    + projDirName + File.separator + experimentDirName+"\n");
                                            logger.info(totalGaussianExpCount + " " + filePathProtocol
                                                    + dataRootPath + File.separator + username + File.separator
                                                    + projDirName + File.separator + experimentDirName);
                                            writer.flush();
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.error(e.getMessage(),e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            writer.close();
        }

        logger.info("Publishing Records");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while(skipLinesCount>0){
            reader.readLine();
            skipLinesCount--;
        }
        String temp = reader.readLine();
        while(temp != null && !temp.isEmpty()){
            logger.info("Publishing metadata for " + temp);
            temp = temp.split(" ")[1];

            CatalogFileRequest catalogFileRequest = new CatalogFileRequest();
            catalogFileRequest.setDirUri(new URI(temp));
            HashMap<String, Object> inputMetadata = new HashMap<>();
            inputMetadata.put("Id", (Paths.get(new URI(temp)).getFileName()));
            inputMetadata.put("Username", (Paths.get(new URI(temp)).getParent().getParent().getFileName()));
            inputMetadata.put("ExperimentName", (Paths.get(new URI(temp)).getFileName()));
            inputMetadata.put("ProjectName", (Paths.get(new URI(temp)).getParent().getFileName()));
            catalogFileRequest.setIngestMetadata(inputMetadata);
            catalogFileRequest.setMimeType(FileTypes.APPLICATION_GAUSSIAN);

            workQueuePublisher.publishMessage(catalogFileRequest);
            temp = reader.readLine();
        }
    }
}