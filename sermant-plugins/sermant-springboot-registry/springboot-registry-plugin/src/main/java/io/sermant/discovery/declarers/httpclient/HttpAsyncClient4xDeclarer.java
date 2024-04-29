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

package io.sermant.discovery.declarers.httpclient;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.discovery.declarers.BaseDeclarer;
import io.sermant.discovery.interceptors.httpclient.HttpAsyncClient4xInterceptor;

/**
 * Interception is handled for HttpAsyncClient 4.x
 *
 * @author zhouss
 * @since 2022-10-11
 */
public class HttpAsyncClient4xDeclarer extends BaseDeclarer {
    /**
     * Fully qualified HTTP requests for enhanced classes
     */
    private static final String[] ENHANCE_CLASSES = {
        "org.apache.http.impl.nio.client.InternalHttpAsyncClient",
        "org.apache.http.impl.nio.client.MinimalHttpAsyncClient"
    };

    /**
     * The fully qualified name of the interception class
     */
    private static final String INTERCEPT_CLASS = HttpAsyncClient4xInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("execute")
                                .and(MethodMatcher.paramTypesEqual(
                                        "org.apache.http.nio.protocol.HttpAsyncRequestProducer",
                                        "org.apache.http.nio.protocol.HttpAsyncResponseConsumer",
                                        "org.apache.http.protocol.HttpContext",
                                        "org.apache.http.concurrent.FutureCallback")),
                        INTERCEPT_CLASS)
        };
    }
}
