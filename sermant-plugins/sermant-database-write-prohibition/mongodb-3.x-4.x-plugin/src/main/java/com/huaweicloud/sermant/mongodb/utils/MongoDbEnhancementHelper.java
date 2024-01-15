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
import com.huaweicloud.sermant.mongodb.interceptors.ExecuteCommandInterceptor;
import com.huaweicloud.sermant.mongodb.interceptors.ExecuteRetryableCommandInterceptor;
import com.huaweicloud.sermant.mongodb.interceptors.MixedBulkWriteOperationInterceptor;

import com.mongodb.ReadPreference;
import com.mongodb.internal.binding.ConnectionSource;
import com.mongodb.internal.connection.Connection;

import org.bson.BsonDocument;
import org.bson.FieldNameValidator;
import org.bson.codecs.Decoder;

/**
 * mongo拦截点辅助类
 *
 * @author daizhenyu
 * @since 2024-01-16
 **/
public class MongoDbEnhancementHelper {
    private static final String MIXED_BULK_WRITE_CLASS = "com.mongodb.internal.operation.MixedBulkWriteOperation";

    private static final String COMMAND_OPERATION_CLASS = "com.mongodb.internal.operation.CommandOperationHelper";

    private static final String EXECUTE_METHOD_NAME = "execute";

    private static final String EXECUTE_ASYNC_METHOD_NAME = "executeAsync";

    private static final String EXECUTE_COMMAND_ASYNC_METHOD_NAME = "executeCommandAsync";

    private static final String EXECUTE_COMMAND_METHOD_NAME = "executeCommand";

    private static final String EXECUTE_WRITE_COMMAND_METHOD_NAME = "executeWriteCommand";

    private static final String EXECUTE_RETRYABLE_COMMAND_METHOD_NAME = "executeRetryableCommand";

    private static final int METHOD_PARAM_COUNT = 9;

    private static final Class[] EXECUTE_COMMAND_PARAMS_TYPE = {
            String.class,
            BsonDocument.class,
            FieldNameValidator.class,
            Decoder.class,
            ConnectionSource.class,
            Connection.class,
            ReadPreference.class
    };

    private static final String[] EXECUTE_RETRY_COMMAND_PARAMS_TYPE = {
            "com.mongodb.internal.binding.WriteBinding",
            "java.lang.String",
            "com.mongodb.ReadPreference",
            "org.bson.FieldNameValidator",
            "org.bson.codecs.Decoder",
            "com.mongodb.internal.operation.CommandOperationHelper.CommandCreator",
            "com.mongodb.internal.operation.CommandOperationHelper.CommandWriteTransformer",
            "com.mongodb.Function"
    };

    private static final String[] EXECUTE_RETRY_COMMAND_ASYNC_PARAMS_TYPE = {
            "com.mongodb.internal.binding.AsyncWriteBinding",
            "java.lang.String",
            "com.mongodb.ReadPreference",
            "org.bson.FieldNameValidator",
            "org.bson.codecs.Decoder",
            "com.mongodb.internal.operation.CommandOperationHelper.CommandCreator",
            "com.mongodb.internal.operation.CommandOperationHelper.CommandWriteTransformerAsync",
            "com.mongodb.Function",
            "com.mongodb.internal.async.SingleResultCallback"
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
                InterceptDeclarer.build(getExecuteAsyncMethodMatcher(), mixedBulkWriteOperationInterceptor)
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
     * 获取MixedBulkWriteOperation executeAsync方法无参拦截器
     *
     * @return InterceptDeclarer MixedBulkWriteOperation executeAsync方法无参拦截器
     */
    public static InterceptDeclarer getExecuteAsyncInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteAsyncMethodMatcher(),
                new MixedBulkWriteOperationInterceptor());
    }

    /**
     * 获取MixedBulkWriteOperation executeAsync方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer MixedBulkWriteOperation executeAsync方法有参拦截器
     */
    public static InterceptDeclarer getExecuteAsyncInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteAsyncMethodMatcher(),
                new MixedBulkWriteOperationInterceptor(handler));
    }

    /**
     * 获取CommandOperationHelper写操作无参拦截器
     *
     * @return InterceptDeclarer[] CommandOperationHelper写操作无参拦截器
     */
    public static InterceptDeclarer[] getCommandOperationHelperInterceptDeclarers() {
        ExecuteCommandInterceptor commandInterceptor = new ExecuteCommandInterceptor();
        return new InterceptDeclarer[]{
                InterceptDeclarer.build(getExecuteCommandMethodMatcher(), commandInterceptor),
                InterceptDeclarer.build(getExecuteCommandAsyncMethodMatcher(), commandInterceptor),
                InterceptDeclarer.build(getExecuteWriteCommandMethodMatcher(), commandInterceptor),
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
        return InterceptDeclarer.build(getExecuteWriteCommandMethodMatcher(), new ExecuteCommandInterceptor());
    }

    /**
     * 获取CommandOperationHelper executeWriteCommand方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer CommandOperationHelper executeWriteCommand方法有参拦截器
     */
    public static InterceptDeclarer getExecuteWriteCommandInterceptDeclarer(
            DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteWriteCommandMethodMatcher(), new ExecuteCommandInterceptor(handler));
    }

    /**
     * 获取CommandOperationHelper executeCommandAsync方法无参拦截器
     *
     * @return InterceptDeclarer CommandOperationHelper executeCommandAsync方法无参拦截器
     */
    public static InterceptDeclarer getExecuteCommandAsyncInterceptDeclarer() {
        return InterceptDeclarer.build(getExecuteCommandAsyncMethodMatcher(), new ExecuteCommandInterceptor());
    }

    /**
     * 获取CommandOperationHelper executeCommandAsync方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer CommandOperationHelper executeCommandAsync方法有参拦截器
     */
    public static InterceptDeclarer getExecuteCommandAsyncInterceptDeclarer(
            DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecuteCommandAsyncMethodMatcher(), new ExecuteCommandInterceptor(handler));
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

    private static MethodMatcher getExecuteAsyncMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_ASYNC_METHOD_NAME);
    }

    private static MethodMatcher getExecuteCommandMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_COMMAND_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_COMMAND_PARAMS_TYPE));
    }

    private static MethodMatcher getExecuteWriteCommandMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_WRITE_COMMAND_METHOD_NAME)
                .and(MethodMatcher.paramCountEquals(METHOD_PARAM_COUNT));
    }

    private static MethodMatcher getExecuteCommandAsyncMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_COMMAND_ASYNC_METHOD_NAME)
                .and(MethodMatcher.paramCountEquals(METHOD_PARAM_COUNT));
    }

    private static MethodMatcher getExecuteRetryableCommandMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_RETRYABLE_COMMAND_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_RETRY_COMMAND_ASYNC_PARAMS_TYPE)
                        .or(MethodMatcher.paramTypesEqual(EXECUTE_RETRY_COMMAND_PARAMS_TYPE)));
    }
}
