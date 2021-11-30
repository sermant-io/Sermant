/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.common;

import java.io.File;

import com.huawei.apm.core.lubanops.bootstrap.commons.LubanApmConstants;

/**
 * 路径声明器，其中定义agent core中各个组成部分的位置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class PathDeclarer {
    /**
     * 获取agent包所在目录
     *
     * @return agent包所在目录
     */
    public static String getAgentPath() {
        return new File(PathDeclarer.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    }

    /**
     * 获取核心功能包目录
     *
     * @return 核心功能包目录
     */
    public static String getCorePath() {
        return getAgentPath() + File.separatorChar + "core";
    }

    /**
     * 获取启动配置(原luban配置)路径，必须为agent包所在目录的bootstrap.properties
     *
     * @return 启动配置路径
     */
    public static String getBootConfigPath() {
        return getAgentPath() + File.separatorChar + LubanApmConstants.CONFIG_FILENAME;
    }

    /**
     * 获取agent core的统一配置
     *
     * @return agent core的统一配置
     */
    public static String getConfigPath() {
        return getAgentPath() + File.separatorChar + "config" + File.separatorChar + "config.properties";
    }

    /**
     * 获取插件设置配置
     *
     * @return 插件设置配置
     */
    public static String getPluginSettingPath() {
        return getAgentPath() + File.separatorChar + "config" + File.separatorChar + "plugins.yaml";
    }

    /**
     * 获取配置存储目录
     *
     * @return 配置存储目录
     */
    public static String getPluginPackagePath() {
        return getAgentPath() + File.separatorChar + "pluginPackage";
    }

    /**
     * 获取luban插件目录
     *
     * @return luban插件目录
     */
    public static String getLubanPluginsPath() {
        return getPluginPackagePath() + File.separatorChar + "luban";
    }

    /**
     * 获取logback日志配置
     *
     * @return logback日志配置
     */
    public static String getLogbackSettingPath() {
        return getAgentPath() + File.separatorChar + "config" + File.separatorChar + "logback.xml";
    }
}
