/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.common;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.huawei.apm.core.exception.PathNotIndexException;

/**
 * 路径索引器，core包使用者需要将配置和插件目录传递给路径索引器，路径索引器将构建出配置路径、插件路径和插件配置路径
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/3
 */
public class PathIndexer {
    private static PathIndexer instance;

    private final List<File> configs;
    private final Map<String, List<File>> plugins;
    private final Map<String, List<File>> pluginConfigs;

    private PathIndexer(List<File> configs, PluginBuildResult buildResult) {
        this.configs = configs;
        this.plugins = buildResult.getPlugins();
        this.pluginConfigs = buildResult.getPluginConfigs();
    }

    /**
     * 获取实例，必须先调用{@link PathIndexer#build(String, String, Set)}方法，否则将报出{@link PathNotIndexException}
     *
     * @return PathIndexer实例
     */
    public static PathIndexer getInstance() {
        if (instance == null) {
            throw new PathNotIndexException();
        }
        return instance;
    }

    /**
     * 获取配置文件集
     *
     * @return 配置文件集
     */
    public List<File> getConfigs() {
        return configs;
    }

    /**
     * 获取所有插件文件集
     *
     * @return 插件文件集
     */
    public Map<String, List<File>> getPlugins() {
        return plugins;
    }

    /**
     * 获取所有插件配置文件集
     *
     * @return 插件配置文件集
     */
    public Map<String, List<File>> getPluginConfigs() {
        return pluginConfigs;
    }

    /**
     * 构建配置文件集
     *
     * @param configPath 配置文件(夹)路径
     * @return 配置文件集
     */
    private static List<File> buildConfigs(String configPath) {
        final File configFile = new File(configPath);
        if (!configFile.exists()) {
            return Collections.emptyList();
        } else if (configFile.isFile()) {
            return Collections.singletonList(configFile);
        } else {
            final File[] configFiles = configFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });
            if (configFiles == null) {
                return Collections.emptyList();
            } else {
                return Arrays.asList(configFiles);
            }
        }
    }

    /**
     * 构建插件文件集
     *
     * @param pluginDir 插件目录
     * @return 插件文件集
     */
    private static List<File> buildPlugin(File pluginDir) {
        if (!pluginDir.isDirectory()) {
            return Collections.emptyList();
        }
        final File[] pluginFiles = pluginDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".jar");
            }
        });
        if (pluginFiles == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(pluginFiles);
    }

    /**
     * 构建插件配置文件集
     *
     * @param pluginDir 插件目录
     * @return 插件配置文件集
     * @throws IOException 插件目录无效
     */
    private static List<File> buildPluginConfig(File pluginDir) throws IOException {
        final File configDir = new File(pluginDir.getCanonicalPath() + File.separatorChar + "config");
        if (!configDir.exists() || !configDir.isDirectory()) {
            return Collections.emptyList();
        }
        final File[] configFiles = configDir.listFiles();
        if (configFiles == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(configFiles);
    }

    /**
     * 构建插件文件集和插件配置文件集
     *
     * @param pluginsPath    插件目录
     * @param excludePlugins 被排除的插件名称，含luban旧插件的目录和其他agent生态圈目录(概念)
     * @return 插件构建结果
     * @throws IOException 插件目录无效
     */
    private static PluginBuildResult buildPlugins(String pluginsPath, Set<String> excludePlugins) throws IOException {
        final File pluginsDir = new File(pluginsPath);
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            return new PluginBuildResult(Collections.emptyMap(), Collections.emptyMap());
        }
        final File[] pluginDirs = pluginsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !excludePlugins.contains(name);
            }
        });
        if (pluginDirs == null) {
            return new PluginBuildResult(Collections.emptyMap(), Collections.emptyMap());
        }
        final Map<String, List<File>> plugins = new HashMap<String, List<File>>();
        final Map<String, List<File>> pluginConfigs = new HashMap<String, List<File>>();
        for (File pluginDir : pluginDirs) {
            final List<File> pluginFiles = buildPlugin(pluginDir);
            if (pluginFiles.isEmpty()) {
                continue;
            }
            plugins.put(pluginDir.getName(), pluginFiles);
            final List<File> pluginConfigFiles = buildPluginConfig(pluginDir);
            if (!pluginConfigFiles.isEmpty()) {
                pluginConfigs.put(pluginDir.getName(), pluginConfigFiles);
            }
        }
        return new PluginBuildResult(plugins, pluginConfigs);
    }

    /**
     * 构建路径索引器
     *
     * @param configPath     配置目录
     * @param pluginsPath    插件目录
     * @param excludePlugins 被排除的插件名称
     * @throws IOException 插件目录无效
     */
    public static void build(String configPath, String pluginsPath, Set<String> excludePlugins) throws IOException {
        instance = new PathIndexer(buildConfigs(configPath), buildPlugins(pluginsPath, excludePlugins));
    }

    /**
     * 插件目录构建结构，含插件文件集合插件配置文件集
     */
    private static class PluginBuildResult {
        private final Map<String, List<File>> plugins;
        private final Map<String, List<File>> pluginConfigs;

        private PluginBuildResult(Map<String, List<File>> plugins,
                Map<String, List<File>> pluginConfigs) {
            this.plugins = plugins;
            this.pluginConfigs = pluginConfigs;
        }

        private Map<String, List<File>> getPlugins() {
            return plugins;
        }

        private Map<String, List<File>> getPluginConfigs() {
            return pluginConfigs;
        }
    }
}
