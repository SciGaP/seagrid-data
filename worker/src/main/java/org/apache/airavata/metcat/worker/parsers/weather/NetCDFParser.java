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
package org.apache.airavata.metcat.worker.parsers.weather;

import org.apache.airavata.metcat.worker.parsers.AbstractParser;
import org.apache.airavata.metcat.worker.parsers.ParserException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class NetCDFParser extends AbstractParser {
    private final static Logger logger = LoggerFactory.getLogger(NetCDFParser.class);

    public NetCDFParser(){
        super();
    }

    @Override
    public JSONObject parse(String localFilePath, Map<String, Object> inputMetadata) throws Exception {
        try {
            InputStream inputStream = new FileInputStream("/Users/supun/Downloads/wrfbdy_d01");
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            org.apache.tika.parser.netcdf.NetCDFParser  netCDFParser = new org.apache.tika.parser.netcdf.NetCDFParser();
            netCDFParser.parse(inputStream, handler, metadata, new ParseContext());
            System.out.println(handler.toString());
//            System.out.println(metadata);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ParserException(e);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        NetCDFParser netCDFParser = new NetCDFParser();
        netCDFParser.parse(null, null);
    }


}