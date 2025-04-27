/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
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

package io.sermant.xds.traffic.management.declarer;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.xds.traffic.management.interceptor.OkHttp3ClientInterceptor;

/**
 * For OKHTTP requests, obtain the instance list from the registry to block them
 *
 * @author zhp
 * @since 2024-12-20
 */
public class OkHttp3ClientDeclarer extends AbstractXdsDeclarer {
    /**
     * The fully qualified name of the enhanced okhttp request
     */
    private static final String[] ENHANCE_CLASSES = {
            "okhttp3.RealCall",
            "okhttp3.internal.connection.RealCall"
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameContains("execute", "getResponseWithInterceptorChain"),
                        new OkHttp3ClientInterceptor())
        };
    }
}
