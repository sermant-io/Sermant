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
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.kafka.extension.KafkaConsumerHandler;
import com.huaweicloud.sermant.kafka.utils.MarkUtils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Interceptor for the construction method of KafkaConsumer Properties
 * {@link KafkaConsumer#KafkaConsumer(Properties, Deserializer, Deserializer)}
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaConsumerPropertiesConstructorInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private KafkaConsumerHandler handler;

    /**
     * Construction method with KafkaConsumerHandler
     *
     * @param handler Construction Method Interception Point handler
     */
    public KafkaConsumerPropertiesConstructorInterceptor(KafkaConsumerHandler handler) {
        this.handler = handler;
    }

    /**
     * No parameter construction method
     */
    public KafkaConsumerPropertiesConstructorInterceptor() {
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        // This is a construction method that is compatible with different versions of KafkaConsumer
        // In lower versions, Properties and Map methods do not affect each other, while in higher versions,
        // Properties will call Map methods
        MarkUtils.setMark(Boolean.TRUE);
        if (handler != null) {
            handler.doBefore(context);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (handler != null) {
            handler.doAfter(context);
        }

        cacheKafkaConsumer(context);
        MarkUtils.removeMark();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        if (handler != null) {
            handler.doOnThrow(context);
        }
        MarkUtils.removeMark();
        return context;
    }

    /**
     * Caching consumer instances
     *
     * @param context Interception point execution context
     */
    private void cacheKafkaConsumer(ExecuteContext context) {
        Object kafkaConsumerObject = context.getObject();
        if (kafkaConsumerObject instanceof KafkaConsumer) {
            KafkaConsumer<?, ?> consumer = (KafkaConsumer<?, ?>) kafkaConsumerObject;
            KafkaConsumerController.addKafkaConsumerCache(consumer);
            LOGGER.info("KafkaConsumer has been cached by Sermant.");
        }
    }
}
