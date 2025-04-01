/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.agentcore.test.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP request test class using HTTP calls for test
 *
 * @author tangle
 * @since 2023-09-07
 */
public class RequestTest {
    /**
     * Dynamic configuration test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "DYNAMIC_CONFIG")
    public void testDynamicConfig() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testDynamicConfig");
    }

    /**
     * Dynamic plugin uninstallation test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "UNINSTALL_PLUGIN")
    public void testUninstallPlugin() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testUninstallPlugin");
    }

    /**
     * Dynamic agent uninstallation test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "UNINSTALL_AGENT")
    public void testUninstallAgent() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testUninstallAgent");
    }

    /**
     * Dynamic agent re-installation test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "REINSTALL_AGENT")
    public void testReInstallPlugin() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testReInstallAgent");
    }

    /**
     * Dynamic plugin installation test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "INSTALL_PLUGIN")
    public void testInstallPlugin() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testInstallPlugin");
    }

    /**
     * Agentmain startup test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "AGENTMAIN_STARTUP")
    public void testAgentmainStartup() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testAgentmainStartup");
    }

    /**
     * Premain startup test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "PREMAIN_STARTUP")
    public void testPremainStartup() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testPremainStartup");
    }

    /**
     * Validates appType and service field in backend post-initialization
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "PREMAIN_STARTUP|AGENTMAIN_STARTUP")
    public void testBackend() throws IOException {
        Map<String, Object> resultMap = RequestUtils.analyzingRequestBackend(
                "http://127.0.0.1:8900/sermant/getPluginsInfo");
        Assertions.assertNotNull(resultMap, "getPluginsInfo result is null");
        Assertions.assertTrue(resultMap.containsKey("appType"), "the result does not contain appType");
        Assertions.assertTrue(resultMap.containsKey("service"), "the result does not contain service");
        Assertions.assertEquals("default", resultMap.getOrDefault("appType", ""), "the value of appType is wrong");
        Assertions.assertEquals("default", resultMap.getOrDefault("service", ""), "the value of service is wrong");
    }

    /**
     * Configuration loading test
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "CONFIG_LOAD")
    public void testCoreAndPluginConfigLoad() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testCoreAndPluginConfigLoad");
    }

    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "COMMON_ENHANCE")
    public void testClassMatch() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testClassMatch");
    }

    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "COMMON_ENHANCE")
    public void testMethodMatch() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testMethodMatch");
    }

    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "COMMON_ENHANCE")
    public void testEnhancement() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testEnhancement");
    }

    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "COMMON_ENHANCE")
    public void testReTransform() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testReTransform");
    }
}
