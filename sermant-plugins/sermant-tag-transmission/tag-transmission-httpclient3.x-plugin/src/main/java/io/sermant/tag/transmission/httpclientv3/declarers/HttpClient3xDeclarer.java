/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.tag.transmission.httpclientv3.declarers;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.tag.transmission.httpclientv3.interceptors.HttpClient3xInterceptor;

/**
 * HttpClient enhanced declarer of traffic label transparent transmission, for 3.x version only
 *
 * @author lilai
 * @since 2023-08-08
 */
public class HttpClient3xDeclarer extends AbstractPluginDeclarer {
    /**
     * the fully qualified name of the enhanced class
     */
    private static final String ENHANCE_CLASSES = "org.apache.commons.httpclient.HttpClient";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASSES);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("executeMethod")
                                .and(MethodMatcher.paramTypesEqual(
                                        "org.apache.commons.httpclient.HostConfiguration",
                                        "org.apache.commons.httpclient.HttpMethod",
                                        "org.apache.commons.httpclient.HttpState")),
                        new HttpClient3xInterceptor())
        };
    }
}
