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

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.PullResultExt;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * pull consumer
 *
 * @author chengyouling
 * @since 2024-11-30
 **/
@Component
public class RocketMqPullConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqPullConsumer.class);

    private static final Map<MessageQueue, Long> OFF_SET_TABLE = new HashMap<>();

    private final int maxReconsumeTimes = 10000;

    private final long pullTimeout = 300000L;

    private final long delay = 2L;

    private final long initialDelay = 0L;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private DefaultMQPullConsumer pullConsumer;

    /**
     * init pull consumer
     *
     * @param mqAddress address
     * @param topic topic
     */
    public void initPullConsumer(String mqAddress, String topic) {
        try {
            if (pullConsumer == null) {
                pullConsumer = new DefaultMQPullConsumer("default");
                pullConsumer.setNamesrvAddr(mqAddress);
                pullConsumer.setMaxReconsumeTimes(maxReconsumeTimes);
                pullConsumer.setConsumerPullTimeoutMillis(pullTimeout);
                pullConsumer.start();
                executorService.scheduleWithFixedDelay(new PullRunnable(pullConsumer, topic), initialDelay, delay,
                        TimeUnit.SECONDS);
            }
        } catch (MQClientException e) {
            LOGGER.error("init pull consumer error!", e);
        }
    }

    /**
     * get pull message count
     *
     * @return message count
     */
    public Map<String, Object> getMessageCount() {
        return RocketMqMessageUtils.getMessageCount();
    }

    private static void putMessageQueueOffSet(MessageQueue mq, PullResultExt pullResult) {
        OFF_SET_TABLE.put(mq, pullResult.getNextBeginOffset());
    }

    private static long getMessageQueueOffSet(MessageQueue mq) {
        return OFF_SET_TABLE.getOrDefault(mq, 0L);
    }

    /**
     * shutdown consumer
     */
    public void shutdownPullConsumer() {
        executorService.shutdown();
        if (pullConsumer != null) {
            pullConsumer.shutdown();
        }
    }

    /**
     * pull consumer runnable
     *
     * @author chengyouling
     * @since 2024-11-30
     **/
    static class PullRunnable implements Runnable {
        private final DefaultMQPullConsumer pullConsumer;

        private final String topic;

        private final int maxNums = 32;

        PullRunnable(DefaultMQPullConsumer pullConsumer, String topic) {
            this.pullConsumer = pullConsumer;
            this.topic = topic;
        }

        @Override
        public void run() {
            try {
                Set<MessageQueue> messageQueues = pullConsumer.fetchSubscribeMessageQueues(topic);
                if (messageQueues.isEmpty()) {
                    return;
                }
                for (MessageQueue mq : messageQueues) {
                    PullResultExt pullResult = (PullResultExt) pullConsumer.pullBlockIfNotFound(mq, null,
                            getMessageQueueOffSet(mq), maxNums);
                    putMessageQueueOffSet(mq, pullResult);
                    if (pullResult.getPullStatus() == PullStatus.FOUND) {
                        List<MessageExt> messageExts = pullResult.getMsgFoundList();
                        for (MessageExt messageExt: messageExts) {
                            RocketMqMessageUtils.convertMessageCount(messageExt);
                        }
                    }
                }
            } catch (MQClientException | MQBrokerException | RemotingException | InterruptedException e) {
                LOGGER.error("get pull message error!", e);
            }
        }
    }
}
