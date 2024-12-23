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

package io.sermant.core.service.heartbeat.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test for ExternalAgentManager
 *
 * @author lilai
 * @since 2024-12-18
 */
public class ExternalAgentInfoTest {
    private ExternalAgentInfo agentInfo;

    @Before
    public void setUp() {
        agentInfo = new ExternalAgentInfo("AgentName", "1.0");
    }

    @Test
    public void testConstructor() {
        Assert.assertNotNull(agentInfo);
        Assert.assertEquals("AgentName", agentInfo.getName());
        Assert.assertEquals("1.0", agentInfo.getVersion());
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("AgentName", agentInfo.getName());
    }

    @Test
    public void testSetName() {
        agentInfo.setName("NewAgentName");
        Assert.assertEquals("NewAgentName", agentInfo.getName());
    }

    @Test
    public void testGetVersion() {
        Assert.assertEquals("1.0", agentInfo.getVersion());
    }

    @Test
    public void testSetVersion() {
        agentInfo.setVersion("2.0");
        Assert.assertEquals("2.0", agentInfo.getVersion());
    }
}
