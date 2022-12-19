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

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.service.client.ScClient;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 测试注册逻辑
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class ScRegisterTest {
    @Mock
    private ScClient scClient;

    private final RegisterConfig registerConfig = new RegisterConfig();

    private final ScRegister scRegister = new ScRegister();

    private final String status = "UP";

    private final String serviceName = "test";

    private final List<MicroserviceInstance> instanceList = new ArrayList<>();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private final RegisterServiceCommonConfig commonConfig = new RegisterServiceCommonConfig();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager
                .getPluginConfig(RegisterServiceCommonConfig.class)).thenReturn(commonConfig);
        ReflectUtils.setFieldValue(scRegister, "client", scClient);
        Mockito.when(scClient.getInstanceStatus()).thenReturn(status);
        Mockito.when(scClient.getRegisterCenterStatus()).thenReturn(status);
        instanceList.add(buildInstance(8001));
        instanceList.add(buildInstance(8002));
        Mockito.when(scClient.queryInstancesByServiceId(serviceName)).thenReturn(instanceList);
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void start() {
        scRegister.start();
    }

    @Test
    public void register() {
        scRegister.register();
        Mockito.verify(scClient, Mockito.times(1)).register();
    }

    @Test
    public void getRegisterCenterStatus() {
        Assert.assertEquals(status, scRegister.getRegisterCenterStatus());
    }

    @Test
    public void getInstanceStatus() {
        Assert.assertEquals(status, scRegister.getInstanceStatus());
    }

    @Test
    public void updateInstanceStatus() {
        scRegister.updateInstanceStatus(status);
        Mockito.verify(scClient, Mockito.times(1)).updateInstanceStatus(status);
    }

    @Test
    public void getInstanceList() {
        final List<ServicecombServiceInstance> instanceList = scRegister.getInstanceList(serviceName);
        Assert.assertEquals(instanceList.size(), this.instanceList.size());
    }

    private MicroserviceInstance buildInstance(int port) {
        final MicroserviceInstance microserviceInstance = new MicroserviceInstance();
        microserviceInstance.setHostName("localhost");
        microserviceInstance.setEndpoints(Collections.singletonList("rest://localhost:" + port));
        return microserviceInstance;
    }
}
