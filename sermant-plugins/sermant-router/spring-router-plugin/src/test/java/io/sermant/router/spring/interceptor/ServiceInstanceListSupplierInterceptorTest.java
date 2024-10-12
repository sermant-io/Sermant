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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.config.TransmitConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.service.LoadBalancerService;
import reactor.core.publisher.Flux;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test ServiceInstanceListSupplierInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class ServiceInstanceListSupplierInterceptorTest {
    private final ServiceInstanceListSupplierInterceptor interceptor;

    private final ExecuteContext context;

    private final TestServiceInstanceListSupplier supplier;

    private static MockedStatic<ServiceManager> mockServiceManager;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * Perform mock before the UT is executed
     */
    @BeforeClass
    public static void before() {
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(LoadBalancerService.class))
                .thenReturn(new TestLoadBalancerService());

        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
                .thenReturn(new RouterConfig());
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(TransmitConfig.class))
                .thenReturn(new TransmitConfig());
    }

    /**
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
        mockPluginConfigManager.close();
    }

    public ServiceInstanceListSupplierInterceptorTest() throws NoSuchMethodException {
        interceptor = new ServiceInstanceListSupplierInterceptor();
        supplier = new TestServiceInstanceListSupplier();
        context = ExecuteContext.forMemberMethod(supplier, String.class.getMethod("trim"), null, null, null);
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
        supplier.setServiceInstances(list);
    }

    /**
     * When the test routing rule is invalid
     */
    @Test
    public void testBeforeWhenInvalid() {
        interceptor.before(context);
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) context.getObject();
        List<ServiceInstance> instances = supplier.get().blockFirst();
        Assert.assertNotNull(instances);
        Assert.assertEquals(2, instances.size());
    }

    /**
     * Test when no data is requested for Thread Local
     */
    @Test
    public void testBeforeWithoutThreadLocal() {
        interceptor.before(context);
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) context.getObject();
        List<ServiceInstance> instances = supplier.get().blockFirst();
        Assert.assertNotNull(instances);
        Assert.assertEquals(2, instances.size());
    }

    /**
     * The test obj is invalid
     */
    @Test
    public void testBeforeWithInvalidObject() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);
        interceptor.before(context);
        Assert.assertNotNull(context.getObject());
    }

    /**
     * When the test instance list is empty
     */
    @Test
    public void testBeforeWithEmptyInstances() {
        supplier.setServiceInstances(Collections.emptyList());
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        interceptor.before(context);
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) context.getObject();
        List<ServiceInstance> instances = supplier.get().blockFirst();
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
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) context.getObject();
        List<ServiceInstance> instances = supplier.get().blockFirst();
        Assert.assertNotNull(instances);
        Assert.assertEquals(1, instances.size());
    }

    public static class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {
        private Flux<List<ServiceInstance>> serviceInstances;

        private final String serviceId;

        public TestServiceInstanceListSupplier() {
            this.serviceId = "foo";
        }

        public void setServiceInstances(List<ServiceInstance> serviceInstances) {
            this.serviceInstances = Flux.just(serviceInstances);
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            return serviceInstances;
        }
    }
}
