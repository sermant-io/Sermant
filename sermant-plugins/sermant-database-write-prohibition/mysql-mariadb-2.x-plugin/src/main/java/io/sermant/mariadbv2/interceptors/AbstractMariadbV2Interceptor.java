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

package io.sermant.mariadbv2.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.database.constant.DatabaseType;
import io.sermant.database.entity.DatabaseInfo;
import io.sermant.database.interceptor.AbstractDatabaseInterceptor;

import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.internal.protocol.Protocol;

/**
 * mariadb2.x abstract interceptor
 *
 * @author daizhenyu
 * @since 2024-02-02
 **/
public abstract class AbstractMariadbV2Interceptor extends AbstractDatabaseInterceptor {
    @Override
    protected void createAndCacheDatabaseInfo(ExecuteContext context) {
        DatabaseInfo info = new DatabaseInfo(DatabaseType.MYSQL);
        context.setLocalFieldValue(DATABASE_INFO, info);
        Protocol protocol = (Protocol) context.getObject();
        info.setDatabaseName(protocol.getDatabase());
        HostAddress hostAddress = protocol.getHostAddress();
        if (hostAddress != null) {
            info.setHostAddress(hostAddress.host);
            info.setPort(hostAddress.port);
        }
    }
}
