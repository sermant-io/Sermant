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

package com.huaweicloud.sermant.core.plugin.config;

import static com.huaweicloud.sermant.core.plugin.common.PluginConstant.CONFIG_DIR_NAME;
import static com.huaweicloud.sermant.core.plugin.common.PluginConstant.CONFIG_FILE_NAME;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.utils.ConfigKeyUtil;
import com.huaweicloud.sermant.core.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * 插件配置管理器，${ConfigManager}统一配置管理器的特化，专门用来加载插件包配置和插件服务包配置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginConfigManager {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 配置对象集合，键为配置对象的实现类Class，值为加载完毕的配置对象
     */
    private static final Map<String, BaseConfig> PLUGIN_CONFIG_MAP = new HashMap<>();

    private PluginConfigManager() {
    }

    /**
     * 加载插件配置
     *
     * @param plugin 插件
     */
    public static void loadPluginConfigs(Plugin plugin) {
        File pluginConfigFile = getPluginConfigFile(plugin.getPath());
        ClassLoader classLoader =
                plugin.getServiceClassLoader() != null ? plugin.getServiceClassLoader() : plugin.getPluginClassLoader();
        for (BaseConfig config : ServiceLoader.load(PluginConfig.class, classLoader)) {
            final String typeKey = ConfigKeyUtil.getTypeKey(config.getClass());
            final BaseConfig retainedConfig = PLUGIN_CONFIG_MAP.get(typeKey);
            if (pluginConfigFile.exists() && pluginConfigFile.isFile()) {
                if (retainedConfig == null) {
                    PLUGIN_CONFIG_MAP.put(typeKey, ConfigManager.doLoad(pluginConfigFile, config));
                    plugin.getConfigs().add(typeKey);
                } else if (retainedConfig.getClass() == config.getClass()) {
                    LOGGER.fine(String.format(Locale.ROOT, "Skip load config [%s] repeatedly. ",
                            config.getClass().getName()));
                } else {
                    LOGGER.warning(String.format(Locale.ROOT, "Type key of %s is %s, same as %s's. ",
                            config.getClass().getName(), typeKey, retainedConfig.getClass().getName()));
                }
                continue;
            }
            if (PLUGIN_CONFIG_MAP.containsKey(typeKey)) {
                continue;
            }

            // 不能从文件加载，则为默认配置
            PLUGIN_CONFIG_MAP.put(typeKey, config);
            plugin.getConfigs().add(typeKey);
        }
    }

    /**
     * 清除插件的配置缓存
     *
     * @param plugin 插件
     */
    public static void cleanPluginConfigs(Plugin plugin) {
        for (String configName : plugin.getConfigs()) {
            PLUGIN_CONFIG_MAP.remove(configName);
        }
    }

    /**
     * 插件端专用的获取配置方法，当插件配置文件不存在时，插件配置将会不初始化出来，该方法将针对这一情况返回一个默认对象
     *
     * @param cls 插件配置类
     * @param <R> 插件配置类型
     * @return 插件配置实例
     */
    public static <R extends PluginConfig> R getPluginConfig(Class<R> cls) {
        return (R) PLUGIN_CONFIG_MAP.get(ConfigKeyUtil.getTypeKey(cls));
    }

    /**
     * 获取插件配置文件
     *
     * @param pluginPath 插件根目录
     * @return 插件配置文件
     */
    public static File getPluginConfigFile(String pluginPath) {
        return new File(pluginPath + File.separatorChar + CONFIG_DIR_NAME + File.separatorChar + CONFIG_FILE_NAME);
    }
}
