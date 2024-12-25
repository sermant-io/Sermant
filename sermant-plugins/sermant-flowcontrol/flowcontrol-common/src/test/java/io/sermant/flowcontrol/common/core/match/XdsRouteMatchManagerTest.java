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

package io.sermant.flowcontrol.common.core.match;

import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity;

import io.sermant.flowcontrol.common.util.XdsAbstractTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * XdsRouteMatchManager Test
 * @author zhp
 * @since 2024-12-18
 */
public class XdsRouteMatchManagerTest extends XdsAbstractTest {

    @Test
    public void testGetMatchedScenarioInfo() {
        HttpRequestEntity requestEntity = new HttpRequestEntity.Builder()
                .setRequestType(RequestEntity.RequestType.CLIENT).setApiPath(PATH)
                .setHeaders(new HashMap<>()).setMethod("test").setServiceName(SERVICE_NAME).build();
        final FlowControlScenario result = XdsRouteMatchManager.INSTANCE.
                getMatchedScenarioInfo(requestEntity, SERVICE_NAME);
        Assert.assertNotNull(result);
        Assert.assertEquals(SERVICE_NAME, result.getServiceName());
        Assert.assertEquals(CLUSTER_NAME, result.getClusterName());
        Assert.assertEquals(ROUTE_NAME, result.getRouteName());
    }
}
