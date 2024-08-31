/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.backend.service;

import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.hotplugging.CommandType;
import io.sermant.backend.entity.hotplugging.HotPluggingConfig;
import io.sermant.implement.service.dynamicconfig.ConfigClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.yaml.snakeyaml.Yaml;

/**
 * Test class for HotPluggingService class
 *
 * @author zhp
 * @since 2024-06-05
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigService.class, ConfigClient.class})
public class HotPluggingServiceTest {
    private static final String HOT_PLUGGING_CONFIG_KEY = "config";

    private static final String HOT_PLUGGING_CONFIG_GROUP = "sermant-hot-plugging";

    private static final String INSTANCE_ID = "82ea03ab-b553-4b24-a4cc-a8c06611ea68";

    private static final String INSTANCE_IDS = "82ea03ab-b553-4b24-a4cc-a8c06611ea68, "
            + "82ea03ab-b553-4b24-a4cc-a8c06611ea89";

    private final Yaml yaml = new Yaml();

    @Mock
    private ConfigClient configClient;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private HotPluggingService hotPluggingService;


    @Before
    public void setUp() {
        PowerMockito.when(configService.getConfigClient()).thenReturn(configClient);
    }

    @Test
    public void testMissParam() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        hotPluggingConfig.setInstanceIds(INSTANCE_ID);
        Result<Boolean> result = hotPluggingService.publishHotPluggingConfig(hotPluggingConfig);
        assertResult(result, ResultCodeType.MISS_PARAM);
    }

    @Test
    public void testOnePlugin() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        hotPluggingConfig.setInstanceIds(INSTANCE_ID);
        hotPluggingConfig.setPluginNames("database-write-prohibition");
        PowerMockito.when(configClient.publishConfig(HOT_PLUGGING_CONFIG_KEY, HOT_PLUGGING_CONFIG_GROUP,
                yaml.dumpAsMap(hotPluggingConfig))).thenReturn(true);
        assertResult(hotPluggingService.publishHotPluggingConfig(hotPluggingConfig), ResultCodeType.SUCCESS);
    }

    @Test
    public void testMultipleInstanceId() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        hotPluggingConfig.setInstanceIds(INSTANCE_IDS);
        hotPluggingConfig.setPluginNames("database-write-prohibition");
        PowerMockito.when(configClient.publishConfig(HOT_PLUGGING_CONFIG_KEY, HOT_PLUGGING_CONFIG_GROUP,
                yaml.dumpAsMap(hotPluggingConfig))).thenReturn(true);
        assertResult(hotPluggingService.publishHotPluggingConfig(hotPluggingConfig), ResultCodeType.SUCCESS);
    }

    @Test
    public void testMultiplePlugin() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        hotPluggingConfig.setInstanceIds(INSTANCE_ID);
        hotPluggingConfig.setPluginNames("database-write-prohibition,tag-transmission");
        PowerMockito.when(configClient.publishConfig(HOT_PLUGGING_CONFIG_KEY, HOT_PLUGGING_CONFIG_GROUP,
                yaml.dumpAsMap(hotPluggingConfig))).thenReturn(true);
        assertResult(hotPluggingService.publishHotPluggingConfig(hotPluggingConfig), ResultCodeType.SUCCESS);
    }

    @Test
    public void testPublishFailure() {
        HotPluggingConfig hotPluggingConfig = new HotPluggingConfig();
        hotPluggingConfig.setCommandType(CommandType.UPDATE_PLUGINS.getValue());
        hotPluggingConfig.setInstanceIds(INSTANCE_ID);
        hotPluggingConfig.setPluginNames("database-write-prohibition,tag-transmission");
        PowerMockito.when(configClient.publishConfig(HOT_PLUGGING_CONFIG_KEY, HOT_PLUGGING_CONFIG_GROUP,
                yaml.dumpAsMap(hotPluggingConfig))).thenReturn(false);
        assertResult(hotPluggingService.publishHotPluggingConfig(hotPluggingConfig), ResultCodeType.FAIL);
    }

    public void assertResult(Result<Boolean> result, ResultCodeType resultCodeType) {
        Assert.assertEquals(result.getCode(), resultCodeType.getCode());
        Assert.assertEquals(result.getMessage(), resultCodeType.getMessage());
    }
}