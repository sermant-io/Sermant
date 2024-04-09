/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.agentcore.test.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

/**
 * 请求测试类，采用http请求调用方式测试
 *
 * @author tangle
 * @since 2023-09-07
 */
public class RequestTest {
    /**
     * 动态配置测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "DYNAMIC_CONFIG")
    public void testDynamicConfig() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testDynamicConfig");
    }

    /**
     * 动态卸载插件测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "UNINSTALL_PLUGIN")
    public void testUninstallPlugin() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testUninstallPlugin");
    }

    /**
     * 动态卸载Agent测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "UNINSTALL_AGENT")
    public void testUninstallAgent() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testUninstallAgent");
    }

    /**
     * 动态重装Agent测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "REINSTALL_AGENT")
    public void testReInstallPlugin() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testReInstallAgent");
    }

    /**
     * 动态安装插件测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "INSTALL_PLUGIN")
    public void testInstallPlugin() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testInstallPlugin");
    }

    /**
     * agentmain启动测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "AGENTMAIN_STARTUP")
    public void testAgentmainStartup() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testAgentmainStartup");
    }

    /**
     * premain启动测试方法
     */
    @Test
    @EnabledIfSystemProperty(named = "agentcore.test.type", matches = "PREMAIN_STARTUP")
    public void testPremainStartup() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testPremainStartup");
    }

    /**
     * 启动后backend的appType和service字段设置测试方法
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
     * 配置加载测试方法
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
