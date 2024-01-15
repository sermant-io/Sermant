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

package com.huaweicloud.sermant.mongodb.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;
import com.huaweicloud.sermant.database.controller.DatabaseController;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;

import com.mongodb.internal.operation.MixedBulkWriteOperation;

/**
 * MixedBulkWriteOperation类拦截声明器
 *
 * @author daizhenyu
 * @since 2024-01-16
 **/
public class MixedBulkWriteOperationInterceptor extends AbstractDatabaseInterceptor {
    /**
     * 无参构造方法
     */
    public MixedBulkWriteOperationInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 写操作处理器
     */
    public MixedBulkWriteOperationInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        String database = ((MixedBulkWriteOperation) context.getObject()).getNamespace().getDatabaseName();
        if (DatabaseWriteProhibitionManager.getProhibitionDatabases().contains(database)) {
            DatabaseController.disableDatabaseWriteOperation(database, context);
        }
        return context;
    }
}
