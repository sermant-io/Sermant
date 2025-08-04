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

package io.sermant.tag.transmission.rabbitmqv5.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.tag.transmission.rabbitmqv5.interceptor.RabbitmqProducerInterceptor;

/**
 * RabbitMQ enhanced producer declarer for Traffic Label Transmission，supports RabbitMQ5.x
 *
 * @since 2025-06-27
 */
public class RabbitmqProducerSendDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "com.rabbitmq.client.impl.ChannelN";

    private static final String METHOD_NAME = "basicPublish";

    private static final String[] METHOD_PARAM_TYPES = {
            "java.lang.String",
            "java.lang.String",
            "boolean",
            "boolean",
            "com.rabbitmq.client.AMQP$BasicProperties",
            "byte[]"
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME)
                        .and(MethodMatcher.paramTypesEqual(METHOD_PARAM_TYPES)), new RabbitmqProducerInterceptor())
        };
    }
}
