/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.router.common.xds.lb;

import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.router.common.xds.TestServiceInstance;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * XdsRandomLoadBalancerTest
 *
 * @author daizhenyu
 * @since 2024-09-10
 **/
public class XdsRandomLoadBalancerTest {

    @Test
    public void testSelectInstance() {
        XdsLoadBalancer loadBalancer = new XdsRandomLoadBalancer();

        TestServiceInstance instance1 = new TestServiceInstance();
        instance1.setService("service1");
        TestServiceInstance instance2 = new TestServiceInstance();
        instance2.setService("service2");
        List<ServiceInstance> instances = new ArrayList<>();
        instances.add(instance1);
        instances.add(instance2);

        ServiceInstance selectedInstance = loadBalancer.selectInstance(instances);
        Assert.assertTrue(instances.contains(selectedInstance));
    }
}
