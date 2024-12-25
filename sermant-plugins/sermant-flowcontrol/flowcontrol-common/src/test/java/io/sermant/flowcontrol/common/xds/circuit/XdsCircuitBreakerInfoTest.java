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

import org.junit.Before;
import org.junit.Test;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Circuit Breaker information Test
 *
 * @author zhp
 * @since 2024-12-02
 */
public class XdsCircuitBreakerInfoTest {
    private XdsCircuitBreakerInfo xdsCircuitBreakerInfoUnderTest;

    @Before
    public void setUp() throws Exception {
        xdsCircuitBreakerInfoUnderTest = new XdsCircuitBreakerInfo();
    }

    @Test
    public void testLocalFailureGetterAndSetter() {
        final Deque<Long> localFailure = new ConcurrentLinkedDeque<>();
        localFailure.add(1L);
        xdsCircuitBreakerInfoUnderTest.setLocalFailure(localFailure);
        assertEquals(localFailure, xdsCircuitBreakerInfoUnderTest.getLocalFailure());
    }

    @Test
    public void testServerFailureGetterAndSetter() {
        final Deque<Long> serverFailure = new ConcurrentLinkedDeque<>();
        serverFailure.add(1L);
        xdsCircuitBreakerInfoUnderTest.setServerFailure(serverFailure);
        assertEquals(serverFailure, xdsCircuitBreakerInfoUnderTest.getServerFailure());
    }

    @Test
    public void testGateWayFailureGetterAndSetter() {
        final Deque<Long> gateWayFailure = new ConcurrentLinkedDeque<>();
        gateWayFailure.add(1L);
        xdsCircuitBreakerInfoUnderTest.setGateWayFailure(gateWayFailure);
        assertEquals(gateWayFailure, xdsCircuitBreakerInfoUnderTest.getGateWayFailure());
    }

    @Test
    public void testIsOpenGetterAndSetter() {
        final boolean isOpen = false;
        xdsCircuitBreakerInfoUnderTest.setOpen(isOpen);
        assertFalse(xdsCircuitBreakerInfoUnderTest.isOpen());
    }

    @Test
    public void testCircuitBreakerTimeGetterAndSetter() {
        final long circuitBreakerTime = System.currentTimeMillis();
        xdsCircuitBreakerInfoUnderTest.setCircuitBreakerEndTime(circuitBreakerTime);
        assertEquals(circuitBreakerTime, xdsCircuitBreakerInfoUnderTest.getCircuitBreakerEndTime());
    }

    @Test
    public void testCircuitBreakerCountGetterAndSetter() {
        final AtomicInteger circuitBreakerCount = new AtomicInteger(1);
        xdsCircuitBreakerInfoUnderTest.setCircuitBreakerCount(circuitBreakerCount);
        assertEquals(circuitBreakerCount, xdsCircuitBreakerInfoUnderTest.getCircuitBreakerCount());
    }

    @Test
    public void testCleanRequestDate() {
        testCircuitBreakerTimeGetterAndSetter();
        xdsCircuitBreakerInfoUnderTest.cleanRequestData();
        assertEquals(0, xdsCircuitBreakerInfoUnderTest.getServerFailure().size());
    }
}
