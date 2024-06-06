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

package io.sermant.backend.controller;

import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.ConfigServerInfo;
import io.sermant.backend.entity.config.PluginType;
import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.service.ConfigService;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for ConfigController class
 *
 * @author zhp
 * @since 2024-06-05
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigService.class, ConfigClient.class})
public class ConfigControllerTest {
    private static final String KEY = "sermant.plugin.registry";

    private static final String ADD_CONFIGURATION_KEY = "sermant.agent.registry";

    private static final String GROUP = "app=default&environment=&service=rest-provider";

    private static final String CONTENT = "enableMongoDbWriteProhibition: true";

    private static final String SERVICE_NAME = "rest-provider";

    private static final String ADDRESS = "127.0.0.1:2181";

    private static final String USERNAME = "test";

    private static final String DYNAMIC_CONFIG_TYPE = "Zookeeper";

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigController configController;

    private ConfigInfo configInfo;

    private ConfigInfo addConfigInfo;

    @Before
    public void setUp() {
        configInfo = new ConfigInfo();
        configInfo.setKey(KEY);
        configInfo.setGroup(GROUP);
        configInfo.setPluginType(PluginType.DATABASE_WRITE_PROHIBITION.getPluginName());
        configInfo.setServiceName(SERVICE_NAME);
        configInfo.setContent(CONTENT);
        addConfigInfo = new ConfigInfo();
        addConfigInfo.setGroup(GROUP);
        addConfigInfo.setKey(ADD_CONFIGURATION_KEY);
        addConfigInfo.setContent(CONTENT);
        List<ConfigInfo> configInfoList = new ArrayList<>();
        configInfoList.add(configInfo);
        PowerMockito.mockStatic(ConfigService.class, ConfigClient.class);
        Result<List<ConfigInfo>> result =
                new Result<>(ResultCodeType.SUCCESS.getCode(), ResultCodeType.SUCCESS.getMessage(), configInfoList);
        PowerMockito.when(configService.getConfigList(configInfo, PluginType.DATABASE_WRITE_PROHIBITION, false))
                .thenReturn(result);
        PowerMockito.when(configService.getConfigList(addConfigInfo, PluginType.DATABASE_WRITE_PROHIBITION, false))
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null));
        PowerMockito.when(configService.getConfigList(addConfigInfo, PluginType.OTHER, true))
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null));
        PowerMockito.when(configService.getConfigList(configInfo, PluginType.OTHER, true))
                .thenReturn(result);
        PowerMockito.when(configService.getConfig(configInfo))
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null, configInfo));
        PowerMockito.when(configService.publishConfig(configInfo))
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null, true));
        PowerMockito.when(configService.publishConfig(addConfigInfo))
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null, true));
        PowerMockito.when(configService.deleteConfig(configInfo))
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null, true));
        ConfigServerInfo configServerInfo = new ConfigServerInfo();
        configServerInfo.setServerAddress(ADDRESS);
        configServerInfo.setUserName(USERNAME);
        configServerInfo.setDynamicConfigType(DYNAMIC_CONFIG_TYPE);
        PowerMockito.when(configService.getConfigurationCenter())
                .thenReturn(new Result<>(ResultCodeType.SUCCESS.getCode(), null, configServerInfo));
    }

    @Test
    public void getConfigList() {
        Result<List<ConfigInfo>> result = configController.getConfigList(configInfo);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertEquals(1, result.getData().size());
        ConfigInfo info = result.getData().get(0);
        Assert.assertEquals(info.getKey(), KEY);
        Assert.assertEquals(info.getGroup(), GROUP);
        Assert.assertEquals(info.getServiceName(), SERVICE_NAME);
        Result<List<ConfigInfo>> newResult = configController.getConfigList(new ConfigInfo());
        Assert.assertFalse(newResult.isSuccess());
        Assert.assertNull(newResult.getData());
    }

    @Test
    public void getConfig() {
        Result<ConfigInfo> result = configController.getConfig(configInfo);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        ConfigInfo info = result.getData();
        Assert.assertEquals(info.getKey(), KEY);
        Assert.assertEquals(info.getGroup(), GROUP);
        Assert.assertEquals(info.getServiceName(), SERVICE_NAME);
        Assert.assertEquals(info.getContent(), CONTENT);
    }

    @Test
    public void addConfig() {
        Result<Boolean> result = configController.addConfig(configInfo);
        Assert.assertFalse(result.isSuccess());
        Result<Boolean> result1 = configController.addConfig(addConfigInfo);
        Assert.assertTrue(result1.isSuccess());
    }

    @Test
    public void updateConfig() {
        Result<Boolean> result = configController.updateConfig(configInfo);
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void deleteConfig() {
        Result<Boolean> result = configController.deleteConfig(configInfo);
        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getData());
    }

    @Test
    public void getConfigurationCenter() {
        Result<ConfigServerInfo> result = configController.getConfigurationCenter();
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        ConfigServerInfo configServerInfo = result.getData();
        Assert.assertEquals(configServerInfo.getServerAddress(), ADDRESS);
        Assert.assertEquals(configServerInfo.getUserName(), USERNAME);
        Assert.assertEquals(configServerInfo.getDynamicConfigType(), DYNAMIC_CONFIG_TYPE);

    }
}