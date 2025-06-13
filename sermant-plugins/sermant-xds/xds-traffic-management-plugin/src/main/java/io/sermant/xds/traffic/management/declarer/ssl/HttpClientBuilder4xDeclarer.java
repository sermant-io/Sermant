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

package io.sermant.xds.traffic.management.declarer.ssl;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.xds.traffic.management.declarer.AbstractXdsDeclarer;
import io.sermant.xds.traffic.management.interceptor.ssl.HttpClientBuilder4xInterceptor;

/**
 * HttpClientBuilder declarer
 *
 * @author lilai
 * @since 2025-06-06
 */
public class HttpClientBuilder4xDeclarer extends AbstractXdsDeclarer {
    private static final String ENHANCE_CLASS = "org.apache.http.impl.client.HttpClientBuilder";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("build"),
                        new HttpClientBuilder4xInterceptor())
        };
    }
}
