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

package com.huawei.dubbo.register;

import com.huawei.dubbo.register.alibaba.ServiceCenterRegistry;
import com.huawei.dubbo.register.alibaba.ServiceCenterRegistryFactory;
import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.constants.Constant;
import com.huawei.dubbo.register.utils.ReflectUtils;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryFactory;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 反射测试
 *
 * @author provenceee
 * @since 2022/2/9
 */
public class ReflectUtilsTest {
    private static final String DUBBO_PROTOCOL = "dubbo";

    private static final String URL_PATH = "com.huawei.foo.BarTest";

    /**
     * 测试加载Alibaba dubbo接口实现类
     */
    @Test
    public void testAlibabaDefineClass() {
        // 测试第一次加载ServiceCenterRegistryFactory
        Class<?> clazz = ReflectUtils.defineClass("com.huawei.dubbo.register.alibaba.ServiceCenterRegistryFactory");
        Assert.assertEquals(ServiceCenterRegistryFactory.class, clazz);

        // 测试第二次加载ServiceCenterRegistryFactory
        Class<?> clazz1 = ReflectUtils.defineClass("com.huawei.dubbo.register.alibaba.ServiceCenterRegistryFactory");
        Assert.assertEquals(ServiceCenterRegistryFactory.class, clazz1);

        // 测试第一次加载ServiceCenterRegistry
        Class<?> clazz2 = ReflectUtils.defineClass("com.huawei.dubbo.register.alibaba.ServiceCenterRegistry");
        Assert.assertEquals(ServiceCenterRegistry.class, clazz2);

        // 测试第二次加载ServiceCenterRegistry
        Class<?> clazz3 = ReflectUtils.defineClass("com.huawei.dubbo.register.alibaba.ServiceCenterRegistry");
        Assert.assertEquals(ServiceCenterRegistry.class, clazz3);
    }

    /**
     * 测试Alibaba RegistryConfig
     *
     * @see com.alibaba.dubbo.config.RegistryConfig
     */
    @Test
    public void testAlibabaRegistryConfig() {
        // 测试构造方法
        RegistryConfig registryConfig = ReflectUtils.newRegistryConfig(RegistryConfig.class);
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals(TestConstant.SC_ADDRESS, registryConfig.getAddress());

        // 没有isValid方法，返回true
        Assert.assertTrue(ReflectUtils.isValid(registryConfig));

        // 测试getProtocol方法
        registryConfig.setProtocol(Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getProtocol(registryConfig));

        // 测试setId、getId方法
        ReflectUtils.setId(registryConfig, Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getId(registryConfig));
    }

    /**
     * 测试Alibaba URL
     *
     * @see com.alibaba.dubbo.common.URL
     */
    @Test
    public void testAlibabaUrl() {
        // 缓存url class
        DubboCache.INSTANCE.setUrlClass(URL.class);
        Assert.assertEquals(URL.class, DubboCache.INSTANCE.getUrlClass());
        testUrl("dubbo://localhost:8080/com.huawei.foo.BarTest");
    }

    /**
     * 测试Alibaba ApplicationConfig
     *
     * @see com.alibaba.dubbo.config.ApplicationConfig
     */
    @Test
    public void testAlibabaApplicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig(TestConstant.FOO);

        // 测试getName方法
        Assert.assertEquals(TestConstant.FOO, ReflectUtils.getName(applicationConfig));

        // 测试getParameters方法
        Assert.assertNull(ReflectUtils.getParameters(applicationConfig));

        // 测试setParameters方法
        ReflectUtils.setParameters(applicationConfig, Collections.singletonMap(TestConstant.FOO, TestConstant.BAR));
        Assert.assertEquals(TestConstant.BAR, ReflectUtils.getParameters(applicationConfig).get(TestConstant.FOO));
    }

    /**
     * 测试Alibaba AbstractInterfaceConfig
     *
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     */
    @Test
    public void testAlibabaAbstractInterfaceConfig() {
        AbstractInterfaceConfig config = new AbstractInterfaceConfig() {
        };
        Assert.assertNull(ReflectUtils.getRegistries(config));
        RegistryConfig registryConfig = new RegistryConfig();
        config.setRegistry(registryConfig);
        Assert.assertEquals(registryConfig, ReflectUtils.getRegistries(config).get(0));
    }

    /**
     * 测试Alibaba ExtensionLoader
     *
     * @see com.alibaba.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testAlibabaExtensionLoader() {
        ExtensionLoader<RegistryFactory> loader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);

        // 初始化
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(loader);
        Assert.assertEquals(0, cachedClasses.size());

        // 加载ServiceCenterRegistryFactory
        cachedClasses.put(Constant.SC_REGISTRY_PROTOCOL, ServiceCenterRegistryFactory.class);
        Assert.assertEquals(1, ReflectUtils.getExtensionClasses(loader).size());
        Assert.assertEquals(ServiceCenterRegistryFactory.class,
            ReflectUtils.getExtensionClasses(loader).get(Constant.SC_REGISTRY_PROTOCOL));
    }

    /**
     * 测试Alibaba NotifyListener
     *
     * @see com.alibaba.dubbo.common.URL
     * @see com.alibaba.dubbo.registry.NotifyListener
     */
    @Test
    public void testAlibabaNotifyListener() {
        NotifyListenerTest notifyListener = new NotifyListenerTest();
        List<Object> list = Collections.singletonList(URL.valueOf(TestConstant.SC_ADDRESS));
        ReflectUtils.notify(notifyListener, list);
        Assert.assertEquals(list, notifyListener.getList());
    }

    /**
     * 测试加载Apache dubbo接口实现类
     */
    @Test
    public void testApacheDefineClass() {
        // 测试第一次加载ServiceCenterRegistryFactory
        Class<?> clazz = ReflectUtils.defineClass("com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory");
        Assert.assertEquals(com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory.class, clazz);

        // 测试第二次加载ServiceCenterRegistryFactory
        Class<?> clazz1 = ReflectUtils.defineClass("com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory");
        Assert.assertEquals(com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory.class, clazz1);

        // 测试第一次加载ServiceCenterRegistry
        Class<?> clazz2 = ReflectUtils.defineClass("com.huawei.dubbo.register.apache.ServiceCenterRegistry");
        Assert.assertEquals(com.huawei.dubbo.register.apache.ServiceCenterRegistry.class, clazz2);

        // 测试第二次加载ServiceCenterRegistry
        Class<?> clazz3 = ReflectUtils.defineClass("com.huawei.dubbo.register.apache.ServiceCenterRegistry");
        Assert.assertEquals(com.huawei.dubbo.register.apache.ServiceCenterRegistry.class, clazz3);
    }

    /**
     * 测试Apache RegistryConfig
     *
     * @see org.apache.dubbo.config.RegistryConfig
     */
    @Test
    public void testApacheRegistryConfig() {
        // 测试构造方法
        org.apache.dubbo.config.RegistryConfig registryConfig = ReflectUtils
            .newRegistryConfig(org.apache.dubbo.config.RegistryConfig.class);
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals(TestConstant.SC_ADDRESS, registryConfig.getAddress());

        // 测试isValid方法
        Assert.assertTrue(ReflectUtils.isValid(registryConfig));
        Assert.assertFalse(ReflectUtils.isValid(new org.apache.dubbo.config.RegistryConfig()));

        // 测试getProtocol方法
        registryConfig.setProtocol(Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getProtocol(registryConfig));

        // 测试setId、getId方法
        ReflectUtils.setId(registryConfig, Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getId(registryConfig));

        // 测试setPrefix方法
        ReflectUtils.setPrefix(registryConfig, "dubbo.registries.");
        Assert.assertEquals("dubbo.registries.", registryConfig.getPrefix());
    }

    /**
     * 测试Apache URL
     *
     * @see org.apache.dubbo.common.URL
     */
    @Test
    public void testApacheUrl() {
        // 缓存url class
        DubboCache.INSTANCE.setUrlClass(org.apache.dubbo.common.URL.class);
        Assert.assertEquals(org.apache.dubbo.common.URL.class, DubboCache.INSTANCE.getUrlClass());
        testUrl("dubbo://localhost:8080/com.huawei.foo.BarTest");
    }

    /**
     * 测试Apache ApplicationConfig
     *
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Test
    public void testApacheApplicationConfig() {
        org.apache.dubbo.config.ApplicationConfig applicationConfig
            = new org.apache.dubbo.config.ApplicationConfig(TestConstant.FOO);

        // 测试getName方法
        Assert.assertEquals(TestConstant.FOO, ReflectUtils.getName(applicationConfig));

        // 测试getParameters方法
        Assert.assertNull(ReflectUtils.getParameters(applicationConfig));

        // 测试setParameters方法
        ReflectUtils.setParameters(applicationConfig, Collections.singletonMap(TestConstant.FOO, TestConstant.BAR));
        Assert.assertEquals(TestConstant.BAR, ReflectUtils.getParameters(applicationConfig).get(TestConstant.FOO));
    }

    /**
     * 测试Apache AbstractInterfaceConfig
     *
     * @see org.apache.dubbo.config.AbstractInterfaceConfig
     */
    @Test
    public void testApacheAbstractInterfaceConfig() {
        org.apache.dubbo.config.AbstractInterfaceConfig config = new org.apache.dubbo.config.AbstractInterfaceConfig() {
        };
        Assert.assertNull(ReflectUtils.getRegistries(config));
        org.apache.dubbo.config.RegistryConfig registryConfig = new org.apache.dubbo.config.RegistryConfig();
        config.setRegistry(registryConfig);
        Assert.assertEquals(registryConfig, ReflectUtils.getRegistries(config).get(0));
    }

    /**
     * 测试Apache ExtensionLoader
     *
     * @see org.apache.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testApacheExtensionLoader() {
        org.apache.dubbo.common.extension.ExtensionLoader<org.apache.dubbo.registry.RegistryFactory> loader =
            org.apache.dubbo.common.extension.ExtensionLoader
                .getExtensionLoader(org.apache.dubbo.registry.RegistryFactory.class);

        // 初始化
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(loader);
        Assert.assertEquals(1, cachedClasses.size());

        // 加载ServiceCenterRegistryFactory
        cachedClasses
            .put(Constant.SC_REGISTRY_PROTOCOL, com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory.class);
        Assert.assertEquals(2, ReflectUtils.getExtensionClasses(loader).size());
        Assert.assertEquals(com.huawei.dubbo.register.apache.ServiceCenterRegistryFactory.class,
            ReflectUtils.getExtensionClasses(loader).get(Constant.SC_REGISTRY_PROTOCOL));
    }

    /**
     * 测试Apache NotifyListener
     *
     * @see org.apache.dubbo.common.URL
     * @see org.apache.dubbo.registry.NotifyListener
     */
    @Test
    public void testApacheNotifyListener() {
        ApacheNotifyListenerTest notifyListener = new ApacheNotifyListenerTest();
        List<Object> list = Collections.singletonList(org.apache.dubbo.common.URL.valueOf(TestConstant.SC_ADDRESS));
        ReflectUtils.notify(notifyListener, list);
        Assert.assertEquals(list, notifyListener.getList());
    }

    private void testUrl(String address) {
        // 测试valueOf方法
        Object url = ReflectUtils.valueOf(address);
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost:8080", ReflectUtils.getAddress(url));
        Assert.assertEquals(URL_PATH, ReflectUtils.getPath(url));

        // 测试setHost方法
        url = ReflectUtils.setHost(url, "localhost1");
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost1:8080", ReflectUtils.getAddress(url));
        Assert.assertEquals(URL_PATH, ReflectUtils.getPath(url));

        // 测试setAddress方法
        url = ReflectUtils.setAddress(url, "localhost2:8081");
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost2:8081", ReflectUtils.getAddress(url));
        Assert.assertEquals(URL_PATH, ReflectUtils.getPath(url));
    }

    /**
     * NotifyListener测试类
     */
    public static class NotifyListenerTest implements NotifyListener {
        private List<URL> list;

        @Override
        public void notify(List<URL> urls) {
            this.list = urls;
        }

        public List<URL> getList() {
            return list;
        }
    }

    /**
     * NotifyListener测试类
     */
    public static class ApacheNotifyListenerTest implements org.apache.dubbo.registry.NotifyListener {
        private List<org.apache.dubbo.common.URL> list;

        @Override
        public void notify(List<org.apache.dubbo.common.URL> urls) {
            this.list = urls;
        }

        public List<org.apache.dubbo.common.URL> getList() {
            return list;
        }
    }
}