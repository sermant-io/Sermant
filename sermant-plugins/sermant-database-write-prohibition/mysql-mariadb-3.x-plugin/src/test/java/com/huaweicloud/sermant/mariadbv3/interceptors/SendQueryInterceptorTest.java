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

package com.huaweicloud.sermant.mariadbv3.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionConfig;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.client.Context;
import org.mariadb.jdbc.client.context.BaseContext;
import org.mariadb.jdbc.client.impl.StandardClient;
import org.mariadb.jdbc.message.ClientMessage;
import org.mariadb.jdbc.message.client.AuthMoreRawPacket;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * prepare方法拦截器单元测试
 *
 * @author daizhenyu
 * @since 2024-02-06
 **/
public class SendQueryInterceptorTest {
    private static DatabaseWriteProhibitionConfig globalConfig = new DatabaseWriteProhibitionConfig();

    private static ExecuteContext context;

    private static Method methodMock;

    private static StandardClient clientMock;

    private static ClientMessage messageMock;

    private static Object[] argument;

    private static String sql;

    private SendQueryInterceptor interceptor = new SendQueryInterceptor();

    @BeforeClass
    public static void setUp() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        clientMock = Mockito.mock(StandardClient.class);
        methodMock = Mockito.mock(Method.class);
        Context contextMock = Mockito.mock(BaseContext.class);
        messageMock = Mockito.mock(AuthMoreRawPacket.class);
        HostAddress serverAddress = HostAddress.from("127.0.0.1", 8080);
        Mockito.when(clientMock.getHostAddress()).thenReturn(serverAddress);
        Mockito.when(clientMock.getContext()).thenReturn(contextMock);
        Mockito.when(contextMock.getDatabase()).thenReturn("database-test");
        Mockito.when(messageMock.description()).thenReturn("INSERT INTO table (name) VALUES ('test')");
        argument = new Object[]{messageMock};
    }

    @AfterClass
    public static void tearDown() {
        Mockito.clearAllCaches();
        DatabaseWriteProhibitionManager.updateGlobalConfig(null);
    }

    @Test
    public void testDoBefore() throws Exception {
        // 数据库禁写开关关闭
        globalConfig.setEnableMySqlWriteProhibition(false);
        context = ExecuteContext.forMemberMethod(clientMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        // 数据库禁写开关关闭，禁写数据库set包含被拦截的数据库
        Set<String> databases = new HashSet<>();
        databases.add("database-test");
        globalConfig.setMySqlDatabases(databases);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        // 数据库禁写开关打开，禁写数据库set包含被拦截的数据库
        globalConfig.setEnableMySqlWriteProhibition(true);
        interceptor.before(context);
        Assert.assertEquals("Database prohibit to write, database: database-test",
                context.getThrowableOut().getMessage());

        //数据库禁写开关打开，sql没有写操作，禁写数据库集合包含被拦截的数据库
        Mockito.when(messageMock.description()).thenReturn("SELECT * FROM table");
        context = ExecuteContext.forMemberMethod(clientMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        //数据库禁写开关打开，禁写数据库集合不包含被拦截的数据库
        Mockito.when(messageMock.description()).thenReturn("INSERT INTO table (name) VALUES ('test')");
        globalConfig.setMySqlDatabases(new HashSet<>());
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());
    }
}
