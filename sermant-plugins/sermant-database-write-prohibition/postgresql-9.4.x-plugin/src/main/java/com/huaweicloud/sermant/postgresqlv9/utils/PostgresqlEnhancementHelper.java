/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.postgresqlv9.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.postgresqlv9.interceptors.Jdbc4StatementInterceptor;
import com.huaweicloud.sermant.postgresqlv9.interceptors.QueryExecutorImplInterceptor;

/**
 * postgresql拦截点辅助类
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class PostgresqlEnhancementHelper {
    private static final String SEND_QUERY_METHOD_NAME = "sendQuery";

    private static final String QUERY_EXECUTOR_CLASS_NAME = "org.postgresql.core.v3.QueryExecutorImpl";

    private static final String INT_CLASS_NAME = "int";

    private static final String QUERY_CLASS_NAME = "org.postgresql.core.Query";

    private static final String PARAMETER_LIST_CLASS_NAME = "org.postgresql.core.ParameterList";

    private static final String ERROR_TRACKING_RESULT_HANDLER_CLASS_NAME =
            "org.postgresql.core.v3.ErrorTrackingResultHandler";

    private static final String[] SEND_QUERY_METHOD_PARAMS_TYPE = {
            QUERY_CLASS_NAME,
            PARAMETER_LIST_CLASS_NAME,
            INT_CLASS_NAME,
            INT_CLASS_NAME,
            INT_CLASS_NAME,
            ERROR_TRACKING_RESULT_HANDLER_CLASS_NAME
    };

    private static final String EXECUTE_METHOD_NAME = "execute";

    private static final String EXECUTE_BATCH_METHOD_NAME = "executeBatch";

    private static final String STATEMENT_CLASS_NAME = "org.postgresql.jdbc4.Jdbc4Statement";

    private static final String[] EXECUTE_METHOD_PARAMS_TYPE = {
            QUERY_CLASS_NAME,
            PARAMETER_LIST_CLASS_NAME,
            INT_CLASS_NAME
    };

    private PostgresqlEnhancementHelper() {
    }

    private static MethodMatcher getSendQueryMethodMatcher() {
        return MethodMatcher.nameEquals(SEND_QUERY_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(SEND_QUERY_METHOD_PARAMS_TYPE));
    }

    /**
     * 获取QueryExecutorImpl sendQuery方法有参拦截声明器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer QueryExecutorImpl sendQuery方法有参拦截声明器
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(), new QueryExecutorImplInterceptor(handler));
    }

    /**
     * 获取QueryExecutorImpl sendQuery方法无参拦截声明器
     *
     * @return InterceptDeclarer QueryExecutorImpl sendQuery方法无参拦截声明器
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(), new QueryExecutorImplInterceptor());
    }

    /**
     * 获取QueryExecutorImpl类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getQueryExecutorImplClassMatcher() {
        return ClassMatcher.nameEquals(QUERY_EXECUTOR_CLASS_NAME);
    }

    /**
     * 获取Jdbc4Statement类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getJdbc4StatementClassMatcher() {
        return ClassMatcher.nameEquals(STATEMENT_CLASS_NAME);
    }

    /**
     * 获取Jdbc4Statement execute方法无参拦截声明器
     *
     * @return InterceptDeclarer Jdbc4Statement execute方法无参拦截声明器
     */
    public static InterceptDeclarer getExecuteInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteMethodMatcher(), new Jdbc4StatementInterceptor());
    }

    /**
     * 获取Jdbc4Statement execute方法有参拦截声明器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer Jdbc4Statement execute方法有参拦截声明器
     */
    public static InterceptDeclarer getExecuteInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteMethodMatcher(), new Jdbc4StatementInterceptor(handler));
    }

    /**
     * 获取Jdbc4Statement executeBatch方法无参拦截声明器
     *
     * @return InterceptDeclarer Jdbc4Statement executeBatch方法无参拦截声明器
     */
    public static InterceptDeclarer getExecuteBatchInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchMethodMatcher(), new Jdbc4StatementInterceptor());
    }

    /**
     * 获取Jdbc4Statement executeBatch方法有参拦截声明器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer Jdbc4Statement executeBatch方法有参拦截声明器
     */
    public static InterceptDeclarer getExecuteBatchInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchMethodMatcher(), new Jdbc4StatementInterceptor(handler));
    }

    private static MethodMatcher getExecuteMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_METHOD_PARAMS_TYPE));
    }

    private static MethodMatcher getExecuteBatchMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_BATCH_METHOD_NAME);
    }
}
