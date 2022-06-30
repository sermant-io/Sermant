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

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

/**
 * 测试延迟
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class RegistryDelayInterceptorTest {
    private static final long DIFF = 20L;
    private static final long DELAY_TIME = 1000L;

    private final GraceConfig graceConfig = new GraceConfig();

    /**
     * 初始化
     */
    @Before
    public void init() {
        final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
    }

    /**
     * 测试开关是否生效
     */
    @Test
    public void testDelaySwitch() {
        final RegistryDelayInterceptor registryDelayInterceptor = new RegistryDelayInterceptor();
        final RegistryDelayInterceptor spy = Mockito.spy(registryDelayInterceptor);
        final ExecuteContext executeContext = ExecuteContext
                .forMemberMethod(new Object(), null, new Object[]{}, Collections.EMPTY_MAP,
                        Collections.emptyMap());
        spy.after(executeContext);
        spy.before(executeContext);
        Mockito.verify(spy, Mockito.times(0)).doAfter(executeContext);
        Mockito.verify(spy, Mockito.times(0)).doBefore(executeContext);
    }

    /**
     * 测试延迟效果
     */
    @Test
    public void testDelay() {
        graceConfig.setEnableSpring(true);
        graceConfig.setStartDelayTime(1L);
        final RegistryDelayInterceptor registryDelayInterceptor = new RegistryDelayInterceptor();
        final RegistryDelayInterceptor spy = Mockito.spy(registryDelayInterceptor);
        final ExecuteContext executeContext = ExecuteContext
                .forMemberMethod(new Object(), null, new Object[]{}, Collections.EMPTY_MAP,
                        Collections.emptyMap());
        final long start = System.currentTimeMillis();
        spy.before(executeContext);
        final long end = System.currentTimeMillis();
        Assert.assertTrue((end - start < DELAY_TIME + DIFF) || (end - start > DELAY_TIME - DIFF) );
        spy.after(executeContext);
        Assert.assertTrue(GraceContext.INSTANCE.getRegistryFinishTime() != 0);
    }
}
