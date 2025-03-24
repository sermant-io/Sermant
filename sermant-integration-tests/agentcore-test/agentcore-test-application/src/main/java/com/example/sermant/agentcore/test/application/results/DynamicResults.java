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

package com.example.sermant.agentcore.test.application.results;

/**
 * Test results for dynamic plugin management
 *
 * @author tangle
 * @since 2023-09-08
 */
public enum DynamicResults {
    /**
     * Non-interference during repeated enhancements with plugin dynamic installation
     */
    DYNAMIC_INSTALL_PLUGIN_REPEAT_ENHANCE("Test dynamic install plugin repetitive enhancement."),

    /**
     * Interceptor invalid after plugin dynamic uninstallation
     */
    DYNAMIC_UNINSTALL_PLUGIN_INTERCEPTOR_FAILURE("Test dynamic uninstall plugin, plugin failure."),

    /**
     * Service close after plugin dynamic uninstallation
     */
    DYNAMIC_UNINSTALL_SERVICE_CLOSE("Test dynamic uninstall plugin service close."),

    /**
     * Existing interceptors unaffected during plugin uninstallation
     */
    DYNAMIC_UNINSTALL_REPEAT_ENHANCE("Test dynamic uninstall plugin not effect other interceptor."),

    /**
     * Plugin invalid after agent uninstallation
     */
    DYNAMIC_UNINSTALL_AGENT_PLUGIN_FAILURE("Test dynamic uninstall, plugin failure."),

    /**
     * Dynamic plugin are activated after agent re-installation
     */
    DYNAMIC_REINSTALL_AGENT_PLUGIN_SUCCESS("Test dynamic reinstall agent, plugin success."),

    /**
     * Static plugins are activated via premain startup
     */
    PREMAIN_STATIC_PLUGIN_INTERCEPTOR_SUCCESS("Test premain startup, static plugin success."),

    /**
     * Dynamic plugin are invalid via premain startup
     */
    PREMAIN_DYNAMIC_PLUGIN_INTERCEPTOR_FAILURE("Test premain startup, dynamic plugin failure."),

    /**
     * Static plugin are invalid via agentmain startup
     */
    AGENTMAIN_STATIC_PLUGIN_INTERCEPTOR_FAILURE("Test agentmain startup, static plugin failure."),

    /**
     * Active plugins are activated via agentmain startup
     */
    AGENTMAIN_ACTIVE_PLUGIN_INTERCEPTOR_SUCCESS("Test agentmain startup, active plugin success."),

    /**
     * Passive plugins are invalid via agentmain startup
     */
    AGENTMAIN_PASSIVE_PLUGIN_INTERCEPTOR_FAILURE("Test agentmain startup, passive plugin failure."),

    /**
     * Dynamically update plugins
     */
    DYNAMIC_UPDATE_PLUGIN("Test dynamic update plugin.");

    /**
     * Test case description
     */
    private String description;

    /**
     * Test result flag
     */
    private boolean result;

    /**
     * Constructor
     *
     * @param description Test case description
     */
    DynamicResults(String description) {
        this.description = description;
        this.result = false;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }
}
