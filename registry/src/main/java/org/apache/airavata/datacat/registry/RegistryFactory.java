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
package org.apache.airavata.datacat.registry;

import org.apache.airavata.datacat.registry.util.RegistryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryFactory {
    private final static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);

    public static final String REGISTRY_IMPL_CLASS = "registry.impl.class";

    public IRegistry getRegistryImpl(){
        String registryClassName = RegistryProperties.getInstance().getProperty(REGISTRY_IMPL_CLASS, "");
        IRegistry registry = instantiate(registryClassName, IRegistry.class);
        return registry;
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