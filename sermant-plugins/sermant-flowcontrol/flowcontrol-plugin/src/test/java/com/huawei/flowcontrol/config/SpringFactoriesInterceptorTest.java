/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.config;

import com.huawei.flowcontrol.common.config.FlowControlConfig;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.inject.ClassInjectDefine;
import com.huaweicloud.sermant.core.service.inject.ClassInjectService;
import com.huaweicloud.sermant.implement.service.inject.InjectServiceImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * injection test
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class SpringFactoriesInterceptorTest {
    private static final String RESPONSE_CLASS = "com.huawei.flowcontrol.inject.DefaultClientHttpResponse";

    private static final String RETRY_RESPONSE_CLASS = "com.huawei.flowcontrol.inject.RetryClientHttpResponse";

    private static final String BOOTSTRAP_FACTORY_NAME = "org.springframework.cloud.bootstrap.BootstrapConfiguration";

    private static final String ENABLE_AUTO_CONFIGURATION_FACTORY_NAME =
            "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private Field hasMethodLoadSpringFactoriesFiled;

    @Before
    public void setUp() throws NoSuchFieldException {
        beforeTest();
        hasMethodLoadSpringFactoriesFiled = SpringFactoriesInterceptor.class
                .getDeclaredField("hasMethodLoadSpringFactories");
        hasMethodLoadSpringFactoriesFiled.setAccessible(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(FlowControlConfig.class
        )).thenReturn(new FlowControlConfig());
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(ClassInjectService.class
        )).thenReturn(new InjectServiceImpl());
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
        serviceManagerMockedStatic.close();
    }

    @Test
    public void doAfterHighVersion() throws Exception {
        final SpringFactoriesInterceptor interceptor = new SpringFactoriesInterceptor();
        hasMethodLoadSpringFactoriesFiled.set(interceptor, Boolean.TRUE);
        ExecuteContext executeContext = ExecuteContext.forMemberMethod(this, String.class.getMethod("trim"),
                null, null, null);
        // high version test
        final Map<String, List<String>> cache = new HashMap<>();
        cache.put(BOOTSTRAP_FACTORY_NAME, new ArrayList<>());
        cache.put(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME, new ArrayList<>());
        executeContext.changeResult(cache);
        executeContext = interceptor.after(executeContext);
        final Map<String, List<String>> result = (Map<String, List<String>>) executeContext.getResult();
        Assert.assertTrue(result.get(BOOTSTRAP_FACTORY_NAME).contains(RESPONSE_CLASS)
                && result.get(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME).contains(RETRY_RESPONSE_CLASS));
    }

    @Test
    public void doAfterLowVersion() throws Exception {
        // low version test
        final SpringFactoriesInterceptor lowVersionInterceptor = new SpringFactoriesInterceptor();
        hasMethodLoadSpringFactoriesFiled.set(lowVersionInterceptor, Boolean.FALSE);
        ExecuteContext executeContext = ExecuteContext.forMemberMethod(this, String.class.getMethod("trim"),
                new Object[]{EnableAutoConfiguration.class
                }, null, null);
        final List<String> lowResult = new ArrayList<>();
        executeContext.changeResult(lowResult);
        executeContext = lowVersionInterceptor.after(executeContext);
        executeContext.changeArgs(new Object[]{BootstrapConfiguration.class});
        executeContext = lowVersionInterceptor.after(executeContext);
        final List<String> injectResult = (List<String>) executeContext.getResult();
        Assert.assertTrue(injectResult.contains(RESPONSE_CLASS) && injectResult.contains(RETRY_RESPONSE_CLASS));
    }

    private void beforeTest() {
        try {
            final Field classDefines = SpringFactoriesInterceptor.class.getDeclaredField("CLASS_DEFINES");
            classDefines.setAccessible(true);
            final List<ClassInjectDefine> defines = (List<ClassInjectDefine>) classDefines.get(null);
            defines.add(new ClassInjectDefine() {
                @Override
                public String injectClassName() {
                    return RESPONSE_CLASS;
                }

                @Override
                public String factoryName() {
                    return BOOTSTRAP_FACTORY_NAME;
                }
            });
            defines.add(new ClassInjectDefine() {
                @Override
                public String injectClassName() {
                    return RETRY_RESPONSE_CLASS;
                }

                @Override
                public String factoryName() {
                    return ENABLE_AUTO_CONFIGURATION_FACTORY_NAME;
                }
            });
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // ignored
        }
    }
}
