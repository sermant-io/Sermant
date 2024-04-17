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

package com.huaweicloud.sermant.mongodbv4.interceptors;

import com.mongodb.ServerApi;
import com.mongodb.connection.ServerDescription;
import com.mongodb.internal.binding.ConnectionSource;
import com.mongodb.internal.connection.Connection;
import com.mongodb.internal.session.SessionContext;

/**
 * Implementation class of ConnectionSource interface
 *
 * @author daizhenyu
 * @since 2024-02-05
 **/
public class ConnectionSourceImpl implements ConnectionSource {
    private ServerDescription description;

    /**
     * Construction method
     *
     * @param description Server information
     */
    public ConnectionSourceImpl(ServerDescription description) {
        this.description = description;
    }
    @Override
    public ServerDescription getServerDescription() {
        return description;
    }

    @Override
    public SessionContext getSessionContext() {
        return null;
    }

    @Override
    public ServerApi getServerApi() {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public ConnectionSource retain() {
        return null;
    }

    @Override
    public void release() {

    }
}
