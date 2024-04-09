/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.kafka.interceptor;

import com.huaweicloud.sermant.config.ProhibitionConfigManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.kafka.cache.KafkaConsumerWrapper;
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.kafka.extension.KafkaConsumerHandler;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Interceptor for KafkaConsumer Construction Method {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(long)}
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(Duration)}
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerPollInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String ERROR_MESSAGE = "Consumer is not subscribed to any topics or "
            + "assigned any partitions";

    private KafkaConsumerHandler handler;

    /**
     * Construction method with KafkaConsumerHandler
     *
     * @param handler The subscribe method intercepts point handler
     */
    public KafkaConsumerPollInterceptor(KafkaConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * Non parametric construction method
     */
    public KafkaConsumerPollInterceptor() {
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        KafkaConsumerWrapper kafkaConsumerWrapper = KafkaConsumerController.getKafkaConsumerCache()
                .get(context.getObject().hashCode());
        if (kafkaConsumerWrapper == null) {
            return context;
        }
        if (handler != null) {
            handler.doBefore(context);
        } else {
            // Kafka does not allow multiple threads to operate consumers simultaneously, so it is not possible to
            // directly disable consumption when a new configuration is detected through dynamic configuration
            // monitoring. Considering that only when poll is truly called will rebalancing be triggered, a suitable
            // approach is to dynamically configure the thread to update the flag bit, check whether it is necessary
            // to handle the prohibition of consumption before poll, perform subscription addition and subtraction of
            // topics, and then trigger rebalancing by poll.
            if (kafkaConsumerWrapper.getIsConfigChanged().get()) {
                KafkaConsumerController.disableConsumption(kafkaConsumerWrapper,
                        ProhibitionConfigManager.getKafkaProhibitionTopics());
                kafkaConsumerWrapper.getIsConfigChanged().set(false);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (handler != null) {
            handler.doAfter(context);
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (context.getThrowable() instanceof IllegalStateException
                && ERROR_MESSAGE.equals(context.getThrowable().getMessage())) {
            context.changeThrowable(null);
            context.changeResult(ConsumerRecords.empty());
            LOGGER.fine("No consuming topic at this moment, catch exception and return empty result");
        }
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }
}
