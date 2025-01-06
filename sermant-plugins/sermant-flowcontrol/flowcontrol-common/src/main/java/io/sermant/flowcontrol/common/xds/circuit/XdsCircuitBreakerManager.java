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
import io.sermant.core.utils.CollectionUtils;
import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Circuit Breaker manager
 *
 * @author zhp
 * @since 2024-12-02
 */
public class XdsCircuitBreakerManager {
    /**
     * The map that stores Circuit breaker information, where the Key of the first level is the service name,
     * the Key of the second level is the cluster name, the Key of the three level is the server address
     */
    private static final Map<String, Map<String, Map<String, XdsCircuitBreakerInfo>>> INSTANCE_CIRCUIT_BREAKER_MAP =
            new ConcurrentHashMap<>();

    /**
     * The map that stores the count of active requests, where the Key of the first level is the service name,
     * the Key of the second level is the cluster name, the Key of the three level is the server address
     */
    private static final Map<String, Map<String, Map<String, AtomicInteger>>> REQUEST_CIRCUIT_BREAKER_MAP =
            new ConcurrentHashMap<>();

    private static final String GATE_WAY_FAILURE = "502,503,504";

    private XdsCircuitBreakerManager() {
    }

    /**
     * increment Active Request
     *
     * @param serviceName service name
     * @param clusterName route name
     * @param address request address
     * @return active request num
     */
    public static int incrementActiveRequests(String serviceName, String clusterName, String address) {
        return getActiveRequestCount(serviceName, clusterName, address).incrementAndGet();
    }

    /**
     * decrease Active Request
     *
     * @param serviceName service name
     * @param clusterName route name
     * @param address request address
     */
    public static void decreaseActiveRequests(String serviceName, String clusterName, String address) {
        getActiveRequestCount(serviceName, clusterName, address).decrementAndGet();
    }

    /**
     * Determine whether instance circuit breaking is opened
     *
     * @param scenarioInfo Flow Control Scenario information
     * @param address service address
     * @return The result of the check, where true indicates that instance circuit breaking is required
     */
    public static boolean needsInstanceCircuitBreaker(FlowControlScenario scenarioInfo, String address) {
        XdsCircuitBreakerInfo circuitBreakerInfo = getCircuitBreakerInfo(scenarioInfo.getServiceName(),
                scenarioInfo.getRouteName(), address);
        return isCircuitBreakerOpen(circuitBreakerInfo);
    }

    /**
     * set circuit breaker status, The circuit breaker will be open when the number of failed instance calls reaches
     * the threshold.
     *
     * @param circuitBreakers circuitBreakers rule
     * @param scenarioInfo scenario information
     */
    public static void setCircuitBeakerStatus(XdsInstanceCircuitBreakers circuitBreakers,
            FlowControlScenario scenarioInfo) {
        XdsCircuitBreakerInfo circuitBreakerInfo = getCircuitBreakerInfo(scenarioInfo.getServiceName(),
                scenarioInfo.getRouteName(), scenarioInfo.getAddress());
        if (!XdsThreadLocalUtil.getSendByteFlag() && circuitBreakers.isSplitExternalLocalOriginErrors()
                && shouldCircuitBreakerByFailure(circuitBreakerInfo.getLocalFailure(),
                circuitBreakers.getConsecutiveLocalOriginFailure(), circuitBreakers.getInterval())) {
            openCircuitBreaker(circuitBreakerInfo, circuitBreakers.getInterval());
        }
        if (shouldCircuitBreakerByFailure(circuitBreakerInfo.getGateWayFailure(),
                circuitBreakers.getConsecutiveGatewayFailure(), circuitBreakers.getInterval())) {
            openCircuitBreaker(circuitBreakerInfo, circuitBreakers.getInterval());
        }
        if (shouldCircuitBreakerByFailure(circuitBreakerInfo.getServerFailure(),
                circuitBreakers.getConsecutive5xxFailure(), circuitBreakers.getInterval())) {
            openCircuitBreaker(circuitBreakerInfo, circuitBreakers.getInterval());
        }
    }

    private static void openCircuitBreaker(XdsCircuitBreakerInfo circuitBreakerInfo, long interval) {
        circuitBreakerInfo.setOpen(true);
        circuitBreakerInfo.getCircuitBreakerCount().incrementAndGet();
        circuitBreakerInfo.cleanRequestData();
        circuitBreakerInfo.setCircuitBreakerEndTime(
                System.currentTimeMillis() + circuitBreakerInfo.getCircuitBreakerCount().get() * interval);
    }

    private static boolean shouldCircuitBreakerByFailure(Deque<Long> times, int failureRequestThreshold,
            long interval) {
        if (failureRequestThreshold <= 0 || CollectionUtils.isEmpty(times) || times.size() < failureRequestThreshold) {
            return false;
        }
        for (int i = times.size(); i > failureRequestThreshold; i--) {
            times.removeFirst();
        }
        long currentTime = System.currentTimeMillis();
        Long time = times.getFirst();
        return currentTime - time <= interval;
    }

    /**
     * Check if circuit breaker is open
     *
     * @param circuitBreakerInfo circuit Breaker information
     * @return if circuit breaker is open
     */
    private static boolean isCircuitBreakerOpen(XdsCircuitBreakerInfo circuitBreakerInfo) {
        return circuitBreakerInfo.isOpen()
                && circuitBreakerInfo.getCircuitBreakerEndTime() > System.currentTimeMillis();
    }

    /**
     * record failure request
     *
     * @param scenarioInfo scenario information
     * @param address request address
     * @param code response code
     * @param circuitBreakers circuit rule
     */
    public static void recordFailureRequest(FlowControlScenario scenarioInfo, String address, int code,
            XdsInstanceCircuitBreakers circuitBreakers) {
        XdsCircuitBreakerInfo circuitBreakerInfo = getCircuitBreakerInfo(scenarioInfo.getServiceName(),
                scenarioInfo.getRouteName(), address);
        if (isCircuitBreakerOpen(circuitBreakerInfo)) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (!XdsThreadLocalUtil.getSendByteFlag()) {
            recordRequestTime(circuitBreakerInfo.getLocalFailure(), circuitBreakers.getConsecutiveLocalOriginFailure(),
                    currentTime);
        }
        if (code != 0 && GATE_WAY_FAILURE.contains(String.valueOf(code))) {
            recordRequestTime(circuitBreakerInfo.getGateWayFailure(), circuitBreakers.getConsecutiveGatewayFailure(),
                    currentTime);
        }
        recordRequestTime(circuitBreakerInfo.getServerFailure(), circuitBreakers.getConsecutive5xxFailure(),
                currentTime);
    }

    private static void recordRequestTime(Deque<Long> times, int failureRequestThreshold, long currentTime) {
        if (failureRequestThreshold <= 0) {
            return;
        }
        for (int i = times.size(); i >= failureRequestThreshold && !times.isEmpty(); i--) {
            times.removeFirst();
        }
        times.add(currentTime);
    }

    private static XdsCircuitBreakerInfo getCircuitBreakerInfo(String serviceName, String routeName,
            String address) {
        Map<String, Map<String, XdsCircuitBreakerInfo>> serviceCircuitBreakerMap = INSTANCE_CIRCUIT_BREAKER_MAP.
                computeIfAbsent(serviceName, key -> new ConcurrentHashMap<>());
        Map<String, XdsCircuitBreakerInfo> instanceCircuitBreakerMap = serviceCircuitBreakerMap.
                computeIfAbsent(routeName, key -> new ConcurrentHashMap<>());
        return instanceCircuitBreakerMap.computeIfAbsent(address, key -> new XdsCircuitBreakerInfo());
    }

    private static AtomicInteger getActiveRequestCount(String serviceName, String clusterName, String address) {
        Map<String, Map<String, AtomicInteger>> clusterCircuitBreakerMap = REQUEST_CIRCUIT_BREAKER_MAP.
                computeIfAbsent(serviceName, key -> new ConcurrentHashMap<>());
        Map<String, AtomicInteger> requestCircuitBreakerMap = clusterCircuitBreakerMap.
                computeIfAbsent(clusterName, key -> new ConcurrentHashMap<>());
        return requestCircuitBreakerMap.computeIfAbsent(address, key -> new AtomicInteger());
    }
}
