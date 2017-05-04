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
package org.apache.airavata.datacat.seagrid.util;

import java.io.*;

public class SEAGridFileScanerProperties {

    public static final String LISTENER_SCANNER_FILE = "../conf/seagrid-scanner.properties";
    public static final String DEFAULT_SCANNER_PROPERTY_FILE = "conf/seagrid-scanner.properties";

    private static SEAGridFileScanerProperties instance;

    private java.util.Properties properties = null;

    private SEAGridFileScanerProperties() {
        try {
            InputStream fileInput;
            if (new File(LISTENER_SCANNER_FILE).exists()) {
                fileInput = new FileInputStream(LISTENER_SCANNER_FILE);
                System.out.println("Using configured seagrid scaner (seagrid-scanner.properties) file");
            } else {
                System.out.println("Using default seagrid scaner property (seagrid-scaner) file");
                fileInput = ClassLoader.getSystemResource(DEFAULT_SCANNER_PROPERTY_FILE).openStream();
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

    public static SEAGridFileScanerProperties getInstance() {
        if (instance == null) {
            instance = new SEAGridFileScanerProperties();
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