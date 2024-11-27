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
 * XdsOutlierDetectionTest
 *
 * @author zhp
 * @since 2024-11-21
 **/
public class XdsOutlierDetectionTest {
    @Test
    public void testXdsOutlierDetection() {
        XdsOutlierDetection xdsOutlierDetection = initOutlierDetection();
        Assert.assertEquals(1.0f, xdsOutlierDetection.getFailurePercentageMinimumHosts(), 0.0f);
        Assert.assertEquals(10, xdsOutlierDetection.getConsecutiveGatewayFailure(), 0.0f);
        Assert.assertEquals(10, xdsOutlierDetection.getMaxEjectionPercent(), 0.0f);
        Assert.assertEquals(20, xdsOutlierDetection.getConsecutiveLocalOriginFailure(), 0.0f);
        Assert.assertTrue(xdsOutlierDetection.isSplitExternalLocalOriginErrors());
        Assert.assertEquals(1000L, xdsOutlierDetection.getInterval());
        Assert.assertEquals(1000L, xdsOutlierDetection.getBaseEjectionTime());
        Assert.assertEquals(30, xdsOutlierDetection.getConsecutive5xxFailure());
    }

    private XdsOutlierDetection initOutlierDetection() {
        XdsOutlierDetection xdsOutlierDetection = new XdsOutlierDetection();
        xdsOutlierDetection.setFailurePercentageMinimumHosts(1.0f);
        xdsOutlierDetection.setConsecutiveGatewayFailure(10);
        xdsOutlierDetection.setMaxEjectionPercent(10f);
        xdsOutlierDetection.setConsecutiveLocalOriginFailure(20);
        xdsOutlierDetection.setSplitExternalLocalOriginErrors(true);
        xdsOutlierDetection.setInterval(1000L);
        xdsOutlierDetection.setBaseEjectionTime(1000L);
        xdsOutlierDetection.setConsecutive5xxFailure(30);
        return xdsOutlierDetection;
    }
}
