/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.database.config;

import java.util.Collections;
import java.util.Set;

/**
 * 数据库禁写配置管理类
 *
 * @author daizhenyu
 * @since 2024-01-22
 */
public class DatabaseWriteProhibitionManager {
    private static DatabaseWriteProhibitionConfig globalConfig = new DatabaseWriteProhibitionConfig();

    private static DatabaseWriteProhibitionConfig localConfig = new DatabaseWriteProhibitionConfig();

    private DatabaseWriteProhibitionManager() {
    }

    /**
     * 获取要禁止消费的数据库集合
     *
     * @return 禁止消费的数据库集合
     */
    public static Set<String> getProhibitionDatabases() {
        if (globalConfig.isEnableDatabaseWriteProhibition()) {
            return globalConfig.getDatabases();
        }
        if (localConfig.isEnableDatabaseWriteProhibition()) {
            return localConfig.getDatabases();
        }
        return Collections.EMPTY_SET;
    }

    /**
     * 获取全局配置
     *
     * @return 全局配置
     */
    public static DatabaseWriteProhibitionConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * 获取局部配置
     *
     * @return 局部配置
     */
    public static DatabaseWriteProhibitionConfig getLocalConfig() {
        return localConfig;
    }

    /**
     * 更新全局配置
     *
     * @param config 禁止消费配置
     */
    public static void updateGlobalConfig(DatabaseWriteProhibitionConfig config) {
        if (config == null) {
            globalConfig = new DatabaseWriteProhibitionConfig();
            return;
        }
        globalConfig = config;
    }

    /**
     * 更新局部配置
     *
     * @param config 禁止消费配置
     */
    public static void updateLocalConfig(DatabaseWriteProhibitionConfig config) {
        if (config == null) {
            localConfig = new DatabaseWriteProhibitionConfig();
            return;
        }
        localConfig = config;
    }

    /**
     * 打印配置信息
     *
     * @return 配置信息
     */
    public static String printConfig() {
        return "Global WriteProhibitionConfig: " + globalConfig.toString() + "; Local WriteProhibitionConfig: "
                + localConfig.toString();
    }
}
