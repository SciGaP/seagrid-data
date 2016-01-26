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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.datacat.commons.MimeTypes;
import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.listner.util.ListenerProperties;
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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiravataRabbitMQListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataRabbitMQListener.class);
    public static final String AIRAVATA_RABBITMQ_BROKER_URL = "airavata,rabbitmq.broker.url";
    public static final String AIRAVATA_RABBITMQ_EXCHANGE_NAME = "airavata.rabbitmq.exchange.name";
    private static final String EXPERIMENT_COMPLETED_STATE = "COMPLETED";

    public static final String DATACAT_RABBITMQ_BROKER_URL = "datacat.rabbitmq.broker.url";
    public static final String DATACAT_RABBITMQ_WORK_QUEUE_NAME = "datacat.rabbitmq.work.queue.name";

    private static final String datacatBrokerUrl = ListenerProperties.getInstance().getProperty(DATACAT_RABBITMQ_BROKER_URL, "");
    private static final String datacatWorkQueueName = ListenerProperties.getInstance().getProperty(DATACAT_RABBITMQ_WORK_QUEUE_NAME, "");

    public static void main(String[] args) {
        try {
            String airavataBrokerUrl = ListenerProperties.getInstance().getProperty(AIRAVATA_RABBITMQ_BROKER_URL, "");
            final String exchangeName = ListenerProperties.getInstance().getProperty(AIRAVATA_RABBITMQ_EXCHANGE_NAME, "");
            RabbitMQStatusConsumer consumer = new RabbitMQStatusConsumer(airavataBrokerUrl, exchangeName);
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
                    if (message.getType().equals(MessageType.EXPERIMENT)) {
                        //TODO Check Experiment Succeeded, Job Succeeded
                        try {
                            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                            TBase messageEvent = message.getEvent();
                            byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                            ThriftUtils.createThriftFromBytes(bytes, event);
                            logger.info(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getType() + "' and with state : '"
                                    + event.getState().toString() + " for Experiment " + event.getExperimentId());
                            if (event.getState().toString().equals(EXPERIMENT_COMPLETED_STATE)) {
                                String experimentId = event.getExperimentId();
                                ExperimentModel experimentModel = AiravataAPIClient.getInstance().getExperiment(experimentId);
                                String applicationName = experimentModel.getExecutionId();
                                if (applicationName.toLowerCase().contains("gaussian")) {
                                    String remoteGaussianLogFilePath = null;
                                    for (OutputDataObjectType outputDataObjectTypes : experimentModel.getExperimentOutputs()) {
                                        if (outputDataObjectTypes.getName().equals("Gaussian-Application-Output")) {
                                            remoteGaussianLogFilePath = outputDataObjectTypes.getValue();
                                        }
                                    }
                                    if (remoteGaussianLogFilePath == null || remoteGaussianLogFilePath.isEmpty()) {
                                        throw new Exception("No Gaussian log file available for experiment : "
                                                + experimentModel.getExperimentId());
                                    }
                                    CatalogFileRequest catalogFileRequest = new CatalogFileRequest();
                                    //FIXME
                                    catalogFileRequest.setFileUri(new URI("scp://gw54.iu.xsede.org:"
                                            + remoteGaussianLogFilePath));
                                    HashMap<String, Object> inputMetadata = new HashMap<>();
                                    inputMetadata.put("experimentId", experimentModel.getExperimentId());
                                    catalogFileRequest.setIngestMetadata(inputMetadata);
                                    catalogFileRequest.setMimeType(MimeTypes.APPLICATION_GAUSSIAN_LOG);

                                    publishMessage(catalogFileRequest);
                                } else if (applicationName.toLowerCase().contains("gamess")) {
                                    String remoteGaussianLogFilePath = null;
                                    for (OutputDataObjectType outputDataObjectTypes : experimentModel.getExperimentOutputs()) {
                                        if (applicationName.contains("Gamess_BR2")) {
                                            if (outputDataObjectTypes.getName().equals("Gamess-Job-Standard-Output")) {
                                                remoteGaussianLogFilePath = outputDataObjectTypes.getValue();
                                            }
                                        } else if (applicationName.contains("Gamess_Stampede")) {
                                            if (outputDataObjectTypes.getName().equals("Gamess-Standard-Out")) {
                                                remoteGaussianLogFilePath = outputDataObjectTypes.getValue();
                                            }
                                        } else {
                                            if (outputDataObjectTypes.getName().equals("Gamess-Standard-Out")) {
                                                remoteGaussianLogFilePath = outputDataObjectTypes.getValue();
                                            }
                                        }
                                    }
                                    if (remoteGaussianLogFilePath == null || remoteGaussianLogFilePath.isEmpty()) {
                                        throw new Exception("No Gamess stdout file available for experiment : "
                                                + experimentModel.getExperimentId());
                                    }
                                    CatalogFileRequest catalogFileRequest = new CatalogFileRequest();
                                    //FIXME
                                    catalogFileRequest.setFileUri(new URI("scp://gw54.iu.xsede.org:"
                                            + remoteGaussianLogFilePath));
                                    HashMap<String, Object> inputMetadata = new HashMap<>();
                                    inputMetadata.put("experimentId", experimentModel.getExperimentId());
                                    catalogFileRequest.setIngestMetadata(inputMetadata);
                                    catalogFileRequest.setMimeType(MimeTypes.APPLICATION_GAMESS_STDOUT);

                                    publishMessage(catalogFileRequest);
                                } else if (applicationName.toLowerCase().contains("nwchem")) {
                                    String remoteGaussianLogFilePath = null;
                                    for (OutputDataObjectType outputDataObjectTypes : experimentModel.getExperimentOutputs()) {
                                        if (outputDataObjectTypes.getName().equals("NWChem-Standard-Out")) {
                                            remoteGaussianLogFilePath = outputDataObjectTypes.getValue();
                                        }
                                    }
                                    if (remoteGaussianLogFilePath == null || remoteGaussianLogFilePath.isEmpty()) {
                                        throw new Exception("No NWChem stdout file available for experiment : "
                                                + experimentModel.getExperimentId());
                                    }
                                    CatalogFileRequest catalogFileRequest = new CatalogFileRequest();
                                    //FIXME
                                    catalogFileRequest.setFileUri(new URI("scp://gw54.iu.xsede.org:"
                                            + remoteGaussianLogFilePath));
                                    HashMap<String, Object> inputMetadata = new HashMap<>();
                                    inputMetadata.put("experimentId", experimentModel.getExperimentId());
                                    catalogFileRequest.setIngestMetadata(inputMetadata);
                                    catalogFileRequest.setMimeType(MimeTypes.APPLICATION_NWCHEM_STDOUT);

                                    publishMessage(catalogFileRequest);
                                } else {
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

    private static void publishMessage(CatalogFileRequest catalogFileRequest) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(datacatBrokerUrl);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        boolean durable = true;
        channel.queueDeclare(datacatWorkQueueName, durable, false, false, null);
        logger.info("Publishing to DataCat work queue ...");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        out = new ObjectOutputStream(bos);
        out.writeObject(catalogFileRequest);
        channel.basicPublish("", datacatWorkQueueName, null, bos.toByteArray());
        logger.info("Successfully published to launch queue ...");
        channel.close();
        connection.close();
    }
}