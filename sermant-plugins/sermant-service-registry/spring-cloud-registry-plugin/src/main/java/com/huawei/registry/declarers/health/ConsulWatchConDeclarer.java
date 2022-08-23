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

package com.huawei.registry.declarers.health;

import com.huawei.registry.declarers.AbstractDoubleRegistryDeclarer;
import com.huawei.registry.interceptors.health.ConsulWatchInterceptor;
import com.huawei.registry.interceptors.health.ConsulWatchRequestInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * consul健康检测增强
 *
 * @author zhouss
 * @since 2021-12-17
 */
public class ConsulWatchConDeclarer extends AbstractDoubleRegistryDeclarer {
    /**
     * nacos心跳发送类
     */
    private static final String ENHANCE_CLASS = "org.springframework.cloud.consul.discovery.ConsulCatalogWatch";

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = ConsulWatchInterceptor.class.getCanonicalName();

    /**
     * 定时任务请求方法
     */
    private static final String HEALTH_REQUEST_INTERCEPT_CLASS = ConsulWatchRequestInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.isConstructor(), INTERCEPT_CLASS),
            InterceptDeclarer.build(MethodMatcher.nameEquals("catalogServicesWatch"), HEALTH_REQUEST_INTERCEPT_CLASS)
        };
    }
}
