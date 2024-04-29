/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.premain.common;

import java.io.File;

/**
 * PathDeclarator, which defines the location of the individual components in the agentCore
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PathDeclarer {
    private PathDeclarer() {
    }

    /**
     * Get the directory where the agent package resides
     *
     * @return agent package directory
     */
    public static String getAgentPath() {
        return new File(PathDeclarer.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    }

    /**
     * Get the core package directory
     *
     * @param agentPath agent path
     * @return core package directory
     */
    public static String getCorePath(String agentPath) {
        return agentPath + File.separatorChar + "core";
    }

    /**
     * Get the god package directory
     *
     * @param agentPath agent path
     * @return god package directory
     */
    public static String getGodLibPath(String agentPath) {
        return agentPath + File.separatorChar + "god";
    }

    /**
     * Get the core service implementation package directory
     *
     * @param agentPath agent path
     * @return core service implementation package directory
     */
    public static String getImplementPath(String agentPath) {
        return agentPath + File.separatorChar + "implement";
    }

    /**
     * Get the common third-party dependency directory
     *
     * @param agentPath agent path
     * @return common third-party dependency directory
     */
    public static String getCommonLibPath(String agentPath) {
        return agentPath + File.separatorChar + "common";
    }

    /**
     * Get the plugin package directory
     *
     * @param agentPath agent path
     * @return plugin package directory
     */
    public static String getPluginPackagePath(String agentPath) {
        return agentPath + File.separatorChar + "pluginPackage";
    }

    /**
     * Get configuration file directory
     *
     * @param agentPath agent path
     * @return Configuration file directory
     */
    private static String getConfigDirPath(String agentPath) {
        return agentPath + File.separatorChar + "config";
    }

    /**
     * Get bootstrap configuration file path
     *
     * @param agentPath agent path
     * @return bootstrap configuration file path
     */
    public static String getBootConfigPath(String agentPath) {
        return getConfigDirPath(agentPath) + File.separatorChar + BootConstant.BOOTSTRAP_CONFIG_FILE_NAME;
    }

    /**
     * Get the path of unified configuration of agent core
     *
     * @param agentPath agent path
     * @return the path of unified configuration of agent core
     */
    public static String getConfigPath(String agentPath) {
        return getConfigDirPath(agentPath) + File.separatorChar + BootConstant.CORE_CONFIG_FILE_NAME;
    }

    /**
     * Get the path of plugin settings configuration
     *
     * @param agentPath agent path
     * @return the path of plugin settings configuration
     */
    public static String getPluginSettingPath(String agentPath) {
        return getConfigDirPath(agentPath) + File.separatorChar + BootConstant.PLUGIN_SETTING_FILE_NAME;
    }

    /**
     * Get the path of logback Log configuration
     *
     * @param agentPath agent path
     * @return the path of logback Log configuration
     */
    public static String getLogbackSettingPath(String agentPath) {
        return getConfigDirPath(agentPath) + File.separatorChar + BootConstant.LOG_SETTING_FILE_NAME;
    }
}
