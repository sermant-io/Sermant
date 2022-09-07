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

package com.huawei.registry.inject.grace;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.services.GraceService;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * 监听测试
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ContextClosedEventListenerTest {
    @Mock
    private GraceService graceService;

    @Test
    public void listener() {
        MockitoAnnotations.openMocks(this);
        final GraceConfig graceConfig = new GraceConfig();
        try (final MockedStatic<PluginServiceManager> pluginServiceManagerMockedStatic =
                Mockito.mockStatic(PluginServiceManager.class);
            final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                        .mockStatic(PluginConfigManager.class);) {
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                    .thenReturn(graceConfig);
            pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(GraceService.class))
                    .thenReturn(graceService);
            final ContextClosedEventListener contextClosedEventListener = new ContextClosedEventListener();
            contextClosedEventListener.listener();
            Mockito.verify(graceService, Mockito.times(0)).shutdown();
            graceConfig.setEnableSpring(true);
            graceConfig.setEnableGraceShutdown(true);
            graceConfig.setEnableOfflineNotify(true);
            contextClosedEventListener.listener();
            Mockito.verify(graceService, Mockito.times(1)).shutdown();
        }
    }
}
