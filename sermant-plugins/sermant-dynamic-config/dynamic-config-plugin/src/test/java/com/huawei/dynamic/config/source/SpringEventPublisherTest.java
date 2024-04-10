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

package com.huawei.dynamic.config.source;

import com.huawei.dynamic.config.DynamicConfiguration;
import com.huawei.dynamic.config.RefreshNotifier;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

/**
 * event publisher testing
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class SpringEventPublisherTest {
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @Test
    public void test() {
        MockitoAnnotations.openMocks(this);
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class)){
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(DynamicConfiguration.class))
                    .thenReturn(new DynamicConfiguration());
            final SpringEventPublisher springEventPublisher = new SpringEventPublisher();
            springEventPublisher.setApplicationEventPublisher(applicationEventPublisher);
            final OriginConfigCenterDisableListenerTest originConfigCenterDisableListenerTest = new OriginConfigCenterDisableListenerTest();
            final RefreshNotifier refreshNotifier = originConfigCenterDisableListenerTest.checkConfigListeners();
            refreshNotifier.refresh(new DynamicConfigEvent("id", "group", "config", DynamicConfigEventType.INIT));
            Mockito.verify(applicationEventPublisher, Mockito.times(0)).publishEvent(Mockito.any());
            refreshNotifier.refresh(new DynamicConfigEvent("id", "group", "config", DynamicConfigEventType.CREATE));
            Mockito.verify(applicationEventPublisher, Mockito.times(1)).publishEvent(Mockito.any());
            originConfigCenterDisableListenerTest.getListeners().clear();
        }
    }

    @After
    public void tearDown() {
        operationManagerMockedStatic.close();
    }
}
