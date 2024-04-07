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

package com.huaweicloud.sermant.premain.common;

/**
 * Bootstrap constant
 *
 * @author luanwenfei
 * @since 2023-07-20
 */
public class BootConstant {
    /**
     * Bootstrap file name
     */
    public static final String BOOTSTRAP_CONFIG_FILE_NAME = "bootstrap.properties";

    /**
     * Core configuration file name
     */
    public static final String CORE_CONFIG_FILE_NAME = "config.properties";

    /**
     * Plugin profile name
     */
    public static final String PLUGIN_SETTING_FILE_NAME = "plugins.yaml";

    /**
     * Logback configuration file name
     */
    public static final String LOG_SETTING_FILE_NAME = "logback.xml";

    /**
     * The key of artifact in agent arguments
     */
    public static final String ARTIFACT_NAME_KEY = "artifact";

    /**
     * The key of app name in agent arguments
     */
    public static final String APP_NAME_KEY = "appName";

    /**
     * The key of app type in agent arguments
     */
    public static final String APP_TYPE_KEY = "appType";

    /**
     * The key of service name key in agent arguments
     */
    public static final String SERVICE_NAME_KEY = "serviceName";

    /**
     * The key of agentPath in agent arguments
     */
    public static final String AGENT_ROOT_DIR_KEY = "agentPath";

    /**
     * The key of Sermant core service implementation package
     */
    public static final String CORE_IMPLEMENT_DIR_KEY = "core.implement.dir";

    /**
     * The key of Sermant common third party dependency directory
     */
    public static final String COMMON_DEPENDENCY_DIR_KEY = "common.dir";

    /**
     * The key of core configuration file name
     */
    public static final String CORE_CONFIG_FILE_KEY = "core.config.file";

    /**
     * The key of the plugin's Settings configuration name
     */
    public static final String PLUGIN_SETTING_FILE_KEY = "plugin.setting.file";

    /**
     * The key of pluginPackage directory
     */
    public static final String PLUGIN_PACKAGE_DIR_KEY = "plugin.package.dir";

    /**
     * The key of logback configuration file
     */
    public static final String LOG_SETTING_FILE_KEY = "log.setting.file";

    /**
     * The key of the instruction in the Agent parameter
     */
    public static final String COMMAND_KEY = "command";

    /**
     * The key of agentPath in agent arguments
     */
    public static final String AGENT_PATH_KEY = "agentPath";

    /**
     * The class in Agent that processes instructions
     */
    public static final String COMMAND_PROCESSOR_CLASS = "com.huaweicloud.sermant.core.command.CommandProcessor";

    /**
     * Method of handling instructions in Agent
     */
    public static final String COMMAND_PROCESS_METHOD = "process";

    /**
     * Agent entry class
     */
    public static final String AGENT_CORE_ENTRANCE_CLASS = "com.huaweicloud.sermant.core.AgentCoreEntrance";

    /**
     * Agent Installation Method
     */
    public static final String AGENT_INSTALL_METHOD = "install";

    private BootConstant() {
    }
}
