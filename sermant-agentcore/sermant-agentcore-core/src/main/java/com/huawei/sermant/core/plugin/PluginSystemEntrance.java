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

package com.huawei.sermant.core.plugin;

import com.huawei.sermant.core.common.BootArgsIndexer;
import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.adaptor.AdaptorManager;
import com.huawei.sermant.core.plugin.agent.ByteEnhanceManager;
import com.huawei.sermant.core.plugin.config.PluginSetting;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

/**
 * 插件系统入口
 * <p>这里将依据插件设定文件：
 * <pre>
 *     1.调用{@link PluginManager}初始化插件包
 *     2.调用{@link AdaptorManager}初始化适配器
 *     3.调用{@link ByteEnhanceManager}做字节码增强
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

    private PluginSystemEntrance() {
    }

    /**
     * 初始化插件相关的内容，分插件和适配器两种
     *
     * @param instrumentation Instrumentation对象
     */
    public static void initialize(Instrumentation instrumentation) {
        final PluginSetting pluginSetting = loadSetting();
        if (PluginManager.initPlugins(pluginSetting.getPlugins(), instrumentation)
                | AdaptorManager.initAdaptors(pluginSetting.getAdaptors(), instrumentation)) {
            ByteEnhanceManager.enhance(instrumentation);
        }
    }

    /**
     * 加载插件设定配置，获取所有需要加载的插件文件夹
     *
     * @return 插件设定配置
     */
    private static PluginSetting loadSetting() {
        final Yaml yaml = new Yaml();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(BootArgsIndexer.getPluginSettingFile()),
                    CommonConstant.DEFAULT_CHARSET);
            return yaml.loadAs(reader, PluginSetting.class);
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
