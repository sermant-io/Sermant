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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.kafka.cache.KafkaConsumerWrapper;
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.kafka.extension.KafkaConsumerHandler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Interceptor for KafkaConsumerClose method
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer#close(long, TimeUnit)}
 *
 * @author lilai
 * @since 2023-12-14
 */
public class KafkaConsumerCloseInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private KafkaConsumerHandler handler;

    /**
     * Construction method with KafkaConsumerHandler
     *
     * @param handler The subscribe method intercepts point handler
     */
    public KafkaConsumerCloseInterceptor(KafkaConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * Non parametric construction method
     */
    public KafkaConsumerCloseInterceptor() {
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        KafkaConsumerWrapper kafkaConsumerWrapper = KafkaConsumerController.getKafkaConsumerCache()
                .get(context.getObject().hashCode());
        if (kafkaConsumerWrapper == null) {
            return context;
        }

        if (handler != null) {
            handler.doAfter(context);
        }

        KafkaConsumerController.removeKafkaConsumeCache(kafkaConsumerWrapper.getKafkaConsumer());
        LOGGER.info("Remove consumer cache after closing.");
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (handler != null) {
            handler.doOnThrow(context);
        }
        return context;
    }
}
