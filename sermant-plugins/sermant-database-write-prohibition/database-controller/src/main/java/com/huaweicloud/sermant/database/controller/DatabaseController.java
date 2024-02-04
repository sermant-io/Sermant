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

package com.huaweicloud.sermant.database.controller;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据库控制器
 *
 * @author daizhenyu
 * @since 2024-01-15
 **/
public class DatabaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static Object result = new Object();

    private DatabaseController() {
    }

    /**
     * 获取需要禁写的数据库清单
     *
     * @param database 数据库名称
     * @param context 拦截点上下文对象
     * @return
     */
    public static void disableDatabaseWriteOperation(String database, ExecuteContext context) {
        context.setThrowableOut(new SQLException("Database prohibit to write, database: " + database));
        context.skip(result);
        LOGGER.log(Level.FINE, "Database prohibit to write, database: {0}", database);
    }
}
