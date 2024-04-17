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
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;
import com.huaweicloud.sermant.database.utils.ThreadDatabaseUrlUtil;

import org.postgresql.jdbc3g.AbstractJdbc3gStatement;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interceptor for AbstractJdbc2Statement execute and executeBatch method
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class Jdbc2StatementInterceptor extends AbstractDatabaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Non-parametric construction method
     */
    public Jdbc2StatementInterceptor() {
    }

    /**
     * Parameterized construction method
     *
     * @param handler Database write operation handler
     */
    public Jdbc2StatementInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (!(context.getObject() instanceof AbstractJdbc3gStatement)) {
            return context;
        }
        AbstractJdbc3gStatement statement = (AbstractJdbc3gStatement) context.getObject();

        // Store link information to ensure that QueryExecutorImplInterceptor can retrieve database information
        try {
            ThreadDatabaseUrlUtil.setDatabaseUrl(statement.getPGConnection().getMetaData().getURL());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "can not obtain the database information.", e);
        }
        return context;
    }

    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        // This interception point is mainly used to store link information into thread variables, without the need to
        // cache basic database information, so the method is implemented as empty
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (handler != null) {
            handler.doAfter(context);
            return context;
        }
        ThreadDatabaseUrlUtil.removeDatabaseUrl();
        return context;
    }
}
