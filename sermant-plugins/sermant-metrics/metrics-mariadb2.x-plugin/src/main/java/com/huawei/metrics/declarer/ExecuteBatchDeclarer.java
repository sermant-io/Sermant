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

import com.huawei.metrics.interceptor.ExecuteBatchInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;

import org.mariadb.jdbc.internal.com.read.dao.Results;
import org.mariadb.jdbc.internal.util.dao.ClientPrepareResult;
import org.mariadb.jdbc.internal.util.dao.ServerPrepareResult;

import java.util.List;

/**
 * mariadb2.x sql批量执行方法拦截声明
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ExecuteBatchDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol";

    private static final String[] METHOD_NAMES = {"executeBatchServer", "executeBatchClient", "executeBatchStmt"};

    private static final int SERVER_PARAM_COUNT = 5;

    private static final int CLIENT_PARAM_COUNT = 4;

    private static final int STMT_PARAM_COUNT = 3;

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    /**
     * 获取插件的拦截声明
     * {@link org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol#executeBatchClient(boolean, Results,
     * ClientPrepareResult, List, boolean)}
     * {@link org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol#executeBatchStmt(boolean, Results, List)}
     * {@link org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol#executeBatchServer(boolean, ServerPrepareResult,
     * Results, String, List, boolean)}
     *
     * @param classLoader 被增强类的类加载器
     * @return 拦截声明集
     */
    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(
                        MethodMatcher.nameContains(METHOD_NAMES).and(MethodMatcher.paramCountEquals(STMT_PARAM_COUNT)
                                .or(MethodMatcher.paramCountEquals(CLIENT_PARAM_COUNT))
                                .or(MethodMatcher.paramCountEquals(SERVER_PARAM_COUNT))),
                        new ExecuteBatchInterceptor())
        };
    }
}
