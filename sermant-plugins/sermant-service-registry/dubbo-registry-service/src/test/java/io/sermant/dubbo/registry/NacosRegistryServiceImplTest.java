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

package io.sermant.dubbo.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.dubbo.registry.cache.DubboCache;
import io.sermant.dubbo.registry.service.NacosRegistryServiceImpl;
import io.sermant.dubbo.registry.utils.ReflectUtils;
import io.sermant.registry.config.NacosRegisterConfig;
import io.sermant.registry.config.RegisterServiceCommonConfig;

import org.apache.dubbo.registry.NotifyListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test NacosRegistryServiceImpl
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class NacosRegistryServiceImplTest {
    private NacosRegistryServiceImpl registryService = new NacosRegistryServiceImpl();
    private NacosRegisterConfig registerConfig = new NacosRegisterConfig();
    private final RegisterServiceCommonConfig commonConfig = new RegisterServiceCommonConfig();
    NamingService namingService = Mockito.mock(NamingService.class);

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException, NacosException {
        MockitoAnnotations.openMocks(this);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        commonConfig.setAddress("127.0.0.1:8848");
        registerConfig.setGroup("default");
        registerConfig.setUsername("nacos");
        registerConfig.setPassword("nacos");
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(NacosRegisterConfig.class))
                .thenReturn(registerConfig);
        Field field = registryService.getClass().getDeclaredField("nacosRegisterConfig");
        field.setAccessible(true);
        field.set(registryService, registerConfig);
        Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(8202);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("protocol", "dubbo");
        metadata.put("path", "io.sermant.dubbo.registry.service.RegistryService");
        instance.setMetadata(metadata);
        List<Instance> instances = new LinkedList<>();
        instances.add(instance);
        Mockito.when(namingService.getAllInstances(
            "providers:io.sermant.dubbo.registry.service.RegistryService:default", "default"))
                .thenReturn(instances);
        ListView<String> listView = new ListView<>();
        List<String> list = new LinkedList<>();
        list.add("providers:io.sermant.dubbo.registry.service.RegistryService:default");
        listView.setData(list);
        Mockito.when(namingService.getServicesOfServer(1, Integer.MAX_VALUE, "default,default"))
                .thenReturn(listView);
    }

    @AfterEach
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * Test registration doRegister
     *
     * @throws NoSuchMethodException Can't find method
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testDoRegister() throws NoSuchFieldException, IllegalAccessException {
        Object url = buildUrl("provider");
        Field field = registryService.getClass().getDeclaredField("namingService");
        field.setAccessible(true);
        field.set(registryService, namingService);
        registryService.doRegister(url);
        Assertions.assertEquals(registryService.getRegistryInstance().getIp(), "127.0.0.1");
    }

    /**
     * Test doSubscribe/doUnsubscribe
     *
     * @throws NoSuchMethodException Can't find method
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testSubscribe() throws NoSuchFieldException, IllegalAccessException {
        Object url = buildUrl("consumer");
        NotifyListener notifyListener = Mockito.mock(NotifyListener.class);
        Field field = registryService.getClass().getDeclaredField("namingService");
        field.setAccessible(true);
        field.set(registryService, namingService);
        registryService.doSubscribe(url, notifyListener);
        registryService.doUnsubscribe(url, notifyListener);
        Assertions.assertEquals(registryService.getOriginToAggregateListener().size(), 0);
    }

    private Object buildUrl(String protocol) {
        Map<String, String> paramers = new HashMap<>();
        paramers.put("interface", "io.sermant.dubbo.registry.service.RegistryService");
        paramers.put("category", "providers");
        String address = "127.0.0.1" + registerConfig.getServiceNameSeparator() + "8202";
        DubboCache.INSTANCE.setUrlClass(URL.class);
        Object object = ReflectUtils.valueOf(address);
        object = ReflectUtils.setProtocol(object, protocol);
        object = ReflectUtils.setPath(object, "io.sermant.dubbo.registry.service.RegistryService");
        object = ReflectUtils.setHost(object, "127.0.0.1");
        return ReflectUtils.addParameters(object, paramers);
    }

    /**
     * Test doSubscribe
     *
     * @throws NoSuchMethodException Can't find method
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testSubscribeWithCompatible() throws NoSuchFieldException, IllegalAccessException {
        Object url = buildUrl("consumer");
        NotifyListener notifyListener = Mockito.mock(NotifyListener.class);
        Field field = registryService.getClass().getDeclaredField("namingService");
        field.setAccessible(true);
        field.set(registryService, namingService);
        registerConfig.setGroup("default,default");
        registryService.doSubscribe(url, notifyListener);
        Assertions.assertEquals(registryService.getOriginToAggregateListener().size(), 1);
    }
}
