/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol.retry;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * Block the dubbo extensionLoader and inject cluster for retry
 *
 * @author zhouss
 * @since 2022-01-27
 */
public class ExtentionLoaderDeclarer extends AbstractPluginDeclarer {
    /**
     * the fully qualified name of the enhanced class
     */
    private static final String APACHE_ENHANCE_CLASS = "org.apache.dubbo.common.extension.ExtensionLoader";

    private static final String ALIBABA_ENHANCE_CLASS = "com.alibaba.dubbo.common.extension.ExtensionLoader";

    /**
     * the fully qualified name of the interceptor class
     */
    private static final String INTERCEPT_CLASS = ExtensionLoaderInterceptor.class.getCanonicalName();

    /**
     * get spi class
     */
    private static final String GET_EXTENSION_INTERCEPT_CLASS = ExtensionLoaderGetExtensionInterceptor.class
            .getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(APACHE_ENHANCE_CLASS, ALIBABA_ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals("getExtensionClasses"), INTERCEPT_CLASS),
                InterceptDeclarer.build(MethodMatcher.nameEquals("getExtension").and(ElementMatchers.takesArgument(0,
                        String.class)),
                        GET_EXTENSION_INTERCEPT_CLASS)
        };
    }
}
