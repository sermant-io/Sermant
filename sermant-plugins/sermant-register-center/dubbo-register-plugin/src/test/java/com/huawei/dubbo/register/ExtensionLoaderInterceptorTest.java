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

import com.huawei.dubbo.register.constants.Constant;
import com.huawei.dubbo.register.interceptor.ExtensionLoaderInterceptor;
import com.huawei.dubbo.register.utils.ReflectUtils;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.registry.RegistryFactory;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * 测试ExtensionLoaderInterceptor
 *
 * @author provenceee
 * @since 2022/2/15
 */
public class ExtensionLoaderInterceptorTest {
    private final ExtensionLoaderInterceptor interceptor;

    private final Object[] arguments;

    public ExtensionLoaderInterceptorTest() {
        interceptor = new ExtensionLoaderInterceptor();
        arguments = new Object[1];
    }

    /**
     * 测试Alibaba ExtensionLoader加载非目标spi
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
     * 测试Alibaba ExtensionLoader加载目标spi
     *
     * @see com.alibaba.dubbo.common.extension.ExtensionLoader
     */
    @Test
    public void testAlibabaExtensionLoader() {
        ExtensionLoader<RegistryFactory> alibabaLoader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);
        ExecuteContext context = ExecuteContext.forMemberMethod(alibabaLoader, null, arguments, null, null);
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());

        // spi名为null
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // spi名不为sc
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // spi名为sc
        arguments[0] = Constant.SC_REGISTRY_PROTOCOL;
        interceptor.before(context);
        Assert.assertEquals(1, cachedClasses.size());
    }

    /**
     * 测试Apache ExtensionLoader加载非目标spi
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
     * 测试Apache ExtensionLoader加载目标spi
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

        // spi名为null
        interceptor.before(context);
        Assert.assertEquals(1, cachedClasses.size());

        // spi名不为sc
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(1, cachedClasses.size());

        // spi名为sc
        arguments[0] = Constant.SC_REGISTRY_PROTOCOL;
        interceptor.before(context);
        Assert.assertEquals(2, cachedClasses.size());
    }

    private void testExtensionLoader(ExecuteContext context) {
        Map<String, Class<?>> cachedClasses = ReflectUtils.getExtensionClasses(context.getObject());

        // spi名为null
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // spi名不为sc
        arguments[0] = TestConstant.BAR;
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());

        // spi名为sc，但非目标spi
        arguments[0] = Constant.SC_REGISTRY_PROTOCOL;
        interceptor.before(context);
        Assert.assertEquals(0, cachedClasses.size());
    }

    /**
     * Alibaba Spi
     */
    @SPI(TestConstant.FOO)
    public interface AlibabaSpiTest {
    }

    /**
     * Apache Spi
     */
    @org.apache.dubbo.common.extension.SPI(TestConstant.FOO)
    public interface ApacheSpiTest {
    }
}