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
import org.apache.airavata.datacat.commons.DataTypes;
import org.apache.airavata.datacat.commons.messaging.WorkQueuePublisher;
import org.apache.airavata.datacat.seagrid.util.SEAGridFileScanerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This class will look for the experiment data located in a given directory and do a full scan for the valid
 * data files to be parsed. Once the first pass is completed, it watches for the new changes that happen in the
 * directory tree and parse new files if they are valid
 */
public class SEAGridFileScanner {

    private final static Logger logger = LoggerFactory.getLogger(SEAGridFileScanner.class);

    private static final String DATACAT_CACHE_OUTPUT_FILE = "datacat.cache.output.file";

    public static final String DATACAT_RABBITMQ_BROKER_URL = "datacat.rabbitmq.broker.url";
    public static final String DATACAT_RABBITMQ_WORK_QUEUE_NAME = "datacat.rabbitmq.work.queue.name";
    public static final String DATA_ROOT_PATH = "data.root.path";

    private static final String datacatBrokerUrl = SEAGridFileScanerProperties.getInstance().getProperty(DATACAT_RABBITMQ_BROKER_URL, "");
    private static final String datacatWorkQueueName = SEAGridFileScanerProperties.getInstance().getProperty(DATACAT_RABBITMQ_WORK_QUEUE_NAME, "");
    private static final String outputFile = SEAGridFileScanerProperties.getInstance().getProperty(DATACAT_CACHE_OUTPUT_FILE, "");

    private static final Map<WatchKey, Path> watcherMap = new HashMap();
    private static final Set<String> pathsScanned = new HashSet<>();
    private static int lastEntryInFile = 0;
    private static int skipLinesCount = 0;


    private static WorkQueuePublisher workQueuePublisher = new WorkQueuePublisher(datacatBrokerUrl, datacatWorkQueueName);


    private static void registerPathRecursively(Path rootDataPath, Path watchDir, WatchService watcher, int depth) throws IOException {


        Files.walkFileTree(watchDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String dirToUri = dir.toUri().toString();
                dirToUri = dirToUri.endsWith("/") ? dirToUri.substring(0, dirToUri.length() -1) : dirToUri;
                if (pathsScanned.contains(dirToUri)) {
                    logger.info("Skipping path monitoring as it is in cache " + dirToUri);
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    if (rootDataPath.relativize(dir).toString().split(File.separator).length <= depth) {
                        logger.info("Register path " + dirToUri);
                        WatchKey watchKey = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                                StandardWatchEventKinds.ENTRY_MODIFY);
                        watcherMap.put(watchKey, dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        logger.info("Skipping path monitoring as it is too deep than " + depth + " : " + dirToUri);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
            }
        });
    }

    /**
     * This method will look for the changes that happen in the data root
     * @param dataRootPath Root path where all data is located
     */
    public static void monitorChanges(Path dataRootPath) {

        logger.info("Monitoring changes in the filesystem " + dataRootPath);
        try {

            WatchService watcher = dataRootPath.getFileSystem().newWatchService();

            //watch for only project directories
            registerPathRecursively(dataRootPath, dataRootPath, watcher, 2);

            while(true) {

                WatchKey watchKey = watcher.take();
                List<WatchEvent<?>> events = watchKey.pollEvents();
                for (WatchEvent event : events) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {

                        logger.info("Entry creation detected");
                        WatchEvent<Path> parsedWatchEvent = (WatchEvent<Path>)event;
                        Path fileName = parsedWatchEvent.context();
                        Path rootDir = watcherMap.get(watchKey);
                        Path childPath = rootDir.resolve(fileName);
                        logger.info("Created: " + childPath.toString());

                        if (childPath.toFile().isDirectory()) {
                            logger.info("Detected directory creation " + childPath.toString());
                            // watch for only experiment directory
                            registerPathRecursively(dataRootPath, childPath, watcher, 3);

                            String pathToWrite = childPath.toUri().toString();
                            pathToWrite = pathToWrite.endsWith("/") ? pathToWrite.substring(0, pathToWrite.length() -1) : pathToWrite;
                            // if the whole directory was copied
                            for (File inFile : childPath.toFile().listFiles()) {
                                if (detectFile(inFile, false)) {
                                    if (!pathsScanned.contains(pathToWrite)) {
                                        logger.info("Parsing path " + pathToWrite);
                                        publishExperimentToParse(pathToWrite);
                                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
                                        writer.write(++lastEntryInFile + " " + pathToWrite + "\n");
                                        writer.close();
                                    } else {
                                        logger.info("Path " + childPath.toString() + " already scanned");
                                    }
                                }
                            }

                        } else {
                            // if files were created one by one
                            logger.info("Detected file creation " + childPath.toString());
                            Path relativeChild = dataRootPath.relativize(childPath);
                            String[] parts = relativeChild.toString().split(File.separator);
                            if (parts.length == 4) {

                                if (detectFile(childPath.toFile(), false)) {
                                    String pathToWrite = childPath.getParent().toUri().toString();
                                    pathToWrite = pathToWrite.endsWith("/") ? pathToWrite.substring(0, pathToWrite.length() -1) : pathToWrite;
                                    if (!pathsScanned.contains(pathToWrite)) {
                                        logger.info("Parsing path " + pathToWrite);
                                        publishExperimentToParse(pathToWrite);
                                        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
                                        writer.write(++lastEntryInFile + " " + pathToWrite + "\n");
                                        writer.close();
                                    } else {
                                        logger.info("Path " + childPath.toString() + " already scanned");
                                    }
                                }

                            } else {
                                logger.info("Path is invalid " + childPath.toString());
                            }
                        }
                    }
                }

                watchKey.reset();
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    public static boolean detectFile(File outputFile, boolean saturated) {

        if (outputFile.exists()) {
            logger.info("Candidate file " + outputFile.getPath());
            if (outputFile.getName().endsWith(".out") || outputFile.getName().endsWith(".log")) {
                logger.info("File " + outputFile.getPath() + " length " + outputFile.length());

                if (!saturated) {
                    // some file creation events come while the file is being copied. So this will wait until
                    // the file is saturated
                    long previousLength;
                    int retries = 0;
                    do {
                        previousLength = outputFile.length();
                        retries++;
                        try {
                            if (previousLength == 0) {
                                logger.info("Waiting 2 seconds as file size is 0");
                                Thread.sleep(2000);
                            } else {
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    } while (previousLength != outputFile.length() && retries < 100);

                    if (retries > 1) {
                        logger.info("Retried " + retries + " iterations until the file " + outputFile.getPath() + " get saturated");
                    }
                }

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(outputFile));
                    String temp = reader.readLine();

                    if (temp != null && !temp.isEmpty() && temp.toLowerCase().contains("gaussian")) {
                        logger.info("File contains gaussian " + outputFile.getPath());
                        boolean failed = true;
                        temp = reader.readLine();
                        while (temp != null) {
                            //Omitting failed experiments
                            if (temp.contains("Normal termination")) {
                                logger.info("File contains Normal termination " + outputFile.getPath());
                                failed = false;
                                logger.info("File selected " + outputFile.getPath());
                                break;
                            }
                            temp = reader.readLine();
                        }
                        return !failed;
                    }

                } catch (Exception e) {
                    logger.error("Failed while detecting file " + outputFile.getAbsolutePath(), e);
                    return false;
                }
            }
            logger.info("File is not a valid one " + outputFile.getPath());
            return false;
        } else {
            logger.info("File does not exist " + outputFile.getPath());
            return false;
        }
    }

    public static void main(String[] args) throws Exception {

        try {
            logger.info("Starting scanner");
            String filePathProtocol = "file://";
            String dataRootPath = SEAGridFileScanerProperties.getInstance().getProperty(DATA_ROOT_PATH, "");

            File dataRoot = new File(dataRootPath);

            if (!dataRoot.exists()) {
                logger.error("Data root path " + dataRootPath + " is not available");
                System.exit(0);
            }

            logger.info("Loading cache....");
            loadCache();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
            int totalGaussianExpCount = lastEntryInFile;

            for (File userDir : Objects.requireNonNull(dataRoot.listFiles())) {
                if (userDir.getName().equals("..") || userDir.getName().equals(".") || !userDir.isDirectory())
                    continue;
                String username = userDir.getName();

                for (File projDir : Objects.requireNonNull(userDir.listFiles())) {
                    if (projDir.getName().equals("..") || projDir.getName().equals(".") || !projDir.isDirectory())
                        continue;
                    String projDirName = projDir.getName();

                    for (File expDir : Objects.requireNonNull(projDir.listFiles())) {
                        if (expDir.getName().equals("..") || expDir.getName().equals(".") || !expDir.isDirectory())
                            continue;
                        String experimentDirName = expDir.getName();

                        String experimentDirAsPath = filePathProtocol + dataRootPath + File.separator + username + File.separator
                                + projDirName + File.separator + experimentDirName;

                        if (!pathsScanned.contains(experimentDirAsPath)) {
                            for (File outputFile : Objects.requireNonNull(expDir.listFiles())) {
                                if (detectFile(outputFile, true)) {
                                    logger.info("Adding " + outputFile.getPath() + " to output file");
                                    totalGaussianExpCount++;
                                    writer.write(totalGaussianExpCount + " " + experimentDirAsPath + "\n");
                                    logger.info(totalGaussianExpCount + " " + experimentDirAsPath);
                                    writer.flush();
                                }
                            }
                        } else {
                            logger.info("Path " + experimentDirAsPath + " already scanned");
                        }
                    }
                }
            }
            writer.close();

            logger.info("Publishing Records");
            BufferedReader reader = new BufferedReader(new FileReader(outputFile));

            while (skipLinesCount > 0) {
                reader.readLine();
                skipLinesCount--;
            }

            String temp = reader.readLine();
            while (temp != null && !temp.isEmpty()) {
                System.out.println(temp);
                logger.info("Publishing metadata for " + temp);
                String filePath = temp.split(" ")[1];
                lastEntryInFile = Integer.parseInt(temp.split(" ")[0]);
                publishExperimentToParse(filePath);
                temp = reader.readLine();
            }
            reader.close();

            monitorChanges(Paths.get(dataRootPath));
        } catch (Exception e) {
            logger.error("Error in file scanner. Exiting ", e);
        }
    }

    private static void loadCache() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(outputFile));
        String temp = reader.readLine();
        while(temp != null && !temp.isEmpty()){
            skipLinesCount++;
            System.out.println(temp);
            logger.info("Saving in cache for " + temp);
            String fileName = temp.split(" ")[1];
            lastEntryInFile = Integer.parseInt(temp.split(" ")[0]);
            pathsScanned.add(fileName.trim());
            temp = reader.readLine();
        }
        reader.close();
    }

    private static  void publishExperimentToParse(String experimentDataPath) throws Exception {

        logger.info("Publishing data to queue " + experimentDataPath);

        CatalogFileRequest catalogFileRequest = new CatalogFileRequest();
        catalogFileRequest.setDirUri(experimentDataPath);
        HashMap<String, Object> inputMetadata = new HashMap<>();
        inputMetadata.put("Id", (Paths.get(new URI(experimentDataPath)).getFileName()).toString());
        inputMetadata.put("Username", (Paths.get(new URI(experimentDataPath)).getParent().getParent().getFileName()).toString());
        inputMetadata.put("ExperimentName", (Paths.get(new URI(experimentDataPath)).getFileName()).toString());
        inputMetadata.put("ProjectName", (Paths.get(new URI(experimentDataPath)).getParent().getFileName()).toString());
        catalogFileRequest.setIngestMetadata(inputMetadata);
        catalogFileRequest.setMimeType(DataTypes.APPLICATION_GAUSSIAN);
        workQueuePublisher.publishMessage(catalogFileRequest);
        pathsScanned.add(experimentDataPath);
    }
}