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

package io.sermant.xds.common.lb;

import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.xds.common.TestServiceInstance;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author daizhenyu
 * @since 2024-09-10
 **/
public class XdsRoundRobinLoadBalancerTest {
    @Test
    public void testSelectInstance() {
        XdsLoadBalancer loadBalancer = new XdsRoundRobinLoadBalancer();
        // create TestServiceInstance
        TestServiceInstance instance1 = new TestServiceInstance();
        instance1.setService("service1");
        TestServiceInstance instance2 = new TestServiceInstance();
        instance2.setService("service2");

        List<ServiceInstance> instances = new ArrayList<>();
        instances.add(instance1);
        instances.add(instance2);

        // first call
        Assert.assertEquals(instance1, loadBalancer.selectInstance(instances));

        // second call
        Assert.assertEquals(instance2, loadBalancer.selectInstance(instances));

        // third call
        Assert.assertEquals(instance1, loadBalancer.selectInstance(instances));
    }
}
