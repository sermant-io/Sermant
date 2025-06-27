/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.demo.tagtransmission.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import io.sermant.demo.tagtransmission.rabbitmq.consumer.controller.RabbitMqConsumerController;
import io.sermant.demo.tagtransmission.util.HttpClientUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ consumer
 *
 * @since 2025-06-27
 */
@Component
public class RabbitMqConsumer implements CommandLineRunner {
    /**
     * Store traffic tags returned by HTTP server called by consumer
     */
    public static final Map<String, String> RABBITMQ_TAG_MAP = new HashMap<>();

    @Value("${common.server.url}")
    private String commonServerUrl;

    @Value("${rabbitmq.address}")
    private String rabbitMqAddress;

    @Value("${rabbitmq.port}")
    private int rabbitMqPort;

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Override
    public void run(String[] args) throws IOException, TimeoutException {
        consumeData();
    }

    /**
     * Consume data from RabbitMQ
     *
     * @throws IOException when connection fails
     * @throws TimeoutException when operation times out
     */
    private void consumeData() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqAddress);
        factory.setPort(rabbitMqPort);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            RABBITMQ_TAG_MAP.put(RabbitMqConsumerController.RABBITMQ_TAG,
                    HttpClientUtils.doHttpUrlConnectionGet(commonServerUrl));
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
