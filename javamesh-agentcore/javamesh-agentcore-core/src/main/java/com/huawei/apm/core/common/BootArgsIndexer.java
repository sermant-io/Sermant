/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.common;

import java.io.File;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import com.huawei.apm.core.exception.SchemaException;
import com.huawei.apm.core.util.JarFileUtil;

/**
 * 路径索引器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/3
 */
public class BootArgsIndexer {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * java agent的版本
     */
    private static final String coreVersion;

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

    public static String getCoreVersion() {
        return coreVersion;
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
        try {
            coreVersion = JarFileUtil.getManifestAttr(new JarFile(currentFile), CommonConstant.CORE_VERSION_KEY)
                    .toString();
        } catch (Exception ignored) {
            throw new SchemaException(SchemaException.MISSING_VERSION, currentFile);
        }
    }
}
