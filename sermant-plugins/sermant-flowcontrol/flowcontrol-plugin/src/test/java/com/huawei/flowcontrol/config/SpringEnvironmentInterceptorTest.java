/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.config;

import com.huawei.flowcontrol.common.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.common.adapte.cse.entity.CseServiceMeta;
import com.huawei.flowcontrol.common.adapte.cse.resolver.RateLimitingRuleResolver;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.NettyGatewayClient;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 环境变量拦截测试
 *
 * @author zhouss
 * @since 2022-04-26
 */
public class SpringEnvironmentInterceptorTest {
    /**
     * 测试规则加载
     */
    @Test
    public void testLoadRule() {
        final SpringEnvironmentInterceptor interceptor = new SpringEnvironmentInterceptor();
        Mockito.mockStatic(ServiceManager.class)
            .when(() -> ServiceManager.getService(GatewayClient.class))
            .thenReturn(new NettyGatewayClient());
        final MockEnvironment mockEnvironment = new MockEnvironment();
        final HashMap<String, Object> ruleMap = new HashMap<>();
        ruleMap.put(RateLimitingRuleResolver.CONFIG_KEY + ".test", "rate: 10");
        mockEnvironment.getPropertySources().addFirst(new MapPropertySource("test", ruleMap));
        final ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
        Mockito.when(applicationContext.getEnvironment()).thenReturn(mockEnvironment);
        final ExecuteContext executeContext = ExecuteContext
            .forMemberMethod(new TestTarget(), null, new Object[]{applicationContext}, null, null);
        CseServiceMeta.getInstance().setDubboService(true);
        interceptor.after(executeContext);
        Assert.assertFalse(
            ResolverManager.INSTANCE.getResolver(RateLimitingRuleResolver.CONFIG_KEY).getRules().isEmpty());
    }

    static class MockEnvironment extends AbstractEnvironment {

    }

    static class TestTarget {
        private final Set<Class<?>> primarySources = new HashSet<>();
    }
}
