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

package com.huawei.dubbo.registry;

import com.huawei.dubbo.registry.constants.Constant;
import com.huawei.dubbo.registry.interceptor.RegistryConfigInterceptor;
import com.huawei.registry.config.RegisterConfig;

import com.alibaba.dubbo.config.RegistryConfig;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 测试RegistryConfigInterceptor
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
     * 测试Alibaba RegistryConfig
     *
     * @throws NoSuchMethodException 找不到方法
     * @see com.alibaba.dubbo.config.RegistryConfig
     */
    @Test
    public void testAlibabaRegistryConfig() throws NoSuchMethodException {
        // 测试setProtocol方法
        Method setProtocolMethod = RegistryConfig.class.getMethod("setProtocol", String.class);

        // 测试关闭迁移开关与注册开关
        test(setProtocolMethod, false, false, TestConstant.BAR);

        // 测试开启迁移开关，关闭注册开关
        test(setProtocolMethod, true, false, TestConstant.BAR);

        // 测试开启迁移开关与注册开关
        test(setProtocolMethod, true, true, TestConstant.BAR);

        // 测试关闭迁移开关，开启注册开关
        test(setProtocolMethod, false, true, Constant.SC_REGISTRY_PROTOCOL);

        // 测试setAddress方法
        Method setAddressMethod = RegistryConfig.class.getMethod("setAddress", String.class);

        // 测试关闭迁移开关与注册开关
        test(setAddressMethod, false, false, TestConstant.BAR);

        // 测试开启迁移开关，关闭注册开关
        test(setAddressMethod, true, false, TestConstant.BAR);

        // 测试开启迁移开关与注册开关
        test(setAddressMethod, true, true, TestConstant.BAR);

        // 测试关闭迁移开关，开启注册开关
        test(setAddressMethod, false, true, Constant.SC_REGISTRY_ADDRESS);
    }

    /**
     * 测试Apache RegistryConfig
     *
     * @throws NoSuchMethodException 找不到方法
     * @see org.apache.dubbo.config.RegistryConfig
     */
    @Test
    public void testApacheRegistryConfig() throws NoSuchMethodException {
        // 测试setProtocol方法
        Method setProtocolMethod = org.apache.dubbo.config.RegistryConfig.class.getMethod("setProtocol", String.class);

        // 测试关闭迁移开关与注册开关
        test(setProtocolMethod, false, false, TestConstant.BAR);

        // 测试开启迁移开关，关闭注册开关
        test(setProtocolMethod, true, false, TestConstant.BAR);

        // 测试开启迁移开关与注册开关
        test(setProtocolMethod, true, true, TestConstant.BAR);

        // 测试关闭迁移开关，开启注册开关
        test(setProtocolMethod, false, true, Constant.SC_REGISTRY_PROTOCOL);

        // 测试setAddress方法
        Method setAddressMethod = org.apache.dubbo.config.RegistryConfig.class.getMethod("setAddress", String.class);

        // 测试关闭迁移开关与注册开关
        test(setAddressMethod, false, false, TestConstant.BAR);

        // 测试开启迁移开关，关闭注册开关
        test(setAddressMethod, true, false, TestConstant.BAR);

        // 测试开启迁移开关与注册开关
        test(setAddressMethod, true, true, TestConstant.BAR);

        // 测试关闭迁移开关，开启注册开关
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