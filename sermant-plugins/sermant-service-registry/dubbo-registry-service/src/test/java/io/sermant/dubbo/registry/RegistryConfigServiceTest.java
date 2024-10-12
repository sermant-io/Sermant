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

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.RegistryConfig;

import io.sermant.dubbo.registry.constants.Constant;
import io.sermant.dubbo.registry.service.RegistryConfigService;
import io.sermant.dubbo.registry.service.RegistryConfigServiceImpl;
import io.sermant.registry.config.RegisterConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Test RegistryConfigServiceImpl
 *
 * @author provenceee
 * @since 2022-02-14
 */
public class RegistryConfigServiceTest {
    private final RegistryConfigService service;

    private final RegisterConfig registerConfig;

    /**
     * Constructor
     */
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
     * Test Alibaba AbstractInterfaceConfig
     *
     * @see com.alibaba.dubbo.config.AbstractInterfaceConfig
     */
    @Test
    public void testAlibabaAbstractInterfaceConfig() {
        AbstractInterfaceConfig interfaceConfig = new AbstractInterfaceConfig() {
        };

        // Test turning off the migration switch and the registration switch
        registerConfig.setOpenMigration(false);
        registerConfig.setEnableDubboRegister(false);
        service.addRegistryConfig(interfaceConfig);
        Assertions.assertNull(interfaceConfig.getRegistries());

        // Test enabling the migration switch and disabling the registration switch
        registerConfig.setOpenMigration(true);
        registerConfig.setEnableDubboRegister(false);
        service.addRegistryConfig(interfaceConfig);
        Assertions.assertNull(interfaceConfig.getRegistries());

        // Test disabling the migration switch and enabling the registration switch
        registerConfig.setOpenMigration(false);
        registerConfig.setEnableDubboRegister(true);
        service.addRegistryConfig(interfaceConfig);
        Assertions.assertNull(interfaceConfig.getRegistries());

        // Enable migration and registration
        registerConfig.setOpenMigration(true);
        registerConfig.setEnableDubboRegister(true);

        // The test doesn't have a registered configuration
        service.addRegistryConfig(interfaceConfig);
        Assertions.assertNull(interfaceConfig.getRegistries());

        // The test has an SC registration configuration
        RegistryConfig registryConfig = new RegistryConfig("sc://localhost:30100");
        registryConfig.setId(Constant.SC_REGISTRY_PROTOCOL);
        interfaceConfig.setRegistry(registryConfig);
        service.addRegistryConfig(interfaceConfig);
        Assertions.assertNotNull(interfaceConfig.getRegistries());
        Assertions.assertEquals(1, interfaceConfig.getRegistries().size());

        // There is a non-SC registered configuration for the test
        interfaceConfig.setRegistry(new RegistryConfig("bar://localhost:8080"));
        service.addRegistryConfig(interfaceConfig);
        Assertions.assertNotNull(interfaceConfig.getRegistries());
        Assertions.assertEquals(2, interfaceConfig.getRegistries().size());
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

        // Test turning off the migration switch
        registerConfig.setOpenMigration(false);
        registerConfig.setEnableDubboRegister(false);
        service.addRegistryConfig(config);
        Assertions.assertNull(config.getRegistries());

        // Test enabling the migration switch and disabling the registration switch
        registerConfig.setOpenMigration(true);
        registerConfig.setEnableDubboRegister(false);
        service.addRegistryConfig(config);
        Assertions.assertNull(config.getRegistries());

        // Test disabling the migration switch and enabling the registration switch
        registerConfig.setOpenMigration(false);
        registerConfig.setEnableDubboRegister(true);
        service.addRegistryConfig(config);
        Assertions.assertNull(config.getRegistries());

        // Enable migration and registration
        registerConfig.setOpenMigration(true);
        registerConfig.setEnableDubboRegister(true);

        // The test doesn't have a registered configuration
        service.addRegistryConfig(config);
        Assertions.assertNull(config.getRegistries());

        // The test has an SC registration configuration
        org.apache.dubbo.config.RegistryConfig registryConfig = new org.apache.dubbo.config.RegistryConfig();
        registryConfig.setAddress("sc://localhost:30100");
        config.setRegistry(registryConfig);
        service.addRegistryConfig(config);
        Assertions.assertNotNull(config.getRegistries());
        Assertions.assertEquals(1, config.getRegistries().size());

        // There is a non-SC registered configuration for the test
        config.setRegistry(new org.apache.dubbo.config.RegistryConfig("bar://localhost:8080"));
        service.addRegistryConfig(config);
        Assertions.assertNotNull(config.getRegistries());
        Assertions.assertEquals(2, config.getRegistries().size());
    }
}
