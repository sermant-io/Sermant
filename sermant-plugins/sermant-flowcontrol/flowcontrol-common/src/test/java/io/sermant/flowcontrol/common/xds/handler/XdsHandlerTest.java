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

package io.sermant.flowcontrol.common.xds.handler;

import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsHttpFault;
import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsRateLimit;
import io.sermant.core.service.xds.entity.XdsRequestCircuitBreakers;
import io.sermant.core.service.xds.entity.XdsRetryPolicy;
import io.sermant.core.service.xds.entity.XdsRoute;
import io.sermant.flowcontrol.common.util.XdsAbstractTest;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Xds Flow Control handler Test
 *
 * @author zhp
 * @since 2024-11-28
 */
public class XdsHandlerTest extends XdsAbstractTest {
    @Test
    public void testGetRequestCircuitBreakers() {
        final Optional<XdsRequestCircuitBreakers> result = XdsHandler.INSTANCE.getRequestCircuitBreakers(
                SERVICE_NAME, CLUSTER_NAME);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(requestCircuitBreakers.getMaxRequests(), result.get().getMaxRequests());
    }

    @Test
    public void testGetInstanceCircuitBreakers() {
        final Optional<XdsInstanceCircuitBreakers> result = XdsHandler.INSTANCE.getInstanceCircuitBreakers(
                SERVICE_NAME, CLUSTER_NAME);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(instanceCircuitBreakers.getInterval(), result.get().getInterval());
    }

    @Test
    public void testGetRetryPolicy() {
        final Optional<XdsRetryPolicy> result = XdsHandler.INSTANCE.getRetryPolicy(
                SERVICE_NAME, ROUTE_NAME);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(retryPolicy.getRetryOn(), result.get().getRetryOn());
    }

    @Test
    public void testGetRateLimit() {
        final Optional<XdsRateLimit> result = XdsHandler.INSTANCE.getRateLimit(
                SERVICE_NAME, ROUTE_NAME, CLUSTER_NAME);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(1, result.get().getResponseHeaderOption().size());
    }

    @Test
    public void testGetHttpFault() {
        final Optional<XdsHttpFault> result = XdsHandler.INSTANCE.getHttpFault(
                SERVICE_NAME, ROUTE_NAME);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(httpFault.getDelay().getFixedDelay(), result.get().getDelay().getFixedDelay());
    }

    @Test
    public void testGetServiceRouteByServiceName() {
        final List<XdsRoute> result = XdsHandler.INSTANCE.getServiceRouteByServiceName(SERVICE_NAME);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetServiceInstanceByServiceName() {
        final Set<ServiceInstance> result = XdsHandler.INSTANCE.
                getServiceInstanceByServiceName(SERVICE_NAME);
        assertEquals(3, result.size());
    }

    @Test
    public void testGetLbPolicyOfCluster() {
        final Optional<XdsLbPolicy> result = XdsHandler.INSTANCE.getLbPolicyOfCluster(SERVICE_NAME,
                CLUSTER_NAME);
        assertEquals(Optional.of(XdsLbPolicy.RANDOM), result);
    }
}
