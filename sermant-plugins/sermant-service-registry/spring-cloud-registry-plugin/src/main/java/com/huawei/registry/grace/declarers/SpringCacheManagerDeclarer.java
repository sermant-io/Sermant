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

import com.huawei.registry.grace.interceptors.SpringCacheManagerInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * Spring缓存管理器拦截
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringCacheManagerDeclarer extends AbstractPluginDeclarer {
    /**
     * 增强类的全限定名
     */
    private static final String[] CACHE_ENHANCE_CLASS = {
        "org.springframework.cloud.loadbalancer.cache.CaffeineBasedLoadBalancerCacheManager",
        "org.springframework.cloud.loadbalancer.cache.DefaultLoadBalancerCacheManager"
    };

    /**
     * 参数数量
     */
    private static final int CONSTRUCTOR_COUNT = 2;

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = SpringCacheManagerInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(CACHE_ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.isConstructor()
                                .and(MethodMatcher.paramCountEquals(CONSTRUCTOR_COUNT)), INTERCEPT_CLASS)
        };
    }
}
