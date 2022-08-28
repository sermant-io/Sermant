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

package com.huaweicloud.loadbalancer;

import com.huaweicloud.loadbalancer.cache.SpringLoadbalancerCache;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;
import com.huaweicloud.loadbalancer.interceptor.ClientFactoryInterceptor;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;

import java.net.URI;
import java.util.Map;

/**
 * 测试LoadBalancerClientFactory getInstance方法的拦截点
 *
 * @author provenceee
 * @see org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
 * @since 2022-03-01
 */
public class ClientFactoryInterceptorTest {
    private static final String FOO = "foo";

    private final ExecuteContext context;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    /**
     * 构造方法
     */
    public ClientFactoryInterceptorTest() throws NoSuchMethodException {
        ObjectProvider<ServiceInstanceListSupplier> suppliers = ServiceInstanceListSuppliers
                .toProvider(FOO, new TestServiceInstance());
        SpringLoadbalancerCache.INSTANCE.putOrigin(FOO, new RoundRobinLoadBalancer(suppliers, FOO));
        SpringLoadbalancerCache.INSTANCE.putProvider(FOO, suppliers);
        Object[] arguments = new Object[1];
        arguments[0] = FOO;
        context = ExecuteContext.forMemberMethod(new Object(), String.class.getMethod("trim"), arguments, null, null);
    }

    /**
     * 配置转换器
     */
    @Before
    public void setUp() {
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(RuleConverter.class))
                .thenReturn(new YamlRuleConverter());
    }

    @After
    public void close() {
        serviceManagerMockedStatic.close();
    }

    /**
     * 测试拦截器逻辑
     */
    @Test
    public void test() {
        // 测试配置为null
        ClientFactoryInterceptor nullConfigInterceptor = new ClientFactoryInterceptor();
        nullConfigInterceptor.after(context);
        Assert.assertNull(context.getResult());

        // 测试未配置负载均衡的场景
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LoadbalancerConfig.class))
                    .thenReturn(new LoadbalancerConfig());
            final ClientFactoryInterceptor interceptor = new ClientFactoryInterceptor();
            interceptor.after(context);
            Assert.assertNull(context.getResult());

            // 测试已配置负载均衡与原生负载均衡一致
            RuleManagerHelper.publishRule(FOO, SpringLoadbalancerType.ROUND_ROBIN.getMapperName());
            interceptor.after(context);
            Assert.assertNotNull(context.getResult());

            // 测试与原生负载均衡不一致
            RuleManagerHelper.publishRule(FOO, SpringLoadbalancerType.RANDOM.getMapperName());
            interceptor.after(context);
            Assert.assertNotNull(context.getResult());

            // 测试已存在缓存的场景
            RuleManagerHelper.publishRule(FOO + "__1", SpringLoadbalancerType.RANDOM.getMapperName());
            interceptor.after(context);
            Assert.assertNotNull(context.getResult());

            // 缓存校验
            Assert.assertEquals(1, SpringLoadbalancerCache.INSTANCE.getNewCache().size());

            // 清理规则
            RuleManagerHelper.deleteRule(FOO, SpringLoadbalancerType.RANDOM.getMapperName());
            RuleManagerHelper.deleteRule(FOO + "__1", SpringLoadbalancerType.RANDOM.getMapperName());
        }
    }

    public static class TestServiceInstance implements ServiceInstance {
        @Override
        public String getServiceId() {
            return null;
        }

        @Override
        public String getHost() {
            return null;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public Map<String, String> getMetadata() {
            return null;
        }
    }
}
