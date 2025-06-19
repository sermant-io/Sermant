/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.tag.transmission.httpclientv5.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.tag.transmission.httpclientv5.interceptors.HttpClient5Interceptor;

/**
 * HttpClient enhanced declarer of traffic label transparent transmission, for 5.x version only
 *
 * @since 2025-06-24
 */
public class HttpClient5Declarer extends AbstractPluginDeclarer {

    private static final String[] ENHANCE_CLASSES = {
            "org.apache.hc.client5.http.impl.classic.InternalHttpClient",
            "org.apache.hc.client5.http.impl.classic.MinimalHttpClient"
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("doExecute")
                                .and(MethodMatcher.paramTypesEqual(
                                        "org.apache.hc.core5.http.HttpHost",
                                        "org.apache.hc.core5.http.ClassicHttpRequest",
                                        "org.apache.hc.core5.http.protocol.HttpContext")),
                        new HttpClient5Interceptor())
        };
    }
}
