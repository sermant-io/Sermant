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

package io.sermant.demo.tagtransmission.rabbitmq.producer.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.sermant.demo.tagtransmission.midware.common.MessageConstant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ message middleware producer controller for producing messages
 *
 * @since 2025-06-27
 */
@RestController
@RequestMapping(value = "rabbitMqProducer")
public class RabbitMqProducerController {
    @Value("${rabbitmq.address}")
    private String rabbitMqAddress;

    @Value("${rabbitmq.port}")
    private int rabbitMqPort;

    @Value("${rabbitmq.queue}")
    private String queueName;

    /**
     * Check if producer process is started normally
     *
     * @return status string
     */
    @RequestMapping(value = "checkRabbitMqProducerStatus", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkRabbitMqProducerStatus() {
        return "ok";
    }

    /**
     * Produce a message via RabbitMQ
     *
     * @return success message
     * @throws IOException when connection fails
     * @throws TimeoutException when operation times out
     */
    @RequestMapping(value = "testRabbitMqProducer", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testRabbitMqProducer() throws IOException, TimeoutException {
        produceRabbitMqData();
        return "rabbitmq-produce-message-success";
    }

    /**
     * Produce RabbitMQ data
     *
     * @throws IOException when connection fails
     * @throws TimeoutException when operation times out
     */
    private void produceRabbitMqData() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMqAddress);
        factory.setPort(rabbitMqPort);
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            String messageBody = buildMessageBody(MessageConstant.MESSAGE_BODY_RABBITMQ);
            channel.basicPublish("", queueName, null, messageBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Build message body with timestamp
     *
     * @param body base message body
     * @return complete message body with timestamp
     */
    private String buildMessageBody(String body) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(MessageConstant.TIME_FORMAT);
        String messageBody = body + dtf.format(LocalDateTime.now());
        return messageBody;
    }
}
