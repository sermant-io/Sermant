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

package com.huawei.registry.utils;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;

/**
 * 测试刷新
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class RefreshUtilsTest {
    private final String serviceName = "test";

    @Test
    public void refreshTargetServiceInstances() {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito.mockStatic(PluginConfigManager.class);){
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                    .thenReturn(new GraceConfig());
            testRibbon();
            testSpringLb();
        }

    }

    private void testRibbon() {
        final DynamicServerListLoadBalancer<Server> balancer = Mockito.mock(DynamicServerListLoadBalancer.class);
        GraceContext.INSTANCE.getGraceShutDownManager().getLoadBalancerCache().put(serviceName, balancer);
        RefreshUtils.refreshTargetServiceInstances(serviceName);
        Mockito.verify(balancer, Mockito.times(1)).updateListOfServers();
        GraceContext.INSTANCE.getGraceShutDownManager().getLoadBalancerCache().remove(serviceName);
    }

    private void testSpringLb() {
        final LoadBalancerCacheManager loadBalancerCacheManager = Mockito.mock(LoadBalancerCacheManager.class);
        final Cache cache = Mockito.mock(Cache.class);
        Mockito.when(loadBalancerCacheManager.getCache(GraceConstants.SPRING_CACHE_MANAGER_LOADBALANCER_CACHE_NAME))
                .thenReturn(cache);
        GraceContext.INSTANCE.getGraceShutDownManager().setLoadBalancerCacheManager(loadBalancerCacheManager);
        RefreshUtils.refreshTargetServiceInstances(serviceName);
        Mockito.verify(cache, Mockito.times(1)).evict(serviceName);
    }
}
