/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.demo.grayscale.rocketmq.producer;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * producer controller
 *
 * @author chengyouling
 * @since 2024-10-30
 */
@RestController
public class MqProducerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqProducerController.class);

    @Value("${rocketmq.address}")
    private String mqAddress;

    @Value("${rocketmq.topic}")
    private String mqTopic;

    private DefaultMQProducer producer;

    private final String errorMessage = "error";

    private final String successMessage = "success";

    private final int sendMsgTimeout = 60000;

    /**
     * init push producer
     *
     * @return is success
     */
    @GetMapping("/initProducer")
    public String initProducer() {
        try {
            if (producer == null) {
                producer = new DefaultMQProducer("default");
                producer.setNamesrvAddr(mqAddress);
                producer.setSendMsgTimeout(sendMsgTimeout);
                producer.start();
            }
        } catch (MQClientException e) {
            LOGGER.error("init pull producer error!", e);
        }
        return successMessage;
    }

    /**
     * pull producer produce message
     *
     * @param message message
     * @return is success
     */
    @GetMapping("/producePullMessage")
    public String producePullMessage(@RequestParam("message") String message) {
        try {
            if (producer == null) {
                initProducer();
            }
            Message mqMessage = new Message(mqTopic + "-PULL", message.getBytes(StandardCharsets.UTF_8));
            producer.send(mqMessage);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            LOGGER.error("send pull message error, address={}, message={}", mqAddress, message, e);
            return errorMessage;
        }
        return successMessage;
    }

    /**
     * lite pull producer produce message
     *
     * @param message message
     * @return is success
     */
    @GetMapping("/produceLitePullMessage")
    public String produceLitePullMessage(@RequestParam("message") String message) {
        try {
            if (producer == null) {
                initProducer();
            }
            Message mqMessage = new Message(mqTopic + "-LITE-PULL", message.getBytes(StandardCharsets.UTF_8));
            producer.send(mqMessage);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            LOGGER.error("send lite pull message error, address={}, message={}", mqAddress, message, e);
            return errorMessage;
        }
        return successMessage;
    }

    /**
     * push producer produce message
     *
     * @param message message
     * @return is success
     */
    @GetMapping("/producePushMessage")
    public String producePushMessage(@RequestParam("message") String message) {
        try {
            if (producer == null) {
                initProducer();
            }
            Message mqMessage = new Message(mqTopic + "-PUSH", message.getBytes(StandardCharsets.UTF_8));
            producer.send(mqMessage);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            LOGGER.error("send push message error, address={}, message={}", mqAddress, message, e);
            return errorMessage;
        }
        return successMessage;
    }
}
