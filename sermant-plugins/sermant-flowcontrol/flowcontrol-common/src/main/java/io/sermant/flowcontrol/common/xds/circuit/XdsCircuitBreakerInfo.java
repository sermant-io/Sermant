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

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Circuit Breaker information
 *
 * @author zhp
 * @since 2024-12-02
 */
public class XdsCircuitBreakerInfo {
    /**
     * Number of local failures
     */
    private Deque<Long> localFailure;

    /**
     * Number of gateway errors, Response status code 502,503,504 is gateway error
     */
    private Deque<Long> gateWayFailure;

    /**
     * The number of server errors
     */
    private Deque<Long> serverFailure;

    /**
     * Identification of whether the circuit breaker is open or not
     */
    private boolean isOpen;

    /**
     * End time of circuit breaker
     */
    private long circuitBreakerEndTime;

    /**
     * Number of circuit breakers
     */
    private AtomicInteger circuitBreakerCount;

    /**
     * Constructor
     */
    public XdsCircuitBreakerInfo() {
        this.localFailure = new LinkedList<>();
        this.gateWayFailure = new LinkedList<>();
        this.serverFailure = new LinkedList<>();
        this.circuitBreakerCount = new AtomicInteger(0);
    }

    public Deque<Long> getLocalFailure() {
        return localFailure;
    }

    public void setLocalFailure(Deque<Long> localFailure) {
        this.localFailure = localFailure;
    }

    public Deque<Long> getGateWayFailure() {
        return gateWayFailure;
    }

    public void setGateWayFailure(Deque<Long> gateWayFailure) {
        this.gateWayFailure = gateWayFailure;
    }

    public Deque<Long> getServerFailure() {
        return serverFailure;
    }

    public void setServerFailure(Deque<Long> serverFailure) {
        this.serverFailure = serverFailure;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public long getCircuitBreakerEndTime() {
        return circuitBreakerEndTime;
    }

    public void setCircuitBreakerEndTime(long circuitBreakerEndTime) {
        this.circuitBreakerEndTime = circuitBreakerEndTime;
    }

    public AtomicInteger getCircuitBreakerCount() {
        return circuitBreakerCount;
    }

    public void setCircuitBreakerCount(AtomicInteger circuitBreakerCount) {
        this.circuitBreakerCount = circuitBreakerCount;
    }

    /**
     * reset the data
     */
    public void cleanRequestData() {
        this.localFailure.clear();
        this.gateWayFailure.clear();
        this.serverFailure.clear();
    }
}
