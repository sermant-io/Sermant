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
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;
import com.huaweicloud.sermant.postgresqlv9.utils.ThreadConnectionUtil;

import org.postgresql.jdbc3g.AbstractJdbc3gStatement;

/**
 * 执行SQL操作的拦截器
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class Jdbc4StatementInterceptor extends AbstractDatabaseInterceptor {
    /**
     * 无参构造方法
     */
    public Jdbc4StatementInterceptor() {
    }

    /**
     * 有参构造方法
     *
     * @param handler 写操作处理器
     */
    public Jdbc4StatementInterceptor(DatabaseHandler handler) {
        this.handler = handler;
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        AbstractJdbc3gStatement statement = (AbstractJdbc3gStatement) context.getObject();

        // 存放链接信息，保证QueryExecutorImplInterceptor可以获取到数据库信息
        ThreadConnectionUtil.setConnection(statement.getPGConnection());
        return context;
    }

    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        // 此拦截点主要是存放链接信息到线程变量中，不需要缓存数据库基础信息，因此方法为空实现
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (handler != null) {
            handler.doAfter(context);
            return context;
        }
        ThreadConnectionUtil.removeConnection();
        return context;
    }
}
