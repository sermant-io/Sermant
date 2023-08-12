/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.service.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.service.register.NacosServiceManager;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

/**
 * 测试nacos服务
 *
 * @author chengyouling
 * @since 2022-11-10
 */
public class NacosClientTest {
    private static final String STATUS_DOWN = "DOWN";

    private final NacosRegisterConfig registerConfig = new NacosRegisterConfig();

    private final RegisterServiceCommonConfig commonConfig = new RegisterServiceCommonConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private NacosServiceManager nacosServiceManager;

    private NacosClient nacosClient;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
        commonConfig.setSecure(true);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class))
                .thenReturn(commonConfig);
        RegisterContext.INSTANCE.getClientInfo().setServiceId("test");
        nacosClient = new NacosClient();
        nacosServiceManager = new NacosServiceManager();
        Map<String, String> map = new HashMap<>();
        map.put("foo", "123");
        RegisterContext.INSTANCE.getClientInfo().setMeta(map);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * 测试注册
     */
    @Test
    public void testRegister() throws NacosException {
        mockNamingService();
        nacosClient.register();
        Assert.assertNotNull(ReflectUtils.getFieldValue(nacosClient, "instance"));
    }

    /**
     * 测试取消注册
     */
    @Test
    public void testDeregister() throws NacosException {
        mockNamingService();
        nacosClient.deregister();
        Assert.assertNotNull(ReflectUtils.getFieldValue(nacosClient, "instance"));
    }

    /**
     * 测试获取服务状态
     */
    @Test
    public void testGetServerStatus() throws NacosException {
        mockNamingService();
        Assert.assertNull(nacosClient.getServerStatus());
    }

    /**
     * 测试更新状态
     */
    @Test
    public void testUpdateInstanceStatus() throws NacosException {
        mockNamingService();
        nacosClient.updateInstanceStatus(STATUS_DOWN);
        Assert.assertNotNull(ReflectUtils.getFieldValue(nacosClient, "instance"));
    }

    /**
     * 测试获取实例状态
     */
    @Test
    public void testGetInstanceStatus() throws NacosException {
        mockNamingService();
        Assert.assertNotNull(nacosClient.getInstanceStatus());
    }

    /**
     * 测试获取实例集合
     */
    @Test
    public void testGetInstances() throws NacosException {
        mockNamingService();
        Assert.assertNotNull(nacosClient.getInstances("test"));
    }

    /**
     * 测试获取服务集合
     */
    @Test
    public void testGetServices() throws NacosException {
        mockNamingService();
        Assert.assertNotNull(nacosClient.getServices());
    }

    private void mockNamingService() throws NacosException {
        final NamingService namingService = Mockito.mock(NamingService.class);
        List<String> list = new ArrayList<>();
        list.add("test");
        ListView<String> services = new ListView<>();
        services.setData(list);
        Mockito.when(namingService.getServicesOfServer(1, Integer.MAX_VALUE, registerConfig.getGroup()))
            .thenReturn(services);
        final NamingMaintainService namingMaintainService = Mockito.mock(NamingMaintainService.class);
        ReflectUtils.setFieldValue(nacosServiceManager, "namingService", namingService);
        ReflectUtils.setFieldValue(nacosServiceManager, "namingMaintainService", namingMaintainService);
        setNacosServiceManager();
    }

    private void setNacosServiceManager() {
        ReflectUtils.setFieldValue(nacosClient, "nacosServiceManager", nacosServiceManager);
        ReflectUtils.setFieldValue(nacosClient, "nacosServiceDiscovery",
                new NacosServiceDiscovery(nacosServiceManager));
    }
}
