/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.BaseTransmitConfigTest;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;

import reactor.core.publisher.Mono;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test LoadBalancerInterceptor
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class LoadBalancerInterceptorTest extends BaseTransmitConfigTest {
    private static MockedStatic<ServiceManager> mockServiceManager;

    private final LoadBalancerInterceptor interceptor;

    private final ExecuteContext context;

    private final Object[] arguments;

    private final TestLoadBalancer loadBalancer;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(LoadBalancerService.class))
                .thenReturn(new TestLoadBalancerService());
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public LoadBalancerInterceptorTest() {
        interceptor = new LoadBalancerInterceptor();
        arguments = new Object[1];
        loadBalancer = new TestLoadBalancer();
        context = ExecuteContext.forMemberMethod(loadBalancer, null, arguments, null, null);
    }

    /**
     * Reset the test data
     */
    @Before
    public void reset() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
        List<ServiceInstance> list = new ArrayList<>();
        list.add(new DefaultServiceInstance("foo1", "foo", "foo", 8080, false));
        list.add(new DefaultServiceInstance("bar2", "foo", "bar", 8081, false));
        arguments[0] = list;
        loadBalancer.setServiceId("foo");
    }

    /**
     * When the test routing rule is invalid
     */
    @Test
    public void testBeforeWhenServiceIdIsNull() {
        loadBalancer.setServiceId(null);
        interceptor.before(context);
        List<ServiceInstance> instances = (List<ServiceInstance>) context.getArguments()[0];
        Assert.assertNotNull(instances);
        Assert.assertEquals(2, instances.size());
    }

    /**
     * When the test instance list is empty
     */
    @Test
    public void testBeforeWithEmptyInstances() {
        arguments[0] = Collections.emptyList();
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.before(context);
        List<ServiceInstance> instances = (List<ServiceInstance>) context.getArguments()[0];
        Assert.assertNotNull(instances);
        Assert.assertEquals(0, instances.size());
    }

    /**
     * Test for normal conditions
     */
    @Test
    public void testBefore() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.before(context);
        List<ServiceInstance> instances = (List<ServiceInstance>) context.getArguments()[0];
        Assert.assertNotNull(instances);
        Assert.assertEquals(1, instances.size());
    }

    public static class TestLoadBalancer implements ReactorServiceInstanceLoadBalancer {
        private String serviceId;

        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            return Mono.empty();
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }
    }
}