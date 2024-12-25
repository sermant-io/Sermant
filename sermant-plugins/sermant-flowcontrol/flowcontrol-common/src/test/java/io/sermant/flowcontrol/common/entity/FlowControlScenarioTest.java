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

package io.sermant.flowcontrol.common.entity;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * FlowControlScenario Test
 *
 * @since 2024-12-10
 * @author zhp
 */
public class FlowControlScenarioTest {
    private FlowControlScenario flowControlScenario;

    @Before
    public void setUp() throws Exception {
        flowControlScenario = new FlowControlScenario();
    }

    @Test
    public void testBusinessNamesGetterAndSetter() {
        final Set<String> businessNames = new HashSet<>(Collections.singletonList("value"));
        flowControlScenario.setMatchedScenarioNames(businessNames);
        assertEquals(businessNames, flowControlScenario.getMatchedScenarioNames());
    }

    @Test
    public void testServiceNameGetterAndSetter() {
        final String serviceName = "serviceName";
        flowControlScenario.setServiceName(serviceName);
        assertEquals(serviceName, flowControlScenario.getServiceName());
    }

    @Test
    public void testClusterNameGetterAndSetter() {
        final String clusterName = "clusterName";
        flowControlScenario.setClusterName(clusterName);
        assertEquals(clusterName, flowControlScenario.getClusterName());
    }

    @Test
    public void testRouteNameGetterAndSetter() {
        final String routeName = "routeName";
        flowControlScenario.setRouteName(routeName);
        assertEquals(routeName, flowControlScenario.getRouteName());
    }

    @Test
    public void testAddressGetterAndSetter() {
        final String address = "127.0.0.1:8080";
        flowControlScenario.setAddress(address);
        assertEquals(address, flowControlScenario.getAddress());
    }
}
