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
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.controller.DatabaseController;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.utils.SqlParserUtils;

import org.mariadb.jdbc.message.ClientMessage;

/**
 * executePipeline Method Interceptor
 *
 * @author daizhenyu
 * @since 2024-01-30
 **/
public class ExecutePipelineInterceptor extends AbstractMariadbV3Interceptor {
    /**
     * No-argument constructor
     */
    public ExecutePipelineInterceptor() {
    }

    /**
     * Parametric constructor
     *
     * @param handler write operation handler
     */
    public ExecutePipelineInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        String database = getDataBaseInfo(context).getDatabaseName();
        if (!DatabaseWriteProhibitionManager.getMySqlProhibitionDatabases().contains(database)) {
            return context;
        }
        ClientMessage[] clientMessages = (ClientMessage[]) context.getArguments()[0];
        for (ClientMessage clientMessage : clientMessages) {
            if (SqlParserUtils.isWriteOperation(clientMessage.description())) {
                DatabaseController.disableDatabaseWriteOperation(database, context);
                return context;
            }
        }
        return context;
    }
}
