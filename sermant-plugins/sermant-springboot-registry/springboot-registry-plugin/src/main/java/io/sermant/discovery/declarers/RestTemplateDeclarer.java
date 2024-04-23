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

package io.sermant.discovery.declarers;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.discovery.interceptors.RestTemplateInterceptor;

/**
 * For the RestTemplate request mode, obtain the instance list from the registry to intercept it
 *
 * @author chengyouling
 * @since 2022-09-27
 */
public class RestTemplateDeclarer extends BaseDeclarer {
    /**
     * The fully qualified name of the enhanced class
     */
    private static final String ENHANCE_CLASS = "org.springframework.web.client.RestTemplate";

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = RestTemplateInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("doExecute"), INTERCEPT_CLASS)
        };
    }
}
