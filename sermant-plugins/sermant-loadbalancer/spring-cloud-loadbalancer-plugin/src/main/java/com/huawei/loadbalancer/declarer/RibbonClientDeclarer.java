/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.loadbalancer.declarer;

import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 针对ribbon client请求增强, 主要配置负载均衡key, 用于后续负载均衡选择使用
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class RibbonClientDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "com.netflix.client.AbstractLoadBalancerAwareClient";

    private static final String INTERCEPT_CLASS = "com.huawei.loadbalancer.interceptor.RibbonClientInterceptor";

    private static final int ARGS_LENGTH = 2;

    /**
     * 构造方法
     */
    public RibbonClientDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, null);
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.nameEquals("buildLoadBalancerCommand").and(MethodMatcher.paramCountEquals(ARGS_LENGTH));
    }
}
