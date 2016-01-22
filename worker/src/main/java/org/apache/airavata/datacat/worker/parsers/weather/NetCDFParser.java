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
package org.apache.airavata.datacat.worker.parsers.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.datacat.worker.parsers.AbstractParser;
import org.apache.airavata.datacat.worker.parsers.ParserException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetCDFParser extends AbstractParser {
    private final static Logger logger = LoggerFactory.getLogger(NetCDFParser.class);

    public NetCDFParser(){
        super();
    }

    @Override
    public JSONObject parse(String localFilePath, Map<String, Object> inputMetadata) throws Exception {
        try {
            InputStream stream = new FileInputStream("/Users/supun/Downloads/wrfout_d01_2000-01-24_12-00-00");
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            org.apache.tika.parser.netcdf.NetCDFParser  netCDFParser = new org.apache.tika.parser.netcdf.NetCDFParser();
            netCDFParser.parse(stream, handler, metadata, new ParseContext());
//            System.out.println(handler.toString());
//            System.out.println(metadata);

            NetcdfFile ncFile = NetcdfFile.open("/Users/supun/Downloads/wrfout_d01_2000-01-24_12-00-00", null);
            Map<String, String> jsonMap = new HashMap<>();
            ncFile.getVariables().stream().forEach(var->{
                String key = var.getDataType() + " " + var.getNameAndDimensions();
                List<Variable> variableList = new ArrayList<>();
                variableList.add(var);
                try {
                    List<Array> arrays = ncFile.readArrays(variableList);
                    jsonMap.put(key, arrays.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("netcdf.json"), jsonMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ParserException(e);
        }
        return null;
    }

    private Property resolveMetadataKey(String localName) {
        if ("title".equals(localName)) {
            return TikaCoreProperties.TITLE;
        }
        return Property.internalText(localName);
    }

    public static void main(String[] args) throws Exception {
        NetCDFParser netCDFParser = new NetCDFParser();
        netCDFParser.parse(null, null);
    }


}