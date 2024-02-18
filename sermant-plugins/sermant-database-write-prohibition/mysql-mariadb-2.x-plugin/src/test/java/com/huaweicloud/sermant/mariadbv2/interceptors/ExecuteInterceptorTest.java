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

package com.huaweicloud.sermant.mariadbv2.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionConfig;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.internal.protocol.MasterProtocol;
import org.mariadb.jdbc.internal.protocol.Protocol;
import org.mariadb.jdbc.internal.util.dao.ClientPrepareResult;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * execute方法拦截器单元测试
 *
 * @author daizhenyu
 * @since 2024-02-06
 **/
public class ExecuteInterceptorTest {
    private static final int PARAM_INDEX = 2;

    private static DatabaseWriteProhibitionConfig globalConfig = new DatabaseWriteProhibitionConfig();

    private static ExecuteContext context;

    private static Method methodMock;

    private static Protocol protocolMock;

    private static Object[] argument;

    private static String sql;

    private static ClientPrepareResult resultMock;

    private ExecuteInterceptor interceptor = new ExecuteInterceptor();

    @BeforeClass
    public static void setUp() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        sql = "INSERT INTO table (name) VALUES ('test')";
        protocolMock = Mockito.mock(MasterProtocol.class);
        methodMock = Mockito.mock(Method.class);
        resultMock = Mockito.mock(ClientPrepareResult.class);
        HostAddress serverAddress = new HostAddress("127.0.0.1", 8080);
        Mockito.when(protocolMock.getHostAddress()).thenReturn(serverAddress);
        Mockito.when(protocolMock.getDatabase()).thenReturn("database-test");
        Mockito.when(resultMock.getSql()).thenReturn(sql);
        argument = new Object[]{null, null, "INSERT INTO table (name) VALUES ('test')"};
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
        context = ExecuteContext.forMemberMethod(protocolMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        // 数据库禁写开关关闭，禁写数据库set包含被拦截的数据库
        Set<String> databases = new HashSet<>();
        databases.add("database-test");
        globalConfig.setMySqlDatabases(databases);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        //数据库禁写开关打开，禁写数据库集合包含被拦截的数据库, 方法入参为String
        globalConfig.setEnableMySqlWriteProhibition(true);
        context = ExecuteContext.forMemberMethod(protocolMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertEquals("Database prohibit to write, database: database-test",
                context.getThrowableOut().getMessage());

        //数据库禁写开关打开，禁写数据库集合包含被拦截的数据库，方法入参为ClientPrepareResult
        argument[PARAM_INDEX] = resultMock;
        context = ExecuteContext.forMemberMethod(protocolMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertEquals("Database prohibit to write, database: database-test",
                context.getThrowableOut().getMessage());

        //数据库禁写开关打开，sql没有写操作，禁写数据库集合包含被拦截的数据库
        sql = "SELECT * FROM table";
        argument[PARAM_INDEX] = sql;
        context = ExecuteContext.forMemberMethod(protocolMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        //数据库禁写开关打开，禁写数据库集合不包含被拦截的数据库, 方法入参为String
        argument[PARAM_INDEX] = "INSERT INTO table (name) VALUES ('test')";
        globalConfig.setMySqlDatabases(new HashSet<>());
        context = ExecuteContext.forMemberMethod(protocolMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());

        //数据库禁写开关打开，禁写数据库集合不包含被拦截的数据库，方法入参为ClientPrepareResult
        argument[PARAM_INDEX] = resultMock;
        context = ExecuteContext.forMemberMethod(protocolMock, methodMock, argument, null, null);
        interceptor.before(context);
        Assert.assertNull(context.getThrowableOut());
    }
}
