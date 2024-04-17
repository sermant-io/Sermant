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

package com.huaweicloud.sermant.postgresqlv42.interceptors;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.constant.DatabaseType;
import com.huaweicloud.sermant.database.entity.DatabaseInfo;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;
import com.huaweicloud.sermant.database.utils.ThreadDatabaseUrlUtil;

import org.postgresql.Driver;
import org.postgresql.core.ParameterList;
import org.postgresql.core.Query;
import org.postgresql.core.QueryExecutor;
import org.postgresql.core.v3.QueryExecutorImpl;
import org.postgresql.util.HostSpec;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Interceptor for QueryExecutorImpl sendOneQuery method
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class QueryExecutorImplInterceptor extends AbstractDatabaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String GET_DATABASE_METHOD_NAME = "getDatabase";

    private static final String GET_GAUSSDB_VERSION_METHOD_NAME = "getGaussdbVersion";

    private boolean isHighVersion;

    private boolean isGaussdbDatabase;

    /**
     * Non-parametric construction method
     */
    public QueryExecutorImplInterceptor() {
        try {
            QueryExecutor.class.getDeclaredMethod(GET_DATABASE_METHOD_NAME);
            isHighVersion = true;
        } catch (NoSuchMethodException e) {
            LOGGER.fine("The current database is Postgresql, and the JDBC version is below 9.4.1210.");
        }
        try {
            QueryExecutor.class.getDeclaredMethod(GET_GAUSSDB_VERSION_METHOD_NAME);
            isGaussdbDatabase = true;
        } catch (NoSuchMethodException e) {
            LOGGER.fine("The current database is Postgresql, QueryExecutor class does not have the "
                    + "getGaussdbVersion method");
        }
    }

    /**
     * Parameterized construction method
     *
     * @param handler Database write operation handler
     */
    public QueryExecutorImplInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        DatabaseInfo databaseInfo = getDataBaseInfo(context);
        String database = databaseInfo.getDatabaseName();
        Query query = (Query) context.getArguments()[0];
        String sql = query.toString((ParameterList) context.getArguments()[1]);
        if (isGaussdbDatabase) {
            handleWriteOperationIfWriteDisabled(sql, database,
                    DatabaseWriteProhibitionManager.getOpenGaussProhibitionDatabases(), context);
            return context;
        }
        handleWriteOperationIfWriteDisabled(sql, database,
                DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases(), context);
        return context;
    }

    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        QueryExecutorImpl queryExecutor = (QueryExecutorImpl) context.getObject();
        DatabaseInfo databaseInfo;
        if (isGaussdbDatabase) {
            databaseInfo = new DatabaseInfo(DatabaseType.OPENGAUSS);
        } else {
            databaseInfo = new DatabaseInfo(DatabaseType.POSTGRESQL);
        }
        context.setLocalFieldValue(DATABASE_INFO, databaseInfo);
        if (isHighVersion) {
            databaseInfo.setDatabaseName(queryExecutor.getDatabase());
            HostSpec hostSpec = queryExecutor.getHostSpec();
            if (hostSpec == null) {
                LOGGER.warning("Unable to obtain the link address of the database.");
                return;
            }
            databaseInfo.setHostAddress(hostSpec.getHost());
            databaseInfo.setPort(hostSpec.getPort());
            return;
        }
        String url = ThreadDatabaseUrlUtil.getDatabaseUrl();
        if (StringUtils.isEmpty(url)) {
            return;
        }
        Properties properties = new Properties();
        properties = Driver.parseURL(url, properties);
        databaseInfo.setHostAddress(properties.getProperty("PGHOST"));
        databaseInfo.setPort(Integer.parseInt(properties.getProperty("PGPORT")));
        databaseInfo.setDatabaseName(properties.getProperty("PGDBNAME"));
    }
}
