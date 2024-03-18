/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.declarer;

import com.huaweicloud.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.router.dubbo.interceptor.Dubbo3AbstractDirectoryInterceptor;

/**
 * 增强AbstractDirectory的子类的doList方法，筛选标签应用的地址
 *
 * @author chengyouling
 * @since 2024-02-20
 */
public class Dubbo3AbstractDirectoryDeclarer extends AbstractPluginDeclarer {
    private static final String APACHE_ENHANCE_CLASS = "org.apache.dubbo.rpc.cluster.directory.AbstractDirectory";

    private static final String METHOD_NAME = "doList";

    private static final int PARAMETER_COUNT = 3;

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.isExtendedFrom(APACHE_ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(getMethodMatcher(), new Dubbo3AbstractDirectoryInterceptor())
        };
    }

    /**
     * 获取方法匹配器
     *
     * @return 方法匹配器
     */
    public MethodMatcher getMethodMatcher() {
        return MethodMatcher.nameEquals(METHOD_NAME).and(MethodMatcher.paramCountEquals(PARAMETER_COUNT));
    }
}