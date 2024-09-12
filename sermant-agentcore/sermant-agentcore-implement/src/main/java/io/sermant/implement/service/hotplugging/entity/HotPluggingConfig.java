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

package io.sermant.implement.service.hotplugging.entity;

/**
 * Hot Plugging configuration class
 *
 * @author zhp
 * @since 2024-08-01
 */
public class HotPluggingConfig {
    /**
     * Command types for hot plugging functionality; currently supports uninstalling and updating plugins
     */
    private String commandType;

    /**
     * Plugin names, separated by ','
     */
    private String pluginNames;

    /**
     * Instance ID，generate by UUID
     */
    private String instanceIds;

    /**
     * path of sermant
     */
    private String agentPath;

    /**
     * Parameter Information for hot plugging
     */
    private String params;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getPluginNames() {
        return pluginNames;
    }

    public void setPluginNames(String pluginNames) {
        this.pluginNames = pluginNames;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    public String getInstanceIds() {
        return instanceIds;
    }

    public void setInstanceIds(String instanceIds) {
        this.instanceIds = instanceIds;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
