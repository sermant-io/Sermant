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

package com.huaweicloud.sermant.mongodb.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionConfig;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;

import com.mongodb.MongoNamespace;
import com.mongodb.internal.operation.MixedBulkWriteOperation;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * ExecuteCommand方法拦截器单元测试
 *
 * @author daizhenyu
 * @since 2024-01-23
 **/
public class ExecuteCommandInterceptorTest {
    private static DatabaseWriteProhibitionConfig globalConfig = new DatabaseWriteProhibitionConfig();

    private static MongoNamespace namespace = new MongoNamespace("database-test",
            "collection-test");

    private static MixedBulkWriteOperation operationMock;

    private static ExecuteContext context;

    private static Method methodMock;

    private static Object[] argument;

    private ExecuteCommandInterceptor interceptor = new ExecuteCommandInterceptor();

    @BeforeClass
    public static void setUp() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        operationMock = Mockito.mock(MixedBulkWriteOperation.class);
        methodMock = Mockito.mock(Method.class);
        Mockito.when(operationMock.getNamespace()).thenReturn(namespace);
        argument = new Object[]{"database-test"};
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    public void testDoBefore() {
        // 数据库禁写开关关闭
        globalConfig.setEnableMongoDbWriteProhibition(false);
        context = ExecuteContext.forMemberMethod(operationMock, methodMock, argument, null, null);
        interceptor.doBefore(context);
        Assert.assertNull(context.getThrowableOut());

        // 数据库禁写开关关闭，禁写数据库set包含被拦截的数据库
        Set<String> databases = new HashSet<>();
        databases.add("database-test");
        globalConfig.setMongoDbDatabases(databases);
        Assert.assertNull(context.getThrowableOut());

        //数据库禁写开关打开，禁写数据库集合包含被拦截的数据库
        globalConfig.setEnableMongoDbWriteProhibition(true);
        context = ExecuteContext.forMemberMethod(operationMock, methodMock, argument, null, null);
        interceptor.doBefore(context);
        Assert.assertEquals("Database prohibit to write, database: database-test",
                context.getThrowableOut().getMessage());

        //数据库禁写开关打开，禁写数据库集合不包含被拦截的数据库
        globalConfig.setMongoDbDatabases(new HashSet<>());
        interceptor.doBefore(context);
        context = ExecuteContext.forMemberMethod(operationMock, methodMock, argument, null, null);
        Assert.assertNull(context.getThrowableOut());
    }
}
