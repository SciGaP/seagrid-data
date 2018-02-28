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
import org.apache.airavata.datacat.worker.util.WorkerConstants;
import org.apache.airavata.datacat.worker.util.WorkerProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.List;

public class DataCatWorker {

    private final static Logger logger = LoggerFactory.getLogger(DataCatWorker.class);

    private static final String PARSER_RESOLVER_CLASS = "parser.resolver.class";

    private final IParserResolver parserResolver;
    private final IRegistry registry;
    private final FileHelper fileHelper;

    public DataCatWorker() {
        String parserResolverClass = WorkerProperties.getInstance().getProperty(PARSER_RESOLVER_CLASS, "");
        parserResolver = instantiate(parserResolverClass, IParserResolver.class);
        registry = RegistryFactory.getRegistryImpl();
        fileHelper = new FileHelper();
    }

    public void handle(CatalogFileRequest catalogFileRequest) throws Exception {
        String workingDir = null;
        String localDirPath = null;
        URI uri = new URI(catalogFileRequest.getDirUri());
        try {
            //Copying data to the local directory
            if (!uri.getScheme().contains("file")) {
                workingDir = WorkerProperties.getInstance().getProperty(WorkerConstants.WORKING_DIR, "/tmp");
                localDirPath = fileHelper.createLocalCopyOfDir(uri, workingDir);
            } else {
                localDirPath = uri.getPath();
            }

            logger.info("Parsing file " + localDirPath);
            List<IParser> parsers = parserResolver.resolveParser(localDirPath, catalogFileRequest);
            if (parsers != null && parsers.size() > 0) {
                JSONObject jsonObject = new JSONObject();
                for (IParser parser : parsers) {
                    jsonObject = parser.parse(jsonObject, localDirPath, catalogFileRequest.getIngestMetadata());
                }
                registry.create(jsonObject);
                logger.info("Published data for directory : " + catalogFileRequest.getDirUri().toString());
            } else {
                logger.warn("No suitable parser found for directory : " + catalogFileRequest.getDirUri().toString());
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing file " + localDirPath, e);
            throw e;
        } finally {
            if (!uri.getScheme().contains("file") && localDirPath != null && !localDirPath.isEmpty()) {
                File file = new File(localDirPath);
                if (file.exists()) {
                    deleteDirectory(file);
                }
            }
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

    private <T> T instantiate(final String className, final Class<T> type) {
        try {
            return type.cast(Class.forName(className).newInstance());
        } catch (final InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}