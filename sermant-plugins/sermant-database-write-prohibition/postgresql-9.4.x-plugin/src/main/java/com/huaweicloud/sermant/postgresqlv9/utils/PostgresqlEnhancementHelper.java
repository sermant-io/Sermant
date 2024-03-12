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
import com.huaweicloud.sermant.postgresqlv9.interceptors.Jdbc2StatementInterceptor;
import com.huaweicloud.sermant.postgresqlv9.interceptors.QueryExecutorImplInterceptor;

/**
 * Helper class for postgresql9.x
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class PostgresqlEnhancementHelper {
    private static final String SEND_QUERY_METHOD_NAME = "sendQuery";

    private static final String V2_QUERY_EXECUTOR_CLASS_NAME = "org.postgresql.core.v2.QueryExecutorImpl";

    private static final String INT_CLASS_NAME = "int";

    private static final String QUERY_CLASS_NAME = "org.postgresql.core.Query";

    private static final String PARAMETER_LIST_CLASS_NAME = "org.postgresql.core.ParameterList";

    private static final String V2_QUERY_CLASS_NAME = "org.postgresql.core.v2.V2Query";

    private static final String V2_SIMPLE_PARAMETER_LIST_CLASS_NAME = "org.postgresql.core.v2.SimpleParameterList";

    private static final String STRING_CLASS_NAME = "java.lang.String";

    private static final String[] SEND_QUERY_METHOD_PARAMS_TYPE = {
            V2_QUERY_CLASS_NAME,
            V2_SIMPLE_PARAMETER_LIST_CLASS_NAME,
            STRING_CLASS_NAME
    };

    private static final String EXECUTE_METHOD_NAME = "execute";

    private static final String EXECUTE_BATCH_METHOD_NAME = "executeBatch";

    private static final String STATEMENT_CLASS_NAME = "org.postgresql.jdbc2.AbstractJdbc2Statement";

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
     * Get the parameterized interceptor declarer for the org.postgresql.core.v2.QueryExecutorImpl sendQuery method
     *
     * @param handler Database write operation handler
     * @return InterceptDeclarer The parameterized interceptor declarer for the QueryExecutorImpl sendQuery method
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(), new QueryExecutorImplInterceptor(handler));
    }

    /**
     * Get the non-parameter interceptor declarer for org.postgresql.core.v2.QueryExecutorImpl sendQuery method
     *
     * @return InterceptDeclarer The non-parameter interceptor declarer for QueryExecutorImpl sendQuery method
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(), new QueryExecutorImplInterceptor());
    }

    /**
     * Get ClassMatcher for the org.postgresql.core.v2.QueryExecutorImpl class
     *
     * @return ClassMatcher Class matcher
     */
    public static ClassMatcher getQueryExecutorImplV2ClassMatcher() {
        return ClassMatcher.nameEquals(V2_QUERY_EXECUTOR_CLASS_NAME);
    }

    /**
     * Get ClassMatcher for AbstractJdbc2Statement class
     *
     * @return ClassMatcher Database write operation handler
     */
    public static ClassMatcher getJdbc2StatementClassMatcher() {
        return ClassMatcher.nameEquals(STATEMENT_CLASS_NAME);
    }

    /**
     * Get the non-parameter interceptor declarer for AbstractJdbc2Statement execute method
     *
     * @return InterceptDeclarer The non-parameter interceptor declarer for AbstractJdbc2Statement execute method
     */
    public static InterceptDeclarer getExecuteInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteMethodMatcher(), new Jdbc2StatementInterceptor());
    }

    /**
     * Get the parameterized interceptor declarer for the AbstractJdbc2Statement execute method
     *
     * @param handler Database write operation handler
     * @return InterceptDeclarer The parameterized interceptor declarer for the AbstractJdbc2Statement execute method
     */
    public static InterceptDeclarer getExecuteInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteMethodMatcher(), new Jdbc2StatementInterceptor(handler));
    }

    /**
     * Get the non-parameter interceptor declarer for AbstractJdbc2Statement executeBatch method
     *
     * @return InterceptDeclarer The non-parameter interceptor declarer for AbstractJdbc2Statement executeBatch method
     */
    public static InterceptDeclarer getExecuteBatchInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchMethodMatcher(), new Jdbc2StatementInterceptor());
    }

    /**
     * Get the parameterized interceptor declarer for AbstractJdbc2Statement executeBatch method
     *
     * @param handler Database write operation handler
     * @return InterceptDeclarer The parameterized interceptor declarer for AbstractJdbc2Statement executeBatch method
     */
    public static InterceptDeclarer getExecuteBatchInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchMethodMatcher(), new Jdbc2StatementInterceptor(handler));
    }

    private static MethodMatcher getExecuteMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_METHOD_PARAMS_TYPE));
    }

    private static MethodMatcher getExecuteBatchMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_BATCH_METHOD_NAME);
    }
}
