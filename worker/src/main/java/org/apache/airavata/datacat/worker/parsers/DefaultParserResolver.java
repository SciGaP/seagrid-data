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
import org.apache.airavata.datacat.worker.util.ParserProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultParserResolver implements IParserResolver {
    private final static Logger logger = LoggerFactory.getLogger(DefaultParserResolver.class);

    public AbstractParser resolveParser(CatalogFileRequest catalogFileRequest){
        AbstractParser parser = null;
        //FIXME
        String mimeType = catalogFileRequest.getMimeType();
        if(mimeType.toLowerCase().contains("gaussian")){
            String className = ParserProperties.getInstance().getProperty(ParserProperties.GAUSSIAN_PARSER, "");
            if(className != null && !className.isEmpty()){
                parser = instantiate(className, AbstractParser.class);
            }
        }else if(mimeType.toLowerCase().contains("gamess")){
            String className = ParserProperties.getInstance().getProperty(ParserProperties.GAMESS_PARSER, "");
            if(className != null && !className.isEmpty()){
                parser = instantiate(className, AbstractParser.class);
            }
        }else if(mimeType.toLowerCase().contains("nwchem")){
            String className = ParserProperties.getInstance().getProperty(ParserProperties.NWCHEM_PARSER, "");
            if(className != null && !className.isEmpty()){
                parser = instantiate(className, AbstractParser.class);
            }
        }else if(mimeType.toLowerCase().contains("molpro")){
            String className = ParserProperties.getInstance().getProperty(ParserProperties.MOLPRO_PARSER, "");
            if(className != null && !className.isEmpty()){
                parser = instantiate(className, AbstractParser.class);
            }
        }
        return parser;
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