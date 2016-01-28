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

import org.apache.airavata.api.Airavata;
import org.apache.airavata.datacat.agent.airavata.listner.util.ListenerProperties;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataAPIClient {

    private final static Logger logger = LoggerFactory.getLogger(AiravataAPIClient.class);

    public static final String AIRAVATA_HOST = "airavata.host";
    public static final String AIRAVATA_PORT = "airavata.port";

    private static AiravataAPIClient instance;
    private Airavata.Client airavataClient;

    private AiravataAPIClient() throws AiravataClientException {
        try {
            this.airavataClient = createAiravataClient();
        } catch (Exception e) {
            throw new AiravataClientException(AiravataErrorType.UNKNOWN);
        }
    }

    public static AiravataAPIClient getInstance() throws AiravataClientException {
        if (AiravataAPIClient.instance == null) {
            AiravataAPIClient.instance = new AiravataAPIClient();
        }
        return AiravataAPIClient.instance;
    }

    private Airavata.Client createAiravataClient() throws TTransportException {
        String host = ListenerProperties.getInstance().getProperty(AIRAVATA_HOST, "");
        int port = Integer.parseInt(ListenerProperties.getInstance().getProperty(AIRAVATA_PORT, ""));
        TTransport transport = new TSocket(host, port);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        return new Airavata.Client(protocol);
    }

    private Airavata.Client getClient() throws AiravataClientException, TTransportException {
        try{
            airavataClient.getAPIVersion(getAuthzToken());
        } catch (Exception e) {
            airavataClient = createAiravataClient();
        }
        return airavataClient;
    }

    //FIXME
    private AuthzToken getAuthzToken() {
        return new AuthzToken(new AuthzToken("empty-token"));
    }

    public ExperimentModel getExperiment(String experimentId) throws TException {
        return getClient().getExperiment(getAuthzToken(), experimentId);
    }
}