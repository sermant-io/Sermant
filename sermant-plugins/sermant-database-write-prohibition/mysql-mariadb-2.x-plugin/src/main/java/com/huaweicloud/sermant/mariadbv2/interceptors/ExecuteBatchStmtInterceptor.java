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
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.controller.DatabaseController;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.utils.SqlParserUtils;

import java.util.List;

/**
 * executeBatchStmt方法拦截器
 *
 * @author daizhenyu
 * @since 2024-01-26
 **/
public class ExecuteBatchStmtInterceptor extends AbstractMariadbV2Interceptor {
    private static final int PARAM_INDEX = 2;

    /**
     * 无参构造方法
     */
    public ExecuteBatchStmtInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 写操作处理器
     */
    public ExecuteBatchStmtInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        String database = getDataBaseInfo(context).getDatabaseName();
        if (!DatabaseWriteProhibitionManager.getMySqlProhibitionDatabases().contains(database)) {
            return context;
        }
        List<String> sqlList = (List) context.getArguments()[PARAM_INDEX];
        for (String sql : sqlList) {
            if (SqlParserUtils.isWriteOperation(sql)) {
                DatabaseController.disableDatabaseWriteOperation(database, context);
                return context;
            }
        }
        return context;
    }
}
