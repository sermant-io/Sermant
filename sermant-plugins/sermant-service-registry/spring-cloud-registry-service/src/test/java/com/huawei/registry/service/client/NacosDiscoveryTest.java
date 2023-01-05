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
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.service.register.NacosServiceInstance;
import com.huawei.registry.service.register.NacosServiceManager;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * 测试nacos服务发现事件处理
 *
 * @author chengyouling
 * @since 2022-11-10
 */
public class NacosDiscoveryTest {
    private final List<Instance> instanceList = new ArrayList<>();

    private final NacosRegisterConfig registerConfig = new NacosRegisterConfig();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
        instanceList.add(buildInstance());
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * 测试实例转化
     */
    @Test
    public void testhostToServiceInstanceList() {
        NacosServiceDiscovery discovery = new NacosServiceDiscovery(new NacosServiceManager());
        List<NacosServiceInstance> list = discovery.convertServiceInstanceList(instanceList, "test");
        Assert.assertEquals(list.size(), instanceList.size());

    }

    private Instance buildInstance() {
        final Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(8001);
        return instance;
    }
}
