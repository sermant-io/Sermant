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

package io.sermant.database.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.database.controller.DatabaseController;
import io.sermant.database.entity.DatabaseInfo;
import io.sermant.database.handler.DatabaseHandler;
import io.sermant.database.utils.SqlParserUtils;

import java.util.Set;

/**
 * database abstract interceptor
 *
 * @author daizhenyu
 * @since 2024-01-22
 **/
public abstract class AbstractDatabaseInterceptor extends AbstractInterceptor {
    /**
     * key for storing database information
     */
    protected static final String DATABASE_INFO = "databaseInfo";

    /**
     * customize the database handling
     */
    protected DatabaseHandler handler;

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        createAndCacheDatabaseInfo(context);
        if (handler != null) {
            handler.doBefore(context);
            return context;
        }
        return doBefore(context);
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (handler != null) {
            handler.doAfter(context);
            return context;
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        if (handler != null) {
            handler.doOnThrow(context);
            return context;
        }
        return context;
    }

    /**
     * before method execution
     *
     * @param context context
     * @return ExecuteContext context
     */
    protected abstract ExecuteContext doBefore(ExecuteContext context);

    /**
     * Create a database instance object and cache it in the context's local property set
     *
     * @param context context
     */
    protected abstract void createAndCacheDatabaseInfo(ExecuteContext context);

    /**
     * Gets database instance information from the context local property set
     *
     * @param context context
     * @return DatabaseInfo database instance information
     */
    protected DatabaseInfo getDataBaseInfo(ExecuteContext context) {
        return (DatabaseInfo) context.getLocalFieldValue(DATABASE_INFO);
    }

    /**
     * If database write prohibition is enabled on the current database and
     * the write operation is performed, the write prohibition logic is performed，
     *
     * @param sql executed sql
     * @param databaseName name of the database where the sql is executed
     * @param prohibitionDatabases write forbidden database collection
     * @param context contextual information
     */
    protected void handleWriteOperationIfWriteDisabled(String sql, String databaseName,
            Set<String> prohibitionDatabases, ExecuteContext context) {
        if (prohibitionDatabases.contains(databaseName) && SqlParserUtils.isWriteOperation(sql)) {
            DatabaseController.disableDatabaseWriteOperation(databaseName, context);
        }
    }

    /**
     * If the mongodb database has write prohibition enabled and it is a write operation, the write prohibition logic
     * will be executed
     *
     * @param databaseName databaseName
     * @param prohibitionDatabases prohibition write database collection
     * @param context contextual information
     */
    protected void handleWriteOperationIfWriteDisabled(String databaseName,
            Set<String> prohibitionDatabases, ExecuteContext context) {
        if (prohibitionDatabases.contains(databaseName)) {
            DatabaseController.disableDatabaseWriteOperation(databaseName, context);
        }
    }
}
