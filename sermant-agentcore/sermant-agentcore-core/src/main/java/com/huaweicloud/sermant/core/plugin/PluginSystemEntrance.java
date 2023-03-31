/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.utils.ConfigValueUtil;
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.agent.ByteEnhanceManager;
import com.huaweicloud.sermant.core.plugin.config.PluginSetting;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 插件系统入口
 * <p>这里将依据插件设定文件：
 * <pre>
 *     1.调用{@link PluginManager}初始化插件包
 *     2.调用{@link ByteEnhanceManager}做字节码增强
 * </pre>
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class PluginSystemEntrance {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final YamlConverter YAML_CONVERTER = OperationManager.getOperation(YamlConverter.class);

    private PluginSystemEntrance() {
    }

    /**
     * 初始化插件相关的内容，分插件和适配器两种
     *
     * @param instrumentation Instrumentation对象
     */
    public static void initialize(Instrumentation instrumentation) {
        final PluginSetting pluginSetting = loadSetting();
        Set<String> plugins = getLoadPlugins(pluginSetting);
        if (plugins == null) {
            LOGGER.info("No plugin is configured to be loaded.");
            return;
        }
        if (PluginManager.initPlugins(plugins, instrumentation)) {
            ByteEnhanceManager.enhance(instrumentation);
        }
    }

    /**
     * 加载插件设定配置，获取所有需要加载的插件文件夹
     *
     * @return 插件设定配置
     */
    private static PluginSetting loadSetting() {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(BootArgsIndexer.getPluginSettingFile()),
                    CommonConstant.DEFAULT_CHARSET);
            Optional<PluginSetting> pluginSettingOptional = YAML_CONVERTER.convert(reader, PluginSetting.class);
            return pluginSettingOptional.orElse(null);
        } catch (IOException ignored) {
            LOGGER.warning("Plugin setting file is not found. ");
            return new PluginSetting();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
        }
    }

    /**
     * 融合配置文件中的场景，插件，和环境变量中的场景信息 1. plugins配置的插件在任何场景都会加载 2. profile定义的场景优先级为: 环境变量 > 系统变量 > 配置文件。策略是优先级高的覆盖优先级低的。 3.
     * 重复添加的插件名会进行去重处理
     *
     * @param pluginSetting 插件配置信息
     * @return 需要加载的插件列表
     */
    private static Set<String> getLoadPlugins(PluginSetting pluginSetting) {
        Map<String, Set<String>> profilesMap = pluginSetting.getProfiles();
        Set<String> plugins = pluginSetting.getPlugins();
        if (profilesMap == null) {
            return plugins;
        }

        String profileFromConfig = pluginSetting.getProfile();
        String profileFromEnv = ConfigValueUtil.getValFromEnv(CommonConstant.PLUGIN_PROFILE);
        Set<String> profiles = new HashSet<>();
        if (profileFromEnv != null) {
            profiles = Arrays.stream(profileFromEnv.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else if (profileFromConfig != null) {
            profiles = Arrays.stream(profileFromConfig.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }

        profiles.forEach(profile -> {
            if (profilesMap.containsKey(profile)) {
                plugins.addAll(profilesMap.get(profile));
            } else {
                LOGGER.warning(String.format(Locale.ROOT, "The value of profile: %s is Invalid", profile));
            }
        });
        return plugins;
    }
}
