/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.auto.sc;

import com.huawei.registry.auto.sc.reactive.ServiceCombReactiveDiscoveryClientTest;
import com.huawei.registry.auto.sc.reactive.ServiceCombReactiveDiscoveryClientTest.TestRegisterService;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

import java.util.Arrays;
import java.util.List;

/**
 * ServiceCombDiscoveryClient Test
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ServiceCombDiscoveryClientTest {
    private final List<String> services = Arrays.asList("a", "b", "c");
    private final ServiceCombDiscoveryClient client = new ServiceCombDiscoveryClient();
    private final List<MicroServiceInstance> instances = Arrays
            .asList(ServiceCombReactiveDiscoveryClientTest.buildInstance(),
                    ServiceCombReactiveDiscoveryClientTest.buildInstance());

    @Before
    public void setUp() {
        ReflectUtils.setFieldValue(client, "registerCenterService", new TestRegisterService());
    }

    @Test
    public void getInstances() {
        final String serviceName = "test";
        final List<ServiceInstance> instances = client.getInstances(serviceName);
        Assert.assertNotNull(instances);
        Assert.assertEquals(instances.size(), this.instances.size());
        Assert.assertEquals(instances.get(0).getServiceId(), serviceName);
    }

    @Test
    public void getServices() {
        final List<String> services = client.getServices();
        Assert.assertNotNull(services);
        Assert.assertEquals(services.size(), this.services.size());
        for (int i = 0; i < services.size(); i++) {
            Assert.assertEquals(services.get(i), this.services.get(i));
        }
    }
}
