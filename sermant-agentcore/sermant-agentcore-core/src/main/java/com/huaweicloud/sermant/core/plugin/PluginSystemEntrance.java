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
import com.huaweicloud.sermant.core.operation.OperationManager;
import com.huaweicloud.sermant.core.operation.converter.api.YamlConverter;
import com.huaweicloud.sermant.core.plugin.config.PluginSetting;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 插件系统入口这里将依据插件设定文件调用{@link PluginManager}安装插件
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

    private PluginSystemEntrance() {
    }

    /**
     * 安装插件,区分是否为动态挂载场景 isDynamic->true 仅安装支持动态安装的active插件，passive插件通过下发指令安装 isDynamic->false加载支持静态安装的插件，默认启动；
     *
     * @param isDynamic 是否为动态安装，基于premain方式启动及agentmain方式启动来判断，premain方式时为false，agentmain方式时为false
     */
    public static void initialize(boolean isDynamic) {
        final PluginSetting pluginSetting = loadSetting();
        Set<String> staticPlugins = pluginSetting.getPlugins();
        if (!isDynamic) {
            // 初始化支持静态安装的插件 premain方式启动时执行
            if (staticPlugins == null || staticPlugins.isEmpty()) {
                LOGGER.info("Non static-support-plugin is configured to be loaded.");
                return;
            }
            PluginManager.initPlugins(staticPlugins, false);
        }

        if (isDynamic) {
            // 初始化支持动态安装的主动启动插件 agentmain方式启动时执行
            Set<String> activePlugins = pluginSetting.getDynamicPlugins().get("active");
            if (activePlugins == null || activePlugins.isEmpty()) {
                LOGGER.info("Non dynamic-support-plugin is configured to be loaded.");
                return;
            }
            PluginManager.initPlugins(activePlugins, true);
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
