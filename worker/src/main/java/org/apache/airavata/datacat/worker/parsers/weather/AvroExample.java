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

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.FileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import parquet.avro.AvroParquetWriter;
import parquet.hadoop.metadata.CompressionCodecName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;



public class AvroExample {


    public void serialize() throws JsonParseException, JsonProcessingException, IOException {

        InputStream in = new FileInputStream("netcdf.json");

        // create a schema
        Schema schema = new Schema.Parser().parse(new File("netcdf.avsc"));
        // create a record to hold json
        GenericRecord AvroRec = new GenericData.Record(schema);
        // this file will have AVro output data
        File AvroFile = new File("netcdf.avro");
        // Create a writer to serialize the record
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);

        dataFileWriter.create(schema, AvroFile);

        // iterate over JSONs present in input file and write to Avro output file
        for (Iterator it = new ObjectMapper().readValues(
                new JsonFactory().createJsonParser(in), JSONObject.class); it.hasNext();) {

            JSONObject JsonRec = (JSONObject) it.next();
            JsonRec.keySet().stream().forEach(key->{
                AvroRec.put(key.toString(), JsonRec.get(key));
            });

            dataFileWriter.append(AvroRec);
        }  // end of for loop

        in.close();
        dataFileWriter.close();

    } // end of serialize method

    public void deserialize () throws IOException {
        // create a schema
        Schema schema = new Schema.Parser().parse(new File("netcdf.avsc"));
        // create a record using schema
        GenericRecord AvroRec = new GenericData.Record(schema);
        File AvroFile = new File("netcdf.avro");
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(AvroFile, datumReader);
        System.out.println("Deserialized data is :");
        while (dataFileReader.hasNext()) {
            AvroRec = dataFileReader.next(AvroRec);
            System.out.println(AvroRec.get("XLAT"));
        }
    }


    public void avroToParaquet() throws IOException {
        GenericDatumReader<Object> greader = new GenericDatumReader<>();
        FileReader<Object> fileReader = DataFileReader.openReader(new File("netcdf.avro"), greader);
        Schema avroSchema = fileReader.getSchema() ;

        // choose compression scheme
        CompressionCodecName compressionCodecName = CompressionCodecName.GZIP;

        // set Parquet file block size and page size values
        int blockSize = 256 * 1024 * 1024;
        int pageSize = 64 * 1024;

        String outputFilename="netcdf.parquet";
        File f=new File(outputFilename);
        if(f.exists()){
            f.delete();
        }
        Path outputPath = new Path(outputFilename);

        // the ParquetWriter object that will consume Avro GenericRecords
        AvroParquetWriter parquetWriter = new AvroParquetWriter(outputPath,avroSchema, compressionCodecName, blockSize, pageSize);


        DataFileReader<GenericRecord> reader = new DataFileReader<>(new File("netcdf.avro"), new GenericDatumReader<>());
        while (reader.hasNext()) {
            GenericRecord record = reader.next();
            parquetWriter.write(record);
        }

        parquetWriter.close();
    }

    public static void main(String[] args) throws IOException {
        AvroExample AvroEx = new AvroExample();
        AvroEx.serialize();
//        AvroEx.deserialize();
        AvroEx.avroToParaquet();
    }
}