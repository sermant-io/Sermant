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

package com.huaweicloud.sermant.mongodb.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.mongodb.constant.MethodParamTypeName;
import com.huaweicloud.sermant.mongodb.interceptors.ExecuteCommandInterceptor;
import com.huaweicloud.sermant.mongodb.interceptors.ExecuteRetryableCommandInterceptor;
import com.huaweicloud.sermant.mongodb.interceptors.ExecuteWriteCommandInterceptor;
import com.huaweicloud.sermant.mongodb.interceptors.MixedBulkWriteOperationInterceptor;

/**
 * mongodb拦截点辅助类
 *
 * @author daizhenyu
 * @since 2024-01-16
 **/
public class MongoDbEnhancementHelper {
    private static final String MIXED_BULK_WRITE_CLASS = "com.mongodb.internal.operation.MixedBulkWriteOperation";

    private static final String COMMAND_OPERATION_CLASS = "com.mongodb.internal.operation.CommandOperationHelper";

    private static final String EXECUTE_METHOD_NAME = "execute";

    private static final String EXECUTE_COMMAND_METHOD_NAME = "executeCommand";

    private static final String EXECUTE_WRITE_COMMAND_METHOD_NAME = "executeWriteCommand";

    private static final String EXECUTE_RETRYABLE_COMMAND_METHOD_NAME = "executeRetryableCommand";

    private static final int METHOD_PARAM_COUNT = 9;

    private static final String[] EXECUTE_COMMAND_PARAMS_TYPE = {
            MethodParamTypeName.STRING_CLASS_NAME,
            MethodParamTypeName.BSON_DOCUMENT_CLASS_NAME,
            MethodParamTypeName.FIELD_NAME_VALIDATOR_CLASS_NAME,
            MethodParamTypeName.DECODER_CLASS_NAME,
            MethodParamTypeName.CONNECTION_SOURCE_CLASS_NAME,
            MethodParamTypeName.CONNECTION_CLASS_NAME,
            MethodParamTypeName.READ_PREFERENCE_CLASS_NAME
    };

    private static final String[] EXECUTE_RETRY_COMMAND_PARAMS_TYPE = {
            MethodParamTypeName.WRITE_BINDING_CLASS_NAME,
            MethodParamTypeName.STRING_CLASS_NAME,
            MethodParamTypeName.READ_PREFERENCE_CLASS_NAME,
            MethodParamTypeName.FIELD_NAME_VALIDATOR_CLASS_NAME,
            MethodParamTypeName.DECODER_CLASS_NAME,
            MethodParamTypeName.COMMAND_CREATOR_CLASS_NAME,
            MethodParamTypeName.COMMAND_WRITE_TRANSFORMER_CLASS_NAME,
            MethodParamTypeName.FUNCTION_CLASS_NAME
    };

    private MongoDbEnhancementHelper() {
    }

    /**
     * 获取CommandOperationHelper类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getCommandOperationHelperClassMatcher() {
        return ClassMatcher.nameEquals(COMMAND_OPERATION_CLASS);
    }

    /**
     * 获取MixedBulkWriteOperation类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getMixedBulkWriteOperationClassMatcher() {
        return ClassMatcher.nameEquals(MIXED_BULK_WRITE_CLASS);
    }

    /**
     * 获取MixedBulkWriteOperation写操作无参拦截器数组
     *
     * @return InterceptDeclarer[] MixedBulkWriteOperation写操作无参拦截器数组
     */
    public static InterceptDeclarer[] getMixedBulkWriteOperationInterceptDeclarers() {
        MixedBulkWriteOperationInterceptor mixedBulkWriteOperationInterceptor =
                new MixedBulkWriteOperationInterceptor();
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(getExecuteMethodMatcher(), mixedBulkWriteOperationInterceptor),
        };
    }

    /**
     * 获取MixedBulkWriteOperation execute方法无参拦截器
     *
     * @return InterceptDeclarer MixedBulkWriteOperation execute方法无参拦截器
     */
    public static InterceptDeclarer getExecuteInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteMethodMatcher(),
                new MixedBulkWriteOperationInterceptor());
    }

    /**
     * 获取MixedBulkWriteOperation execute方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer MixedBulkWriteOperation execute方法有参拦截器
     */
    public static InterceptDeclarer getExecuteInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteMethodMatcher(),
                new MixedBulkWriteOperationInterceptor(handler));
    }

    /**
     * 获取CommandOperationHelper写操作无参拦截器数组
     *
     * @return InterceptDeclarer[] CommandOperationHelper写操作无参拦截器数组
     */
    public static InterceptDeclarer[] getCommandOperationHelperInterceptDeclarers() {
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(getExecuteCommandMethodMatcher(), new ExecuteCommandInterceptor()),
                InterceptDeclarer.build(getExecuteWriteCommandMethodMatcher(), new ExecuteWriteCommandInterceptor()),
                InterceptDeclarer.build(getExecuteRetryableCommandMethodMatcher(),
                        new ExecuteRetryableCommandInterceptor())
        };
    }

    /**
     * 获取CommandOperationHelper executeCommand方法无参拦截器
     *
     * @return InterceptDeclarer CommandOperationHelper executeCommand方法无参拦截器
     */
    public static InterceptDeclarer getExecuteCommandInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteCommandMethodMatcher(), new ExecuteCommandInterceptor());
    }

    /**
     * 获取CommandOperationHelper executeCommand方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer CommandOperationHelper executeCommand方法有参拦截器
     */
    public static InterceptDeclarer getExecuteCommandInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteCommandMethodMatcher(), new ExecuteCommandInterceptor(handler));
    }

    /**
     * 获取CommandOperationHelper executeWriteCommand方法无参拦截器
     *
     * @return InterceptDeclarer CommandOperationHelper executeWriteCommand方法无参拦截器
     */
    public static InterceptDeclarer getExecuteWriteCommandInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteWriteCommandMethodMatcher(), new ExecuteWriteCommandInterceptor());
    }

    /**
     * 获取CommandOperationHelper executeWriteCommand方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer CommandOperationHelper executeWriteCommand方法有参拦截器
     */
    public static InterceptDeclarer getExecuteWriteCommandInterceptDeclarer(
            DatabaseHandler handler) {
        return InterceptDeclarer
                .build(getExecuteWriteCommandMethodMatcher(), new ExecuteWriteCommandInterceptor(handler));
    }

    /**
     * 获取CommandOperationHelper executeRetryableCommand方法无参拦截器
     *
     * @return InterceptDeclarer CommandOperationHelper executeRetryableCommand方法无参拦截器
     */
    public static InterceptDeclarer getExecuteRetryableCommandInterceptDeclarer() {
        return InterceptDeclarer
                .build(getExecuteRetryableCommandMethodMatcher(), new ExecuteRetryableCommandInterceptor());
    }

    /**
     * 获取CommandOperationHelper executeRetryableCommand方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer CommandOperationHelper executeRetryableCommand方法有参拦截器
     */
    public static InterceptDeclarer getExecuteRetryableCommandInterceptDeclarer(
            DatabaseHandler handler) {
        return InterceptDeclarer
                .build(getExecuteRetryableCommandMethodMatcher(), new ExecuteRetryableCommandInterceptor(handler));
    }

    private static MethodMatcher getExecuteMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_METHOD_NAME);
    }

    private static MethodMatcher getExecuteCommandMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_COMMAND_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_COMMAND_PARAMS_TYPE));
    }

    private static MethodMatcher getExecuteWriteCommandMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_WRITE_COMMAND_METHOD_NAME)
                .and(MethodMatcher.paramCountEquals(METHOD_PARAM_COUNT));
    }

    private static MethodMatcher getExecuteRetryableCommandMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_RETRYABLE_COMMAND_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_RETRY_COMMAND_PARAMS_TYPE));
    }
}
