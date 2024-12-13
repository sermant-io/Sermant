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

package io.sermant.implement.service.xds.flowcontrol;

import io.sermant.core.service.xds.XdsFlowControlService;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.CommonDataGenerator;
import io.sermant.implement.service.xds.cache.XdsDataCache;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * XdsFlowControlServiceImplTest
 *
 * @author zhp
 * @since 2024-12-02
 **/
public class XdsFlowControlServiceImplTest extends BaseXdsTest {
    private static final String SERVICE_NAME = "serviceA";

    private static final String ROUTE_NAME = "test-route";

    private static final String CLUSTER_NAME = "outbound|8080||serviceA.default.svc.cluster.local";

    private static final String PORT = "8080";

    private static XdsFlowControlService flowControlService;

    @BeforeClass
    public static void setUp() {
        flowControlService = new XdsFlowControlServiceImpl();
        XdsDataCache.updateRouteConfigurations(CommonDataGenerator.createRouteConfigurations());
        XdsDataCache.updateServiceClusterMap(CommonDataGenerator
                .createServiceClusterMap(SERVICE_NAME, CLUSTER_NAME));
    }

    @AfterClass
    public static void tearDown() {
        XdsDataCache.updateRouteConfigurations(new ArrayList<>());
        XdsDataCache.updateServiceClusterMap(new HashMap<>());
    }

    @Test
    public void testGetRequestCircuitBreakers() {
        Optional<XdsRequestCircuitBreakers> result = flowControlService.getRequestCircuitBreakers(SERVICE_NAME, CLUSTER_NAME);
        Assert.assertTrue(result.isPresent());
        XdsRequestCircuitBreakers requestCircuitBreakers = result.get();
        Assert.assertEquals(100, requestCircuitBreakers.getMaxRequests());
    }

    @Test
    public void testGetInstanceCircuitBreakers() {
        Optional<XdsInstanceCircuitBreakers> result = flowControlService.getInstanceCircuitBreakers(SERVICE_NAME, CLUSTER_NAME);
        Assert.assertTrue(result.isPresent());
        XdsInstanceCircuitBreakers instanceCircuitBreakers = result.get();
        Assert.assertEquals(1000, instanceCircuitBreakers.getInterval());
    }

    @Test
    public void testGetHttpFault() {
        Optional<XdsHttpFault> result = flowControlService.getHttpFault(SERVICE_NAME, ROUTE_NAME);
        Assert.assertTrue(result.isPresent());
        XdsHttpFault httpFault = result.get();
        Assert.assertEquals(503, httpFault.getAbort().getHttpStatus());
    }

    @Test
    public void testGetRateLimit() {
        Optional<XdsRateLimit> result = flowControlService.getRateLimit(SERVICE_NAME, ROUTE_NAME, PORT);
        Assert.assertTrue(result.isPresent());
        XdsRateLimit rateLimit = result.get();
        Assert.assertEquals(10, rateLimit.getTokenBucket().getMaxTokens());
    }
}
