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
package org.apache.airavata.datacat.worker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ParserProperties {

    private final static Logger logger = LoggerFactory.getLogger(ParserProperties.class);

    public static final String GAUSSIAN_PARSER = "gaussian.parser";
    public static final String GAMESS_PARSER = "gamess.parser";
    public static final String NWCHEM_PARSER = "nwchem.parser";
    public static final String MOLPRO_PARSER = "molpro.parser";

    public static final String PARSER_PROPERTY_FILE = "../conf/datacat-parser.properties";
    public static final String DEFAULT_PARSER_PROPERTY_FILE = "conf/datacat-parser.properties";

    private static ParserProperties instance;

    private java.util.Properties properties = null;

    private ParserProperties() {
        try {
            InputStream fileInput;
            if (new File(PARSER_PROPERTY_FILE).exists()) {
                fileInput = new FileInputStream(PARSER_PROPERTY_FILE);
                logger.info("Using configured parser property (datacat-parser.properties) file");
            } else {
                logger.info("Using default parser property (datacat-parser) file");
                fileInput = ClassLoader.getSystemResource(DEFAULT_PARSER_PROPERTY_FILE).openStream();
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

    public static ParserProperties getInstance() {
        if (instance == null) {
            instance = new ParserProperties();
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