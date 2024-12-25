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

package io.sermant.flowcontrol.common.util;

import io.sermant.flowcontrol.common.entity.FlowControlScenario;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * XdsThreadLocalUtil Test
 *
 * @author zhp
 * @since 2024-11-30
 */
public class XdsThreadLocalUtilTest {
    @Test
    public void testSetAndRemoveSendByteFlag() {
        assertFalse(XdsThreadLocalUtil.getSendByteFlag());
        XdsThreadLocalUtil.setSendByteFlag(true);
        assertTrue(XdsThreadLocalUtil.getSendByteFlag());
        XdsThreadLocalUtil.removeSendByteFlag();
        assertFalse(XdsThreadLocalUtil.getSendByteFlag());
    }

    @Test
    public void testSetScenarioInfo() {
        final FlowControlScenario flowControlScenario = new FlowControlScenario();
        flowControlScenario.setMatchedScenarioNames(new HashSet<>(Collections.singletonList("value")));
        flowControlScenario.setServiceName("serviceName");
        flowControlScenario.setClusterName("clusterName");
        flowControlScenario.setRouteName("routeName");
        flowControlScenario.setAddress("address");
        XdsThreadLocalUtil.setScenarioInfo(flowControlScenario);
        FlowControlScenario scenarioInfo = XdsThreadLocalUtil.getScenarioInfo();
        assertEquals(scenarioInfo, flowControlScenario);
        XdsThreadLocalUtil.removeScenarioInfo();
        assertNull(XdsThreadLocalUtil.getScenarioInfo());
    }
}
