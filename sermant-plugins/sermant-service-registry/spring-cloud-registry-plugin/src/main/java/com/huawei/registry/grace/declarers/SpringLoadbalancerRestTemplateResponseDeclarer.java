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

import com.huawei.registry.grace.interceptors.SpringLoadbalancerRestTemplateResponseInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 拦截负载均衡请求方法, 仅针对RestTemplate请求
 *
 * @author zhouss
 * @since 2022-05-25
 */
public class SpringLoadbalancerRestTemplateResponseDeclarer extends AbstractPluginDeclarer {
    private static final String[] ENHANCE_CLASSES = {
        "org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor",
        "org.springframework.cloud.client.loadbalancer.RetryLoadBalancerInterceptor"
    };

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS =
        SpringLoadbalancerRestTemplateResponseInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("intercept")
                        .and(MethodMatcher.paramTypesEqual("org.springframework.http.HttpRequest", "byte[]",
                                "org.springframework.http.client.ClientHttpRequestExecution")), INTERCEPT_CLASS)
        };
    }
}
