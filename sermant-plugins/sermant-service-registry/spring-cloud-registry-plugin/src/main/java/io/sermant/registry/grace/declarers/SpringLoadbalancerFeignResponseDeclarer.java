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
import io.sermant.registry.grace.interceptors.SpringLoadbalancerFeignResponseInterceptor;

/**
 * Intercept load balancer request execution, including ribbon and spring loadbalacner request execution
 *
 * @author zhouss
 * @since 2022-05-25
 */
public class SpringLoadbalancerFeignResponseDeclarer extends AbstractPluginDeclarer {
    private static final String[] ENHANCE_CLASSES = {
        "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient",
        "org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient",
        "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient",
        "org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient"
    };

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = SpringLoadbalancerFeignResponseInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
                InterceptDeclarer.build(MethodMatcher.nameEquals("execute")
                        .and(MethodMatcher.paramTypesEqual("feign.Request", "feign.Request$Options")), INTERCEPT_CLASS)
        };
    }
}
