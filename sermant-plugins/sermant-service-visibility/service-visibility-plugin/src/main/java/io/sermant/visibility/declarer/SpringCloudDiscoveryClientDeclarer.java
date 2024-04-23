/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.visibility.declarer;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.visibility.interceptor.SpringCloudDiscoveryClientInterceptor;

/**
 * Spring service discovery interceptor Declarer
 *
 * @author zhp
 * @since 2022-12-05
 */
public class SpringCloudDiscoveryClientDeclarer extends AbstractCollectorDeclarer {

    private static final String ENHANCE_CLASS = "org.springframework.cloud.client.discovery.DiscoveryClient";

    private static final String INTERCEPT_CLASS = SpringCloudDiscoveryClientInterceptor.class.getCanonicalName();

    private static final String METHOD_NAME = "getInstances";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME), INTERCEPT_CLASS)};
    }
}
