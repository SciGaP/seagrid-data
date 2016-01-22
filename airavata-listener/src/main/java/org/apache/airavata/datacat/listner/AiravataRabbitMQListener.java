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
package org.apache.airavata.datacat.listner;

import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.datacat.commons.MimeTypes;
import org.apache.airavata.datacat.commons.ParseMetadataRequest;
import org.apache.airavata.datacat.listner.util.ListenerProperties;
import org.apache.airavata.datacat.worker.DataCatWorker;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusConsumer;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiravataRabbitMQListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataRabbitMQListener.class);
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String AIRAVATA_RABBITMQ_EXCHANGE_NAME = "airavata.rabbitmq.exchange.name";
    private static final String EXPERIMENT_COMPLETED_STATE = "COMPLETED";

    public static void main(String[] args) {
        try {
            String brokerUrl = ListenerProperties.getInstance().getProperty(RABBITMQ_BROKER_URL, "");
            logger.info("RabbitMQ broker url " + brokerUrl);
            final String exchangeName = ListenerProperties.getInstance().getProperty(AIRAVATA_RABBITMQ_EXCHANGE_NAME, "");
            RabbitMQStatusConsumer consumer = new RabbitMQStatusConsumer(brokerUrl, exchangeName);

            final DataCatWorker datacatWorker = new DataCatWorker();
            consumer.listen(new MessageHandler() {
                @Override
                public Map<String, Object> getProperties() {
                    Map<String, Object> props = new HashMap<String, Object>();
                    List<String> routingKeys = new ArrayList<>();
                    routingKeys.add("*.*"); // listen for gateway/experiment level messages
                    props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
                    return props;
                }

                @Override
                public void onMessage(MessageContext message) {
                    if (message.getType().equals(MessageType.EXPERIMENT)){
                        try {
                            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                            TBase messageEvent = message.getEvent();
                            byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                            ThriftUtils.createThriftFromBytes(bytes, event);
                            logger.info(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getType() + "' and with state : '"
                                    + event.getState().toString() + " for Experiment " + event.getExperimentId());
                            if(event.getState().toString().equals(EXPERIMENT_COMPLETED_STATE)){
                                String experimentId = event.getExperimentId();
                                ExperimentModel experimentModel = AiravataAPIClient.getInstance().getExperiment(experimentId);
                                String applicationName = experimentModel.getExecutionId();
                                if(applicationName.toLowerCase().contains("gaussian")){
                                    String remoteGaussianLogFilePath = null;
                                    for(OutputDataObjectType outputDataObjectTypes : experimentModel.getExperimentOutputs()){
                                        if(outputDataObjectTypes.getName().equals("Gaussian-Application-Output")){
                                            remoteGaussianLogFilePath = outputDataObjectTypes.getValue();
                                        }
                                    }
                                    if(remoteGaussianLogFilePath == null || remoteGaussianLogFilePath.isEmpty()){
                                        throw new Exception("No Gaussian log file available for experiment : "
                                                + experimentModel.getExperimentId());
                                    }
                                    ParseMetadataRequest parseMetadataRequest = new ParseMetadataRequest();
                                    //FIXME
                                    parseMetadataRequest.setFileUri(new URI("scp://gw54.iu.xsede.org:"
                                            + remoteGaussianLogFilePath));
                                    HashMap<String, Object> inputMetadata = new HashMap<>();
                                    inputMetadata.put("experimentId", experimentModel.getExperimentId());
                                    parseMetadataRequest.setIngestMetadata(inputMetadata);
                                    parseMetadataRequest.setMimeType(MimeTypes.APPLICATION_GAUSSIAN);

                                    //TODO RabbitMQ Queue
                                    datacatWorker.handle(parseMetadataRequest);
                                }else{
                                    logger.info("Unsupported application format for experiment : "
                                            + experimentModel.getExperimentId());
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}