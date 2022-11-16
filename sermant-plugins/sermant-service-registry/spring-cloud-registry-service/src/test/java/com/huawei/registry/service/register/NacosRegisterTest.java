/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.service.register;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.service.client.NacosClient;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

/**
 * 测试注册逻辑
 *
 * @author chengyouling
 * @since 2022-11-10
 */
public class NacosRegisterTest {
    @Mock
    private NacosClient client;

    private final NacosRegisterConfig registerConfig = new NacosRegisterConfig();

    private final NacosRegister nacosRegister = new NacosRegister();

    private final String status = "UP";

    private final String serviceName = "test";

    private final List<NacosServiceInstance> instanceList = new ArrayList<>();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
        ReflectUtils.setFieldValue(nacosRegister, "client", client);
        Mockito.when(client.getInstanceStatus()).thenReturn(status);
        Mockito.when(client.getServerStatus()).thenReturn(status);
        instanceList.add(buildInstance(8001));
        instanceList.add(buildInstance(8002));
        Mockito.when(client.getInstances(serviceName)).thenReturn(instanceList);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void start() {
        nacosRegister.start();
    }

    @Test
    public void register() {
        nacosRegister.register();
        Mockito.verify(client, Mockito.times(1)).register();
    }

    @Test
    public void getRegisterCenterStatus() {
        Assert.assertEquals(status, nacosRegister.getRegisterCenterStatus());
    }

    @Test
    public void getInstanceStatus() {
        Assert.assertEquals(status, nacosRegister.getInstanceStatus());
    }

    @Test
    public void updateInstanceStatus() {
        nacosRegister.updateInstanceStatus(status);
        Mockito.verify(client, Mockito.times(1)).updateInstanceStatus(status);
    }

    @Test
    public void getInstanceList() {
        final List<NacosServiceInstance> instanceList = nacosRegister.getInstanceList(serviceName);
        Assert.assertEquals(instanceList.size(), this.instanceList.size());
    }

    private NacosServiceInstance buildInstance(int port) {
        final NacosServiceInstance instance = new NacosServiceInstance();
        instance.setHost("localhost");
        instance.setPort(port);
        return instance;
    }
}
