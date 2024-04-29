/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.registry.declarers.health;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.registry.declarers.AbstractDoubleRegistryDeclarer;
import io.sermant.registry.interceptors.health.EurekaHealthInterceptor;
import io.sermant.registry.interceptors.health.EurekaRegisterInterceptor;

/**
 * Interception for health determinations
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class EurekaHealthDeclarer extends AbstractDoubleRegistryDeclarer {
    /**
     * The fully qualified name of the enhanced class has the highest injection priority for this client, so only
     * intercepting this client is necessary
     */
    private static final String ENHANCE_CLASS = "com.netflix.discovery.DiscoveryClient";

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = EurekaHealthInterceptor.class.getCanonicalName();

    /**
     * Timer injection interception
     */
    private static final String REGISTER_INTERCEPT_CLASS = EurekaRegisterInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("renew"), INTERCEPT_CLASS),
                InterceptDeclarer.build(MethodMatcher.nameEquals("register"), REGISTER_INTERCEPT_CLASS)
        };
    }
}
