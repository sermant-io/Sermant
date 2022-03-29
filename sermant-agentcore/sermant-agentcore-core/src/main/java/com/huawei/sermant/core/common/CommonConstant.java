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

package com.huawei.sermant.core.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 通用常量管理类，
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-27
 */
public class CommonConstant {
    /**
     * 框架默认字符集
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 启动配置文件名
     */
    public static final String BOOTSTRAP_CONFIG_FILE_NAME = "bootstrap.properties";

    /**
     * 核心配置文件名
     */
    public static final String CORE_CONFIG_FILE_NAME = "config.properties";

    /**
     * 插件设定文件名
     */
    public static final String PLUGIN_SETTING_FILE_NAME = "plugins.yaml";

    /**
     * logback配置文件名
     */
    public static final String LOG_SETTING_FILE_NAME = "logback.xml";

    /**
     * 核心包版本键
     */
    public static final String CORE_VERSION_KEY = "Sermant-Version";

    /**
     * 启动配置中应用名称的键
     */
    public static final String APP_NAME_KEY = "appName";

    /**
     * 启动配置中实例名称的键
     */
    public static final String INSTANCE_NAME_KEY = "instanceName";

    /**
     * 启动配置中应用类型的键
     */
    public static final String APP_TYPE_KEY = "appType";

    /**
     * 启动配置中的agentPath
     */
    public static final String AGENT_ROOT_DIR_KEY = "agentPath";

    /**
     * sermant的配置文件名的键
     */
    public static final String CORE_CONFIG_FILE_KEY = "core.config.file";

    /**
     * 插件的设置配置名的键
     */
    public static final String PLUGIN_SETTING_FILE_KEY = "plugin.setting.file";

    /**
     * pluginPackage插件包的键
     */
    public static final String PLUGIN_PACKAGE_DIR_KEY = "plugin.package.dir";

    /**
     * logback配置的键
     */
    public static final String LOG_SETTING_FILE_KEY = "log.setting.file";

    private CommonConstant() {
    }
}
