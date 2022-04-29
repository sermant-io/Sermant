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
import com.huawei.dynamic.config.inject.ClassInjectDefine;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    @Before
    public void init() {
        Mockito.mockStatic(PluginConfigManager.class)
            .when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class
            )).thenReturn(new DynamicConfiguration());
    }

    @Test
    public void doAfter() throws NoSuchMethodException {
        final SpringFactoriesInterceptor interceptor = new SpringFactoriesInterceptor();
        beforeTest();
        ExecuteContext executeContext = ExecuteContext.forMemberMethod(this, this.getClass().getMethod("doAfter"),
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

        // 低版本测试
        final SpringFactoriesInterceptor lowVersionInterceptor = new SpringFactoriesInterceptor();
        final MultiValueMap<String, String> lowVersionCache = new LinkedMultiValueMap<>();
        lowVersionCache.put(BOOTSTRAP_FACTORY_NAME, new ArrayList<>());
        lowVersionCache.put(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME, new ArrayList<>());
        executeContext.changeResult(lowVersionCache);
        executeContext = lowVersionInterceptor.doAfter(executeContext);
        final MultiValueMap<String, String> lowVersionResult = (MultiValueMap<String, String>) executeContext
            .getResult();
        Assert.assertTrue(lowVersionResult.get(BOOTSTRAP_FACTORY_NAME).contains(PROPERTY_LOCATOR_CLASS)
            && lowVersionResult.get(ENABLE_AUTO_CONFIGURATION_FACTORY_NAME).contains(EVENT_PUBLISHER_CLASS));
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
