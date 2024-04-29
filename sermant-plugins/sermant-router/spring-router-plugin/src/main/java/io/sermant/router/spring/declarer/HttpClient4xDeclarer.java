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

package io.sermant.router.spring.declarer;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.router.spring.interceptor.HttpClient4xInterceptor;

/**
 * For HTTP requests, obtain the instance list from the registry to intercept the <h1>4.x version only</h1>
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class HttpClient4xDeclarer extends BaseRegistryPluginAdaptationDeclarer {
    /**
     * Fully qualified HTTP requests for enhanced classes
     */
    private static final String[] ENHANCE_CLASSES = {
            "org.apache.http.impl.client.AbstractHttpClient",
            "org.apache.http.impl.client.DefaultRequestDirector",
            "org.apache.http.impl.client.InternalHttpClient",
            "org.apache.http.impl.client.MinimalHttpClient"
    };

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = HttpClient4xInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameContains("doExecute", "execute")
                                .and(MethodMatcher.paramTypesEqual(
                                        "org.apache.http.HttpHost",
                                        "org.apache.http.HttpRequest",
                                        "org.apache.http.protocol.HttpContext")),
                        INTERCEPT_CLASS)
        };
    }
}
