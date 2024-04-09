/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import static org.junit.Assert.assertNotNull;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;

import java.util.Collections;

/**
 * Test interception cacheManager
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class SpringCacheManagerInterceptorTest {
    /**
     * PluginConfigManager mock object
     */
    public MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic;

    /**
     * Initialize
     */
    @Before
    public void init() {
        final GraceConfig graceConfig = new GraceConfig();
        graceConfig.setEnableSpring(true);
        pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(graceConfig);
        pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(new RegisterConfig());
    }

    @After
    public void tearDown() {
        pluginConfigManagerMockedStatic.close();
    }

    /**
     * Test to get the cache manager
     */
    @Test
    public void testCacheManager() {
        final SpringCacheManagerInterceptor springCacheManagerInterceptor = new SpringCacheManagerInterceptor();
        final LoadBalancerCacheManager cacheManager = Mockito.mock(LoadBalancerCacheManager.class);
        final ExecuteContext executeContext = ExecuteContext
                .forMemberMethod(cacheManager, null, null, Collections.emptyMap(), Collections.emptyMap());
        springCacheManagerInterceptor.doAfter(executeContext);
        assertNotNull(GraceContext.INSTANCE.getGraceShutDownManager().getLoadBalancerCacheManager());
    }
}
