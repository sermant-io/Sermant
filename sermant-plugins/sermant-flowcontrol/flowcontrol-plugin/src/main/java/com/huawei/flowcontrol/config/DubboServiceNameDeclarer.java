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

package com.huawei.flowcontrol.config;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * alibaba dubbo initialize the configuration intercept
 *
 * @author zhouss
 * @since 2022-01-27
 */
public class DubboServiceNameDeclarer extends AbstractPluginDeclarer {
    /**
     * alibaba dubbo configuration class
     */
    private static final String ALIBABA_ENHANCE_CLASS = "com.alibaba.dubbo.config.ApplicationConfig";

    /**
     * apache dubbo configuration class
     */
    private static final String APACHE_ENHANCE_CLASS = "org.apache.dubbo.config.ApplicationConfig";

    /**
     * the fully qualified name of the interceptor class
     */
    private static final String INTERCEPT_CLASS = DubboServiceNameInterceptor.class.getCanonicalName();

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ALIBABA_ENHANCE_CLASS, APACHE_ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
            InterceptDeclarer.build(MethodMatcher.nameEquals("setName"), INTERCEPT_CLASS)
        };
    }
}
