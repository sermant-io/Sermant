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
import io.sermant.mq.grayscale.interceptor.SubscriptionDataUpdateInterceptor;

/**
 * TAG/SQL92 query message statement declarer
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class SubscriptionDataUpdateDeclarer extends MqAbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.apache.rocketmq.common.filter.FilterAPI";

    private static final String METHOD_NAME = "buildSubscriptionData";

    private static final String METHOD_NAME_BUILD = "build";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME_BUILD)
                                .or(MethodMatcher.nameEquals(METHOD_NAME)), new SubscriptionDataUpdateInterceptor())
        };
    }
}
