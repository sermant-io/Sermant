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
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.constant.DatabaseType;
import com.huaweicloud.sermant.database.entity.DatabaseInfo;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;

import org.postgresql.core.Query;
import org.postgresql.core.v3.QueryExecutorImpl;
import org.postgresql.util.HostSpec;

import java.util.logging.Logger;

/**
 * 执行SQL操作拦截器的测试类
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
        String database = databaseInfo.getDatabaseName();
        Query query = (Query) context.getArguments()[0];
        String sql = query.toString();
        handleWriteOperationIfWriteDisabled(sql, database,
                DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases(), context);
        return context;
    }

    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        QueryExecutorImpl queryExecutor = (QueryExecutorImpl) context.getObject();
        DatabaseInfo databaseInfo = new DatabaseInfo(DatabaseType.POSTGRESQL);
        context.setLocalFieldValue(DATABASE_INFO, databaseInfo);
        databaseInfo.setDatabaseName(queryExecutor.getDatabase());
        HostSpec hostSpec = queryExecutor.getHostSpec();
        if (hostSpec == null) {
            LOGGER.info("Unable to obtain the link address of the database.");
            return;
        }
        databaseInfo.setHostAddress(hostSpec.getHost());
        databaseInfo.setPort(hostSpec.getPort());
    }
}
