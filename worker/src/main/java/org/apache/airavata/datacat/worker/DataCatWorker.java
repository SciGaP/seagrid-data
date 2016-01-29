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
package org.apache.airavata.datacat.worker;

import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.registry.IRegistry;
import org.apache.airavata.datacat.registry.RegistryFactory;
import org.apache.airavata.datacat.worker.parsers.IParser;
import org.apache.airavata.datacat.worker.parsers.IParserResolver;
import org.apache.airavata.datacat.worker.util.FileHelper;
import org.apache.airavata.datacat.worker.util.ParserProperties;
import org.apache.airavata.datacat.worker.util.WorkerConstants;
import org.apache.airavata.datacat.worker.util.WorkerProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.UUID;

public class DataCatWorker {

    private final static Logger logger = LoggerFactory.getLogger(DataCatWorker.class);

    private static final String PARSER_RESOLVER_CLASS="parser.resolver.class";

    private final IParserResolver parserResolver;
    private final IRegistry registry;
    private final FileHelper fileHelper;

    public DataCatWorker(){
        String parserResolverClass = ParserProperties.getInstance().getProperty(PARSER_RESOLVER_CLASS, "");
        parserResolver = instantiate(parserResolverClass, IParserResolver.class);
        registry = RegistryFactory.getRegistryImpl();
        fileHelper = new FileHelper();
    }

    public void handle(CatalogFileRequest catalogFileRequest){
        IParser parser = parserResolver.resolveParser(catalogFileRequest);
        if(parser != null){
            String workingDir = null;
            try {
                URI uri = catalogFileRequest.getFileUri();
                workingDir = WorkerProperties.getInstance().getProperty(WorkerConstants.WORKING_DIR, "/tmp");
                if(!workingDir.endsWith(File.separator)){
                    workingDir += File.separator;
                }
                workingDir += UUID.randomUUID().toString();
                new File(workingDir).mkdirs();
                String localFilePath = fileHelper.createLocalCopyOfFile(uri, workingDir);
                JSONObject jsonObject = parser.parse(Paths.get(localFilePath).getFileName().toString(), workingDir,
                        catalogFileRequest.getIngestMetadata());
                registry.create(jsonObject);
                logger.info("Published data for file : " + catalogFileRequest.getFileUri().toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.error("Failed to publish data for file : " + catalogFileRequest.getFileUri().toString());
            } finally {
                if(workingDir != null && !workingDir.isEmpty()) {
                    File file = new File(workingDir);
                    if(file.exists()){
                        deleteDirectory(file);
                    }
                }
            }
        }else{
            logger.warn("No suitable parser found for file : " + catalogFileRequest.getFileUri().toString());
        }
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private  <T> T instantiate(final String className, final Class<T> type){
        try{
            return type.cast(Class.forName(className).newInstance());
        } catch(final InstantiationException e){
            throw new IllegalStateException(e);
        } catch(final IllegalAccessException e){
            throw new IllegalStateException(e);
        } catch(final ClassNotFoundException e){
            throw new IllegalStateException(e);
        }
    }
}