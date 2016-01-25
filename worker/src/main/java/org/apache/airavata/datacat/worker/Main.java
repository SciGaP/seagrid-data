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
package org.apache.airavata.datacat.worker;

import com.rabbitmq.client.*;
import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.apache.airavata.datacat.worker.util.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String DATACAT_RABBITMQ_BROKER_URL="datacat.rabbitmq.broker.url";
    private static final String DATACAT_RABBITMQ_WORKER_QUEUE_NAME="datacat.rabbitmq.worker.queue.name";

    public static void main(String[] argv) throws Exception {
        String workQueueName = WorkerProperties.getInstance().getProperty(DATACAT_RABBITMQ_WORKER_QUEUE_NAME, "");
        String brokerUrl = WorkerProperties.getInstance().getProperty(DATACAT_RABBITMQ_BROKER_URL, "");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(brokerUrl);
        factory.setAutomaticRecoveryEnabled(true);
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        logger.info("connected to rabbitmq: " + connection + " for " + workQueueName);

        boolean durable = true;
        channel.queueDeclare(workQueueName, durable, false, false, null);
        channel.basicQos(1);
        final DataCatWorker dataCatWorker = new DataCatWorker();
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                try {
                    ByteArrayInputStream in = new ByteArrayInputStream(body);
                    ObjectInputStream is = new ObjectInputStream(in);
                    CatalogFileRequest catalogFileRequest = (CatalogFileRequest)is.readObject();
                    dataCatWorker.handle(catalogFileRequest);
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume(workQueueName, false, consumer);
    }
}