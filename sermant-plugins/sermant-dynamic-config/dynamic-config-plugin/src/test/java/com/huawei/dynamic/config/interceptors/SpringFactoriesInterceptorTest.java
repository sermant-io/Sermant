/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.interceptors;

import com.huawei.dynamic.config.DynamicConfiguration;

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注入测试
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class SpringFactoriesInterceptorTest {
    private static final String PROPERTY_LOCATOR_CLASS = "com.huawei.dynamic.config.source.SpringPropertyLocator";

    private static final String EVENT_PUBLISHER_CLASS = "com.huawei.dynamic.config.source.SpringEventPublisher";

    private static final String BOOTSTRAP_FACTORY_NAME = "org.springframework.cloud.bootstrap.BootstrapConfiguration";

    private static final String ENABLE_AUTO_CONFIGURATION_FACTORY_NAME =
            "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    private MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private Field hasMethodLoadSpringFactoriesFiled;

    @Before
    public void setUp() throws NoSuchMethodException, NoSuchFieldException {
        beforeTest();
        hasMethodLoadSpringFactoriesFiled = SpringFactoriesInterceptor.class
                .getDeclaredField("hasMethodLoadSpringFactories");
        hasMethodLoadSpringFactoriesFiled.setAccessible(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class
        )).thenReturn(new DynamicConfiguration());
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
    public void doAfterHighVersion() throws NoSuchMethodException, IllegalAccessException {
        final SpringFactoriesInterceptor interceptor = new SpringFactoriesInterceptor();
        hasMethodLoadSpringFactoriesFiled.set(interceptor, Boolean.TRUE);
        ExecuteContext executeContext = ExecuteContext.forMemberMethod(this, this.getClass().getMethod("doAfterHighVersion"),
                null, null, null);
        // 高版本测试
        final Map<String, List<String>> cache = new HashMap<>();
        cache.put(BOOTSTRAP_FACTORY_NAME, new ArrayList<>());
        cache.put(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME, new ArrayList<>());
        executeContext.changeResult(cache);
        executeContext = interceptor.doAfter(executeContext);
        final Map<String, List<String>> result = (Map<String, List<String>>) executeContext.getResult();
        Assert.assertTrue(result.get(BOOTSTRAP_FACTORY_NAME).contains(PROPERTY_LOCATOR_CLASS)
                && result.get(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME).contains(EVENT_PUBLISHER_CLASS));
    }

    @Test
    public void doAfterLowVersion() throws NoSuchMethodException, IllegalAccessException {
        // 低版本测试
        final SpringFactoriesInterceptor lowVersionInterceptor = new SpringFactoriesInterceptor();
        hasMethodLoadSpringFactoriesFiled.set(lowVersionInterceptor, Boolean.FALSE);
        ExecuteContext executeContext = ExecuteContext.forMemberMethod(this, this.getClass().getMethod("doAfterLowVersion"),
                new Object[]{org.springframework.boot.autoconfigure.EnableAutoConfiguration.class
                }, null, null);
        final List<String> lowResult = new ArrayList<>();
        executeContext.changeResult(lowResult);
        executeContext = lowVersionInterceptor.doAfter(executeContext);
        executeContext.changeArgs(new Object[]{org.springframework.cloud.bootstrap.BootstrapConfiguration.class
        });
        executeContext = lowVersionInterceptor.doAfter(executeContext);
        final List<String> injectResult = (List<String>) executeContext.getResult();
        Assert.assertTrue(injectResult.contains(PROPERTY_LOCATOR_CLASS) && injectResult.contains(EVENT_PUBLISHER_CLASS));
    }

    private void beforeTest() {
        try {
            final Field classDefines = SpringFactoriesInterceptor.class.getDeclaredField("CLASS_DEFINES");
            classDefines.setAccessible(true);
            final List<ClassInjectDefine> defines = (List<ClassInjectDefine>) classDefines.get(null);
            defines.add(new ClassInjectDefine() {
                @Override
                public String injectClassName() {
                    return PROPERTY_LOCATOR_CLASS;
                }

                @Override
                public String factoryName() {
                    return BOOTSTRAP_FACTORY_NAME;
                }
            });
            defines.add(new ClassInjectDefine() {
                @Override
                public String injectClassName() {
                    return EVENT_PUBLISHER_CLASS;
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
