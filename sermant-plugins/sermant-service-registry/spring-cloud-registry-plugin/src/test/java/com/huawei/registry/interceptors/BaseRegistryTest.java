/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.interceptors;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.config.RegisterServiceCommonConfig;
import com.huawei.registry.context.RegisterContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Test the basic configuration
 *
 * @author zhouss
 * @since 2022-09-07
 */
public abstract class BaseRegistryTest<T extends Interceptor> {
    protected static final RegisterConfig REGISTER_CONFIG = new RegisterConfig();

    protected static final RegisterServiceCommonConfig COMMON_CONFIG = new RegisterServiceCommonConfig();

    protected static MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    protected static MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic;

    protected static MockedStatic<ServiceManager> serviceManagerMockedStatic;

    protected T interceptor;

    protected BaseRegistryTest() {
        interceptor = getInterceptor();
    }

    @BeforeClass
    public static void init() {
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(REGISTER_CONFIG);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(
                RegisterServiceCommonConfig.class)).thenReturn(COMMON_CONFIG);
        pluginServiceManagerMockedStatic = Mockito.mockStatic(PluginServiceManager.class);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
    }

    @AfterClass
    public static void clear() {
        pluginConfigManagerMockedStatic.close();
        pluginServiceManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
        REGISTER_CONFIG.setEnableSpringRegister(false);
        REGISTER_CONFIG.setOpenMigration(false);
        RegisterDynamicConfig.INSTANCE.setClose(false);
        RegisterContext.INSTANCE.setAvailable(false);
    }

    /**
     * @return Test the interceptor
     */
    protected abstract T getInterceptor();

    /**
     * Build a basic context
     *
     * @return context
     * @throws NoSuchMethodException Won't be thrown
     */
    protected ExecuteContext buildContext() throws NoSuchMethodException {
        return buildContext(this, null);
    }

    /**
     * Build a basic context
     *
     * @param arguments Parameter
     * @param target Object
     * @return context
     * @throws NoSuchMethodException Won't be thrown
     */
    protected ExecuteContext buildContext(Object target, Object[] arguments) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(target, String.class.getDeclaredMethod("trim"),
                arguments, null, null);
    }

    /**
     * build a basic context
     *
     * @param arguments Parameter
     * @param target Object
     * @param result The result of ExecuteContext
     * @return context
     * @throws NoSuchMethodException Won't be thrown
     */
    protected ExecuteContext buildContext(Object target, Object[] arguments, Object result)
            throws NoSuchMethodException {
        ExecuteContext context = ExecuteContext.forMemberMethod(target, String.class.getDeclaredMethod("trim"),
                arguments, null, null);
        context.changeResult(result);
        return context;
    }
}
