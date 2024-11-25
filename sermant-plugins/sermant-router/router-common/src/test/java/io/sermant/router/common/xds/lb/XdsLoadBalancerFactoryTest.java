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

import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsLoadBalanceService;
import io.sermant.core.service.xds.entity.XdsLbPolicy;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * XdsLoadBalancerFactoryTest
 *
 * @author daizhenyu
 * @since 2024-09-10
 **/
public class XdsLoadBalancerFactoryTest {
    private static MockedStatic<ServiceManager> serviceManager;

    private static XdsLoadBalanceService loadBalanceService;

    @BeforeClass
    public static void setUp() {
        loadBalanceService = Mockito.mock(XdsLoadBalanceService.class);

        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        serviceManager = Mockito.mockStatic(ServiceManager.class);
        Mockito.when(ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);
        Mockito.when(xdsCoreService.getLoadBalanceService()).thenReturn(loadBalanceService);
    }

    @AfterClass
    public static void tearDown() {
        serviceManager.close();
    }

    @Test
    public void testGetLoadBalancer() {
        // random
        Mockito.when(loadBalanceService.getLbPolicyOfCluster("serviceA",
                "outbound|8080||serviceA.default.svc.cluster.local"))
                .thenReturn(XdsLbPolicy.RANDOM);
        XdsLoadBalancer loadBalancer = XdsLoadBalancerFactory
                .getLoadBalancer("serviceA", "outbound|8080||serviceA.default.svc.cluster.local");
        Assert.assertEquals("io.sermant.router.common.xds.lb.XdsRandomLoadBalancer",
                loadBalancer.getClass().getCanonicalName());

        // round robin
        Mockito.when(loadBalanceService.getLbPolicyOfCluster("serviceB",
                "outbound|8080||serviceB.default.svc.cluster.local"))
                .thenReturn(XdsLbPolicy.ROUND_ROBIN);
        loadBalancer = XdsLoadBalancerFactory
                .getLoadBalancer("serviceB", "outbound|8080||serviceB.default.svc.cluster.local");
        Assert.assertEquals("io.sermant.router.common.xds.lb.XdsRoundRobinLoadBalancer",
                loadBalancer.getClass().getCanonicalName());
        Assert.assertEquals(loadBalancer, XdsLoadBalancerFactory
                .getLoadBalancer("serviceB", "outbound|8080||serviceB.default.svc.cluster.local"));
    }
}
