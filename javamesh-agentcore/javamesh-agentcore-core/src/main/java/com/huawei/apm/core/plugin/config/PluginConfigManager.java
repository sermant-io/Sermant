/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.plugin.config;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.huawei.apm.core.agent.interceptor.InterceptorChainManager;
import com.huawei.apm.core.config.ConfigManager;
import com.huawei.apm.core.config.common.BaseConfig;
import com.huawei.apm.core.config.utils.ConfigKeyUtil;
import com.huawei.apm.core.exception.IllegalConfigException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.util.FileUtil;

/**
 * 插件配置管理器，${ConfigManager}统一配置管理器的特化，专门用来加载插件包配置和插件服务包配置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class PluginConfigManager extends ConfigManager {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 插件配置文件名
     */
    private static final String CONFIG_FILE_NAME = "config.yaml";

    /**
     * 默认配置对象集
     */
    private static final Map<String, PluginConfig> DEFAULT_CONFIG_MAP = new HashMap<String, PluginConfig>();

    /**
     * 加载插件服务包配置
     *
     * @param configDir   配置文件夹
     * @param classLoader 加载插件服务包的类加载器
     */
    public static void loadServiceConfig(File configDir, ClassLoader classLoader) {
        final String configDirStr = FileUtil.getCanonicalPath(configDir);
        if (configDirStr == null) {
            return;
        }
        final File configFile = new File(configDirStr + File.separatorChar + CONFIG_FILE_NAME);
        if (!configFile.exists() || !configFile.isFile()) {
            return;
        }
        loadConfig(configFile, PluginConfig.class, classLoader,
                new ConfigConsumer() {
                    @Override
                    public void accept(BaseConfig config) {
                        if (config instanceof AliaConfig) {
                            InterceptorChainManager.addAlia((AliaConfig) config);
                        }
                    }
                });
    }

    /**
     * 插件端专用的获取配置方法，当插件配置文件不存在时，插件配置将会不初始化出来，该方法将针对这一情况返回一个默认对象
     *
     * @param cls 插件配置类
     * @param <R> 插件配置类型
     * @return 插件配置实例
     */
    public static <R extends PluginConfig> R getPluginConfig(Class<R> cls) {
        final R config = ConfigManager.getConfig(cls);
        if (config != null) {
            return config;
        }
        LOGGER.fine(String.format(Locale.ROOT, "Missing config file of %s, use default instance. ", cls.getName()));
        return getDefaultPluginConfig(cls);
    }

    /**
     * 获取默认的配置对象
     *
     * @param cls 插件配置类
     * @param <R> 插件配置类型
     * @return 默认配置对象
     */
    private static synchronized <R extends PluginConfig> R getDefaultPluginConfig(Class<R> cls) {
        final String typeKey = ConfigKeyUtil.getTypeKey(cls);
        final PluginConfig pluginConfig = DEFAULT_CONFIG_MAP.get(typeKey);
        if (pluginConfig != null) {
            return (R) pluginConfig;
        }
        LOGGER.fine(String.format(Locale.ROOT, "Create default instance of %s. ", cls.getName()));
        final R defaultConfig = createPluginConfig(cls);
        DEFAULT_CONFIG_MAP.put(typeKey, defaultConfig);
        return defaultConfig;
    }

    /**
     * 创建默认配置对象
     *
     * @param cls 插件配置类
     * @param <R> 插件配置类型
     * @return 默认配置对象
     */
    private static <R extends PluginConfig> R createPluginConfig(Class<R> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        throw new IllegalConfigException(cls);
    }
}
