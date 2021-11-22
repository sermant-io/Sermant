/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.common;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 路径索引器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/3
 */
public class PathIndexer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * javamesh的配置文件名的键
     */
    public static String JAVAMESH_CONFIG_FILE = "javamesh.config.file";

    /**
     * 插件的设置配置名的键
     */
    public static String JAVAMESH_PLUGIN_SETTING_FILE = "javamesh.plugin.setting.file";

    /**
     * pluginPackage插件包的键
     */
    public static String JAVAMESH_PLUGIN_PACKAGE_DIR = "javamesh.plugin.package.dir";

    /**
     * logback配置的键
     */
    public static String JAVAMESH_LOGBACK_SETTING_FILE = "javamesh.log.setting.file";

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
        configFile = new File(argsMap.get(PathIndexer.JAVAMESH_CONFIG_FILE).toString());
        if (!configFile.exists() || !configFile.isFile()) {
            LOGGER.warning("Config file is not found! ");
        }
        pluginSettingFile = new File(argsMap.get(PathIndexer.JAVAMESH_PLUGIN_SETTING_FILE).toString());
        if (!pluginSettingFile.exists() || !pluginSettingFile.isFile()) {
            LOGGER.warning("Plugin setting file is not found! ");
        }
        pluginPackageDir = new File(argsMap.get(PathIndexer.JAVAMESH_PLUGIN_PACKAGE_DIR).toString());
        if (!pluginPackageDir.exists() || !pluginPackageDir.isDirectory()) {
            LOGGER.warning("Plugin package directory is not found! ");
        }
    }
}
