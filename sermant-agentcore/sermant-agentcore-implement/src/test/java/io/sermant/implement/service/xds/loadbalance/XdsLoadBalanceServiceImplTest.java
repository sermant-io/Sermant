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

package io.sermant.implement.service.xds.loadbalance;

import io.sermant.core.service.xds.XdsLoadBalanceService;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.CommonDataGenerator;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

/**
 * XdsLoadBalanceServiceImplTest
 *
 * @author daizhenyu
 * @since 2024-08-26
 **/
public class XdsLoadBalanceServiceImplTest extends BaseXdsTest {
    private static XdsLoadBalanceService loadBalanceService;

    @BeforeClass
    public static void setUp() {
        loadBalanceService = new XdsLoadBalanceServiceImpl();
        XdsDataCache.updateServiceClusterMap(CommonDataGenerator
                .createServiceClusterMap("serviceA", "outbound|8080||serviceA.default.svc.cluster.local"));
    }

    @AfterClass
    public static void tearDown() {
        XdsDataCache.updateServiceClusterMap(new HashMap<>());
    }

    @Test
    public void testGetLbPolicyOfCluster() {
        // clusterName is invalid
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED,
                loadBalanceService.getLbPolicyOfCluster("outbound|8080|serviceA.default.svc.cluster.local"));

        // clusterName is valid, but cluster not cached
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED,
                loadBalanceService.getLbPolicyOfCluster("outbound|8080||serviceB.default.svc.cluster.local"));

        // clusterName is valid, and cluster cached
        Assert.assertEquals(XdsLbPolicy.RANDOM,
                loadBalanceService.getLbPolicyOfCluster("outbound|8080||serviceA.default.svc.cluster.local"));
    }

    @Test
    public void testGetBaseLbPolicyOfService() {
        // service not cached
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED,
                loadBalanceService.getBaseLbPolicyOfService("serviceB"));

        // service cached
        Assert.assertEquals(XdsLbPolicy.RANDOM,
                loadBalanceService.getBaseLbPolicyOfService("serviceA"));
    }
}