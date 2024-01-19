/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.declarer.alibaba;

import com.huawei.metrics.declarer.AbstractDeclarer;
import com.huawei.metrics.interceptor.dubbo.alibaba.MonitorFilterInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * dubbo2.6.x监控过滤器增强声明
 *
 * @author zhp
 * @since 2023-10-17
 */
public class MonitorFilterDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "com.alibaba.dubbo.monitor.support.MonitorFilter";

    private static final String METHODS_NAME = "invoke";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{InterceptDeclarer.build(MethodMatcher.nameEquals(METHODS_NAME),
                new MonitorFilterInterceptor())};
    }
}
