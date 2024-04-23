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

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.registry.RegistryFactory;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.dubbo.registry.alibaba.ServiceCenterRegistryFactory;
import io.sermant.dubbo.registry.constants.Constant;
import io.sermant.dubbo.registry.interceptor.ExtensionLoaderInterceptor;
import io.sermant.dubbo.registry.utils.ReflectUtils;
import io.sermant.registry.config.RegisterServiceCommonConfig;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Test ExtensionLoaderInterceptor
 *
 * @author provenceee
 * @since 2022-02-15
 */
public class ExtensionLoaderInterceptorTest {
    private final ExtensionLoaderInterceptor interceptor;

    private final Object[] arguments;

    private final RegisterServiceCommonConfig commonConfig;

    /**
     * Constructor
     */
    public ExtensionLoaderInterceptorTest() throws NoSuchFieldException, IllegalAccessException {
        interceptor = new ExtensionLoaderInterceptor();
        arguments = new Object[1];
        commonConfig = new RegisterServiceCommonConfig();
        Field commonConfigField = interceptor.getClass().getDeclaredField("commonConfig");
        commonConfigField.setAccessible(true);
        commonConfigField.set(interceptor, commonConfig);
    }

    /**
     * Test Alibaba ExtensionLoader loading non target spi
     *
     * @see com.alibaba.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testInvalidAlibabaExtensionLoader() {
        ExtensionLoader<AlibabaSpiTest> alibabaLoader = ExtensionLoader.getExtensionLoader(AlibabaSpiTest.class);
        ExecuteContext context = ExecuteContext.forMemberMethod(alibabaLoader, null, arguments, null, null);
        testExtensionLoader(context);
    }

    /**
     * Test Alibaba ExtensionLoader loading target spi
     *
     * @see com.alibaba.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testAlibabaExtensionLoader() {
        ExtensionLoader<RegistryFactory> alibabaLoader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);
        ExecuteContext context = ExecuteContext.forMemberMethod(alibabaLoader, null, arguments, null, null);
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());
        cachedClasses.remove("sc", ServiceCenterRegistryFactory.class);

        // The SPI is named Null
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // The SPI name is not SC
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // The SPI name is SC
        arguments[0] = Constant.SC_REGISTRY_PROTOCOL;
        interceptor.before(context);
        Assert.assertEquals(1, cachedClasses.size());
    }

    /**
     * Test Apache ExtensionLoader loading non target SPI
     *
     * @see org.apache.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testInvalidApacheExtensionLoader() {
        org.apache.dubbo.common.extension.ExtensionLoader<ApacheSpiTest> apacheLoader =
                org.apache.dubbo.common.extension.ExtensionLoader.getExtensionLoader(ApacheSpiTest.class);
        ExecuteContext context = ExecuteContext.forMemberMethod(apacheLoader, null, arguments, null, null);
        testExtensionLoader(context);
    }

    /**
     * Test Apache ExtensionLoader loading target spi
     *
     * @see org.apache.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testApacheExtensionLoader() {
        org.apache.dubbo.common.extension.ExtensionLoader<org.apache.dubbo.registry.RegistryFactory> apacheLoader =
                org.apache.dubbo.common.extension.ExtensionLoader
                        .getExtensionLoader(org.apache.dubbo.registry.RegistryFactory.class);
        ExecuteContext context = ExecuteContext.forMemberMethod(apacheLoader, null, arguments, null, null);
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());
        cachedClasses.remove("sc", io.sermant.dubbo.registry.apache.ServiceCenterRegistryFactory.class);

        // The SPI is named Null
        interceptor.before(context);
        Assert.assertEquals(1, cachedClasses.size());

        // The SPI name is not SC
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(1, cachedClasses.size());

        // The SPI name is SC
        arguments[0] = Constant.SC_REGISTRY_PROTOCOL;
        interceptor.before(context);
        Assert.assertEquals(2, cachedClasses.size());
    }

    private void testExtensionLoader(ExecuteContext context) {
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());

        // The SPI is named Null
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // The SPI name is not SC
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // SPI named sc, but not the target SPI
        arguments[0] = Constant.SC_REGISTRY_PROTOCOL;
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());
    }

    /**
     * Alibaba Spi
     *
     * @since 2022-02-15
     */
    @SPI(TestConstant.FOO)
    public interface AlibabaSpiTest {
    }

    /**
     * Apache Spi
     *
     * @since 2022-02-15
     */
    @org.apache.dubbo.common.extension.SPI(TestConstant.FOO)
    public interface ApacheSpiTest {
    }
}