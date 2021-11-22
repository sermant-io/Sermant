/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.huawei.apm.core.common.PathIndexer;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.classloader.PluginClassLoader;
import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.plugin.config.PluginSetting;
import com.huawei.apm.core.plugin.service.PluginServiceManager;
import com.huawei.apm.core.util.FileUtil;

/**
 * 插件管理器，在这里将对插件相关的资源或操作进行管理
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class PluginManager {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    public static final String CONFIG_DIR_NAME = "config";

    public static final String PLUGIN_DIR_NAME = "plugin";

    public static final String SERVICE_DIR_NAME = "service";

    /**
     * 初始化插件包、配置、插件服务包等插件相关的内容
     *
     * @param instrumentation Instrumentation对象
     */
    public static void initialize(Instrumentation instrumentation) {
        final Set<String> pluginNames = loadSetting().getPlugins();
        if (pluginNames == null || pluginNames.isEmpty()) {
            return;
        }
        final String pluginPackage = FileUtil.getCanonicalPath(PathIndexer.getPluginPackageDir());
        if (pluginPackage == null) {
            return;
        }
        for (String pluginName : pluginNames) {
            initPlugin(pluginPackage + File.separatorChar + pluginName, instrumentation);
        }
    }

    /**
     * 初始化一个插件的插件包、配置、插件服务包等相关内容，主要包含以下流程：
     * <pre>
     *     1.加载插件包
     *     2.创建自定义类加载器加载插件服务包
     *     3.加载插件配置信息
     *     4.初始化插件服务
     * </pre>
     *
     * @param pluginPath      插件路径
     * @param instrumentation Instrumentation对象
     */
    private static void initPlugin(String pluginPath, Instrumentation instrumentation) {
        loadPlugins(new File(pluginPath + File.separatorChar + PLUGIN_DIR_NAME), instrumentation);
        ClassLoader classLoader = loadServices(new File(pluginPath + File.separatorChar + SERVICE_DIR_NAME));
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        loadConfig(new File(pluginPath + File.separatorChar + CONFIG_DIR_NAME), classLoader);
        initService(classLoader);
    }

    /**
     * 初始化服务，交由{@link PluginServiceManager#initPluginService(ClassLoader)}完成
     *
     * @param classLoader 加载插件服务包的自定义ClassLoader
     */
    private static void initService(ClassLoader classLoader) {
        PluginServiceManager.initPluginService(classLoader);
    }

    /**
     * 由{@link PluginConfigManager#loadServiceConfig(java.io.File, java.lang.ClassLoader)}方法加载插件配置信息
     *
     * @param configDir   配置文件夹
     * @param classLoader 加载插件服务包的自定义类加载器
     */
    private static void loadConfig(File configDir, ClassLoader classLoader) {
        PluginConfigManager.loadServiceConfig(configDir, classLoader);
    }

    /**
     * 遍历目录下所有jar包
     *
     * @param dir 目标文件夹
     * @return 所有jar包
     */
    private static File[] listJars(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return new File[0];
        }
        final File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".jar");
            }
        });
        if (files == null) {
            return new File[0];
        }
        return files;
    }

    /**
     * 获取所有jar包的URL
     *
     * @param files jar包集
     * @return jar包的URL集
     */
    private static URL[] toURLs(File[] files) {
        final List<URL> urls = new ArrayList<URL>();
        for (File file : files) {
            final URL url = FileUtil.toURL(file);
            if (url != null) {
                urls.add(url);
            }
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * 创建自定义类加载器加载所有插件服务包，若无插件服务包，则不会创建类加载器
     *
     * @param serviceDir 插件服务文件夹
     * @return 自定义类加载器
     */
    private static ClassLoader loadServices(File serviceDir) {
        final File[] jars = listJars(serviceDir);
        if (jars.length > 0) {
            return new PluginClassLoader(toURLs(jars));
        }
        return null;
    }

    /**
     * 加载所有插件包
     *
     * @param pluginDir       插件包目录
     * @param instrumentation Instrumentation对象
     */
    private static void loadPlugins(File pluginDir, Instrumentation instrumentation) {
        for (File jar : listJars(pluginDir)) {
            final JarFile jarFile = FileUtil.toJarFile(jar);
            if (jarFile != null) {
                instrumentation.appendToSystemClassLoaderSearch(jarFile);
            }
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
            reader = new InputStreamReader(new FileInputStream(PathIndexer.getPluginSettingFile()),
                    Charset.forName("UTF-8"));
            return yaml.loadAs(reader, PluginSetting.class);
        } catch (IOException ignored) {
            LOGGER.warning("Plugin setting file is not found. ");
            return new PluginSetting();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
