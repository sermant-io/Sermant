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

package com.huaweicloud.loadbalancer;

import com.huaweicloud.loadbalancer.cache.SpringLoadbalancerCache;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;
import com.huaweicloud.loadbalancer.interceptor.ClientFactoryInterceptor;
import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

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
import java.util.Optional;

/**
 * Test the interception point of the LoadBalancerClientFactory getInstance method
 *
 * @author provenceee
 * @see org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
 * @since 2022-03-01
 */
public class ClientFactoryInterceptorTest {
    private static final String FOO = "clientFactoryFoo";

    private final ExecuteContext context;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    /**
     * construction method
     */
    public ClientFactoryInterceptorTest() throws NoSuchMethodException {
        Object[] arguments = new Object[1];
        arguments[0] = FOO;
        context = ExecuteContext.forMemberMethod(new Object(), String.class.getMethod("trim"), arguments, null, null);
    }

    /**
     * configuration converter
     */
    @Before
    public void setUp() {
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(RuleConverter.class))
                .thenReturn(new YamlRuleConverter());
        ObjectProvider<ServiceInstanceListSupplier> suppliers = ServiceInstanceListSuppliers
                .toProvider(FOO, new TestServiceInstance());
        SpringLoadbalancerCache.INSTANCE.putOrigin(FOO, new RoundRobinLoadBalancer(suppliers, FOO));
        SpringLoadbalancerCache.INSTANCE.putProvider(FOO, suppliers);
    }

    @After
    public void close() {
        serviceManagerMockedStatic.close();
    }

    /**
     * test the interceptor logic
     */
    @Test
    public void test() {
        // test: configure is null
        ClientFactoryInterceptor nullConfigInterceptor = new ClientFactoryInterceptor();
        nullConfigInterceptor.after(context);
        Assert.assertNull(context.getResult());
        cleanCache();
        // test:Scenarios in which load balancing is not configured
        try (final MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic = Mockito
                .mockStatic(PluginConfigManager.class)) {
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(LoadbalancerConfig.class))
                    .thenReturn(new LoadbalancerConfig());
            final ClientFactoryInterceptor interceptor = new ClientFactoryInterceptor();

            // test: running together will fail
            interceptor.after(context);
            Assert.assertNull(context.getResult());

            // test: The configured load balancer is consistent with the native load balancer
            RuleManagerHelper.publishRule(FOO, SpringLoadbalancerType.ROUND_ROBIN.getMapperName());
            interceptor.after(context);
            Assert.assertNotNull(context.getResult());

            // test: inconsistent with native load balancing
            RuleManagerHelper.publishRule(FOO, SpringLoadbalancerType.RANDOM.getMapperName());
            interceptor.after(context);
            Assert.assertNotNull(context.getResult());

            // test: the scenario where the cache exists
            RuleManagerHelper.publishRule(FOO + "__1", SpringLoadbalancerType.RANDOM.getMapperName());
            interceptor.after(context);
            Assert.assertNotNull(context.getResult());

            // cache check
            Assert.assertEquals(1, SpringLoadbalancerCache.INSTANCE.getNewCache().size());

            // clean up rule
            RuleManagerHelper.deleteRule(FOO, SpringLoadbalancerType.RANDOM.getMapperName());
            RuleManagerHelper.deleteRule(FOO + "__1", SpringLoadbalancerType.RANDOM.getMapperName());
        }
    }

    private void cleanCache() {
        SpringLoadbalancerCache.INSTANCE.getNewCache().clear();
        final Optional<Object> originCache = ReflectUtils
                .getFieldValue(SpringLoadbalancerCache.INSTANCE, "originCache");
        Assert.assertTrue(originCache.isPresent() && originCache.get() instanceof Map);
        ((Map<?, ?>) originCache.get()).clear();
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
