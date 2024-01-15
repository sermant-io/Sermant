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

package com.huaweicloud.sermant.database.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据库禁写插件配置类
 *
 * @author daizhenyu
 * @since 2024-01-15
 **/
public class DatabaseWriteProhibitionConfig {
    /**
     * 是否开启禁写
     */
    private boolean enableDatabaseWriteProhibition = false;

    /**
     * 需要禁写的数据库
     */
    private Set<String> databases = new HashSet<>();

    public boolean isEnableDatabaseWriteProhibition() {
        return enableDatabaseWriteProhibition;
    }

    public void setEnableDatabaseWriteProhibition(boolean enableDatabaseWriteProhibition) {
        this.enableDatabaseWriteProhibition = enableDatabaseWriteProhibition;
    }

    /**
     * 获取禁消费的数据库列表
     *
     * @return 数据库列表
     */
    public Set<String> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<String> databases) {
        this.databases = databases;
    }

    @Override
    public String toString() {
        return "enableDatabaseWriteProhibition=" + enableDatabaseWriteProhibition + ", databases=" + databases;
    }
}
