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
package org.apache.airavata.datacat.commons.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.airavata.datacat.commons.CatalogFileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class WorkQueuePublisher {
    private final static Logger logger = LoggerFactory.getLogger(WorkQueuePublisher.class);

    private  final String datacatBrokerUrl;
    private  final String datacatWorkQueueName;

    public WorkQueuePublisher(String datacatBrokerUrl, String datacatWorkQueueName){
        this.datacatBrokerUrl = datacatBrokerUrl;
        this.datacatWorkQueueName = datacatWorkQueueName;
    }

    public void publishMessage(CatalogFileRequest catalogFileRequest) throws Exception {
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