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

package com.huawei.registry.interceptors.cloud3.x;

import static org.junit.Assert.assertEquals;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.DiscoveryServiceInstance;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拦截获取实例测试
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ZookeeperInstanceSupplierInterceptorTest {
    private final String serviceName = "test";

    private final List<ServiceInstance> allInstances = new ArrayList<>();

    private final List<ServiceInstance> scInstances = new ArrayList<>();

    private final List<ServiceInstance> originInstances = new ArrayList<>();

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        final RegisterConfig registerConfig = new RegisterConfig();
        registerConfig.setEnableSpringRegister(true);
        registerConfig.setOpenMigration(true);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
    }

    @After
    public void tearDown() throws Exception {
        pluginConfigManagerMockedStatic.close();
    }

    @Test
    public void doAfter() throws NoSuchMethodException {
        RegisterContext.INSTANCE.setAvailable(true);
        final ZookeeperInstanceSupplierInterceptor interceptor = new ZookeeperInstanceSupplierInterceptor();
        final ExecuteContext context = interceptor.doAfter(buildContext());
        final Object result = context.getResult();
        Assert.assertTrue(result instanceof List);
        List<ServiceInstance> instances = (List<ServiceInstance>) result;
        assertEquals(instances.size(), (scInstances.size() + originInstances.size()));
        RegisterContext.INSTANCE.setAvailable(false);
    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        scInstances.add(new DiscoveryServiceInstance(buildInstance(8001), serviceName));
        scInstances.add(new DiscoveryServiceInstance(buildInstance(8002), serviceName));
        scInstances.add(new DiscoveryServiceInstance(buildInstance(8003), serviceName));
        scInstances.add(new DiscoveryServiceInstance(buildInstance(8004), serviceName));
        originInstances.add(new ZookeeperServiceInstance(serviceName, buildZkInstance(8005)));
        originInstances.add(new ZookeeperServiceInstance(serviceName, buildZkInstance(8006)));
        originInstances.add(new ZookeeperServiceInstance(serviceName, buildZkInstance(8007)));
        originInstances.add(new ZookeeperServiceInstance(serviceName, buildZkInstance(8008)));
        allInstances.addAll(originInstances);
        allInstances.addAll(scInstances);
        final ExecuteContext context = ExecuteContext.forMemberMethod(this, String.class.getDeclaredMethod("trim"),
                new Object[]{allInstances}, null, null);
        context.changeResult(originInstances);
        return context;
    }

    /**
     * 构建实例
     *
     * @param port 端口
     * @return 实例
     */
    public org.apache.curator.x.discovery.ServiceInstance buildZkInstance(int port) {
        final org.apache.curator.x.discovery.ServiceInstance serviceInstance = Mockito
                .mock(org.apache.curator.x.discovery.ServiceInstance.class);
        Mockito.when(serviceInstance.getPort()).thenReturn(port);
        Mockito.when(serviceInstance.getAddress()).thenReturn("localhost");
        Mockito.when(serviceInstance.buildUriSpec()).thenReturn("http://localhost:" + port);
        return serviceInstance;
    }

    /**
     * 构建实例
     *
     * @param port 端口
     * @return 实例
     */
    public MicroServiceInstance buildInstance(int port) {
        return new MicroServiceInstance() {
            @Override
            public String getServiceName() {
                return serviceName;
            }

            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public String getIp() {
                return "127.0.0.1";
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public String getServiceId() {
                return serviceName;
            }

            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public Map<String, String> getMetadata() {
                return new HashMap<>();
            }

            @Override
            public boolean isSecure() {
                return false;
            }
        };
    }
}
