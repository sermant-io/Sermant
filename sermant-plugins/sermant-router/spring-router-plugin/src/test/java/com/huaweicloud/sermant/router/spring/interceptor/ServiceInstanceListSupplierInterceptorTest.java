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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.spring.cache.RequestData;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;
import com.huaweicloud.sermant.router.spring.service.SpringConfigService;
import com.huaweicloud.sermant.router.spring.utils.ThreadLocalUtils;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试ServiceInstanceListSupplierInterceptor
 *
 * @author provenceee
 * @since 2022-09-08
 */
public class ServiceInstanceListSupplierInterceptorTest {
    private final ServiceInstanceListSupplierInterceptor interceptor;

    private final ExecuteContext context;

    private final TestServiceInstanceListSupplier supplier;

    private static TestSpringConfigService configService;

    private static MockedStatic<ServiceManager> mockServiceManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        Map<String, Object> map = new HashMap<>();
        map.put(CommonConstant.LOG_SETTING_FILE_KEY,
            ServiceRegistryInterceptorTest.class.getResource("/logback-test.xml").getPath());
        LoggerFactory.init(map);
        configService = new TestSpringConfigService();
        mockServiceManager = Mockito.mockStatic(ServiceManager.class);
        mockServiceManager.when(() -> ServiceManager.getService(SpringConfigService.class)).thenReturn(configService);
        mockServiceManager.when(() -> ServiceManager.getService(LoadBalancerService.class))
            .thenReturn(new TestLoadBalancerService());
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockServiceManager.close();
    }

    public ServiceInstanceListSupplierInterceptorTest() throws NoSuchMethodException {
        interceptor = new ServiceInstanceListSupplierInterceptor();
        supplier = new TestServiceInstanceListSupplier();
        context = ExecuteContext.forMemberMethod(supplier, String.class.getMethod("trim"), null, null, null);
    }

    @Before
    public void reset() {
        ThreadLocalUtils.removeRequestHeader();
        ThreadLocalUtils.removeRequestData();
        configService.setInvalid(false);
        List<ServiceInstance> list = new ArrayList<>();
        list.add(new DefaultServiceInstance("foo1", "foo", "foo", 8080, false));
        list.add(new DefaultServiceInstance("bar2", "foo", "bar", 8081, false));
        supplier.setServiceInstances(list);
    }

    /**
     * 测试路由规则无效时
     */
    @Test
    public void testBeforeWhenInvalid() {
        configService.setInvalid(true);
        interceptor.before(context);
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) context.getObject();
        List<ServiceInstance> instances = supplier.get().blockFirst();
        Assert.assertNotNull(instances);
        Assert.assertEquals(2, instances.size());
    }

    /**
     * 测试ThreadLocal没有请求数据时
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
     * 测试obj无效
     */
    @Test
    public void testBeforeWithInvalidObject() {
        ThreadLocalUtils.setRequestData(new RequestData(Collections.emptyMap(), "", ""));
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);
        interceptor.before(context);
        Assert.assertNotNull(context.getObject());
    }

    /**
     * 测试实例列表为空时
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
     * 测试正常情况
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