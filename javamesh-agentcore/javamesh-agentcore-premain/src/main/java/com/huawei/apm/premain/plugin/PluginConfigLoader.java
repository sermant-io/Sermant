/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.premain.lubanops.utils.LibPathUtils;

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
        final File pluginBaseDir = new File(LibPathUtils.getPluginsPath());
        if (!pluginBaseDir.exists() || !pluginBaseDir.isDirectory()) {
            return Collections.emptyList();
        }
        final String[] pluginDirPaths = pluginBaseDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(LibPathUtils.getLubanOpsDirName());
            }
        });
        if (pluginDirPaths == null) {
            return Collections.emptyList();
        }
        final List<File> configFiles = new ArrayList<File>();
        for (String pluginDirPath : pluginDirPaths) {
            final File configDir = new File(pluginDirPath + File.separatorChar + "config");
            if (!configDir.exists() || !configDir.isDirectory()) {
                continue;
            }
            final File[] configs = configDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".yaml");
                }
            });
            if (configs == null) {
                continue;
            }
            for (File config : configs) {
                if (config.isFile()) {
                    configFiles.add(config);
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
