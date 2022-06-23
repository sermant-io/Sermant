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

package com.huawei.registry.grace.declarers;

import com.huawei.registry.config.grace.GraceContext;
import com.huawei.registry.grace.interceptors.SpringLoadbalancerWarmUpInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * springcloud预热拦截，通过拦截选择服务的方法, 执行预热逻辑进行处理 当前拦截器仅对SpringCloud loadbalancer生效
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class SpringLoadbalancerWarmUpDeclarer extends AbstractPluginDeclarer {
    /**
     * 负载均衡增强
     */
    private static final String[] ENHANCE_CLASSES = {
        "org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer",
        "org.springframework.cloud.loadbalancer.core.RandomLoadBalancer"
    };

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = SpringLoadbalancerWarmUpInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        // 插件开始加载时间
        GraceContext.INSTANCE.setStartTime(System.currentTimeMillis());
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("getInstanceResponse")
                        .and(MethodMatcher.paramTypesEqual("java.util.List")), INTERCEPT_CLASS)
        };
    }
}
