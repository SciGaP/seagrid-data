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
package org.apache.airavata.datacat.agent.airavata.listner.rabbitmq;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.messaging.event.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class RabbitMQStatusConsumer implements Consumer {
    private static Logger logger = LoggerFactory.getLogger(RabbitMQStatusConsumer.class);
    private String exchangeName;
    private String url;
    private Connection connection;
    private Channel channel;
    private int prefetchCount;
    private Map<String, RabbitMQStatusConsumer.QueueDetails> queueDetailsMap = new HashMap();

    public RabbitMQStatusConsumer() throws AiravataException {
        try {
            this.url = ServerSettings.getSetting("rabbitmq.broker.url");
            this.exchangeName = ServerSettings.getSetting("rabbitmq.status.exchange.name");
            this.prefetchCount = Integer.valueOf(ServerSettings.getSetting("prefetch.count", String.valueOf(64))).intValue();
            this.createConnection();
        } catch (ApplicationSettingsException var3) {
            String message = "Failed to get read the required properties from airavata to initialize rabbitmq";
            logger.error(message, var3);
            throw new AiravataException(message, var3);
        }
    }

    public RabbitMQStatusConsumer(String brokerUrl, String exchangeName) throws AiravataException {
        this.exchangeName = exchangeName;
        this.url = brokerUrl;
        this.createConnection();
    }

    private void createConnection() throws AiravataException {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(this.url);
            connectionFactory.setAutomaticRecoveryEnabled(true);
            this.connection = connectionFactory.newConnection();
            this.connection.addShutdownListener(new ShutdownListener() {
                public void shutdownCompleted(ShutdownSignalException cause) {
                }
            });
            logger.info("connected to rabbitmq: " + this.connection + " for " + this.exchangeName);
            this.channel = this.connection.createChannel();
            this.channel.basicQos(this.prefetchCount);
            this.channel.exchangeDeclare(this.exchangeName, "topic", true);
        } catch (Exception var3) {
            String msg = "could not open channel for exchange " + this.exchangeName;
            logger.error(msg);
            throw new AiravataException(msg, var3);
        }
    }

    public String listen(final MessageHandler handler) throws AiravataException {
        try {
            Map<String, Object> props = handler.getProperties();
            Object routing = props.get("routingKey");
            if(routing == null) {
                throw new IllegalArgumentException("The routing key must be present");
            } else {
                List<String> keys = new ArrayList();
                if(routing instanceof List) {
                    Iterator var5 = ((List)routing).iterator();

                    while(var5.hasNext()) {
                        Object o = var5.next();
                        keys.add(o.toString());
                    }
                } else if(routing instanceof String) {
                    keys.add((String)routing);
                }

                String queueName = (String)props.get("queue");
                String consumerTag = (String)props.get("consumerTag");
                if(queueName == null) {
                    if(!this.channel.isOpen()) {
                        this.channel = this.connection.createChannel();
                        this.channel.exchangeDeclare(this.exchangeName, "topic", false);
                    }

                    queueName = this.channel.queueDeclare().getQueue();
                } else {
                    this.channel.queueDeclare(queueName, true, false, false, (Map)null);
                }

                final String id = this.getId(keys, queueName);
                if(this.queueDetailsMap.containsKey(id)) {
                    throw new IllegalStateException("This subscriber is already defined for this Consumer, cannot define the same subscriber twice");
                } else {
                    if(consumerTag == null) {
                        consumerTag = "default";
                    }

                    Iterator var8 = keys.iterator();

                    while(var8.hasNext()) {
                        String routingKey = (String)var8.next();
                        this.channel.queueBind(queueName, this.exchangeName, routingKey);
                    }

                    this.channel.basicConsume(queueName, true, consumerTag, new DefaultConsumer(this.channel) {
                        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
                            Message message = new Message();

                            String gatewayId;
                            try {
                                ThriftUtils.createThriftFromBytes(body, message);
                                TBase event = null;
                                gatewayId = null;
                                if(message.getMessageType().equals(MessageType.EXPERIMENT)) {
                                    ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), experimentStatusChangeEvent);
                                    logger.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType() + "'  with status " + experimentStatusChangeEvent.getState());
                                    event = experimentStatusChangeEvent;
                                    gatewayId = experimentStatusChangeEvent.getGatewayId();
                                } else if(message.getMessageType().equals(MessageType.PROCESS)) {
                                    ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), processStatusChangeEvent);
                                    logger.debug("Message Recieved with message id :" + message.getMessageId() + " and with " + "message type " + message.getMessageType() + " with status " + processStatusChangeEvent.getState());
                                    event = processStatusChangeEvent;
                                    gatewayId = processStatusChangeEvent.getProcessIdentity().getGatewayId();
                                } else if(message.getMessageType().equals(MessageType.TASK)) {
                                    TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), taskStatusChangeEvent);
                                    logger.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType() + "'  with status " + taskStatusChangeEvent.getState());
                                    event = taskStatusChangeEvent;
                                    gatewayId = taskStatusChangeEvent.getTaskIdentity().getGatewayId();
                                } else if(message.getMessageType() == MessageType.PROCESSOUTPUT) {
                                    TaskOutputChangeEvent taskOutputChangeEvent = new TaskOutputChangeEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), taskOutputChangeEvent);
                                    logger.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType());
                                    event = taskOutputChangeEvent;
                                    gatewayId = taskOutputChangeEvent.getTaskIdentity().getGatewayId();
                                } else if(message.getMessageType().equals(MessageType.JOB)) {
                                    JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), jobStatusChangeEvent);
                                    logger.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType() + "'  with status " + jobStatusChangeEvent.getState());
                                    event = jobStatusChangeEvent;
                                    gatewayId = jobStatusChangeEvent.getJobIdentity().getGatewayId();
                                } else if(message.getMessageType().equals(MessageType.LAUNCHPROCESS)) {
                                    TaskSubmitEvent taskSubmitEvent = new TaskSubmitEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), taskSubmitEvent);
                                    logger.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType() + "'  for experimentId: " + taskSubmitEvent.getExperimentId() + "and taskId: " + taskSubmitEvent.getTaskId());
                                    event = taskSubmitEvent;
                                    gatewayId = taskSubmitEvent.getGatewayId();
                                } else if(message.getMessageType().equals(MessageType.TERMINATEPROCESS)) {
                                    TaskTerminateEvent taskTerminateEvent = new TaskTerminateEvent();
                                    ThriftUtils.createThriftFromBytes(message.getEvent(), taskTerminateEvent);
                                    logger.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType() + "'  for experimentId: " + taskTerminateEvent.getExperimentId() + "and taskId: " + taskTerminateEvent.getTaskId());
                                    event = taskTerminateEvent;
                                    gatewayId = null;
                                }

                                MessageContext messageContext = new MessageContext((TBase)event, message.getMessageType(), message.getMessageId(), gatewayId);
                                messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
                                messageContext.setIsRedeliver(envelope.isRedeliver());
                                handler.onMessage(messageContext);
                            } catch (TException var9) {
                                gatewayId = "Failed to de-serialize the thrift message, from routing keys and queueName " + id;
                                logger.warn(gatewayId, var9);
                            }

                        }
                    });
                    this.queueDetailsMap.put(id, new RabbitMQStatusConsumer.QueueDetails(queueName, keys));
                    return id;
                }
            }
        } catch (Exception var10) {
            String msg = "could not open channel for exchange " + this.exchangeName;
            logger.error(msg);
            throw new AiravataException(msg, var10);
        }
    }

    public void stopListen(String id) throws AiravataException {
        RabbitMQStatusConsumer.QueueDetails details = (RabbitMQStatusConsumer.QueueDetails)this.queueDetailsMap.get(id);
        if(details != null) {
            String key;
            try {
                Iterator var3 = details.getRoutingKeys().iterator();

                while(var3.hasNext()) {
                    key = (String)var3.next();
                    this.channel.queueUnbind(details.getQueueName(), this.exchangeName, key);
                }

                this.channel.queueDelete(details.getQueueName(), true, true);
            } catch (IOException var5) {
                key = "could not un-bind queue: " + details.getQueueName() + " for exchange " + this.exchangeName;
                logger.debug(key);
            }
        }

    }

    private String getId(List<String> routingKeys, String queueName) {
        String id = "";

        String key;
        for(Iterator var4 = routingKeys.iterator(); var4.hasNext(); id = id + "_" + key) {
            key = (String)var4.next();
        }

        return id + "_" + queueName;
    }

    public void close() {
        if(this.connection != null) {
            try {
                this.connection.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

    private class QueueDetails {
        String queueName;
        List<String> routingKeys;

        private QueueDetails(String queueName, List<String> routingKeys) {
            this.queueName = queueName;
            this.routingKeys = routingKeys;
        }

        public String getQueueName() {
            return this.queueName;
        }

        public List<String> getRoutingKeys() {
            return this.routingKeys;
        }
    }
}
