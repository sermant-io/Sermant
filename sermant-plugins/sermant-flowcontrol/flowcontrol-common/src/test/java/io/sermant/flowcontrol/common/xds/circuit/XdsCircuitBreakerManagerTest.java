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

package io.sermant.flowcontrol.common.xds.circuit;

import io.sermant.core.service.xds.entity.XdsInstanceCircuitBreakers;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Circuit Breaker manager Test
 *
 * @author zhp
 * @since 2024-12-02
 */
public class XdsCircuitBreakerManagerTest {
    private static final String SERVICE_NAME = "provider";

    private static final String ROUTE_NAME = "routeA";

    private static final String CLUSTER_NAME = "clusterA";

    private static final String ADDRESS = "127.0.0.1:8080";

    @Test
    public void testActiveRequests() {
        assertEquals(1, XdsCircuitBreakerManager.incrementActiveRequests(SERVICE_NAME, CLUSTER_NAME, ADDRESS));
        assertEquals(2, XdsCircuitBreakerManager.incrementActiveRequests(SERVICE_NAME, CLUSTER_NAME, ADDRESS));
        XdsCircuitBreakerManager.decreaseActiveRequests(SERVICE_NAME, CLUSTER_NAME, ADDRESS);
        assertEquals(2, XdsCircuitBreakerManager.incrementActiveRequests(SERVICE_NAME, CLUSTER_NAME, ADDRESS));
    }

    @Test
    public void testCircuitBreaker() throws InterruptedException {
        final FlowControlScenario scenarioInfo = new FlowControlScenario();
        scenarioInfo.setServiceName(SERVICE_NAME);
        scenarioInfo.setClusterName(CLUSTER_NAME);
        scenarioInfo.setRouteName(ROUTE_NAME);
        scenarioInfo.setAddress(ADDRESS);
        final XdsInstanceCircuitBreakers circuitBreakers = new XdsInstanceCircuitBreakers();
        circuitBreakers.setSplitExternalLocalOriginErrors(false);
        circuitBreakers.setConsecutiveLocalOriginFailure(0);
        circuitBreakers.setConsecutiveGatewayFailure(0);
        circuitBreakers.setConsecutive5xxFailure(1);
        circuitBreakers.setInterval(1000L);
        circuitBreakers.setBaseEjectionTime(10000L);
        boolean result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertFalse(result);

        // Test the number of errors from the server reached the threshold
        XdsCircuitBreakerManager.recordFailureRequest(scenarioInfo, ADDRESS, 500, circuitBreakers);
        XdsCircuitBreakerManager.setCircuitBeakerStatus(circuitBreakers, scenarioInfo);
        result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertTrue(result);

        // Test whether the circuit breaker time has been exceeded and whether it has been restored
        Thread.sleep(1100L);
        result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertFalse(result);

        // Test the number of errors from the local source reached the threshold
        circuitBreakers.setSplitExternalLocalOriginErrors(true);
        circuitBreakers.setConsecutiveLocalOriginFailure(1);
        XdsThreadLocalUtil.setConnectionStatus(false);
        XdsCircuitBreakerManager.recordFailureRequest(scenarioInfo, ADDRESS, -1, circuitBreakers);
        XdsCircuitBreakerManager.setCircuitBeakerStatus(circuitBreakers, scenarioInfo);
        result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertTrue(result);

        // Test whether the actual circuit breaker time is the product of the number of circuit breakers multiplied
        // by the configured circuit breaker time
        Thread.sleep(1100L);
        result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertTrue(result);
        Thread.sleep(1000L);
        result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertFalse(result);

        // Test the number of errors from the gateway reached the threshold
        circuitBreakers.setConsecutive5xxFailure(0);
        circuitBreakers.setConsecutiveGatewayFailure(1);
        XdsCircuitBreakerManager.recordFailureRequest(scenarioInfo, ADDRESS, 503, circuitBreakers);
        XdsCircuitBreakerManager.setCircuitBeakerStatus(circuitBreakers, scenarioInfo);
        result = XdsCircuitBreakerManager.needsInstanceCircuitBreaker(scenarioInfo, ADDRESS);
        assertTrue(result);
    }
}
