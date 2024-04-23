/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.registry.grace.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.grace.interceptors.SpringRibbonWarmUpInterceptor;

/**
 * Spring cloud pre-warm-up interception, by intercepting the method of selecting a service, executing the pre-warm-up
 * logic for processing, the current interceptor only takes effect for Riboon
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class SpringRibbonWarmUpDeclarer extends AbstractPluginDeclarer {
    /**
     * Universal automatic registration class
     */
    private static final String ENHANCE_CLASS = "com.netflix.loadbalancer.AbstractServerPredicate";

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = SpringRibbonWarmUpInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        // Plugin start load time
        GraceContext.INSTANCE.setStartTime(System.currentTimeMillis());
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("getEligibleServers")
                        .and(MethodMatcher.paramTypesEqual("java.util.List", "java.lang.Object")), INTERCEPT_CLASS)
        };
    }
}
