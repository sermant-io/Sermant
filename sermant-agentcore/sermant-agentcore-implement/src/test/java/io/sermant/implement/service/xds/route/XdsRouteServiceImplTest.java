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

package io.sermant.implement.service.xds.route;

import io.sermant.core.service.xds.XdsRouteService;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.implement.service.xds.BaseXdsTest;
import io.sermant.implement.service.xds.CommonDataGenerator;
import io.sermant.implement.service.xds.cache.XdsDataCache;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * XdsRouteServiceImplTest
 *
 * @author daizhenyu
 * @since 2024-08-26
 **/
public class XdsRouteServiceImplTest extends BaseXdsTest {
    private static XdsRouteService routeService;

    @BeforeClass
    public static void setUp() {
        routeService = new XdsRouteServiceImpl();
        XdsDataCache.updateRouteConfigurations(CommonDataGenerator.createRouteConfigurations());
        XdsDataCache.updateServiceClusterMap(CommonDataGenerator
                .createServiceClusterMap("serviceA", "outbound|8080||serviceA.default.svc.cluster.local"));
    }

    @AfterClass
    public static void tearDown() {
        XdsDataCache.updateRouteConfigurations(new ArrayList<>());
        XdsDataCache.updateServiceClusterMap(new HashMap<>());
    }

    @Test
    public void testGetServiceRoute() {
        // service not in cache
        List<XdsRoute> result = routeService.getServiceRoute("serviceC");
        Assert.assertEquals(0, result.size());

        // service in cache, but route is empty
        result = routeService.getServiceRoute("serviceB");
        Assert.assertEquals(0, result.size());

        // service in cache, and route is not empty
        result = routeService.getServiceRoute("serviceA");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("test-route", result.get(0).getName());
    }

    @Test
    public void testIsLocalityRoute() {
        // cluster not in cache
        Assert.assertFalse(routeService.isLocalityRoute("outbound|8080||serviceB.default.svc.cluster.local"));

        // cluster in cache
        Assert.assertTrue(routeService.isLocalityRoute("outbound|8080||serviceA.default.svc.cluster.local"));
    }
}