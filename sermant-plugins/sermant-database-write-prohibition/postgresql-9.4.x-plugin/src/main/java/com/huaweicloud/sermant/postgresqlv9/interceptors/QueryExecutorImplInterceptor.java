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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.constant.DatabaseType;
import com.huaweicloud.sermant.database.entity.DatabaseInfo;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;
import com.huaweicloud.sermant.postgresqlv9.utils.ThreadConnectionUtil;

import org.postgresql.Driver;
import org.postgresql.core.Query;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 执行SQL操作的拦截器
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class QueryExecutorImplInterceptor extends AbstractDatabaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 无参构造方法
     */
    public QueryExecutorImplInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 写操作处理器
     */
    public QueryExecutorImplInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        DatabaseInfo databaseInfo = getDataBaseInfo(context);
        Query query = (Query) context.getArguments()[0];
        String sql = query.toString();
        String database = databaseInfo.getDatabaseName();
        handleWriteOperationIfWriteDisabled(sql, database,
                DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases(), context);
        return context;
    }

    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        DatabaseInfo databaseInfo = new DatabaseInfo(DatabaseType.POSTGRESQL);
        context.setLocalFieldValue(DATABASE_INFO, databaseInfo);
        Connection connection = ThreadConnectionUtil.getConnection();
        if (connection == null) {
            return;
        }
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            if (databaseMetaData == null || StringUtils.isEmpty(databaseMetaData.getURL())) {
                return;
            }
            Properties properties = new Properties();
            properties = Driver.parseURL(databaseMetaData.getURL(), properties);
            databaseInfo.setHostAddress(properties.getProperty("PGHOST"));
            databaseInfo.setPort(Integer.parseInt(properties.getProperty("PGPORT")));
            databaseInfo.setDatabaseName(properties.getProperty("PGDBNAME"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "can not obtain the database information.", e);
        }
    }
}
