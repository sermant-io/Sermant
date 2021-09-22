/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.premain.utils.LibPathUtils;

import org.yaml.snakeyaml.Yaml;

/**
 * 插件配置{@link PluginConfig}加载器
 *
 * @author h30007557
 * @version 1.0.0
 * @since 2021/8/24
 */
public class PluginConfigLoader {
    /**
     * 插件配置目录名称
     */
    public static final String PLUGIN_CONFIGS_DIR_NAME = "configs";

    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 遍历所有的{@link PluginConfig}并执行操作
     *
     * @param configConsumer 消费操作
     */
    public static void foreachPluginConfig(PluginConfigConsumer configConsumer) {
        final String pluginConfigsDir = LibPathUtils.getAgentPath() + File.separator + PLUGIN_CONFIGS_DIR_NAME;
        final File[] configFiles = new File(pluginConfigsDir).listFiles();
        if (configFiles == null) {
            return;
        }
        final Yaml yaml = new Yaml();
        for (File configFile : configFiles) {
            consumeConfigFile(configFile, yaml, configConsumer);
        }
    }

    /**
     * 加载配置文件为{@link PluginConfig}并消费
     *
     * @param configFile     配置文件
     * @param yaml           snakeyaml主对象
     * @param configConsumer 消费方法
     */
    private static void consumeConfigFile(File configFile, Yaml yaml, PluginConfigConsumer configConsumer) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(configFile), Charset.forName("UTF-8"));
            configConsumer.accept(yaml.loadAs(reader, PluginConfig.class));
        } catch (IOException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Failed to load plugin config [%s]. ",
                    configFile.getName()));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * {@link PluginConfig}的消费方法
     */
    public interface PluginConfigConsumer {
        void accept(PluginConfig config);
    }
}
