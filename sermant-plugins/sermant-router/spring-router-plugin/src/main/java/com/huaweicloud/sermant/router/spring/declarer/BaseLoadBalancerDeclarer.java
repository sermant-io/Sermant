/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.declarer;

import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * The Ribbon BaseLoadBalancer enhanced load balancer class filters downstream instances
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class BaseLoadBalancerDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "com.netflix.loadbalancer.BaseLoadBalancer";

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.spring.interceptor.BaseLoadBalancerInterceptor";

    private static final String[] METHOD_NAME = {"getReachableServers", "getAllServers"};

    /**
     * Constructor
     */
    public BaseLoadBalancerDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, null);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    /**
     * Get the method matcher
     *
     * @return Method matcher
     */
    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.nameContains(METHOD_NAME);
    }
}