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

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.implement.operation.converter.YamlConverterImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Test the configuration read function
 *
 * @author zhouss
 * @since 2022-06-29
 */
public class RegistryConfigResolverTest {
    private static final long TEST_START_DELAY_TIME = 20L;
    private static final long TEST_WARM_UP_TIME = 1200L;
    private static final long TEST_SHUTDOWN_WAIT_TIME = 300L;
    private static final long TEST_DEFAULT_SHUTDOWN_WAIT_TIME = 0L;
    private static final long TEST_HTTP_SERVER_PORT = 26688L;
    private static final long TEST_UPSTREAM_ADDRESS_MAXSIZE = 5000L;
    private static final long TEST_UPSTREAM_ADDRESS_EXPIRED_TIME = 600L;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    @Before
    public void setUp() {
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class)).thenReturn(new YamlConverterImpl());
    }

    @After
    public void tearDown() throws Exception {
        operationManagerMockedStatic.close();
    }

    /**
     * Test the analysis of elegant online and offline configurations
     */
    @Test
    public void testUpdateGraceConfig() {
        try (MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class)) {
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                    .thenReturn(new GraceConfig());
            RegistryConfigResolver configResolver = new GraceConfigResolver();
            final DynamicConfigEvent event = Mockito.mock(DynamicConfigEvent.class);
            Mockito.when(event.getContent()).thenReturn("rule:\n"
                    + "  enableSpring: true # SpringCloud Elegant On/Off Line Switch\n"
                    + "  startDelayTime: 20  # Elegant up/down line start-up delay time, in S\n"
                    + "  enableWarmUp: true # Specifies whether to enable preheating\n"
                    + "  warmUpTime: 1200    # Warm-up time, in S\n"
                    + "  enableGraceShutdown: false # Whether to enable elegant offline\n"
                    + "  shutdownWaitTime: 300  # The maximum wait time for correlation traffic detection before closing"
                    + ", in S. enabledGraceShutdown must be enabled for it to take effect\n"
                    + "  enableOfflineNotify: true # Specifies whether to enable offline notifications\n"
                    + "  httpServerPort: 26688 # Enable the HTTP server port for active notifications when you go offline\n"
                    + "  upstreamAddressMaxSize: 5000 # The default size of the cache upstream address\n"
                    + "  upstreamAddressExpiredTime: 600 # The expiration time of the upstream address of the cache");
            Mockito.when(event.getKey()).thenReturn("sermant.agent.grace");
            configResolver.updateConfig(event);
            final GraceConfig graceConfig = config(configResolver, GraceConfig.class);
            Assert.assertTrue(graceConfig.isEnableSpring());
            Assert.assertEquals(graceConfig.getStartDelayTime(), TEST_START_DELAY_TIME);
            Assert.assertTrue(graceConfig.isEnableWarmUp());
            Assert.assertEquals(graceConfig.getWarmUpTime(), TEST_WARM_UP_TIME);
            Assert.assertFalse(graceConfig.isEnableGraceShutdown());
            Assert.assertEquals(graceConfig.getShutdownWaitTime(), TEST_SHUTDOWN_WAIT_TIME);
            Assert.assertTrue(graceConfig.isEnableOfflineNotify());
            Assert.assertEquals(graceConfig.getHttpServerPort(), TEST_HTTP_SERVER_PORT);
            Assert.assertEquals(graceConfig.getUpstreamAddressMaxSize(), TEST_UPSTREAM_ADDRESS_MAXSIZE);
            Assert.assertEquals(graceConfig.getUpstreamAddressExpiredTime(), TEST_UPSTREAM_ADDRESS_EXPIRED_TIME);
            Mockito.when(event.getContent()).thenReturn("rule:\n"
                    + "  enableSpring: true # SpringCloud Elegant On/Off Line Switch");
            configResolver.updateConfig(event);
            final GraceConfig config = config(configResolver, GraceConfig.class);
            Assert.assertEquals(config.getShutdownWaitTime(), TEST_DEFAULT_SHUTDOWN_WAIT_TIME);
        }
    }

    private <T> T config(RegistryConfigResolver configResolver, Class<T> clazz) {
        final Optional<Object> getOriginConfig = ReflectUtils
                .invokeMethod(configResolver, "getOriginConfig", null, null);
        Assert.assertTrue(getOriginConfig.isPresent() && getOriginConfig.get().getClass() == clazz);
        return (T) getOriginConfig.get();
    }

    /**
     * Test the registration switch configuration
     */
    @Test
    public void testRegistrySwitchConfig() {
        RegistryConfigResolver configResolver = new OriginRegistrySwitchConfigResolver();
        final DynamicConfigEvent event = Mockito.mock(DynamicConfigEvent.class);
        Mockito.when(event.getContent()).thenReturn("origin.__registry__.needClose: true");
        Mockito.when(event.getKey()).thenReturn("sermant.agent.registry");
        configResolver.updateConfig(event);
        final RegisterDynamicConfig config = config(configResolver, RegisterDynamicConfig.class);
        Assert.assertTrue(config.isNeedCloseOriginRegisterCenter());
    }
}
