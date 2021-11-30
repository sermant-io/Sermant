/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.plugin.common;

/**
 * 插件管理系统常量
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/27
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
     * java-mesh插件名称配置键，于manifest中获取
     */
    public static final String PLUGIN_NAME_KEY = "Java-mesh-Plugin-Name";

    /**
     * java-mesh插件版本配置键，于manifest中获取
     */
    public static final String PLUGIN_VERSION_KEY = "Java-mesh-Plugin-Version";

    /**
     * java-mesh插件默认版本
     */
    public static final String PLUGIN_DEFAULT_VERSION = "unknown";
}
