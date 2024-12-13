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

package io.sermant.core.service.xds.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * XdsInstanceCircuitBreakerTest
 *
 * @author zhp
 * @since 2024-11-21
 **/
public class XdsInstanceCircuitBreakerTest {
    @Test
    public void testXdsOutlierDetection() {
        XdsInstanceCircuitBreakers xdsInstanceCircuitBreakers = initInstanceCircuitBreakers();
        Assert.assertEquals(1.0f, xdsInstanceCircuitBreakers.getFailurePercentageMinimumHosts(), 0.0f);
        Assert.assertEquals(10, xdsInstanceCircuitBreakers.getConsecutiveGatewayFailure(), 0.0f);
        Assert.assertEquals(10, xdsInstanceCircuitBreakers.getMaxEjectionPercent(), 0.0f);
        Assert.assertEquals(20, xdsInstanceCircuitBreakers.getConsecutiveLocalOriginFailure(), 0.0f);
        Assert.assertTrue(xdsInstanceCircuitBreakers.isSplitExternalLocalOriginErrors());
        Assert.assertEquals(1000L, xdsInstanceCircuitBreakers.getInterval());
        Assert.assertEquals(1000L, xdsInstanceCircuitBreakers.getBaseEjectionTime());
        Assert.assertEquals(30, xdsInstanceCircuitBreakers.getConsecutive5xxFailure());
    }

    private XdsInstanceCircuitBreakers initInstanceCircuitBreakers() {
        XdsInstanceCircuitBreakers xdsInstanceCircuitBreakers = new XdsInstanceCircuitBreakers();
        xdsInstanceCircuitBreakers.setFailurePercentageMinimumHosts(1);
        xdsInstanceCircuitBreakers.setConsecutiveGatewayFailure(10);
        xdsInstanceCircuitBreakers.setMaxEjectionPercent(10);
        xdsInstanceCircuitBreakers.setConsecutiveLocalOriginFailure(20);
        xdsInstanceCircuitBreakers.setSplitExternalLocalOriginErrors(true);
        xdsInstanceCircuitBreakers.setInterval(1000L);
        xdsInstanceCircuitBreakers.setBaseEjectionTime(1000L);
        xdsInstanceCircuitBreakers.setConsecutive5xxFailure(30);
        return xdsInstanceCircuitBreakers;
    }
}
