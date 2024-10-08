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

import io.sermant.core.service.BaseService;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.utils.KeyGenerateUtils;
import io.sermant.dubbo.registry.alibaba.NacosRegistry;
import io.sermant.dubbo.registry.alibaba.NacosRegistryFactory;
import io.sermant.dubbo.registry.service.nacos.NacosRegistryService;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Test NacosRegistryFactory
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class NacosRegistryFactoryTest {
    /**
     * Constructor
     */
    public NacosRegistryFactoryTest() throws NoSuchFieldException, IllegalAccessException {
        Field field = ServiceManager.class.getDeclaredField("SERVICES");
        field.setAccessible(true);
        Map<String, BaseService> map = (Map<String, BaseService>) field.get(null);
        map.put(KeyGenerateUtils.generateClassKeyWithClassLoader(NacosRegistryService.class),
                new NacosRegistryService() {

            @Override
            public void doRegister(Object url) {

            }

            @Override
            public void doSubscribe(Object url, Object notifyListener) {

            }

            @Override
            public void doUnregister(Object url) {

            }

            @Override
            public void buildNamingService(Map<String, String> parameters) {

            }

            @Override
            public void doUnsubscribe(Object url, Object notifyListener) {

            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        });
    }

    /**
     * Test Alibaba NacosRegistryFactory
     *
     * @throws NoSuchMethodException Can't find method
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testAlibabaNacosRegistryFactory()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        NacosRegistryFactory registryFactory = new NacosRegistryFactory();
        Method method = registryFactory.getClass().getDeclaredMethod("createRegistry", URL.class);
        method.setAccessible(true);
        Assert.assertEquals(NacosRegistry.class, method.invoke(registryFactory,
            URL.valueOf(TestConstant.NACOS_ADDRESS)).getClass());
    }

    /**
     * Test Apache NacosRegistryFactory
     *
     * @throws NoSuchMethodException Can't find method
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testApacheNacosRegistryFactory()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        io.sermant.dubbo.registry.apache.NacosRegistryFactory registryFactory =
            new io.sermant.dubbo.registry.apache.NacosRegistryFactory();
        Method method = registryFactory.getClass()
            .getDeclaredMethod("createRegistry", org.apache.dubbo.common.URL.class);
        method.setAccessible(true);
        Assert.assertEquals(io.sermant.dubbo.registry.apache.NacosRegistry.class, method.invoke(registryFactory,
            org.apache.dubbo.common.URL.valueOf(TestConstant.NACOS_ADDRESS)).getClass());
    }
}
