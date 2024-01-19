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

package com.huawei.metrics.declarer;

import com.huawei.metrics.interceptor.PreparedStatementConstructorInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * Mysql5.1.x sql执行方法拦截声明
 *
 * @author zhp
 * @since 2024-01-15
 */
public class PreparedStatementConstructorDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "com.mysql.jdbc.PreparedStatement";

    private static final int[] PARAM_COUNTS = {3, 4};

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(
                        MethodMatcher.isConstructor().and(MethodMatcher.paramCountEquals(PARAM_COUNTS[0])
                                .or(MethodMatcher.paramCountEquals(PARAM_COUNTS[1]))),
                        new PreparedStatementConstructorInterceptor())
        };
    }
}
