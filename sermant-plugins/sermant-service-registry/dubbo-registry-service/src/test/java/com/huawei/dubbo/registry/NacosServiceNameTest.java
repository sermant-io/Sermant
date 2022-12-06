/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.registry;

import org.apache.dubbo.common.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.huawei.dubbo.registry.entity.NacosServiceName;
import com.huawei.registry.config.NacosRegisterConfig;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 测试NacosServiceName
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class NacosServiceNameTest {
    private final NacosRegisterConfig registerConfig = new NacosRegisterConfig();
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8848;
    public static final String PROTOCOL_KEY = "dubbo";
    private static final String CATEGORY_KEY = "category";
    private static final String INTERFACE_KEY = "interface";
    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        registerConfig.setAddress("127.0.0.1:8848");
        registerConfig.setGroup("DEFAULT_GROUP");
        registerConfig.setUsername("nacos");
        registerConfig.setPassword("nacos");
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
    }

    @AfterEach
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * 测试NacosServiceName通过url构建
     */
    @Test
    public void testBuildNacosServiceNameByUrl() {
        URL url = new URL(PROTOCOL_KEY, HOST, PORT);
        url = url.addParameter(CATEGORY_KEY, "providers");
        url = url.addParameter(INTERFACE_KEY, "interface");
        NacosServiceName nacosServiceName = new NacosServiceName(url);
        Assertions.assertEquals(nacosServiceName.getValue(), "providers:interface:DEFAULT_GROUP");
    }

    /**
     * 测试NacosServiceName通过serviceName构建
     */
    @Test
    public void testBuildNacosServiceNameByName() {
        String serviceName = "providers:interface:DEFAULT_GROUP";
        NacosServiceName nacosServiceName = new NacosServiceName(serviceName);
        Assertions.assertEquals(nacosServiceName.getValue(), serviceName);
    }

    /**
     * 测试是否包含“，”、“*”
     */
    @Test
    public void testIsCompatible() {
        String serviceName = "providers:interface:DEFAULT_GROUP";
        NacosServiceName nacosServiceName = new NacosServiceName(serviceName);
        Assertions.assertEquals(nacosServiceName.isCompatible(nacosServiceName), true);
        String serviceName2 = "providers:interface:DEFAULT";
        NacosServiceName nacosServiceName2 = new NacosServiceName(serviceName2);
        Assertions.assertEquals(nacosServiceName.isCompatible(nacosServiceName2), false);
    }

    /**
     * 测试equal
     */
    @Test
    public void testEqual() {
        String serviceName = "providers:interface:DEFAULT_GROUP";
        NacosServiceName nacosServiceName = new NacosServiceName(serviceName);
        Assertions.assertEquals(nacosServiceName.isCompatible(nacosServiceName), true);
        String serviceName2 = "providers:interface:DEFAULT";
        NacosServiceName nacosServiceName2 = new NacosServiceName(serviceName2);
        Assertions.assertEquals(nacosServiceName.equals(nacosServiceName2), false);
    }
}
