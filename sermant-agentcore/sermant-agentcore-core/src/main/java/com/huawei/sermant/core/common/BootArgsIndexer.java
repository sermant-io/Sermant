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

import com.huawei.sermant.core.exception.SchemaException;
import com.huawei.sermant.core.utils.JarFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 路径索引器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-03
 */
public class BootArgsIndexer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * java agent的版本
     */
    private static final String CORE_VERSION;

    /**
     * 配置文件
     */
    private static File configFile;

    /**
     * 插件的设置配置
     */
    private static File pluginSettingFile;

    /**
     * pluginPackage插件包
     */
    private static File pluginPackageDir;

    private BootArgsIndexer() {
    }

    public static String getCoreVersion() {
        return CORE_VERSION;
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static File getPluginSettingFile() {
        return pluginSettingFile;
    }

    public static File getPluginPackageDir() {
        return pluginPackageDir;
    }

    /**
     * 构建路径索引器
     *
     * @param argsMap 启动参数
     */
    public static void build(Map<String, Object> argsMap) {
        configFile = new File(argsMap.get(CommonConstant.CORE_CONFIG_FILE_KEY).toString());
        if (!configFile.exists() || !configFile.isFile()) {
            LOGGER.warning("Config file is not found! ");
        }
        pluginSettingFile = new File(argsMap.get(CommonConstant.PLUGIN_SETTING_FILE_KEY).toString());
        if (!pluginSettingFile.exists() || !pluginSettingFile.isFile()) {
            LOGGER.warning("Plugin setting file is not found! ");
        }
        pluginPackageDir = new File(argsMap.get(CommonConstant.PLUGIN_PACKAGE_DIR_KEY).toString());
        if (!pluginPackageDir.exists() || !pluginPackageDir.isDirectory()) {
            LOGGER.warning("Plugin package directory is not found! ");
        }
    }

    static {
        final String currentFile = BootArgsIndexer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(currentFile);
            CORE_VERSION = JarFileUtils.getManifestAttr(jarFile, CommonConstant.CORE_VERSION_KEY).toString();
        } catch (IOException e) {
            throw new SchemaException(SchemaException.MISSING_VERSION, currentFile);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
        }
    }
}
