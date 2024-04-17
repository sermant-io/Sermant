/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huaweicloud.sermant.utils.InvokeUtils;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Interceptor for KafkaConsumer subscribe method
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer#subscribe(Pattern, ConsumerRebalanceListener)}
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer#subscribe(Collection, ConsumerRebalanceListener)}
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerSubscribeInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private KafkaConsumerHandler handler;

    /**
     * Construction method with KafkaConsumerHandler
     *
     * @param handler The subscribe method intercepts point handler
     */
    public KafkaConsumerSubscribeInterceptor(KafkaConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * Non parametric construction method
     */
    public KafkaConsumerSubscribeInterceptor() {
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (InvokeUtils.isKafkaInvokeBySermant(Thread.currentThread().getStackTrace())) {
            return context;
        }
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (InvokeUtils.isKafkaInvokeBySermant(Thread.currentThread().getStackTrace())) {
            return context;
        }
        KafkaConsumerWrapper kafkaConsumerWrapper = KafkaConsumerController.getKafkaConsumerCache()
                .get(context.getObject().hashCode());
        if (kafkaConsumerWrapper == null) {
            return context;
        }
        kafkaConsumerWrapper.setOriginalTopics(kafkaConsumerWrapper.getKafkaConsumer().subscription());

        if (handler != null) {
            handler.doAfter(context);
        } else {
            LOGGER.info("Try to check if it is need to disable consumption after assignment...");

            // The host application checks whether it is necessary to unsubscribe from the Topic every time it
            // subscribes
            KafkaConsumerController.disableConsumption(kafkaConsumerWrapper,
                    ProhibitionConfigManager.getKafkaProhibitionTopics());
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (InvokeUtils.isKafkaInvokeBySermant(Thread.currentThread().getStackTrace())) {
            return context;
        }
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }
}
