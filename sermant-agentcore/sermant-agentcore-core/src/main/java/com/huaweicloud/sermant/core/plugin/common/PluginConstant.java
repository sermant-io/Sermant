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

package com.huaweicloud.sermant.core.plugin.common;

import java.io.File;

/**
 * Plugin Constant
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-27
 */
public class PluginConstant {
    /**
     * Plugin configuration file directory
     */
    public static final String CONFIG_DIR_NAME = "config";

    /**
     * Plugin package directory
     */
    public static final String PLUGIN_DIR_NAME = "plugin";

    /**
     * Plugin service package directory
     */
    public static final String SERVICE_DIR_NAME = "service";

    /**
     * Plugin configuration file name
     */
    public static final String CONFIG_FILE_NAME = "config.yaml";

    /**
     * sermant plugin name configuration key, obtained in the manifest
     */
    public static final String PLUGIN_NAME_KEY = "Sermant-Plugin-Name";

    /**
     * sermant plugin version configuration key, obtained in the manifest
     */
    public static final String PLUGIN_VERSION_KEY = "Sermant-Plugin-Version";

    /**
     * sermant plugin default version
     */
    public static final String PLUGIN_DEFAULT_VERSION = "unknown";

    private PluginConstant() {
    }

    /**
     * Get the plugin configuration file
     *
     * @param pluginPath Plugin root directory
     * @return Plugin configuration file
     */
    public static File getPluginConfigFile(String pluginPath) {
        return new File(pluginPath + File.separatorChar + CONFIG_DIR_NAME + File.separatorChar + CONFIG_FILE_NAME);
    }
}
