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

package com.huaweicloud.sermant.tag.transmission.declarers;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.tag.transmission.interceptors.ApacheDubboConsumerInterceptor;

/**
 * dubbo流量标签透传的consumer端增强声明，支持dubbo2.7.x, 3.x版本
 *
 * @author daizhenyu
 * @since 2023-08-12
 **/
public class ApacheDubboConsumerDeclarer extends AbstractPluginDeclarer {
    /**
     * 增强类的全限定名、拦截器、拦截方法
     */
    private static final String ENHANCE_CLASS_APACHE_DUBBO_V3 =
            "org.apache.dubbo.rpc.cluster.filter.support.ConsumerContextFilter";

    private static final String ENHANCE_CLASS_APACHE_DUBBO_V2 = "org.apache.dubbo.rpc.filter.ConsumerContextFilter";

    private static final String INTERCEPT_CLASS = ApacheDubboConsumerInterceptor.class.getCanonicalName();

    private static final String METHOD_NAME = "invoke";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASS_APACHE_DUBBO_V3, ENHANCE_CLASS_APACHE_DUBBO_V2);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(MethodMatcher.nameEquals(METHOD_NAME), INTERCEPT_CLASS)
        };
    }
}
