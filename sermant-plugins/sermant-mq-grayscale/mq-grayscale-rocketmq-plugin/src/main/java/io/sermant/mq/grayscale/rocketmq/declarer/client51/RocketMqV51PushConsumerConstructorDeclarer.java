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

package io.sermant.mq.grayscale.rocketmq.declarer.client51;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.mq.grayscale.rocketmq.interceptor.client51.RocketMqV51ConsumerConstructorInterceptor;

/**
 * 5.1.x client push consumer set gray consumer group declarer
 *
 * @author chengyouling
 * @since 2024-09-07
 **/
public class RocketMqV51PushConsumerConstructorDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "org.apache.rocketmq.client.consumer.DefaultMQPushConsumer";

    private static final String PARAMETER_STRING = "java.lang.String";

    private static final String PARAMETER_HOOK = "org.apache.rocketmq.remoting.RPCHook";

    private static final String PARAMETER_STRATEGY = "org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy";

    private static final String[] METHOD_PARAM_TYPES = {
            PARAMETER_STRING,
            PARAMETER_HOOK,
            PARAMETER_STRATEGY,
            "boolean",
            PARAMETER_STRING
    };

    private static final String[] METHOD_PARAM_THREE_TYPES = {
            PARAMETER_STRING,
            PARAMETER_HOOK,
            PARAMETER_STRATEGY
    };

    private static final String[] METHOD_PARAM_TWO_TYPES = {
            PARAMETER_STRING,
            PARAMETER_HOOK
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.isConstructor()
                                .and(MethodMatcher.paramTypesEqual(METHOD_PARAM_TYPES)
                                        .or(MethodMatcher.paramTypesEqual(METHOD_PARAM_THREE_TYPES))
                                        .or(MethodMatcher.paramTypesEqual(METHOD_PARAM_TWO_TYPES))),
                        new RocketMqV51ConsumerConstructorInterceptor())
        };
    }
}
