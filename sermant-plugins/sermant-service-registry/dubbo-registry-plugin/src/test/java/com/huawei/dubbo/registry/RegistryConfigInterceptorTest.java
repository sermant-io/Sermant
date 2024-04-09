/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.dubbo.registry;

import com.huawei.dubbo.registry.constants.Constant;
import com.huawei.dubbo.registry.interceptor.RegistryConfigInterceptor;
import com.huawei.registry.config.RegisterConfig;

import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.alibaba.dubbo.config.RegistryConfig;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Test RegistryConfigInterceptor
 *
 * @author provenceee
 * @since 2022-04-14
 */
public class RegistryConfigInterceptorTest {
    private final RegistryConfigInterceptor interceptor;

    private final RegisterConfig registerConfig;

    private final Object[] arguments;

    public RegistryConfigInterceptorTest() throws IllegalAccessException, NoSuchFieldException {
        interceptor = new RegistryConfigInterceptor();
        registerConfig = new RegisterConfig();
        Field field = interceptor.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, registerConfig);
        arguments = new Object[1];
        arguments[0] = TestConstant.BAR;
    }

    /**
     * Test Alibaba RegistryConfig
     *
     * @throws NoSuchMethodException 找不到方法
     * @see com.alibaba.dubbo.config.RegistryConfig
     */
    @Test
    public void testAlibabaRegistryConfig() throws NoSuchMethodException {
        // Test the setProtocol method
        Method setProtocolMethod = RegistryConfig.class.getMethod("setProtocol", String.class);

        // Test turning off the migration switch and the registration switch
        test(setProtocolMethod, false, false, TestConstant.BAR);

        // Test enabling the migration switch and disabling the registration switch
        test(setProtocolMethod, true, false, TestConstant.BAR);

        // Test enabling the migration switch and registration switch
        test(setProtocolMethod, true, true, TestConstant.BAR);

        // Test disabling the migration switch and enabling the registration switch
        test(setProtocolMethod, false, true, Constant.SC_REGISTRY_PROTOCOL);

        // Test the setAddress method
        Method setAddressMethod = RegistryConfig.class.getMethod("setAddress", String.class);

        // Test turning off the migration switch and the registration switch
        test(setAddressMethod, false, false, TestConstant.BAR);

        // Test enabling the migration switch and disabling the registration switch
        test(setAddressMethod, true, false, TestConstant.BAR);

        // Test enabling the migration switch and registration switch
        test(setAddressMethod, true, true, TestConstant.BAR);

        // Test disabling the migration switch and enabling the registration switch
        test(setAddressMethod, false, true, Constant.SC_REGISTRY_ADDRESS);
    }

    /**
     * Test Apache RegistryConfig
     *
     * @throws NoSuchMethodException Can't find method
     * @see org.apache.dubbo.config.RegistryConfig
     */
    @Test
    public void testApacheRegistryConfig() throws NoSuchMethodException {
        // Test the setProtocol method
        Method setProtocolMethod = org.apache.dubbo.config.RegistryConfig.class.getMethod("setProtocol", String.class);

        // Test turning off the migration switch and the registration switch
        test(setProtocolMethod, false, false, TestConstant.BAR);

        // Test enabling the migration switch and disabling the registration switch
        test(setProtocolMethod, true, false, TestConstant.BAR);

        // Test enabling the migration switch and registration switch
        test(setProtocolMethod, true, true, TestConstant.BAR);

        // Test disabling the migration switch and enabling the registration switch
        test(setProtocolMethod, false, true, Constant.SC_REGISTRY_PROTOCOL);

        // Test the setAddress method
        Method setAddressMethod = org.apache.dubbo.config.RegistryConfig.class.getMethod("setAddress", String.class);

        // Test turning off the migration switch and the registration switch
        test(setAddressMethod, false, false, TestConstant.BAR);

        // Test enabling the migration switch and disabling the registration switch
        test(setAddressMethod, true, false, TestConstant.BAR);

        // Test enabling the migration switch and registration switch
        test(setAddressMethod, true, true, TestConstant.BAR);

        // Test disabling the migration switch and enabling the registration switch
        test(setAddressMethod, false, true, Constant.SC_REGISTRY_ADDRESS);
    }

    private void test(Method method, boolean openMigration, boolean enableDubboRegister, String expected) {
        arguments[0] = TestConstant.BAR;
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), method, arguments, null, null);
        registerConfig.setOpenMigration(openMigration);
        registerConfig.setEnableDubboRegister(enableDubboRegister);
        interceptor.before(context);
        Assert.assertEquals(expected, arguments[0]);
    }
}