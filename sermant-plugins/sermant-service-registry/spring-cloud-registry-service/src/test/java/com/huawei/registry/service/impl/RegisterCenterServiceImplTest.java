/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.service.impl;

import static org.junit.Assert.*;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.config.RegisterType;
import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.service.register.Register;
import com.huawei.registry.service.register.RegisterManager;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Register for the test
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class RegisterCenterServiceImplTest {
    private final RegisterCenterServiceImpl registerCenterService = new RegisterCenterServiceImpl();

    private final RegisterConfig registerConfig = new RegisterConfig();

    private final RegisterServiceCommonConfig registerServiceCommonConfig = new RegisterServiceCommonConfig();

    private Map<RegisterType, Register> backUp;

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    @Before
    public void setUp() {
        pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);

        pluginConfigManagerMockedStatic.when(()
                -> PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class))
                .thenReturn(registerServiceCommonConfig);

        // To prevent the internal logic of the registration, clean up the registrar and test the recovery
        final RegisterManager instance = RegisterManager.INSTANCE;
        final Optional<Object> registerMap = ReflectUtils.getFieldValue(instance, "registerMap");
        Assert.assertTrue(registerMap.isPresent());
        backUp = new HashMap<>((Map<RegisterType, Register>) registerMap.get());
        ((Map<?, ?>) registerMap.get()).clear();
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        ReflectUtils.setFieldValue(RegisterManager.INSTANCE, "registerMap", backUp);
    }

    @Test
    public void register() {
        final FixedResult fixedResult = new FixedResult();
        registerCenterService.register(fixedResult);
        Assert.assertTrue(fixedResult.isSkip());
        registerConfig.setOpenMigration(true);
        final FixedResult openResult = new FixedResult();
        registerCenterService.register(openResult);
        Assert.assertFalse(openResult.isSkip());
    }

    @Test
    public void unRegister() {
        registerCenterService.unRegister();
        final Optional<Object> isStopped = ReflectUtils.getFieldValue(registerCenterService, "isStopped");
        Assert.assertTrue(isStopped.isPresent());
        Assert.assertTrue(isStopped.get() instanceof AtomicBoolean);
        Assert.assertTrue(((AtomicBoolean) isStopped.get()).get());
    }

    @Test
    public void getServerList() {
        Assert.assertTrue(registerCenterService.getServerList("test").isEmpty());
    }

    @Test
    public void getServices() {
        Assert.assertTrue(registerCenterService.getServices().isEmpty());
    }
}
