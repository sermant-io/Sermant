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

package com.huaweicloud.sermant.postgresqlv9.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionConfig;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.utils.ThreadDatabaseUrlUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.postgresql.core.BaseConnection;
import org.postgresql.jdbc3g.AbstractJdbc3gStatement;
import org.postgresql.jdbc4.Jdbc4Connection;
import org.postgresql.jdbc4.Jdbc4DatabaseMetaData;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Statement interceptor test class for executing SQL operations
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class Jdbc2StatementInterceptorTest {
    private static final DatabaseWriteProhibitionConfig GLOBAL_CONFIG = new DatabaseWriteProhibitionConfig();

    private static Method methodMock;

    private static AbstractJdbc3gStatement abstractJdbc3gStatement;

    private static BaseConnection connection;

    private final Jdbc2StatementInterceptor jdbc2StatementInterceptor = new Jdbc2StatementInterceptor();

    private static final String URL = "jdbc:postgresql://localhost:5432/database-test";

    @BeforeClass
    public static void setUp() throws SQLException {
        DatabaseWriteProhibitionManager.updateGlobalConfig(GLOBAL_CONFIG);
        methodMock = Mockito.mock(Method.class);
        abstractJdbc3gStatement = Mockito.mock(AbstractJdbc3gStatement.class);
        DatabaseMetaData metaData = Mockito.mock(Jdbc4DatabaseMetaData.class);
        connection = Mockito.mock(Jdbc4Connection.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);
        Mockito.when(metaData.getURL()).thenReturn(URL);
        Mockito.when(abstractJdbc3gStatement.getPGConnection()).thenReturn(connection);
    }

    @Test
    public void testDoBefore() throws Exception {
        ExecuteContext context = ExecuteContext.forMemberMethod(abstractJdbc3gStatement, methodMock,
                null, null, null);
        jdbc2StatementInterceptor.before(context);
        Assert.assertNotNull(ThreadDatabaseUrlUtil.getDatabaseUrl());
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        connection.close();
        ThreadDatabaseUrlUtil.removeDatabaseUrl();
        Mockito.clearAllCaches();
    }
}