/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.common.PathIndexer;

/**
 * 插件配置{@link PluginConfig}加载器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/24
 */
public abstract class PluginConfigLoader {
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
        final List<File> configFiles = getConfigFiles();
        final Yaml yaml = new Yaml();
        for (File configFile : configFiles) {
            consumeConfigFile(configFile, yaml, configConsumer);
        }
    }

    private static List<File> getConfigFiles() {
        final List<File> configFiles = new ArrayList<File>();
        for (List<File> configDirFiles : PathIndexer.getInstance().getPluginConfigs().values()) {
            for (File configFile : configDirFiles) {
                if (configFile.getName().endsWith(".yaml")) {
                    configFiles.add(configFile);
                }
            }
        }
        return configFiles;
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
