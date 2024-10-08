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
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryFactory;

import io.sermant.dubbo.registry.alibaba.ServiceCenterRegistry;
import io.sermant.dubbo.registry.alibaba.ServiceCenterRegistryFactory;
import io.sermant.dubbo.registry.cache.DubboCache;
import io.sermant.dubbo.registry.constants.Constant;
import io.sermant.dubbo.registry.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Reflex test
 *
 * @author provenceee
 * @since 2022-02-09
 */
public class ReflectUtilsTest {
    private static final String DUBBO_PROTOCOL = "dubbo";

    /**
     * Test loading Alibaba Dubbo interface implementation class
     */
    @Test
    public void testAlibabaDefineClass() {
        // Test loading ServiceCenterRegistryFactory for the first time
        Class<?> clazz =
            ReflectUtils.defineClass("io.sermant.dubbo.registry.alibaba.ServiceCenterRegistryFactory").orElse(null);
        Assert.assertEquals(ServiceCenterRegistryFactory.class, clazz);

        // Test loading ServiceCenterRegistryFactory for the second time
        Class<?> clazz1 = ReflectUtils.defineClass("io.sermant.dubbo.registry.alibaba.ServiceCenterRegistryFactory")
            .orElse(null);
        Assert.assertEquals(ServiceCenterRegistryFactory.class, clazz1);

        // 测试第一次加载ServiceCenterRegistry
        Class<?> clazz2 = ReflectUtils.defineClass("io.sermant.dubbo.registry.alibaba.ServiceCenterRegistry")
            .orElse(null);
        Assert.assertEquals(ServiceCenterRegistry.class, clazz2);

        // Test loading ServiceCenterRegistry for the second time
        Class<?> clazz3 = ReflectUtils.defineClass("io.sermant.dubbo.registry.alibaba.ServiceCenterRegistry")
            .orElse(null);
        Assert.assertEquals(ServiceCenterRegistry.class, clazz3);
    }

    /**
     * Test Alibaba RegistryConfig
     *
     * @see com.alibaba.dubbo.config.RegistryConfig
     */
    @Test
    public void testAlibabaRegistryConfig() {
        // Test Constructor
        RegistryConfig registryConfig = ReflectUtils.newRegistryConfig(RegistryConfig.class).orElse(null);
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals(TestConstant.SC_ADDRESS, registryConfig.getAddress());

        // There is no isValid method, which returns true
        Assert.assertTrue(ReflectUtils.isValid(registryConfig));

        // Test the getProtocol method
        registryConfig.setProtocol(Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getProtocol(registryConfig));

        // 测试setId、getId方法
        ReflectUtils.setId(registryConfig, Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getId(registryConfig));
    }

    /**
     * Test Alibaba URL
     *
     * @see com.alibaba.dubbo.common.URL
     */
    @Test
    public void testAlibabaUrl() {
        // 缓存url class
        DubboCache.INSTANCE.setUrlClass(URL.class);
        Assert.assertEquals(URL.class, DubboCache.INSTANCE.getUrlClass());
        testUrl();
    }

    /**
     * Test Alibaba ApplicationConfig
     *
     * @see com.alibaba.dubbo.config.ApplicationConfig
     */
    @Test
    public void testAlibabaApplicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig(TestConstant.FOO);

        // Test the getName method
        Assert.assertEquals(TestConstant.FOO, ReflectUtils.getName(applicationConfig));
    }

    /**
     * Test Alibaba AbstractInterfaceConfig
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
     * Test Alibaba ExtensionLoader
     *
     * @see com.alibaba.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testAlibabaExtensionLoader() {
        ExtensionLoader<RegistryFactory> loader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);

        // Initialization, other UTs may have already loaded, remove ServiceCenterRegistryFactory first
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(loader);
        cachedClasses.remove("sc", ServiceCenterRegistryFactory.class);
        Assert.assertEquals(0, cachedClasses.size());

        // load ServiceCenterRegistryFactory
        cachedClasses.put(Constant.SC_REGISTRY_PROTOCOL, ServiceCenterRegistryFactory.class);
        Assert.assertEquals(1, ReflectUtils.getExtensionClasses(loader).size());
        Assert.assertEquals(ServiceCenterRegistryFactory.class,
            ReflectUtils.getExtensionClasses(loader).get(Constant.SC_REGISTRY_PROTOCOL));
    }

    /**
     * Test Alibaba NotifyListener
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
     * Test loading the Apache dubbo interface implementation class
     */
    @Test
    public void testApacheDefineClass() {
        // Test loading ServiceCenterRegistryFactory for the first time
        Class<?> clazz = ReflectUtils.defineClass("io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory")
            .orElse(null);
        Assert.assertEquals(io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory.class, clazz);

        // Test loading ServiceCenterRegistryFactory for the second time
        Class<?> clazz1 = ReflectUtils.defineClass("io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory")
            .orElse(null);
        Assert.assertEquals(io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory.class, clazz1);

        // Test loading ServiceCenterRegistry for the first time
        Class<?> clazz2 = ReflectUtils.defineClass("io.sermant.dubbo.registry.apache.ServiceCenterRegistry")
            .orElse(null);
        Assert.assertEquals(io.sermant.dubbo.registry.apache.ServiceCenterRegistry.class, clazz2);

        // Test loading ServiceCenterRegistry for the second time
        Class<?> clazz3 = ReflectUtils.defineClass("io.sermant.dubbo.registry.apache.ServiceCenterRegistry")
            .orElse(null);
        Assert.assertEquals(io.sermant.dubbo.registry.apache.ServiceCenterRegistry.class, clazz3);
    }

    /**
     * Test Apache RegistryConfig
     *
     * @see org.apache.dubbo.config.RegistryConfig
     */
    @Test
    public void testApacheRegistryConfig() {
        // Test Constructor
        org.apache.dubbo.config.RegistryConfig registryConfig = ReflectUtils
            .newRegistryConfig(org.apache.dubbo.config.RegistryConfig.class).orElse(null);
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals(TestConstant.SC_ADDRESS, registryConfig.getAddress());

        // The test is theValid method
        Assert.assertTrue(ReflectUtils.isValid(registryConfig));
        Assert.assertFalse(ReflectUtils.isValid(new org.apache.dubbo.config.RegistryConfig()));

        // The test is getProtocol method
        registryConfig.setProtocol(Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getProtocol(registryConfig));

        // The test is setId and getId method
        ReflectUtils.setId(registryConfig, Constant.SC_REGISTRY_PROTOCOL);
        Assert.assertEquals(Constant.SC_REGISTRY_PROTOCOL, ReflectUtils.getId(registryConfig));

        // The test is setPrefix method
        ReflectUtils.setPrefix(registryConfig, "dubbo.registries.");
        Assert.assertEquals("dubbo.registries.", registryConfig.getPrefix());
    }

    /**
     * Test Apache URL
     *
     * @see org.apache.dubbo.common.URL
     */
    @Test
    public void testApacheUrl() {
        // Cache url class
        DubboCache.INSTANCE.setUrlClass(org.apache.dubbo.common.URL.class);
        Assert.assertEquals(org.apache.dubbo.common.URL.class, DubboCache.INSTANCE.getUrlClass());
        testUrl();
    }

    /**
     * Test Apache ApplicationConfig
     *
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Test
    public void testApacheApplicationConfig() {
        org.apache.dubbo.config.ApplicationConfig applicationConfig
            = new org.apache.dubbo.config.ApplicationConfig(TestConstant.FOO);

        // Test the getName method
        Assert.assertEquals(TestConstant.FOO, ReflectUtils.getName(applicationConfig));

    }

    /**
     * Test Apache AbstractInterfaceConfig
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
     * Test Apache ExtensionLoader
     *
     * @see org.apache.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testApacheExtensionLoader() {
        org.apache.dubbo.common.extension.ExtensionLoader<org.apache.dubbo.registry.RegistryFactory> loader =
            org.apache.dubbo.common.extension.ExtensionLoader
                .getExtensionLoader(org.apache.dubbo.registry.RegistryFactory.class);

        // Initialization, other UTs may have already loaded, remove ServiceCenterRegistryFactory first
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(loader);
        cachedClasses.remove("sc", io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory.class);
        Assert.assertEquals(1, cachedClasses.size());

        // Load ServiceCenterRegistryFactory
        cachedClasses
            .put(Constant.SC_REGISTRY_PROTOCOL, io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory.class);
        Assert.assertEquals(2, ReflectUtils.getExtensionClasses(loader).size());
        Assert.assertEquals(io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory.class,
            ReflectUtils.getExtensionClasses(loader).get(Constant.SC_REGISTRY_PROTOCOL));
    }

    /**
     * Test Apache NotifyListener
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

    private void testUrl() {
        // Test the valueOf method
        Object url = ReflectUtils.valueOf("dubbo://localhost:8080/io.sermant.foo.BarTest?group=bar&version=0.0.1");
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost:8080", ReflectUtils.getAddress(url));
        Assert.assertEquals("io.sermant.foo.BarTest", ReflectUtils.getPath(url));
        Assert.assertEquals(2, ReflectUtils.getParameters(url).size());
        Assert.assertEquals("bar", ReflectUtils.getParameters(url).get("group"));
        Assert.assertEquals("0.0.1", ReflectUtils.getParameters(url).get("version"));
        Assert.assertEquals("localhost", ReflectUtils.getHost(url));
        Assert.assertEquals(8080, ReflectUtils.getPort(url));
        Assert.assertEquals("bar", ReflectUtils.getParameter(url, "group"));
        url = ReflectUtils.addParameters(url, Collections.singletonMap("interface", "interface"));
        Assert.assertEquals("interface", ReflectUtils.getServiceInterface(url));

        // Test the setHost method
        url = ReflectUtils.setHost(url, "localhost1");
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost1:8080", ReflectUtils.getAddress(url));
        Assert.assertEquals("io.sermant.foo.BarTest", ReflectUtils.getPath(url));
        Assert.assertEquals(3, ReflectUtils.getParameters(url).size());
        Assert.assertEquals("bar", ReflectUtils.getParameters(url).get("group"));
        Assert.assertEquals("0.0.1", ReflectUtils.getParameters(url).get("version"));

        // Test the setAddress method
        url = ReflectUtils.setAddress(url, "localhost2:8081");
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost2:8081", ReflectUtils.getAddress(url));
        Assert.assertEquals("io.sermant.foo.BarTest", ReflectUtils.getPath(url));
        Assert.assertEquals(3, ReflectUtils.getParameters(url).size());
        Assert.assertEquals("bar", ReflectUtils.getParameters(url).get("group"));
        Assert.assertEquals("0.0.1", ReflectUtils.getParameters(url).get("version"));

        // Test the setPath method
        url = ReflectUtils.setPath(url, "io.sermant.foo.FooTest");
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost2:8081", ReflectUtils.getAddress(url));
        Assert.assertEquals("io.sermant.foo.FooTest", ReflectUtils.getPath(url));
        Assert.assertEquals(3, ReflectUtils.getParameters(url).size());
        Assert.assertEquals("bar", ReflectUtils.getParameters(url).get("group"));
        Assert.assertEquals("0.0.1", ReflectUtils.getParameters(url).get("version"));

        // Test the removeParameters method
        url = ReflectUtils.removeParameters(url, Collections.singletonList("group"));
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost2:8081", ReflectUtils.getAddress(url));
        Assert.assertEquals("io.sermant.foo.FooTest", ReflectUtils.getPath(url));
        Assert.assertEquals(2, ReflectUtils.getParameters(url).size());
        Assert.assertNull(ReflectUtils.getParameters(url).get("group"));
        Assert.assertEquals("0.0.1", ReflectUtils.getParameters(url).get("version"));

        // Test the addParameters method
        url = ReflectUtils.addParameters(url, Collections.singletonMap("group", "foo"));
        Assert.assertNotNull(url);
        Assert.assertEquals(DUBBO_PROTOCOL, ReflectUtils.getProtocol(url));
        Assert.assertEquals("localhost2:8081", ReflectUtils.getAddress(url));
        Assert.assertEquals("io.sermant.foo.FooTest", ReflectUtils.getPath(url));
        Assert.assertEquals(3, ReflectUtils.getParameters(url).size());
        Assert.assertEquals("foo", ReflectUtils.getParameters(url).get("group"));
        Assert.assertEquals("0.0.1", ReflectUtils.getParameters(url).get("version"));
    }

    /**
     * NotifyListener Test class
     *
     * @since 2022-02-09
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
     * NotifyListener Test class
     *
     * @since 2022-02-09
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
