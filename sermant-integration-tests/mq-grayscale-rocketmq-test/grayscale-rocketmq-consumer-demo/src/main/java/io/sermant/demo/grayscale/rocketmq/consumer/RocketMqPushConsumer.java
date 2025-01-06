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

package io.sermant.demo.grayscale.rocketmq.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * push consumer
 *
 * @author chengyouling
 * @since 2024-11-30
 **/
@Component
public class RocketMqPushConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqPushConsumer.class);

    private DefaultMQPushConsumer pushConsumer;

    private final long pushTimeout = 300000L;

    /**
     * init push consumer
     *
     * @param mqTopic topic
     * @param mqAddress address
     */
    public void initPushConsumer(String mqTopic, String mqAddress) {
        try {
            if (pushConsumer == null) {
                pushConsumer = new DefaultMQPushConsumer("default");
                pushConsumer.setNamesrvAddr(mqAddress);
                pushConsumer.subscribe(mqTopic, "*");
                pushConsumer.setConsumeTimeout(pushTimeout);
                pushConsumer.registerMessageListener((MessageListenerConcurrently)(messages, context) -> {
                    for (MessageExt messageExt : messages) {
                        RocketMqMessageUtils.convertMessageCount(messageExt);
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                });
                pushConsumer.start();
            }
        } catch (MQClientException e) {
            LOGGER.error("init push consumer error!", e);
        }
    }

    /**
     * get push message count
     *
     * @param mqTopic topic
     * @param mqAddress address
     * @return message count
     */
    public Map<String, Object> getMessageCount(String mqTopic, String mqAddress) {
        if (pushConsumer == null) {
            initPushConsumer(mqTopic, mqAddress);
        }
        return RocketMqMessageUtils.getMessageCount();
    }

    /**
     * shutdown consumer
     */
    public void shutdownPushConsumer() {
        if (pushConsumer != null) {
            pushConsumer.shutdown();
        }
    }
}
