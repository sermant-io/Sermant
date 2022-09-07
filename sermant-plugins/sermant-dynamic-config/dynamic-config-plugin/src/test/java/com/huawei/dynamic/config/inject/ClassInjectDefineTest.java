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

package com.huawei.dynamic.config.inject;

import com.huawei.dynamic.config.DynamicConfiguration;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.inject.ClassInjectDefine;
import com.huaweicloud.sermant.core.plugin.inject.ClassInjectDefine.Plugin;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 注入spi测试
 *
 * @author zhouss
 * @since 2022-04-21
 */
public class ClassInjectDefineTest {
    @Test
    public void testSpi() {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            final DynamicConfiguration configuration = new DynamicConfiguration();
            configuration.setEnableDynamicConfig(true);
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
                    .thenReturn(configuration);
            final List<ClassInjectDefine> defines = new ArrayList<>();
            for (ClassInjectDefine classInjectDefine : ServiceLoader.load(ClassInjectDefine.class)) {
                if (classInjectDefine.plugin() == Plugin.DYNAMIC_CONFIG_PLUGIN) {
                    defines.add(classInjectDefine);
                }
            }
            Assert.assertTrue(contains(DynamicPropertiesInjectDefine.class, defines));
            Assert.assertTrue(contains(ProcessorClassInjectDefine.class, defines));
            Assert.assertTrue(contains(PublisherClassInjectDefine.class, defines));
        }
    }

    private boolean contains(Class<? extends ClassInjectDefine> clazz, List<ClassInjectDefine> defines) {
        return defines.stream().anyMatch(define -> define.getClass() == clazz);
    }
}
