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
package org.apache.airavata.datacat.worker.parsers;

import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.commons.DataTypes;
import org.apache.airavata.datacat.worker.util.ParserConfig;
import org.apache.airavata.datacat.worker.util.ParserYamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultParserResolver implements IParserResolver {
    private final static Logger logger = LoggerFactory.getLogger(DefaultParserResolver.class);

    public List<IParser> resolveParser(String localDirPath, CatalogFileRequest catalogFileRequest) throws Exception {
        List<IParser> parsers = new ArrayList<>();
        String mimeType = catalogFileRequest.getMimeType();

        if(mimeType == null || mimeType.isEmpty()){
            mimeType = resolveMimeTypeForData(localDirPath, catalogFileRequest);
        }

        List<ParserConfig> parserConfigs = (new ParserYamlConfiguration()).getDataParsers();
        if(mimeType.equals(DataTypes.APPLICATION_GAUSSIAN)){
            Optional<ParserConfig> parserConfig = parserConfigs.stream().filter(p->p.getDataType()
                    .equals(DataTypes.APPLICATION_GAUSSIAN)).findFirst();
            if(parserConfig.isPresent()) {
                parserConfig.get().getParserClasses().stream().forEach(className->{
                    if (className != null && !className.isEmpty()) {
                        parsers.add(instantiate(className, IParser.class));
                    }
                });
            }
        }else if(mimeType.equals(DataTypes.APPLICATION_GAMESS)){
            Optional<ParserConfig> parserConfig = parserConfigs.stream().filter(p -> p.getDataType()
                    .equals(DataTypes.APPLICATION_GAMESS)).findFirst();
            if(parserConfig.isPresent()) {
                parserConfig.get().getParserClasses().stream().forEach(className->{
                    if (className != null && !className.isEmpty()) {
                        parsers.add(instantiate(className, IParser.class));
                    }
                });
            }
        }else if(mimeType.equals(DataTypes.APPLICATION_NWCHEM)){
            Optional<ParserConfig> parserConfig = parserConfigs.stream().filter(p -> p.getDataType()
                    .equals(DataTypes.APPLICATION_NWCHEM)).findFirst();
            if(parserConfig.isPresent()) {
                parserConfig.get().getParserClasses().stream().forEach(className->{
                    if (className != null && !className.isEmpty()) {
                        parsers.add(instantiate(className, IParser.class));
                    }
                });
            }
        }else if(mimeType.equals(DataTypes.APPLICATION_MOLPRO)){
            Optional<ParserConfig> parserConfig = parserConfigs.stream().filter(p -> p.getDataType()
                    .equals(DataTypes.APPLICATION_MOLPRO)).findFirst();
            if(parserConfig.isPresent()) {
                parserConfig.get().getParserClasses().stream().forEach(className->{
                    if (className != null && !className.isEmpty()) {
                        parsers.add(instantiate(className, IParser.class));
                    }
                });
            }
        }

        return parsers;
    }

    private String resolveMimeTypeForData(final String localDirPath, final CatalogFileRequest catalogFileRequest) throws Exception {
        List<ParserConfig> parserConfigs = (new ParserYamlConfiguration()).getDataParsers();
        for(ParserConfig p : parserConfigs){
            String detectClass = p.getDataDetectorClass();
            IDataDetector dataDetector = instantiate(detectClass, IDataDetector.class);
            if(dataDetector.detectData(localDirPath, catalogFileRequest)){
                return p.getDataType();
            }
        }
        return null;
    }

    private  <T> T instantiate(final String className, final Class<T> type){
        try{
            return type.cast(Class.forName(className).newInstance());
        } catch(final InstantiationException e){
            throw new IllegalStateException(e);
        } catch(final IllegalAccessException e){
            throw new IllegalStateException(e);
        } catch(final ClassNotFoundException e){
            throw new IllegalStateException(e);
        }
    }
}