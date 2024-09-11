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

package io.sermant.core.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * CommonConstant
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-27
 */
public class CommonConstant {
    /**
     * default character set
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * logback file name
     */
    public static final String LOG_SETTING_FILE_NAME = "logback.xml";

    /**
     * sermant version
     */
    public static final String CORE_VERSION_KEY = "Sermant-Version";

    /**
     * key of application name
     */
    public static final String APP_NAME_KEY = "appName";

    /**
     * key of application type
     */
    public static final String APP_TYPE_KEY = "appType";

    /**
     * key of service name
     */
    public static final String SERVICE_NAME_KEY = "serviceName";

    /**
     * key of Sermant core functions implement package
     */
    public static final String CORE_IMPLEMENT_DIR_KEY = "core.implement.dir";

    /**
     * Sermant public third party dependency directory
     */
    public static final String COMMON_DEPENDENCY_DIR_KEY = "common.dir";

    /**
     * key of Sermant configuration file name
     */
    public static final String CORE_CONFIG_FILE_KEY = "core.config.file";

    /**
     * key of plugin setting file
     */
    public static final String PLUGIN_SETTING_FILE_KEY = "plugin.setting.file";

    /**
     * key of pluginPackage
     */
    public static final String PLUGIN_PACKAGE_DIR_KEY = "plugin.package.dir";

    /**
     * key of logback setting file
     */
    public static final String LOG_SETTING_FILE_KEY = "log.setting.file";

    /**
     * key of profile
     */
    public static final String PLUGIN_PROFILE = "profile";

    /**
     * COMMA
     */
    public static final String COMMA = ",";

    /**
     * DOT
     */
    public static final String DOT = ".";

    /**
     * COLON
     */
    public static final String COLON = ":";

    /**
     * Byte-buddy error field in the log
     */
    public static final String ERROR = "ERROR";

    /**
     * Bytecode enhancement success field in the byte-buddy log
     */
    public static final String TRANSFORM = "TRANSFORM";

    /**
     * Default enhanced bytecode file output path parent directory
     */
    public static final String ENHANCED_CLASS_OUTPUT_PARENT_DIR = "enhancedClasses";

    /**
     * The key of artifact in agent arguments
     */
    public static final String ARTIFACT_NAME_KEY = "artifact";

    private CommonConstant() {
    }
}
