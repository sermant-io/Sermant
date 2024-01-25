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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;

/**
 * 数据库禁写配置管理类单元测试
 *
 * @author daizhenyu
 * @since 2024-01-23
 **/
public class DatabaseWriteProhibitionManagerTest {
    private static DatabaseWriteProhibitionConfig globalConfig;

    private static DatabaseWriteProhibitionConfig localConfig;

    @BeforeClass
    public static void setUp() {
        globalConfig = new DatabaseWriteProhibitionConfig();
        HashSet<String> globalDatabases = new HashSet<>();
        globalDatabases.add("database-test-1");
        globalConfig.setDatabases(globalDatabases);
        localConfig = new DatabaseWriteProhibitionConfig();
        HashSet<String> localDatabases = new HashSet<>();
        localDatabases.add("database-test-2");
        localConfig.setDatabases(localDatabases);
    }

    /**
     * 测试Global和Local配置都开启的情况
     */
    @Test
    public void testGetProhibitionDatabasesWithGlobalAndLocalConfigEnabled() {
        globalConfig.setEnableDatabaseWriteProhibition(true);
        localConfig.setEnableDatabaseWriteProhibition(true);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertEquals(globalConfig.getDatabases(), DatabaseWriteProhibitionManager.getProhibitionDatabases());
    }

    /**
     * 测试Global配置开启的情况
     */
    @Test
    public void testGetProhibitionDatabasesWithJustGlobalConfigEnabled() {
        globalConfig.setEnableDatabaseWriteProhibition(true);
        localConfig.setEnableDatabaseWriteProhibition(false);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertEquals(globalConfig.getDatabases(), DatabaseWriteProhibitionManager.getProhibitionDatabases());
    }

    /**
     * 测试Local配置开启的情况
     */
    @Test
    public void testGetProhibitionDatabasesWithJustLocalConfigEnabled() {
        globalConfig.setEnableDatabaseWriteProhibition(false);
        localConfig.setEnableDatabaseWriteProhibition(true);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertEquals(localConfig.getDatabases(), DatabaseWriteProhibitionManager.getProhibitionDatabases());
    }

    /**
     * 测试Global和Local配置都关闭的情况
     */
    @Test
    public void testGetProhibitionDatabasesWithBothConfigsDisabled() {
        globalConfig.setEnableDatabaseWriteProhibition(false);
        localConfig.setEnableDatabaseWriteProhibition(false);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertTrue(DatabaseWriteProhibitionManager.getProhibitionDatabases().isEmpty());
    }

    /**
     * 测试更新配置不为null的情况
     */
    @Test
    public void testUpdateConfigWithNonNullConfig() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);
        Assert.assertEquals(globalConfig, DatabaseWriteProhibitionManager.getGlobalConfig());
        Assert.assertEquals(localConfig, DatabaseWriteProhibitionManager.getLocalConfig());
    }

    /**
     * 测试更新配置为null的情况
     */
    @Test
    public void testUpdateConfigWithNullConfig() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(null);
        DatabaseWriteProhibitionManager.updateLocalConfig(null);

        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getGlobalConfig().getDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getLocalConfig().getDatabases().size());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getGlobalConfig().isEnableDatabaseWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getLocalConfig().isEnableDatabaseWriteProhibition());
    }
}
