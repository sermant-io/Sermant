/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.loadbalancer;

import com.huawei.loadbalancer.cache.LoadbalancerCache;
import com.huawei.loadbalancer.config.LoadbalancerConfig;
import com.huawei.loadbalancer.config.SpringLoadbalancerType;
import com.huawei.loadbalancer.interceptor.ClientFactoryInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    private final LoadbalancerConfig config;

    private final ClientFactoryInterceptor interceptor;

    private final ExecuteContext context;

    /**
     * 构造方法
     */
    public ClientFactoryInterceptorTest() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        interceptor = new ClientFactoryInterceptor();
        config = new LoadbalancerConfig();
        Field field = interceptor.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(interceptor, config);
        ObjectProvider<ServiceInstanceListSupplier> suppliers = ServiceInstanceListSuppliers
            .toProvider(FOO, new TestServiceInstance());
        LoadbalancerCache.INSTANCE.putOrigin(FOO, new RoundRobinLoadBalancer(suppliers, FOO));
        LoadbalancerCache.INSTANCE.putProvider(FOO, suppliers);
        Object[] arguments = new Object[1];
        arguments[0] = FOO;
        context = ExecuteContext.forMemberMethod(new Object(), String.class.getMethod("trim"), arguments, null, null);
    }

    @Test
    public void test() {
        // 测试配置为null
        ClientFactoryInterceptor nullConfigInterceptor = new ClientFactoryInterceptor();
        nullConfigInterceptor.after(context);
        Assert.assertNull(context.getResult());

        // 测试负载均衡策略为null
        config.setSpringType(null);
        interceptor.after(context);
        Assert.assertNull(context.getResult());

        // 测试负载均衡策略不变
        config.setSpringType(SpringLoadbalancerType.ROUND_ROBIN);
        interceptor.after(context);
        Assert.assertNull(context.getResult());

        // 测试负载均衡策略改变
        config.setSpringType(SpringLoadbalancerType.RANDOM);
        interceptor.after(context);
        Assert.assertEquals(LoadbalancerCache.INSTANCE.getNewCache().get(FOO).orElse(null), context.getResult());
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