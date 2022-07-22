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

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * 测试配置读取功能
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

    /**
     * 测试优雅上下线配置解析
     */
    @Test
    public void testUpdateGraceConfig() {
        Mockito.mockStatic(PluginConfigManager.class).when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(new GraceConfig());
        RegistryConfigResolver configResolver = new GraceConfigResolver();
        final DynamicConfigEvent event = Mockito.mock(DynamicConfigEvent.class);
        Mockito.when(event.getContent()).thenReturn("rule:\n"
                + "  enableSpring: true # springCloud优雅上下线开关\n"
                + "  startDelayTime: 20  # 优雅上下线启动延迟时间, 单位S\n"
                + "  enableWarmUp: true # 是否开启预热\n"
                + "  warmUpTime: 1200    # 预热时间, 单位S\n"
                + "  enableGraceShutdown: false # 是否开启优雅下线\n"
                + "  shutdownWaitTime: 300  # 关闭前相关流量检测的最大等待时间, 单位S. 需开启enabledGraceShutdown才会生效\n"
                + "  enableOfflineNotify: true # 是否开启下线主动通知\n"
                + "  httpServerPort: 26688 # 开启下线主动通知时的httpServer端口\n"
                + "  upstreamAddressMaxSize: 5000 # 缓存上游地址的默认大小\n"
                + "  upstreamAddressExpiredTime: 600 # 缓存上游地址的过期时间");
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
                + "  enableSpring: true # springCloud优雅上下线开关");
        configResolver.updateConfig(event);
        final GraceConfig config = config(configResolver, GraceConfig.class);
        Assert.assertEquals(config.getShutdownWaitTime(), TEST_DEFAULT_SHUTDOWN_WAIT_TIME);
    }

    private <T> T config(RegistryConfigResolver configResolver, Class<T> clazz) {
        final Optional<Object> getOriginConfig = ReflectUtils
                .invokeMethod(configResolver, "getOriginConfig", null, null);
        Assert.assertTrue(getOriginConfig.isPresent() && getOriginConfig.get().getClass() == clazz);
        return (T) getOriginConfig.get();
    }

    /**
     * 测试注册开关配置
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
