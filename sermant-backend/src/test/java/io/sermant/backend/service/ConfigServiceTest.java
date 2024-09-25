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

import io.sermant.backend.common.conf.CommonConst;
import io.sermant.backend.common.conf.DynamicConfig;
import io.sermant.backend.entity.config.ConfigInfo;
import io.sermant.backend.entity.config.Result;
import io.sermant.backend.entity.config.ResultCodeType;
import io.sermant.backend.entity.template.PageTemplateInfo;
import io.sermant.implement.service.dynamicconfig.ConfigClient;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

/**
 * Test class for ConfigService class
 *
 * @author zhp
 * @since 2024-06-05
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamicConfig.class, ConfigClient.class, PageTemplateService.class})
public class ConfigServiceTest {
    private static final String KEY = ".*";

    private static final String GROUP = "app=default&environment=&service=rest-provider";

    private static final String CONTENT = "enableMongoDbWriteProhibition: true";

    @Mock
    private ConfigClient configClient;

    @Mock
    private DynamicConfig dynamicConfig;

    @Mock
    private PageTemplateService pageTemplateService;

    @InjectMocks
    private ConfigService configService;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(DynamicConfig.class, ConfigClient.class, PageTemplateService.class);
        Map<String, List<String>> map = new HashMap<>();
        List<String> configList = new ArrayList<>();
        configList.add(KEY);
        map.put(GROUP, configList);
        PowerMockito.when(configClient.getConfigList(StringUtils.EMPTY, GROUP, true)).thenReturn(map);
        PowerMockito.when(configClient.isConnect()).thenReturn(true);
        PowerMockito.when(configClient.getConfig(KEY, GROUP)).thenReturn(CONTENT);
        PowerMockito.when(configClient.publishConfig(KEY, GROUP, CONTENT)).thenReturn(true);
        PowerMockito.when(configClient.removeConfig(KEY, GROUP)).thenReturn(true);
        PowerMockito.when(dynamicConfig.isEnable()).thenReturn(true);
        PageTemplateInfo pageTemplateInfo = new PageTemplateInfo();
        pageTemplateInfo.setKeyRule(new ArrayList<>());
        pageTemplateInfo.setGroupRule(new ArrayList<>());
        Result<PageTemplateInfo> result = new Result<>(ResultCodeType.SUCCESS, pageTemplateInfo);
        PowerMockito.when(pageTemplateService.getTemplate("common")).thenReturn(result);
    }

    @Test
    public void getConfigList() {
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setGroup(GROUP);
        configInfo.setKey(KEY);
        configInfo.setPluginType("common");
        configInfo.setGroupRule(GROUP);
        configInfo.setExactMatchFlag(true);
        Result<List<ConfigInfo>> result = configService.getConfigList(configInfo);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
        Assert.assertEquals(result.getData().size(), 1);
        ConfigInfo info = result.getData().get(0);
        Assert.assertEquals(info.getGroup(), GROUP);
        Assert.assertEquals(info.getKey(), KEY);
        Assert.assertNull(info.getEnvironment());
    }

    @Test
    public void getConfig() {
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setGroup(GROUP);
        configInfo.setKey(KEY);
        configInfo.setPluginType("springboot-registry");
        Result<ConfigInfo> result = configService.getConfig(configInfo);
        ConfigInfo info = result.getData();
        Assert.assertEquals(info.getGroup(), GROUP);
        Assert.assertEquals(info.getKey(), KEY);
        Assert.assertEquals(info.getContent(), CONTENT);
    }

    @Test
    public void addConfig() {
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setGroup(GROUP);
        configInfo.setKey(KEY);
        configInfo.setContent(CONTENT);
        configInfo.setPluginType("springboot-registry");
        Result<Boolean> result = configService.publishConfig(configInfo);
        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getData());
    }

    @Test
    public void deleteConfig() {
        ConfigInfo configInfo = new ConfigInfo();
        configInfo.setGroup(GROUP);
        configInfo.setKey(KEY);
        Result<Boolean> result = configService.deleteConfig(configInfo);
        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getData());
    }
}