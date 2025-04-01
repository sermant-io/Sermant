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

package com.example.sermant.agentcore.test.application.router;

/**
 * HTTP request routing paths
 *
 * @author tangle
 * @since 2023-09-26
 */
public class RouterPath {
    /**
     * Path for service startup test
     */
    public static final String REQUEST_PATH_PING = "/ping";

    /**
     * Path for dynamic configuration test
     */
    public static final String REQUEST_PATH_DYNAMIC_CONFIG = "/testDynamicConfig";

    /**
     * Path for dynamic plugin installation test
     */
    public static final String REQUEST_PATH_INSTALL_PLUGIN = "/testInstallPlugin";

    /**
     * Path for dynamic plugin uninstallation test
     */
    public static final String REQUEST_PATH_UNINSTALL_PLUGIN = "/testUninstallPlugin";

    /**
     * Path for agent uninstallation test
     */
    public static final String REQUEST_PATH_UNINSTALL_AGENT = "/testUninstallAgent";

    /**
     * Path for agent re-installation test
     */
    public static final String REQUEST_PATH_REINSTALL_AGENT = "/testReInstallAgent";

    /**
     * Path for premain initialization test
     */
    public static final String REQUEST_PATH_PREMAIN_STARTUP = "/testPremainStartup";

    /**
     * Path for agentmain initialization test
     */
    public static final String REQUEST_PATH_AGENTMAIN_STARTUP = "/testAgentmainStartup";

    /**
     * Path for configuration loading test
     */
    public static final String REQUEST_PATH_CORE_AND_PLUGIN_CONFIG_LOAD = "/testCoreAndPluginConfigLoad";

    /**
     * Path for class matching test
     */
    public static final String REQUEST_PATH_CLASS_MATCH = "/testClassMatch";

    /**
     * Path for method matching test
     */
    public static final String REQUEST_PATH_METHOD_MATCH = "/testMethodMatch";

    /**
     * Path for bytecode enhancement test
     */
    public static final String REQUEST_PATH_ENHANCEMENT = "/testEnhancement";

    /**
     * Path for class retransformation test
     */
    public static final String REQUEST_PATH_RE_TRANSFORM = "/testReTransform";

    /**
     * Test plugin upgrade
     */
    public static final String UPDATE_PLUGIN = "/testUpdatePlugin";

    private RouterPath() {
    }
}
