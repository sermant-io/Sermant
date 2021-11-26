/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.apm.core.common.PathIndexer;
import com.huawei.apm.core.lubanops.bootstrap.commons.LubanApmConstants;
import com.huawei.apm.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.apm.core.util.FieldUtil;

/**
 * 启动参数构建器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public abstract class BootArgsBuilder {
    /**
     * 构建启动参数
     *
     * @param agentArgs 程序入参
     * @return 启动参数
     */
    public static Map<String, Object> build(String agentArgs) {
        final Properties configMap = loadConfig();
        final Map<String, Object> argsMap = toArgsMap(agentArgs);
        addNotNullEntries(argsMap, configMap);
        addNormalEntries(argsMap, configMap);
        addSpeEntries(argsMap, configMap);
        addPathEntries(argsMap);
        return argsMap;
    }

    /**
     * 构建入参集
     *
     * @param args 程序入参
     * @return 入参集
     */
    private static Map<String, Object> toArgsMap(String args) {
        final Map<String, Object> argsMap = new HashMap<String, Object>();
        if (args == null) {
            return argsMap;
        }
        for (String arg : args.trim().split(",")) {
            final int index = arg.indexOf('=');
            if (index >= 0) {
                argsMap.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
            }
        }
        return argsMap;
    }

    /**
     * 读取启动配置
     *
     * @return 启动配置
     */
    private static Properties loadConfig() {
        final Properties properties = new Properties();
        InputStream configStream = null;
        try {
            configStream = new FileInputStream(PathDeclarer.getBootConfigPath());
            properties.load(configStream);
        } catch (IOException e) {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return properties;
    }

    /**
     * 通用获取参数值方式，优先从配置获取，在从环境变量获取
     *
     * @param key       参数键
     * @param configMap 配置集
     * @return 参数值
     */
    private static String getCommonValue(String key, Properties configMap) {
        String value = configMap.getProperty(FieldUtil.toUnderline(key, '.', false));
        value = value == null ? System.getProperty(key) : value;
        return value;
    }

    /**
     * 添加非空的键值对，涉及的参数均不可为空
     *
     * @param argsMap   参数集
     * @param configMap 配置集
     */
    private static void addNotNullEntries(Map<String, Object> argsMap, Properties configMap) {
        String key = LubanApmConstants.APP_NAME_COMMONS;
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            if (value == null) {
                throw new IllegalArgumentException(LubanApmConstants.APP_NAME_COMMONS + " not found. ");
            } else {
                argsMap.put(key, value);
            }
        }
        key = LubanApmConstants.INSTANCE_NAME_COMMONS;
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            argsMap.put(key, value == null ? "default" : value);
        }
        key = LubanApmConstants.APP_TYPE_COMMON;
        if (!argsMap.containsKey(key)) {
            final String value = getCommonValue(key, configMap);
            argsMap.put(key, value == null ? 0 : Integer.parseInt(value));
        }
    }

    /**
     * 添加普通键值对，为空时不添加
     *
     * @param argsMap   参数集
     * @param configMap 配置集
     */
    private static void addNormalEntries(Map<String, Object> argsMap, Properties configMap) {
        for (String key : new String[]{
                LubanApmConstants.ENV_COMMONS,
                LubanApmConstants.ENV_TAG_COMMONS,
                LubanApmConstants.BIZ_PATH_COMMONS,
                LubanApmConstants.SUB_BUSINESS_COMMONS,
                LubanApmConstants.ENV_SECRET_COMMONS}) {
            if (!argsMap.containsKey(key)) {
                final String value = getCommonValue(key, configMap);
                if (value != null) {
                    argsMap.put(key, value);
                }
            }
        }
    }

    /**
     * 添加特殊键值对，参数键风格为'.'连接，而不是小驼峰，为空时不添加
     *
     * @param argsMap   参数集
     * @param configMap 配置集
     */
    private static void addSpeEntries(Map<String, Object> argsMap, Properties configMap) {
        for (String key : new String[]{
                AgentConfigManager.MASTER_ACCESS_KEY,
                AgentConfigManager.MASTER_SECRET_KEY,
                AgentConfigManager.MASTER_ADDRESS}) {
            final String camelKey = FieldUtil.toCamel(key, '.', false);
            if (!argsMap.containsKey(camelKey)) {
                String value = configMap.getProperty(key);
                value = value == null ? System.getProperty(camelKey) : value;
                if (value != null) {
                    argsMap.put(key, value);
                }
            }
        }
    }

    /**
     * 添加路径键值对
     *
     * @param argsMap 参数集
     */
    private static void addPathEntries(Map<String, Object> argsMap) {
        argsMap.put(LubanApmConstants.AGENT_PATH_COMMONS, PathDeclarer.getAgentPath());
        argsMap.put(LubanApmConstants.BOOT_PATH_COMMONS, PathDeclarer.getCorePath());
        argsMap.put(LubanApmConstants.PLUGINS_PATH_COMMONS, PathDeclarer.getLubanPluginsPath());
        argsMap.put(PathIndexer.JAVAMESH_CONFIG_FILE, PathDeclarer.getConfigPath());
        argsMap.put(PathIndexer.JAVAMESH_PLUGIN_SETTING_FILE, PathDeclarer.getPluginSettingPath());
        argsMap.put(PathIndexer.JAVAMESH_PLUGIN_PACKAGE_DIR, PathDeclarer.getPluginPackagePath());
        argsMap.put(LoggerFactory.JAVAMESH_LOGBACK_SETTING_FILE, PathDeclarer.getLogbackSettingPath());
    }
}
