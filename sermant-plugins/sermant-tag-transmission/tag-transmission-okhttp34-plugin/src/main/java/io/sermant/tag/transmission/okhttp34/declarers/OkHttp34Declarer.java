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

package io.sermant.tag.transmission.okhttp34.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.tag.transmission.okhttp34.interceptors.OkHttp34Interceptor;

/**
 * OkHttp3.x,4.x tag transmission plugin declarer
 *
 * @since 2025-05-30
 */
public class OkHttp34Declarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASSES = "okhttp3.Request$Builder";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASSES);
    }

    /**
     * Get intercept declarer for OkHttp3.x,4.x plugin
     *
     * @return intercept declarer
     */
    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.nameEquals("build"),
                new OkHttp34Interceptor())
        };
    }
}
