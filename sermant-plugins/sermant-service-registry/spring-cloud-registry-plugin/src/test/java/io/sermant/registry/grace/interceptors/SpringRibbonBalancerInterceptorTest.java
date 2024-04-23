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

package io.sermant.registry.grace.interceptors;

import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.grace.GraceContext;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Load balancing test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class SpringRibbonBalancerInterceptorTest {
    private final String lbName = "lb";

    @Mock
    private DynamicServerListLoadBalancer balancer;

    @Test
    public void doBefore() throws NoSuchMethodException {
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                    .thenReturn(new GraceConfig());
            MockitoAnnotations.openMocks(this);
            final SpringRibbonBalancerInterceptor interceptor = new SpringRibbonBalancerInterceptor();
            interceptor.doBefore(buildContext());
            Assert.assertEquals(GraceContext.INSTANCE.getGraceShutDownManager().getLoadBalancerCache().get(lbName), balancer);
        }

    }

    private ExecuteContext buildContext() throws NoSuchMethodException {
        Mockito.when(balancer.getName()).thenReturn(lbName);
        return ExecuteContext.forMemberMethod(balancer, String.class.getDeclaredMethod("trim"), null, null, null);
    }
}
