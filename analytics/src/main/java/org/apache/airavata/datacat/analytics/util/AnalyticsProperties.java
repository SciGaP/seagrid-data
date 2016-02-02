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
package org.apache.airavata.datacat.analytics.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class AnalyticsProperties {

    private final static Logger logger = LoggerFactory.getLogger(AnalyticsProperties.class);

    public static final String ANALYTICS_PROPERTY_FILE = "../conf/datacat-analytics.properties";
    public static final String DEFAULT_ANALYTICS_PROPERTY_FILE = "conf/datacat-analytics.properties";

    private static AnalyticsProperties instance;

    private java.util.Properties properties = null;

    private AnalyticsProperties() {
        try {
            InputStream fileInput;
            if (new File(ANALYTICS_PROPERTY_FILE).exists()) {
                fileInput = new FileInputStream(ANALYTICS_PROPERTY_FILE);
                logger.info("Using configured worker property (datacat-analytics.properties) file");
            } else {
                logger.info("Using default worker property (datacat-analytics) file");
                fileInput = ClassLoader.getSystemResource(DEFAULT_ANALYTICS_PROPERTY_FILE).openStream();
            }
            java.util.Properties properties = new java.util.Properties();
            properties.load(fileInput);
            fileInput.close();
            this.properties = properties;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AnalyticsProperties getInstance() {
        if (instance == null) {
            instance = new AnalyticsProperties();
        }
        return instance;
    }

    public String getProperty(String key, String defaultVal) {
        String val = this.properties.getProperty(key);

        if (val.isEmpty() || val == "") {
            return defaultVal;
        } else {
            return val;
        }
    }
}