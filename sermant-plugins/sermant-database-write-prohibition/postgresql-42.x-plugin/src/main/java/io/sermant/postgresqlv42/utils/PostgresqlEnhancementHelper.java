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

package io.sermant.postgresqlv42.utils;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.database.handler.DatabaseHandler;
import io.sermant.postgresqlv42.interceptors.PgStatementInterceptor;
import io.sermant.postgresqlv42.interceptors.QueryExecutorImplInterceptor;

/**
 * Helper class for postgresql42.x
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class PostgresqlEnhancementHelper {
    private static final String SEND_ONE_QUERY_METHOD_NAME = "sendOneQuery";

    private static final String ENHANCE_CLASS_NAME = "org.postgresql.core.v3.QueryExecutorImpl";

    private static final String INT_CLASS_NAME = "int";

    private static final String SIMPLE_QUERY_CLASS_NAME = "org.postgresql.core.v3.SimpleQuery";

    private static final String SIMPLE_PARAMETER_LIST_CLASS_NAME = "org.postgresql.core.v3.SimpleParameterList";

    private static final String EXECUTE_METHOD_NAME = "execute";

    private static final String EXECUTE_BATCH_METHOD_NAME = "executeBatch";

    private static final String PG_STATEMENT_CLASS_NAME = "org.postgresql.jdbc.PgStatement";

    private static final String QUERY_CLASS_NAME = "org.postgresql.core.Query";

    private static final String PARAMETER_LIST_CLASS_NAME = "org.postgresql.core.ParameterList";

    private static final String[] STATEMENT_EXECUTE_METHOD_PARAMS_TYPE = {
            QUERY_CLASS_NAME,
            PARAMETER_LIST_CLASS_NAME,
            INT_CLASS_NAME
    };

    private static final String[] EXECUTE_METHOD_PARAMS_TYPE = {
            SIMPLE_QUERY_CLASS_NAME,
            SIMPLE_PARAMETER_LIST_CLASS_NAME,
            INT_CLASS_NAME, INT_CLASS_NAME,
            INT_CLASS_NAME
    };

    private PostgresqlEnhancementHelper() {
    }

    private static MethodMatcher getSendOneQueryMethodMatcher() {
        return MethodMatcher.nameEquals(SEND_ONE_QUERY_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_METHOD_PARAMS_TYPE));
    }

    /**
     * Get the parameterized interceptor declarer for the QueryExecutorImpl sendOneQuery method
     *
     * @param handler Database write operation handler
     * @return InterceptDeclarer The parameterized interceptor declarer for the QueryExecutorImpl sendOneQuery method
     */
    public static InterceptDeclarer getSendOneQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getSendOneQueryMethodMatcher(), new QueryExecutorImplInterceptor(handler));
    }

    /**
     * Get the non-parameter interceptor declarer for the QueryExecutorImpl sendOneQuery method
     *
     * @return InterceptDeclarer The non-parameter interceptor declarer for the QueryExecutorImpl sendOneQuery method
     */
    public static InterceptDeclarer getSendOneQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getSendOneQueryMethodMatcher(), new QueryExecutorImplInterceptor());
    }

    /**
     * Get the ClassMatcher of the QueryExecutorImpl class
     *
     * @return ClassMatcher Class matcher
     */
    public static ClassMatcher getQueryExecutorImplClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS_NAME);
    }

    private static MethodMatcher getExecuteMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(STATEMENT_EXECUTE_METHOD_PARAMS_TYPE));
    }

    private static MethodMatcher getExecuteBatchMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_BATCH_METHOD_NAME);
    }

    /**
     * Get ClassMatcher for PgStatement class
     *
     * @return ClassMatcher Database write operation handler
     */
    public static ClassMatcher getPgStatementClassMatcher() {
        return ClassMatcher.nameEquals(PG_STATEMENT_CLASS_NAME);
    }

    /**
     * Get the non-parameter interceptor declarer for PgStatement execute method
     *
     * @return InterceptDeclarer The non-parameter interceptor declarer for PgStatement execute method
     */
    public static InterceptDeclarer getPgStatementExecuteInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteMethodMatcher(), new PgStatementInterceptor());
    }

    /**
     * Get the parameterized interceptor declarer for the PgStatement execute method
     *
     * @param handler Database write operation handler
     * @return InterceptDeclarer The parameterized interceptor declarer for the PgStatement execute method
     */
    public static InterceptDeclarer getPgStatementExecuteInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteMethodMatcher(), new PgStatementInterceptor(handler));
    }

    /**
     * Get the non-parameter interceptor declarer for PgStatement executeBatch method
     *
     * @return InterceptDeclarer The non-parameter interceptor declarer for PgStatement executeBatch method
     */
    public static InterceptDeclarer getPgStatementExecuteBatchInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchMethodMatcher(), new PgStatementInterceptor());
    }

    /**
     * Get the parameterized interceptor declarer for PgStatement executeBatch method
     *
     * @param handler Database write operation handler
     * @return InterceptDeclarer The parameterized interceptor declarer for PgStatement executeBatch method
     */
    public static InterceptDeclarer getPgStatementExecuteBatchInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchMethodMatcher(), new PgStatementInterceptor(handler));
    }
}
