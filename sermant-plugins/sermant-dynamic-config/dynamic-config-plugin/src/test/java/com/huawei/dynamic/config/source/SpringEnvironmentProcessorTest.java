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

package com.huawei.dynamic.config.source;

import com.huawei.dynamic.config.ConfigHolder;
import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.sources.MockEnvironment;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.PropertySource;

/**
 * 配置源测试
 *
 * @author zhouss
 * @since 2022-04-16
 */
public class SpringEnvironmentProcessorTest {
    private static final String KEY = "test";
    private static final String VALUE = String.valueOf(Integer.MIN_VALUE);
    private static final String CONTENT = "test: " + VALUE;
    private DynamicConfigEvent event;

    @Before
    public void before() {
        event = Mockito.mock(DynamicConfigEvent.class);
        Mockito.when(event.getKey()).thenReturn(KEY);
        Mockito.when(event.getContent()).thenReturn(CONTENT);
        final DynamicConfiguration configuration = Mockito.mock(DynamicConfiguration.class);
        Mockito.when(configuration.getFirstRefreshDelayMs()).thenReturn(0L);
        Mockito.mockStatic(PluginConfigManager.class)
            .when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
            .thenReturn(configuration);
    }

    @Test
    public void locate() {
        final SpringEnvironmentProcessor springEnvironmentProcessor = new SpringEnvironmentProcessor();
        final MockEnvironment mockEnvironment = new MockEnvironment();
        springEnvironmentProcessor.postProcessEnvironment(mockEnvironment, null);
        final PropertySource<?> source = mockEnvironment.getPropertySources().get("Sermant-Dynamic-Config");
        Assert.assertNotNull(source);
        // 注意此处有进行configSource注入测试, 看查看spi文件，会按照指定顺序排序
        ConfigHolder.INSTANCE.resolve(event);
        Assert.assertEquals(mockEnvironment.getProperty(KEY), VALUE);
    }
}
