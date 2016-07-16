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
package org.apache.airavata.datacat.agent.airavata.listner;

import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataAPIClient {

    private final static Logger logger = LoggerFactory.getLogger(AiravataAPIClient.class);

    private static AiravataAPIClient instance;

    public static AiravataAPIClient getInstance() throws AiravataClientException {
        if (AiravataAPIClient.instance == null) {
            AiravataAPIClient.instance = new AiravataAPIClient();
        }
        return AiravataAPIClient.instance;
    }

    public ExperimentModel getExperiment(String experimentId) throws TException, RegistryException {
        ExperimentCatalog experimentCatalog = RegistryFactory.getExperimentCatalog("seagrid");
        return (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
    }

    public static void main(String[] args) throws TException, RegistryException {
        AiravataAPIClient airavataAPIClient = new AiravataAPIClient();
        airavataAPIClient.getExperiment("gdfsdfsd_e6c92acd-acc2-4b14-82c7-7221899d19ed");
    }

}