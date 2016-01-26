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

import org.apache.airavata.datacat.worker.parsers.IParser;
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
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetCDFParser implements IParser {
    private final static Logger logger = LoggerFactory.getLogger(NetCDFParser.class);

    public NetCDFParser(){
        super();
    }

    public JSONObject parse(String inputFileName, String workingDir, Map<String, Object> inputMetadata) throws Exception {
        try {
            InputStream stream = new FileInputStream("/Users/supun/Downloads/wrfout_d01_2000-01-24_12-00-00");
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            org.apache.tika.parser.netcdf.NetCDFParser  netCDFParser = new org.apache.tika.parser.netcdf.NetCDFParser();
            netCDFParser.parse(stream, handler, metadata, new ParseContext());
//            System.out.println(handler.toString());
//            System.out.println(metadata);

            NetcdfFile ncFile = NetcdfFile.open("wrfout_d01_2000-01-24_12-00-00", null);
            BufferedWriter recordWriter = new BufferedWriter(new FileWriter("netcdf.json"));
            BufferedWriter schemaWriter = new BufferedWriter(new FileWriter("netcdf.avsc"));
            schemaWriter.write("{\"namespace\" : \"org.apache.airavata.netcdf\",\n" +
                    "  \"name\": \"NetCDFRecord\",\n" +
                    "  \"type\" :  \"record\",\n" +
                    "  \"fields\" : [\n");
            recordWriter.write("{\n");
            int count = ncFile.getVariables().size();
            for(Variable var : ncFile.getVariables()){
                String key = var.getDataType() + " " + var.getNameAndDimensions();
                List<Variable> variableList = new ArrayList<>();
                variableList.add(var);
                try {
                    count--;
                    if(var.getName().equals("Times"))
                        continue;
                    List<Array> arrays = ncFile.readArrays(variableList);
                    recordWriter.write("  \"" + var.getNameAndDimensions().replaceAll("\\(([^\\)]+)\\)", ""));
                    recordWriter.write("\":[");
                    String[] bits = arrays.get(0).toString().split(" ");
                    boolean first = true;
                    for(String bit : bits){
                        if(first){
                            first = false;
                        }else{
                            recordWriter.write(",");
                        }
                        recordWriter.write(bit);
                    }
                    recordWriter.write("]");
                    if(var.getDataType().equals(DataType.CHAR)){
                        schemaWriter.write("    {\"name\": \""+ var.getName() +"\", \"type\": [\"null\",\n" +
                                "                {\n" +
                                "                    \"type\":\"array\",\n" +
                                "                    \"items\":\"string\"\n" +
                                "                }\n" +
                                "            ],\n" +
                                "            \"default\":null}");
                    }else if(var.getDataType().equals(DataType.INT)){
                        schemaWriter.write("    {\"name\": \""+ var.getName() +"\", \"type\": [\"null\",\n" +
                                "                {\n" +
                                "                    \"type\":\"array\",\n" +
                                "                    \"items\":\"int\"\n" +
                                "                }\n" +
                                "            ],\n" +
                                "            \"default\":null}");
                    }else if(var.getDataType().equals(DataType.DOUBLE)){
                        schemaWriter.write("    {\"name\": \"" + var.getName() + "\", \"type\":[\"null\",\n" +
                                "                {\n" +
                                "                    \"type\":\"array\",\n" +
                                "                    \"items\":\"double\"\n" +
                                "                }\n" +
                                "            ],\n" +
                                "            \"default\":null}");
                    }else if(var.getDataType().equals(DataType.FLOAT)){
                        schemaWriter.write("    {\"name\": \""+ var.getName() +"\", \"type\": [\"null\",\n" +
                                "                {\n" +
                                "                    \"type\":\"array\",\n" +
                                "                    \"items\":\"double\"\n" +
                                "                }\n" +
                                "            ],\n" +
                                "            \"default\":null}");
                    }

                    if(count>0){
                        schemaWriter.write(",\n");
                        recordWriter.write(",\n");
                    }else{
                        schemaWriter.write("\n");
                        recordWriter.write("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            schemaWriter.write("  ]\n" +
                    "}");
            recordWriter.write("}");
            schemaWriter.close();
            recordWriter.close();
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
        netCDFParser.parse(null, null, null);
    }


}