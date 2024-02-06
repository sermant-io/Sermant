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

package com.huaweicloud.sermant.mariadbv2.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.mariadbv2.interceptors.ExecuteBatchStmtInterceptor;
import com.huaweicloud.sermant.mariadbv2.interceptors.ExecuteInterceptor;
import com.huaweicloud.sermant.mariadbv2.interceptors.ExecuteServerInterceptor;
import com.huaweicloud.sermant.mariadbv2.interceptors.PrepareInterceptor;

/**
 * mariadb2.x拦截点辅助类
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
     * 获取AbstractQueryProtocol类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getQueryProtocolClassMatcher() {
        return ClassMatcher.nameEquals(QUERY_PROTOCOL_CLASS);
    }

    /**
     * 获取AbstractQueryProtocol类sql执行无参拦截器数组
     *
     * @return InterceptDeclarer[] AbstractQueryProtocol类sql执行无参拦截器数组
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
     * 获取AbstractQueryProtocol类executeQuery方法无参拦截器
     *
     * @return InterceptDeclarer AbstractQueryProtocol类executeQuery方法无参拦截器
     */
    public static InterceptDeclarer getExecuteQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteQueryMethodMatcher(),
                new ExecuteInterceptor());
    }

    /**
     * 获取AbstractQueryProtocol类executeQuery方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer AbstractQueryProtocol类execute方法有参拦截器
     */
    public static InterceptDeclarer getExecuteQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteQueryMethodMatcher(),
                new ExecuteInterceptor(handler));
    }

    /**
     * 获取AbstractQueryProtocol类executeBatchClient方法无参拦截器
     *
     * @return InterceptDeclarer AbstractQueryProtocol类executeBatchClient方法无参拦截器
     */
    public static InterceptDeclarer getExecuteBatchClientInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchClientMethodMatcher(),
                new ExecuteInterceptor());
    }

    /**
     * 获取AbstractQueryProtocol类executeBatchClient方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer AbstractQueryProtocol类executeBatchClient方法有参拦截器
     */
    public static InterceptDeclarer getExecuteBatchClientInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchClientMethodMatcher(),
                new ExecuteInterceptor(handler));
    }

    /**
     * 获取AbstractQueryProtocol类executeBatchStmt方法无参拦截器
     *
     * @return InterceptDeclarer AbstractQueryProtocol类executeBatchStmt方法无参拦截器
     */
    public static InterceptDeclarer getExecuteBatchStmtInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchStmtMethodMatcher(),
                new ExecuteBatchStmtInterceptor());
    }

    /**
     * 获取AbstractQueryProtocol类executeBatchStmt方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer AbstractQueryProtocol类executeBatchStmt方法有参拦截器
     */
    public static InterceptDeclarer getExecuteBatchStmtInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchStmtMethodMatcher(),
                new ExecuteBatchStmtInterceptor(handler));
    }

    /**
     * 获取AbstractQueryProtocol类executePreparedQuery方法无参拦截器
     *
     * @return InterceptDeclarer AbstractQueryProtocol类executePreparedQuery方法无参拦截器
     */
    public static InterceptDeclarer getExecutePreparedQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getExecutePreparedQueryMethodMatcher(),
                new ExecuteServerInterceptor());
    }

    /**
     * 获取AbstractQueryProtocol类executePreparedQuery方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer AbstractQueryProtocol类executePreparedQuery方法有参拦截器
     */
    public static InterceptDeclarer getExecutePreparedQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecutePreparedQueryMethodMatcher(),
                new ExecuteServerInterceptor(handler));
    }

    /**
     * 获取AbstractQueryProtocol类executeBatchServer方法无参拦截器
     *
     * @return InterceptDeclarer AbstractQueryProtocol类executeBatchServer方法无参拦截器
     */
    public static InterceptDeclarer getExecuteBatchServerInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteBatchServerMethodMatcher(),
                new ExecuteServerInterceptor());
    }

    /**
     * 获取AbstractQueryProtocol类executeBatchServer方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer AbstractQueryProtocol类executeBatchServer方法有参拦截器
     */
    public static InterceptDeclarer getExecuteBatchServerInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteBatchServerMethodMatcher(),
                new ExecuteServerInterceptor(handler));
    }

    /**
     * 获取AbstractQueryProtocol类prepare方法无参拦截器
     *
     * @return InterceptDeclarer AbstractQueryProtocol类prepare方法无参拦截器
     */
    public static InterceptDeclarer getPrepareInterceptDeclarer() {
        return InterceptDeclarer.build(getPrepareMethodMatcher(),
                new PrepareInterceptor());
    }

    /**
     * 获取AbstractQueryProtocol类prepare方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer AbstractQueryProtocol类prepare方法有参拦截器
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
