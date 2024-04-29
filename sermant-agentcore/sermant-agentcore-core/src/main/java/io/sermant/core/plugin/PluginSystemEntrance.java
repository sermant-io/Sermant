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

package io.sermant.core.plugin;

import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.plugin.config.PluginSetting;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.MapUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Plugin system entrance, where the plugins are installed by invoking {@link PluginManager} according to the plugin
 * configuration file
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
public class PluginSystemEntrance {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private PluginSystemEntrance() {
    }

    /**
     * Dynamic installation scenario: isDynamic->true indicates that only active plug-ins supporting dynamic
     * installation are installed. passive plugins are installed by delivering commands. isDynamic->false indicates that
     * plugins supporting static installation are loaded.
     *
     * @param isDynamic Whether the installation is dynamic is determined based on the startup mode of premain and the
     * startup mode of agentmain. The value is false for premain and true for agentmain
     */
    public static void initialize(boolean isDynamic) {
        final PluginSetting pluginSetting = loadSetting();
        Set<String> staticPlugins = pluginSetting.getPlugins();
        if (!isDynamic) {
            // Initialize plugins that supports static installation when it is started in premain mode
            if (CollectionUtils.isEmpty(staticPlugins)) {
                LOGGER.info("Non static-support-plugin is configured to be loaded.");
                return;
            }
            PluginManager.initPlugins(staticPlugins, false);
        }

        if (isDynamic) {
            // Initialize active plugins that supports dynamic installation when it is started in agentmain mode
            Map<String, Set<String>> dynamicPlugins = pluginSetting.getDynamicPlugins();
            if (MapUtils.isEmpty(dynamicPlugins)) {
                LOGGER.info("Non dynamic-support-plugin is configured to be loaded.");
                return;
            }
            Set<String> activePlugins = dynamicPlugins.get("active");
            if (CollectionUtils.isEmpty(activePlugins)) {
                LOGGER.info("Non active dynamic-support-plugin is configured to be loaded.");
                return;
            }
            PluginManager.initPlugins(activePlugins, true);
        }
    }

    /**
     * Load the plugin Settings configuration and get all the plugin folders that need to be loaded
     *
     * @return Plugin configuration
     */
    private static PluginSetting loadSetting() {
        Reader reader = null;
        try {
            reader = new InputStreamReader(Files.newInputStream(BootArgsIndexer.getPluginSettingFile().toPath()),
                    CommonConstant.DEFAULT_CHARSET);
            Optional<PluginSetting> pluginSettingOptional = OperationManager.getOperation(YamlConverter.class)
                    .convert(reader, PluginSetting.class);
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
}
