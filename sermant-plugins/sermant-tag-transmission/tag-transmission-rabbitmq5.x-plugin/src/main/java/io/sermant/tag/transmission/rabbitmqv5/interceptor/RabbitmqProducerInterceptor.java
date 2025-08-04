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

package io.sermant.tag.transmission.rabbitmqv5.interceptor;

import com.rabbitmq.client.AMQP;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractClientInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Producer interceptor for RabbitMQ traffic tag transparent transmission, supporting RabbitMQ5.x
 *
 * @since 2025-06-27
 */
public class RabbitmqProducerInterceptor extends AbstractClientInterceptor<AMQP.BasicProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final int PROPERTY_INDEX = 4;

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();

        AMQP.BasicProperties properties = initPropertiesIfNecessary(arguments);
        injectTrafficTag2Carrier(properties);

        arguments[PROPERTY_INDEX] = properties;
        return context;
    }

    /**
     * init properties field if null
     *
     * @param arguments args
     * @return message properties
     */
    protected AMQP.BasicProperties initPropertiesIfNecessary(Object[] arguments) {
        AMQP.BasicProperties properties = (AMQP.BasicProperties) arguments[PROPERTY_INDEX];
        if (Objects.isNull(properties)) {
            return new AMQP.BasicProperties.Builder().build();
        }
        return properties;
    }

    /**
     * Add traffic tag to AMQP.BasicProperties
     *
     * @param properties RabbitMQ tag transfer carrier
     */
    @Override
    protected void injectTrafficTag2Carrier(AMQP.BasicProperties properties) {
        Map<String, Object> originHeaders = Optional.of(properties)
                .map(AMQP.BasicProperties::getHeaders)
                .orElse(Collections.emptyMap());

        Map<String, Object> newHeaders = new HashMap<>(originHeaders);
        TrafficUtils.getTrafficTag().getTag().entrySet()
            .stream()
            .filter(entry -> TagKeyMatcher.isMatch(entry.getKey()))
            .filter(entry -> !originHeaders.containsKey(entry.getKey()))
            .peek(entry -> LOGGER.log(Level.FINE, "Traffic tag {0} have been filtered.", entry))
            .forEach(entry -> newHeaders.put(entry.getKey(), CollectionUtils.getFirst(entry.getValue(), null)));

        // reflection set field value
        ReflectUtils.setFieldValue(properties, "headers", newHeaders);
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }
}
