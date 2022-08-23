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

package com.huaweicloud.loadbalancer.declarer;

import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * RandomLoadBalancer增强类
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class RandomDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.springframework.cloud.loadbalancer.core.RandomLoadBalancer";

    private static final String INTERCEPT_CLASS = "com.huaweicloud.loadbalancer.interceptor.LoadBalancerInterceptor";

    /**
     * 构造方法
     */
    public RandomDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, null);
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.isConstructor();
    }
}
