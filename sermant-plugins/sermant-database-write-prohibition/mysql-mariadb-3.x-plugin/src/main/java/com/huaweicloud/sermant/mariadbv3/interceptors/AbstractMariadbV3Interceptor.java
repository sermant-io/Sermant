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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.database.constant.DatabaseType;
import com.huaweicloud.sermant.database.entity.DatabaseInfo;
import com.huaweicloud.sermant.database.interceptor.AbstractDatabaseInterceptor;

import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.client.impl.StandardClient;

import java.util.logging.Logger;

/**
 * Abstract Interceptor of Mariadb3.X
 *
 * @author daizhenyu
 * @since 2024-01-30
 **/
public abstract class AbstractMariadbV3Interceptor extends AbstractDatabaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        DatabaseInfo info = new DatabaseInfo(DatabaseType.MYSQL);
        context.setLocalFieldValue(DATABASE_INFO, info);
        StandardClient client = (StandardClient) context.getObject();
        if (client.getContext() != null) {
            info.setDatabaseName(client.getContext().getDatabase());
        } else {
            LOGGER.warning("Failed to obtain database name.");
        }
        HostAddress hostAddress = client.getHostAddress();
        info.setHostAddress(hostAddress.host);
        info.setPort(hostAddress.port);
    }
}
