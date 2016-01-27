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
package org.apache.airavata.datacat.query.api;

import org.apache.airavata.datacat.query.api.services.QueryAPIService;
import org.apache.airavata.datacat.query.api.util.QueryAPIProperties;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class QueryAPIServer {
    private final static Logger logger = LoggerFactory.getLogger(QueryAPIServer.class);

    public static final String QUERY_SERVER_URI = "query.server.uri";

    public static void main(String[] args) {
        try {
//            // Grizzly ssl configuration
//            SSLContextConfigurator sslContext = new SSLContextConfigurator();
//
//            // set up security context
//            if (new File("../security/" + QueryAPIProperties.getInstance()
//                    .getProperty(Constants.KEYSTORE_FILE, "")).exists()) {
//                sslContext.setKeyStoreFile("../security/" + QueryAPIProperties.getInstance()
//                        .getProperty(Constants.KEYSTORE_FILE, ""));
//                logger.info("Using the configured key store");
//            } else {
//                sslContext.setKeyStoreFile(ClassLoader.getSystemResource("security/" +
//                        QueryAPIProperties.getInstance()
//                                .getProperty(Constants.KEYSTORE_FILE, "")).getPath());
//                logger.info("Using the default key store");
//            }
//            sslContext.setKeyStorePass(QueryAPIProperties.getInstance()
//                    .getProperty(Constants.KEYSTORE_PWD,""));
//
//            if (new File("../security/" + QueryAPIProperties.getInstance()
//                    .getProperty(Constants.TRUST_STORE_FILE, "")).exists()) {
//                sslContext.setTrustStoreFile("../security/" + QueryAPIProperties.getInstance()
//                        .getProperty(Constants.TRUST_STORE_FILE, ""));
//                logger.info("Using the configured trust store");
//            } else {
//                sslContext.setTrustStoreFile(ClassLoader.getSystemResource("security/" +
//                        QueryAPIProperties.getInstance()
//                                .getProperty(Constants.TRUST_STORE_FILE, "")).getPath());
//                logger.info("Using the default trust store");
//            }
//            sslContext.setTrustStoreFile(QueryAPIProperties.getInstance()
//                    .getProperty(Constants.TRUST_STORE_PWD, ""));

            URI datacatQueryUri = UriBuilder.fromUri(
                    QueryAPIProperties.getInstance().getProperty(QUERY_SERVER_URI, "")).build();

            ResourceConfig dataCatQueryConfig = new ResourceConfig(QueryAPIService.class);
            final HttpServer dataCatQueryServer = GrizzlyHttpServerFactory.createHttpServer(datacatQueryUri, dataCatQueryConfig);

            try {
                new Thread() {
                    public void run() {
                        try {
                            logger.info("Starting DataCat Query Service. URI:"
                                    + QueryAPIProperties.getInstance().getProperty(QUERY_SERVER_URI,"")
                                    ,"datacat-query-service");
                            dataCatQueryServer.start();
                            Thread.currentThread().join();
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("There was an error when starting DataCat Query Service." +
                                    "Unable to start DataCat Query Service", e);
                            System.exit(-1);
                        }
                    }
                }.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        dataCatQueryServer.stop();
                        logger.info("DataCat Query Service Stopped!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Unable to start DataCat Query Service", e);
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString(), e);
        }
    }
}