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

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * litePull consumer
 *
 * @author chengyouling
 * @since 2024-11-30
 **/
@Component
public class RocketMqLitePullConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqLitePullConsumer.class);

    private DefaultLitePullConsumer litePullConsumer;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final long delay = 2L;

    private final long initialDelay = 0L;

    /**
     * init lite pull consumer
     *
     * @param mqTopic topic
     * @param mqAddress address
     */
    public void initLitePullConsumer(String mqTopic, String mqAddress) {
        if (litePullConsumer == null) {
            try {
                litePullConsumer = new DefaultLitePullConsumer("default");
                litePullConsumer.setNamesrvAddr(mqAddress);
                litePullConsumer.subscribe(mqTopic, "*");
                litePullConsumer.start();
                executorService.scheduleWithFixedDelay(new LitePullRunnable(litePullConsumer), initialDelay, delay,
                        TimeUnit.SECONDS);
            } catch (MQClientException e) {
                LOGGER.error("init lite pull consumer error!", e);
            }
        }
    }

    /**
     * get lite pull message count
     *
     * @param mqTopic topic
     * @param mqAddress address
     * @return message count
     */
    public Map<String, Object> getMessageCount(String mqTopic, String mqAddress) {
        if (litePullConsumer == null) {
            initLitePullConsumer(mqTopic, mqAddress);
        }
        return RocketMqMessageUtils.getMessageCount();
    }

    /**
     * shutdown consumer
     */
    public void shutdownLitePullConsumer() {
        executorService.shutdown();
        if (litePullConsumer != null) {
            litePullConsumer.shutdown();
        }
    }

    /**
     * lite pull consumer runnable
     *
     * @author chengyouling
     * @since 2024-11-30
     **/
    static class LitePullRunnable implements Runnable {
        private final DefaultLitePullConsumer litePullConsumer;

        private final long pullTimeout = 30000L;

        LitePullRunnable(DefaultLitePullConsumer litePullConsumer) {
            this.litePullConsumer = litePullConsumer;
        }

        @Override
        public void run() {
            List<MessageExt> messageExts = litePullConsumer.poll(pullTimeout);
            messageExts.forEach(RocketMqMessageUtils::convertMessageCount);
        }
    }
}
