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

package io.sermant.database.config;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;

/**
 * Database forbid write configuration management class unit tests
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
        HashSet<String> globalMongoDbDatabases = new HashSet<>();
        globalMongoDbDatabases.add("mongodb-test-1");
        globalConfig.setMongoDbDatabases(globalMongoDbDatabases);
        HashSet<String> globalMysqlDatabases = new HashSet<>();
        globalMysqlDatabases.add("mysql-test-1");
        globalConfig.setMySqlDatabases(globalMysqlDatabases);
        HashSet<String> globalPostgreSqlDatabases = new HashSet<>();
        globalPostgreSqlDatabases.add("postgresql-test-1");
        globalConfig.setPostgreSqlDatabases(globalPostgreSqlDatabases);
        HashSet<String> globalOpenGaussDatabases = new HashSet<>();
        globalOpenGaussDatabases.add("mongodb-test-1");
        globalConfig.setOpenGaussDatabases(globalOpenGaussDatabases);
        localConfig = new DatabaseWriteProhibitionConfig();
        HashSet<String> localMongoDbDatabases = new HashSet<>();
        localMongoDbDatabases.add("mongodb-test-1");
        localConfig.setMongoDbDatabases(localMongoDbDatabases);
        HashSet<String> localMysqlDatabases = new HashSet<>();
        localMysqlDatabases.add("mysql-test-1");
        globalConfig.setMySqlDatabases(localMysqlDatabases);
        HashSet<String> localPostgreSqlDatabases = new HashSet<>();
        localPostgreSqlDatabases.add("postgresql-test-1");
        globalConfig.setPostgreSqlDatabases(localPostgreSqlDatabases);
        HashSet<String> localOpenGaussDatabases = new HashSet<>();
        localOpenGaussDatabases.add("mongodb-test-1");
        globalConfig.setOpenGaussDatabases(localOpenGaussDatabases);
    }

    /**
     * Test whether the Global and Local configurations are enabled
     */
    @Test
    public void testGetProhibitionDatabasesWithGlobalAndLocalConfigEnabled() {
        globalConfig.setEnableMongoDbWriteProhibition(true);
        globalConfig.setEnableMySqlWriteProhibition(true);
        globalConfig.setEnablePostgreSqlWriteProhibition(true);
        globalConfig.setEnableOpenGaussWriteProhibition(true);
        localConfig.setEnableMongoDbWriteProhibition(true);
        localConfig.setEnableMySqlWriteProhibition(true);
        localConfig.setEnablePostgreSqlWriteProhibition(true);
        localConfig.setEnableOpenGaussWriteProhibition(true);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertEquals(globalConfig.getMongoDbDatabases(),
                DatabaseWriteProhibitionManager.getMongoDbProhibitionDatabases());
        Assert.assertEquals(globalConfig.getMySqlDatabases(),
                DatabaseWriteProhibitionManager.getMySqlProhibitionDatabases());
        Assert.assertEquals(globalConfig.getPostgreSqlDatabases(),
                DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases());
        Assert.assertEquals(globalConfig.getOpenGaussDatabases(),
                DatabaseWriteProhibitionManager.getOpenGaussProhibitionDatabases());
    }

    /**
     * test whether the global configuration is enabled
     */
    @Test
    public void testGetProhibitionDatabasesWithJustGlobalConfigEnabled() {
        globalConfig.setEnableMongoDbWriteProhibition(true);
        globalConfig.setEnableMySqlWriteProhibition(true);
        globalConfig.setEnablePostgreSqlWriteProhibition(true);
        globalConfig.setEnableOpenGaussWriteProhibition(true);
        localConfig.setEnableMongoDbWriteProhibition(false);
        localConfig.setEnableMySqlWriteProhibition(false);
        localConfig.setEnablePostgreSqlWriteProhibition(false);
        localConfig.setEnableOpenGaussWriteProhibition(false);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertEquals(globalConfig.getMongoDbDatabases(),
                DatabaseWriteProhibitionManager.getMongoDbProhibitionDatabases());
        Assert.assertEquals(globalConfig.getMySqlDatabases(),
                DatabaseWriteProhibitionManager.getMySqlProhibitionDatabases());
        Assert.assertEquals(globalConfig.getPostgreSqlDatabases(),
                DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases());
        Assert.assertEquals(globalConfig.getOpenGaussDatabases(),
                DatabaseWriteProhibitionManager.getOpenGaussProhibitionDatabases());
    }

    /**
     * test whether the local configuration is enabled
     */
    @Test
    public void testGetProhibitionDatabasesWithJustLocalConfigEnabled() {
        globalConfig.setEnableMongoDbWriteProhibition(false);
        globalConfig.setEnableMySqlWriteProhibition(false);
        globalConfig.setEnablePostgreSqlWriteProhibition(false);
        globalConfig.setEnableOpenGaussWriteProhibition(false);
        localConfig.setEnableMongoDbWriteProhibition(true);
        localConfig.setEnableMySqlWriteProhibition(true);
        localConfig.setEnablePostgreSqlWriteProhibition(true);
        localConfig.setEnableOpenGaussWriteProhibition(true);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertEquals(localConfig.getMongoDbDatabases(),
                DatabaseWriteProhibitionManager.getMongoDbProhibitionDatabases());
        Assert.assertEquals(localConfig.getMySqlDatabases(),
                DatabaseWriteProhibitionManager.getMySqlProhibitionDatabases());
        Assert.assertEquals(localConfig.getPostgreSqlDatabases(),
                DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases());
        Assert.assertEquals(localConfig.getOpenGaussDatabases(),
                DatabaseWriteProhibitionManager.getOpenGaussProhibitionDatabases());
    }

    /**
     * Test when both Global and Local configurations are off
     */
    @Test
    public void testGetProhibitionDatabasesWithBothConfigsDisabled() {
        globalConfig.setEnableMongoDbWriteProhibition(false);
        globalConfig.setEnableMySqlWriteProhibition(false);
        globalConfig.setEnablePostgreSqlWriteProhibition(false);
        globalConfig.setEnableOpenGaussWriteProhibition(false);
        localConfig.setEnableMongoDbWriteProhibition(false);
        localConfig.setEnableMySqlWriteProhibition(false);
        localConfig.setEnablePostgreSqlWriteProhibition(false);
        localConfig.setEnableOpenGaussWriteProhibition(false);
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);

        Assert.assertTrue(DatabaseWriteProhibitionManager.getMongoDbProhibitionDatabases().isEmpty());
        Assert.assertTrue(DatabaseWriteProhibitionManager.getMySqlProhibitionDatabases().isEmpty());
        Assert.assertTrue(DatabaseWriteProhibitionManager.getPostgreSqlProhibitionDatabases().isEmpty());
        Assert.assertTrue(DatabaseWriteProhibitionManager.getOpenGaussProhibitionDatabases().isEmpty());
    }

    /**
     * tests if the update configuration is not null
     */
    @Test
    public void testUpdateConfigWithNonNullConfig() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(globalConfig);
        DatabaseWriteProhibitionManager.updateLocalConfig(localConfig);
        Assert.assertEquals(globalConfig, DatabaseWriteProhibitionManager.getGlobalConfig());
        Assert.assertEquals(localConfig, DatabaseWriteProhibitionManager.getLocalConfig());
    }

    /**
     * test when the update configuration is null
     */
    @Test
    public void testUpdateConfigWithNullConfig() {
        DatabaseWriteProhibitionManager.updateGlobalConfig(null);
        DatabaseWriteProhibitionManager.updateLocalConfig(null);

        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getGlobalConfig().getMongoDbDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getGlobalConfig().getMySqlDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getGlobalConfig().getPostgreSqlDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getGlobalConfig().getOpenGaussDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getLocalConfig().getMongoDbDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getLocalConfig().getMySqlDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getLocalConfig().getPostgreSqlDatabases().size());
        Assert.assertEquals(0, DatabaseWriteProhibitionManager.getLocalConfig().getOpenGaussDatabases().size());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getGlobalConfig().isEnableMongoDbWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getGlobalConfig().isEnableMySqlWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getGlobalConfig().isEnablePostgreSqlWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getGlobalConfig().isEnableOpenGaussWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getLocalConfig().isEnableMongoDbWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getLocalConfig().isEnableMySqlWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getLocalConfig().isEnablePostgreSqlWriteProhibition());
        Assert.assertFalse(DatabaseWriteProhibitionManager.getLocalConfig().isEnableOpenGaussWriteProhibition());
    }
}
