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

package com.huawei.dynamic.config.subscribe;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.sources.TestConfigSources;
import com.huawei.dynamic.config.sources.TestLowestConfigSources;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.subscribe.processor.OrderConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 配置监听器测试
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class ConfigListenerTest {
    @Test
    public void test() {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);) {
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
                    .thenReturn(new DynamicConfiguration());
            final ConfigListener configListener = new ConfigListener();
            ConfigHolder.INSTANCE.getConfigSources()
                    .removeIf(configSource -> configSource.getClass() == TestConfigSources.class
                            || configSource.getClass() == TestLowestConfigSources.class);
            configListener.process(new OrderConfigEvent("id", "group", "test: 1", DynamicConfigEventType.CREATE,
                    Collections.singletonMap("test", 1)));
            Assert.assertEquals(ConfigHolder.INSTANCE.getConfig("test"), 1);
            ConfigHolder.INSTANCE.getConfigSources().add(new TestConfigSources());
            ConfigHolder.INSTANCE.getConfigSources().add(new TestLowestConfigSources());
        }
    }
}
