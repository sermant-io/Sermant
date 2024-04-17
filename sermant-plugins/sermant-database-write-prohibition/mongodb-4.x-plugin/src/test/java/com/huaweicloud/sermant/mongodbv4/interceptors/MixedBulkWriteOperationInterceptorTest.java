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

package com.huaweicloud.sermant.mongodbv4.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionConfig;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;

import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ServerDescription;
import com.mongodb.internal.binding.SingleServerBinding;
import com.mongodb.internal.binding.WriteBinding;
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
 * MixedBulkWriteOperationInterceptor UT
 *
 * @author daizhenyu
 * @since 2024-01-23
 **/
public class MixedBulkWriteOperationInterceptorTest {
    private static DatabaseWriteProhibitionConfig globalConfig = new DatabaseWriteProhibitionConfig();

    private static MongoNamespace namespace = new MongoNamespace("database-test",
            "collection-test");

    private static MixedBulkWriteOperation operationMock;

    private static ExecuteContext context;

    private static Method methodMock;

    private static Object[] argument;

    private MixedBulkWriteOperationInterceptor interceptor = new MixedBulkWriteOperationInterceptor();

    @BeforeClass
    public static void setUp() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        operationMock = Mockito.mock(MixedBulkWriteOperation.class);
        methodMock = Mockito.mock(Method.class);
        Mockito.when(operationMock.getNamespace()).thenReturn(namespace);
        WriteBinding binding = Mockito.mock(SingleServerBinding.class);
        ServerDescription description = Mockito.mock(ServerDescription.class);
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 8080);
        Mockito.when(binding.getWriteConnectionSource()).thenReturn(new ConnectionSourceImpl(description));
        Mockito.when(description.getAddress()).thenReturn(serverAddress);
        argument = new Object[]{binding};
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    public void testDoBefore() throws Exception {
        // Database write prohibition switch is turned off
        globalConfig.setEnableMongoDbWriteProhibition(false);
        context = ExecuteContext.forMemberMethod(operationMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        // Database write prohibition switch is turned off,
        // and the write-prohibited database set contains intercepted databases.
        Set<String> databases = new HashSet<>();
        databases.add("database-test");
        globalConfig.setMongoDbDatabases(databases);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        // Database write prohibition switch is turned on,
        // and the write-prohibited database set contains intercepted databases.
        globalConfig.setEnableMongoDbWriteProhibition(true);
        context = ExecuteContext.forMemberMethod(operationMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertEquals("Database prohibit to write, database: database-test",
                context.getThrowableOut().getMessage());

        // Database write prohibition switch is turned on,
        // and the write-prohibited database set does not contain intercepted databases.
        globalConfig.setMongoDbDatabases(new HashSet<>());
        context = ExecuteContext.forMemberMethod(operationMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());
    }
}
