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

package io.sermant.mq.grayscale.declarer;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.mq.grayscale.interceptor.MqPullConsumerConstructorInterceptor;

/**
 * DefaultMQPullConsumer consumer group builder declarer
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqPullConsumerConstructorDeclarer extends MqAbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.apache.rocketmq.client.consumer.DefaultMQPullConsumer";

    private static final String STRING_PARAMETER = "java.lang.String";

    private static final String HOOK_PARAMETER = "org.apache.rocketmq.remoting.RPCHook";

    private static final String[] METHOD_PARAM_TYPES_2 = {
            STRING_PARAMETER, HOOK_PARAMETER
    };

    private static final String[] METHOD_PARAM_TYPES_3 = {
            STRING_PARAMETER, STRING_PARAMETER, HOOK_PARAMETER
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.isConstructor()
                .and(MethodMatcher.paramTypesEqual(METHOD_PARAM_TYPES_2)
                        .or(MethodMatcher.paramTypesEqual(METHOD_PARAM_TYPES_3))),
                        new MqPullConsumerConstructorInterceptor())
        };
    }
}
