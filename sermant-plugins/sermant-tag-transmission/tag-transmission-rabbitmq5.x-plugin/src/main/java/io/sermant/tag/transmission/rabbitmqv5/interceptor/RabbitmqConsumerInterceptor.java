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
import com.rabbitmq.client.Consumer;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.StringUtils;
import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.tag.transmission.config.strategy.TagKeyMatcher;
import io.sermant.tag.transmission.interceptors.AbstractServerInterceptor;
import io.sermant.tag.transmission.rabbitmqv5.wrapper.ConsumerWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * RabbitMQ consumer interceptor for transparent transmission of traffic tags, supports RabbitMQ5.x
 *
 * @since 2025-06-27
 */
public class RabbitmqConsumerInterceptor extends AbstractServerInterceptor<AMQP.BasicProperties> {

    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final int CONSUMER_INDEX = 6;

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        Object[] arguments = context.getArguments();

        // wrapped consumer
        Consumer consumeCallback = (Consumer) arguments[CONSUMER_INDEX];
        arguments[CONSUMER_INDEX] = new ConsumerWrapper(consumeCallback, this::updateTrafficTag);
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        return context;
    }

    /**
     * Parse traffic tag from properties
     *
     * @param properties RabbitMQ consumer traffic tag carrier
     * @return traffic tag map
     */
    @Override
    protected Map<String, List<String>> extractTrafficTagFromCarrier(AMQP.BasicProperties properties) {
        return Optional.ofNullable(properties)
                .map(AMQP.BasicProperties::getHeaders)
                .orElse(Collections.emptyMap())
                .entrySet()
                .stream()
                .filter(entry -> TagKeyMatcher.isMatch(entry.getKey()))
                .peek(entry -> LOGGER.log(Level.FINE, "Traffic tag {0} have been extracted from rabbitmq.", entry))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Collections.singletonList(StringUtils.getString(entry.getValue(), null))
                ));
    }

    /**
     * Update traffic tag
     *
     * @param properties RabbitMQ properties
     */
    protected void updateTrafficTag(AMQP.BasicProperties properties) {
        Map<String, List<String>> tagMap = extractTrafficTagFromCarrier(properties);
        TrafficUtils.setTrafficTag(new TrafficTag(tagMap));
    }
}
