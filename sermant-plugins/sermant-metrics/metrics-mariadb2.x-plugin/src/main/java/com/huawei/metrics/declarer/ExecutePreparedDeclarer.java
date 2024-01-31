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

import com.huawei.metrics.interceptor.ExecutePreparedInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * mariadb2.x sql执行方法拦截声明
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ExecutePreparedDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    /**
     * 获取插件的拦截声明 {@link org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol#prepare(String, boolean)}
     *
     * @param classLoader 被增强类的类加载器
     * @return 拦截声明集
     */
    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{InterceptDeclarer.build(MethodMatcher.nameEquals("prepare"),
                new ExecutePreparedInterceptor())
        };
    }
}
