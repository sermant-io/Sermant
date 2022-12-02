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

package com.huawei.sermant.premain.common;

import com.huaweicloud.sermant.core.common.CommonConstant;

import java.io.File;

/**
 * 路径声明器，其中定义agent core中各个组成部分的位置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PathDeclarer {
    private PathDeclarer() {
    }

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
     * 获取核心功能实现包目录
     *
     * @return 核心功能实现包目录
     */
    public static String getImplementPath() {
        return getAgentPath() + File.separatorChar + "implement";
    }

    /**
     * 获取公共第三方依赖目录
     *
     * @return 核心功能实现包目录
     */
    public static String getCommonLibPath() {
        return getAgentPath() + File.separatorChar + "lib";
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
     * 配置文件夹
     *
     * @return 配置文件夹
     */
    private static String getConfigDirPath() {
        return getAgentPath() + File.separatorChar + "config";
    }

    /**
     * 获取启动配置路径
     *
     * @return 启动配置路径
     */
    public static String getBootConfigPath() {
        return getConfigDirPath() + File.separatorChar + CommonConstant.BOOTSTRAP_CONFIG_FILE_NAME;
    }

    /**
     * 获取agent core的统一配置
     *
     * @return agent core的统一配置
     */
    public static String getConfigPath() {
        return getConfigDirPath() + File.separatorChar + CommonConstant.CORE_CONFIG_FILE_NAME;
    }

    /**
     * 获取插件设置配置
     *
     * @return 插件设置配置
     */
    public static String getPluginSettingPath() {
        return getConfigDirPath() + File.separatorChar + CommonConstant.PLUGIN_SETTING_FILE_NAME;
    }

    /**
     * 获取logback日志配置
     *
     * @return logback日志配置
     */
    public static String getLogbackSettingPath() {
        return getConfigDirPath() + File.separatorChar + CommonConstant.LOG_SETTING_FILE_NAME;
    }
}
