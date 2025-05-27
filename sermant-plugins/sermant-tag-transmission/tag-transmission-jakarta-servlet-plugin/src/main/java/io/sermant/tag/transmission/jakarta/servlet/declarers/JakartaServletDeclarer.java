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

package io.sermant.tag.transmission.jakarta.servlet.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.tag.transmission.jakarta.servlet.interceptors.JakartaServletInterceptor;

/**
 * Servlet 5.x, 6.x enhanced declarer of traffic label transparent transmission
 *
 * @since 2025-05-22
 */
public class JakartaServletDeclarer extends AbstractPluginDeclarer {
    private static final String ENHANCE_CLASS = "jakarta.servlet.http.HttpServlet";

    private static final String METHOD_NAME = "service";

    private static final String[] METHOD_ARGS = {
        "jakarta.servlet.http.HttpServletRequest",
        "jakarta.servlet.http.HttpServletResponse"
    };

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(
                MethodMatcher.nameEquals(METHOD_NAME)
                    .and(MethodMatcher.paramTypesEqual(METHOD_ARGS[0],METHOD_ARGS[1])),
                new JakartaServletInterceptor())
        };
    }
}
