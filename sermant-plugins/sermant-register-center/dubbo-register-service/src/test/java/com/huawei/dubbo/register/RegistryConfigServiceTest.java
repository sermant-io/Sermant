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
import com.huawei.dubbo.register.service.RegistryConfigService;
import com.huawei.dubbo.register.service.RegistryConfigServiceImpl;
import com.huawei.register.config.RegisterConfig;

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.RegistryConfig;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 测试RegistryConfigServiceImpl
 *
 * @author provenceee
 * @since 2022/2/14
 */
public class RegistryConfigServiceTest {
    private final RegistryConfigService service;

    private final RegisterConfig registerConfig;

    public RegistryConfigServiceTest() throws IllegalAccessException, NoSuchFieldException {
        service = new RegistryConfigServiceImpl();
        registerConfig = new RegisterConfig();
        Field field = service.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(service, registerConfig);
    }

    /**
     * 测试Alibaba AbstractInterfaceConfig
     *
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     */
    @Test
    public void testAlibabaAbstractInterfaceConfig() {
        AbstractInterfaceConfig interfaceConfig = new AbstractInterfaceConfig() {
        };

        // 测试关闭迁移开关
        service.addRegistryConfig(interfaceConfig);
        Assert.assertNull(interfaceConfig.getRegistries());

        // 开启迁移开关
        registerConfig.setOpenMigration(true);

        // 测试没有注册配置
        service.addRegistryConfig(interfaceConfig);
        Assert.assertNull(interfaceConfig.getRegistries());

        // 测试存在sc注册配置
        RegistryConfig registryConfig = new RegistryConfig("sc://localhost:30100");
        registryConfig.setId(Constant.SC_REGISTRY_PROTOCOL);
        interfaceConfig.setRegistry(registryConfig);
        service.addRegistryConfig(interfaceConfig);
        Assert.assertNotNull(interfaceConfig.getRegistries());
        Assert.assertEquals(1, interfaceConfig.getRegistries().size());

        // 测试存在非sc注册配置
        interfaceConfig.setRegistry(new RegistryConfig("bar://localhost:8080"));
        service.addRegistryConfig(interfaceConfig);
        Assert.assertNotNull(interfaceConfig.getRegistries());
        Assert.assertEquals(2, interfaceConfig.getRegistries().size());
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

        // 测试关闭迁移开关
        service.addRegistryConfig(config);
        Assert.assertNull(config.getRegistries());

        // 开启迁移开关
        registerConfig.setOpenMigration(true);

        // 测试没有注册配置
        service.addRegistryConfig(config);
        Assert.assertNull(config.getRegistries());

        // 测试存在sc注册配置
        org.apache.dubbo.config.RegistryConfig registryConfig = new org.apache.dubbo.config.RegistryConfig();
        registryConfig.setAddress("sc://localhost:30100");
        config.setRegistry(registryConfig);
        service.addRegistryConfig(config);
        Assert.assertNotNull(config.getRegistries());
        Assert.assertEquals(1, config.getRegistries().size());

        // 测试存在非sc注册配置
        config.setRegistry(new org.apache.dubbo.config.RegistryConfig("bar://localhost:8080"));
        service.addRegistryConfig(config);
        Assert.assertNotNull(config.getRegistries());
        Assert.assertEquals(2, config.getRegistries().size());
    }
}