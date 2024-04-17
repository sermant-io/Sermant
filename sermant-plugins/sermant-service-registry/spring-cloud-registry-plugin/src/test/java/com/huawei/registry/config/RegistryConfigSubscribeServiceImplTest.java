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

package com.huawei.registry.config;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.utils.EnvUtils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

/**
 * Configure the subscription initialization test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class RegistryConfigSubscribeServiceImplTest {
    @Test
    public void subscribeRegistryConfig() {
        final RegistryConfigSubscribeServiceImpl mock = Mockito.mock(RegistryConfigSubscribeServiceImpl.class);
        String serviceName = "test";
        mock.subscribeRegistryConfig(serviceName);
        Mockito.verify(mock, Mockito.times(1)).subscribeRegistryConfig(serviceName);
    }

    @Test
    public void testFixGrace() {
        final RegistryConfigSubscribeServiceImpl service = new RegistryConfigSubscribeServiceImpl();
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);){
            final GraceConfig graceConfig = new GraceConfig();
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                    .thenReturn(graceConfig);
            final Map<String, String> env = Collections.singletonMap(GraceConstants.ENV_GRACE_ENABLE, "true");
            EnvUtils.addEnv(env);
            ReflectUtils.invokeMethod(service, "fixGrace", null, null);
            Assert.assertTrue(graceConfig.isEnableGraceShutdown());
            Assert.assertTrue(graceConfig.isEnableOfflineNotify());
            Assert.assertTrue(graceConfig.isEnableWarmUp());
            EnvUtils.delEnv(env);
        } catch (Exception exception) {
            // ignored
        }
    }
}
