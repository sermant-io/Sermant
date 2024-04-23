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

package io.sermant.registry.entity;

import io.sermant.registry.config.ConfigConstants;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.grace.GraceContext;

import io.sermant.core.plugin.config.PluginConfigManager;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Gracefully close the test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class GraceShutdownHookTest {
    @Test
    public void test() {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            final GraceConfig graceConfig = new GraceConfig();
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                    .thenReturn(graceConfig);
            final GraceShutdownBehavior graceShutdownBehavior = new GraceShutdownBehavior();
            graceConfig.setEnableSpring(true);
            graceConfig.setEnableGraceShutdown(true);
            graceConfig.setShutdownWaitTime(1);
            final long start = System.currentTimeMillis();
            GraceContext.INSTANCE.getGraceShutDownManager().increaseRequestCount();
            graceShutdownBehavior.run();
            Assert.assertTrue(System.currentTimeMillis() - start
                    >= graceConfig.getShutdownWaitTime() * ConfigConstants.SEC_DELTA);
            Assert.assertTrue(GraceContext.INSTANCE.getGraceShutDownManager().isShutDown());
            GraceContext.INSTANCE.getGraceShutDownManager().setShutDown(false);
            GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
        }
    }
}
