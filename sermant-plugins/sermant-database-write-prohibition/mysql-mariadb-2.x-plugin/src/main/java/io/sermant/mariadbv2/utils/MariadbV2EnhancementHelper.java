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

package io.sermant.mariadbv2.utils;

import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;
import io.sermant.database.handler.DatabaseHandler;
import io.sermant.mariadbv2.interceptors.ExecuteBatchStmtInterceptor;
import io.sermant.mariadbv2.interceptors.ExecuteInterceptor;
import io.sermant.mariadbv2.interceptors.ExecuteServerInterceptor;
import io.sermant.mariadbv2.interceptors.PrepareInterceptor;

/**
 * mariadb2.x intercept point helper class
 *
 * @author daizhenyu
 * @since 2024-01-26
 **/
public class MariadbV2EnhancementHelper {
    private static final String QUERY_PROTOCOL_CLASS = "org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol";

    private static final String EXECUTE_QUERY_METHOD_NAME = "executeQuery";

    private static final String EXECUTE_BATCH_CLIENT_METHOD_NAME = "executeBatchClient";

    private static final String EXECUTE_BATCH_SERVER_METHOD_NAME = "executeBatchServer";

    private static final String EXECUTE_BATCH_STMT_METHOD_NAME = "executeBatchStmt";

    private static final String EXECUTE_PREPARED_QUERY_METHOD_NAME = "executePreparedQuery";

    private static final String PREPARE_METHOD_NAME = "prepare";

    private static final int FIRST_OVER_LOAD_METHOD_PARAM_COUNT = 3;

    private static final int SECOND_OVER_LOAD_METHOD_PARAM_COUNT = 4;

    private static final int THIRD_OVER_LOAD_METHOD_PARAM_COUNT = 5;

    private MariadbV2EnhancementHelper() {
    }

    /**
     * Gets the Class Matcher of the AbstractQueryProtocol class
     *
     * @return ClassMatcher class matcher
     */
    public static ClassMatcher getQueryProtocolClassMatcher() {
        return ClassMatcher.nameEquals(QUERY_PROTOCOL_CLASS);
    }

    /**
     * Get the AbstractQueryProtocol class sql to execute an array of parameterless interceptors
     *
     * @return InterceptDeclarer[] AbstractQueryProtocol sql to execute an array of parameterless interceptors
     */
    public static InterceptDeclarer[] getQueryProtocolInterceptDeclarers() {
        ExecuteInterceptor executeInterceptor = new ExecuteInterceptor();
        ExecuteServerInterceptor executeServerInterceptor = new ExecuteServerInterceptor();
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(getExecuteQueryMethodMatcher(), executeInterceptor),
                InterceptDeclarer.build(getExecuteBatchClientMethodMatcher(), executeInterceptor),
                InterceptDeclarer.build(getExecuteBatchStmtMethodMatcher(), new ExecuteBatchStmtInterceptor()),
                InterceptDeclarer.build(getExecutePreparedQueryMethodMatcher(), executeServerInterceptor),
                InterceptDeclarer.build(getExecuteBatchServerMethodMatcher(), executeServerInterceptor),
                InterceptDeclarer.build(getPrepareMethodMatcher(), new PrepareInterceptor())
        };
    }

    /**
     * Gets the parameterless interceptor for the executeQuery method of the AbstractQueryProtocol class
     *
     * @return InterceptDeclarer the parameterless interceptor for the executeQuery method
     */
    public static InterceptDeclarer getExecuteQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteQueryMethodMatcher(),
                new ExecuteInterceptor());
    }

    /**
     * Gets the parameterized interceptor of the AbstractQueryProtocol class executeQuery method
     *
     * @param handler database custom processor
     * @return InterceptDeclarer the parameterized interceptor of the executeQuery method
     */
    public static InterceptDeclarer getExecuteQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteQueryMethodMatcher(),
                new ExecuteInterceptor(handler));
    }

    /**
     * Gets the parameterless interceptor for the executeBatchClient method of the AbstractQueryProtocol class
     *
     * @return InterceptDeclarer the parameterless interceptor for the executeBatchClient method
     */
    public static InterceptDeclarer getExecuteBatchClientInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchClientMethodMatcher(),
                new ExecuteInterceptor());
    }

    /**
     * Gets the parameterized interceptor of the AbstractQueryProtocol class executeBatchClient method
     *
     * @param handler database custom processor
     * @return InterceptDeclarer the parameterized interceptor of the executeBatchClient method
     */
    public static InterceptDeclarer getExecuteBatchClientInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchClientMethodMatcher(),
                new ExecuteInterceptor(handler));
    }

    /**
     * Gets the parameterless interceptor for the executeBatchStmt method of the AbstractQueryProtocol class
     *
     * @return InterceptDeclarer the parameterless interceptor for the executeBatchStmt method
     */
    public static InterceptDeclarer getExecuteBatchStmtInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchStmtMethodMatcher(),
                new ExecuteBatchStmtInterceptor());
    }

    /**
     * Gets the parameterized interceptor of the AbstractQueryProtocol class executeBatchStmt method
     *
     * @param handler database custom processor
     * @return InterceptDeclarer A parameterized interceptor for the executeBatchStmt method
     */
    public static InterceptDeclarer getExecuteBatchStmtInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchStmtMethodMatcher(),
                new ExecuteBatchStmtInterceptor(handler));
    }

    /**
     * Gets the parameterless interceptor for the executePreparedQuery method of the AbstractQueryProtocol class
     *
     * @return InterceptDeclarer A parameterless interceptor for the executePreparedQuery method
     */
    public static InterceptDeclarer getExecutePreparedQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getExecutePreparedQueryMethodMatcher(),
                new ExecuteServerInterceptor());
    }

    /**
     * Gets the parameterized interceptor of the AbstractQueryProtocol class executePreparedQuery method
     *
     * @param handler database custom processor
     * @return InterceptDeclarer Parameter interceptor of the AbstractQueryProtocol class executePreparedQuery method
     */
    public static InterceptDeclarer getExecutePreparedQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecutePreparedQueryMethodMatcher(),
                new ExecuteServerInterceptor(handler));
    }

    /**
     * Gets the parameterless interceptor for the executeBatchServer method of the AbstractQueryProtocol class
     *
     * @return InterceptDeclarer Parameterless interceptor for the AbstractQueryProtocol class executeBatchServer method
     */
    public static InterceptDeclarer getExecuteBatchServerInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchServerMethodMatcher(),
                new ExecuteServerInterceptor());
    }

    /**
     * Gets the parameterised interceptor of the AbstractQueryProtocol class executeBatchServer method
     *
     * @param handler database custom processor
     * @return InterceptDeclarer Parameter interceptor of the AbstractQueryProtocol class executeBatchServer method
     */
    public static InterceptDeclarer getExecuteBatchServerInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchServerMethodMatcher(),
                new ExecuteServerInterceptor(handler));
    }

    /**
     * Gets the parameterless interceptor for the prepare method of the AbstractQueryProtocol class
     *
     * @return InterceptDeclarer The parameterless interceptor of the AbstractQueryProtocol class prepare method
     */
    public static InterceptDeclarer getPrepareInterceptDeclarer() {
        return InterceptDeclarer.build(getPrepareMethodMatcher(),
                new PrepareInterceptor());
    }

    /**
     * Gets the parameterized interceptor of the AbstractQueryProtocol class prepare method
     *
     * @param handler database custom processor
     * @return InterceptDeclarer The parameterized interceptor of the AbstractQueryProtocol class prepare method
     */
    public static InterceptDeclarer getPrepareInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getPrepareMethodMatcher(),
                new PrepareInterceptor(handler));
    }

    private static MethodMatcher getExecuteQueryMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_QUERY_METHOD_NAME)
                .and(MethodMatcher.paramCountEquals(FIRST_OVER_LOAD_METHOD_PARAM_COUNT)
                        .or(MethodMatcher.paramCountEquals(SECOND_OVER_LOAD_METHOD_PARAM_COUNT))
                        .or(MethodMatcher.paramCountEquals(THIRD_OVER_LOAD_METHOD_PARAM_COUNT)));
    }

    private static MethodMatcher getExecuteBatchStmtMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_BATCH_STMT_METHOD_NAME);
    }

    private static MethodMatcher getExecuteBatchClientMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_BATCH_CLIENT_METHOD_NAME);
    }

    private static MethodMatcher getExecutePreparedQueryMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_PREPARED_QUERY_METHOD_NAME);
    }

    private static MethodMatcher getExecuteBatchServerMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_BATCH_SERVER_METHOD_NAME);
    }

    private static MethodMatcher getPrepareMethodMatcher() {
        return MethodMatcher.nameEquals(PREPARE_METHOD_NAME);
    }
}
