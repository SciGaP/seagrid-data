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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParserYamlConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(ParserYamlConfiguration.class);

    public static final String PARSER_PROPERTY_FILE = "../conf/datacat-parser.yaml";
    public static final String DEFAULT_PARSER_PROPERTY_FILE = "conf/datacat-parser.yaml";


    private static final String DATA_PARSERS = "data.parsers";
    private static final String PARSERS = "parsers";
    private static final String DATA_TYPE = "data.type";
    private static final String DATA_DETECTOR = "data.detector";

    private List<ParserConfig> dataParsers = new ArrayList<>();

    public ParserYamlConfiguration() throws Exception {
        InputStream fileInputStream;
        if (new File(PARSER_PROPERTY_FILE).exists()) {
            fileInputStream = new FileInputStream(PARSER_PROPERTY_FILE);
            logger.info("Using configured parser property (datacat-parser.properties) file");
        } else {
            logger.info("Using default parser property (datacat-parser) file");
            fileInputStream = ClassLoader.getSystemResource(DEFAULT_PARSER_PROPERTY_FILE).openStream();
        }
        parse(fileInputStream);
    }

    private void parse(InputStream resourceAsStream) throws Exception {
        if (resourceAsStream == null) {
            throw new Exception("Configuration file{datacat-parser.yaml} is not fund");
        }
        Yaml yaml = new Yaml();
        Object load = yaml.load(resourceAsStream);
        if (load == null) {
            throw new Exception("Yaml configuration object null");
        }

        if (load instanceof Map) {
            Map<String, Object> loadMap = (Map<String, Object>) load;
            List<Map<String,Object >> parserYamls = (List<Map<String, Object>>) loadMap.get(DATA_PARSERS);
            ParserConfig parserConfig;
            if (parserYamls != null) {
                for (Map<String, Object> jobSub : parserYamls) {
                    parserConfig = new ParserConfig();
                    String dataType = ((String) jobSub.get(DATA_TYPE));
                    parserConfig.setDataType(dataType);
                    String apllicationDetector = ((String) jobSub.get(DATA_DETECTOR));
                    parserConfig.setDataDetectorClass(apllicationDetector);
                    ArrayList<String> parsers = (ArrayList<String>)jobSub.get(PARSERS);
                    parserConfig.setParserClasses(parsers);
                    dataParsers.add(parserConfig);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ParserYamlConfiguration parserYamlConfiguration = new ParserYamlConfiguration();
    }

    public List<ParserConfig> getDataParsers() {
        return dataParsers;
    }
}