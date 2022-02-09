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

package com.huawei.sermant.core.plugin.common;

import java.io.File;

/**
 * 插件管理系统常量
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-27
 */
public class PluginConstant {
    /**
     * 插件配置文件目录
     */
    public static final String CONFIG_DIR_NAME = "config";

    /**
     * 插件包目录
     */
    public static final String PLUGIN_DIR_NAME = "plugin";

    /**
     * 插件服务包目录
     */
    public static final String SERVICE_DIR_NAME = "service";

    /**
     * 插件配置文件名
     */
    public static final String CONFIG_FILE_NAME = "config.yaml";

    /**
     * sermant插件名称配置键，于manifest中获取
     */
    public static final String PLUGIN_NAME_KEY = "Sermant-Plugin-Name";

    /**
     * sermant插件版本配置键，于manifest中获取
     */
    public static final String PLUGIN_VERSION_KEY = "Sermant-Plugin-Version";

    /**
     * sermant插件默认版本
     */
    public static final String PLUGIN_DEFAULT_VERSION = "unknown";

    /**
     * 获取插件配置文件
     *
     * @param pluginPath 插件根目录
     * @return 插件配置文件
     */
    public static File getPluginConfigFile(String pluginPath) {
        return new File(pluginPath + File.separatorChar + CONFIG_DIR_NAME + File.separatorChar + CONFIG_FILE_NAME);
    }
}
