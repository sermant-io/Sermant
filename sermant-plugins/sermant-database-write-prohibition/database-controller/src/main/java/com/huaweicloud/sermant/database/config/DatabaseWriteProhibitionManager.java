/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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
 * configuration management classes of write prohibition plugin
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
     * Gets a collection of databases that MongoDb prohibits writing to
     *
     * @return A collection of databases that MongoDb prohibits writing to
     */
    public static Set<String> getMongoDbProhibitionDatabases() {
        if (globalConfig.isEnableMongoDbWriteProhibition()) {
            return globalConfig.getMongoDbDatabases();
        }
        if (localConfig.isEnableMongoDbWriteProhibition()) {
            return localConfig.getMongoDbDatabases();
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Gets a collection of databases that MySQL prohibits writing to
     *
     * @return A collection of databases that MySQL prohibits writing to
     */
    public static Set<String> getMySqlProhibitionDatabases() {
        if (globalConfig.isEnableMySqlWriteProhibition()) {
            return globalConfig.getMySqlDatabases();
        }
        if (localConfig.isEnableMySqlWriteProhibition()) {
            return localConfig.getMySqlDatabases();
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Gets a collection of databases that PostgreSQL prohibits writing to
     *
     * @return A collection of databases that PostgreSQL prohibits writing to
     */
    public static Set<String> getPostgreSqlProhibitionDatabases() {
        if (globalConfig.isEnablePostgreSqlWriteProhibition()) {
            return globalConfig.getPostgreSqlDatabases();
        }
        if (localConfig.isEnablePostgreSqlWriteProhibition()) {
            return localConfig.getPostgreSqlDatabases();
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Gets a collection of databases that OpenGauss prohibits writing to
     *
     * @return A collection of databases that OpenGauss prohibits writing to
     */
    public static Set<String> getOpenGaussProhibitionDatabases() {
        if (globalConfig.isEnableOpenGaussWriteProhibition()) {
            return globalConfig.getOpenGaussDatabases();
        }
        if (localConfig.isEnableOpenGaussWriteProhibition()) {
            return localConfig.getOpenGaussDatabases();
        }
        return Collections.EMPTY_SET;
    }

    /**
     * get global configuration
     *
     * @return global configuration
     */
    public static DatabaseWriteProhibitionConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * get local configuration
     *
     * @return local configuration
     */
    public static DatabaseWriteProhibitionConfig getLocalConfig() {
        return localConfig;
    }

    /**
     * update global configuration
     *
     * @param config database write prohibition configurations
     */
    public static void updateGlobalConfig(DatabaseWriteProhibitionConfig config) {
        if (config == null) {
            globalConfig = new DatabaseWriteProhibitionConfig();
            return;
        }
        globalConfig = config;
    }

    /**
     * update local configuration
     *
     * @param config database write prohibition configurations
     */
    public static void updateLocalConfig(DatabaseWriteProhibitionConfig config) {
        if (config == null) {
            localConfig = new DatabaseWriteProhibitionConfig();
            return;
        }
        localConfig = config;
    }

    /**
     * print configuration information
     *
     * @return configuration information
     */
    public static String printConfig() {
        return "Global WriteProhibitionConfig: " + globalConfig.toString() + "; Local WriteProhibitionConfig: "
                + localConfig.toString();
    }
}
