/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.huawei.javamesh.core.common.BootArgsIndexer;
import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.exception.SchemaException;
import com.huawei.javamesh.core.plugin.classloader.PluginClassLoader;
import com.huawei.javamesh.core.plugin.common.PluginConstant;
import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.javamesh.core.plugin.config.PluginSetting;
import com.huawei.javamesh.core.plugin.service.PluginServiceManager;
import com.huawei.javamesh.core.util.JarFileUtil;

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
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 插件名称与版本的集合
     */
    private static final Map<String, String> PLUGIN_VERSION_MAP = new HashMap<>();

    /**
     * 获取插件名和插件版本的集合
     *
     * @return 插件名和插件版本的集合
     */
    public static Map<String, String> getPluginVersionMap() {
        return Collections.unmodifiableMap(PLUGIN_VERSION_MAP);
    }

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
        final String pluginPackage;
        try {
            pluginPackage = BootArgsIndexer.getPluginPackageDir().getCanonicalPath();
        } catch (IOException ignored) {
            return;
        }
        for (String pluginName : pluginNames) {
            initPlugin(pluginName, pluginPackage + File.separatorChar + pluginName, instrumentation);
            setDefaultVersion(pluginName);
        }
    }

    /**
     * 当插件的版本不存在时，使用默认的版本号
     *
     * @param pluginName 插件名称
     */
    private static void setDefaultVersion(String pluginName) {
        if (!PLUGIN_VERSION_MAP.containsKey(pluginName)) {
            PLUGIN_VERSION_MAP.put(pluginName, PluginConstant.PLUGIN_DEFAULT_VERSION);
        }
    }

    /**
     * 获取插件包目录
     *
     * @param pluginPath 插件根目录
     * @return 插件包目录
     */
    private static File getPluginDir(String pluginPath) {
        return new File(pluginPath + File.separatorChar + PluginConstant.PLUGIN_DIR_NAME);
    }

    /**
     * 获取插件服务包目录
     *
     * @param pluginPath 插件根目录
     * @return 插件服务包目录
     */
    private static File getServiceDir(String pluginPath) {
        return new File(pluginPath + File.separatorChar + PluginConstant.SERVICE_DIR_NAME);
    }

    /**
     * 获取插件配置文件
     *
     * @param pluginPath 插件根目录
     * @return 插件配置文件
     */
    private static File getPluginConfigFile(String pluginPath) {
        return new File(pluginPath + File.separatorChar + PluginConstant.CONFIG_DIR_NAME + File.separatorChar +
                PluginConstant.CONFIG_FILE_NAME);
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
     * @param pluginName      插件名称
     * @param pluginPath      插件路径
     * @param instrumentation Instrumentation对象
     */
    private static void initPlugin(String pluginName, String pluginPath, Instrumentation instrumentation) {
        loadPlugins(pluginName, getPluginDir(pluginPath), instrumentation);
        ClassLoader classLoader = loadServices(pluginName, getServiceDir(pluginPath));
        loadConfig(getPluginConfigFile(pluginPath), classLoader);
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
     * @param configFile  配置文件
     * @param classLoader 加载插件服务包的自定义类加载器
     */
    private static void loadConfig(File configFile, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        PluginConfigManager.loadServiceConfig(configFile, classLoader);
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
     * 创建自定义类加载器加载所有插件服务包，若无插件服务包，则不会创建类加载器
     *
     * @param pluginName 插件名称
     * @param serviceDir 插件服务文件夹
     * @return 自定义类加载器
     */
    private static ClassLoader loadServices(String pluginName, File serviceDir) {
        final URL[] urls = toURLs(pluginName, listJars(serviceDir));
        if (urls.length > 0) {
            return new PluginClassLoader(urls);
        }
        return null;
    }

    /**
     * 获取插件所有jar包的URL，将进行jar包的校验和版本的校验
     *
     * @param pluginName 插件名称
     * @param jars       jar包集
     * @return jar包的URL集
     */
    private static URL[] toURLs(String pluginName, File[] jars) {
        final List<URL> urls = new ArrayList<URL>();
        for (File jar : jars) {
            final JarFile jarFile = toJarFile(pluginName, jar, true);
            if (jarFile != null) {
                final URL url = toURL(jar);
                if (url != null) {
                    urls.add(url);
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * 通过文件获取url
     *
     * @param file 文件
     * @return url
     */
    private static URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Get URL of %s failed. ", file.getName()));
        }
        return null;
    }

    /**
     * 将插件包文件转换为jar包，并校验插件版本
     *
     * @param pluginName 插件名称
     * @param jar        插件包文件
     * @return 插件包jar包
     */
    private static JarFile toJarFile(String pluginName, File jar, boolean ifAllowExtJar) {
        try {
            final JarFile jarFile = new JarFile(jar);
            if (!ifAllowExtJar && !checkSchema(pluginName, jarFile)) {
                throw new SchemaException(SchemaException.UNEXPECTED_EXT_JAR, jar.getPath());
            }
            return jarFile;
        } catch (IOException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Check schema of %s failed. ", jar.getPath()));
        }
        return null;
    }

    /**
     * 检查名称和版本
     *
     * @param pluginName 插件名称
     * @param jarFile    插件包
     * @return 为真时经过名称和版本校验，为插件包或插件服务包，为假时表示第三方jar包
     * @throws IOException 获取manifest文件异常
     */
    private static boolean checkSchema(String pluginName, JarFile jarFile) throws IOException {
        final Object nameAttr = JarFileUtil.getManifestAttr(jarFile, PluginConstant.PLUGIN_NAME_KEY);
        if (nameAttr == null) {
            return false;
        }
        if (!nameAttr.toString().equals(pluginName)) {
            throw new SchemaException(SchemaException.UNEXPECTED_NAME, nameAttr.toString(), pluginName);
        }
        final Object versionAttr = JarFileUtil.getManifestAttr(jarFile, PluginConstant.PLUGIN_VERSION_KEY);
        final String givingVersion =
                versionAttr == null ? PluginConstant.PLUGIN_DEFAULT_VERSION : versionAttr.toString();
        final String expectingVersion = PLUGIN_VERSION_MAP.get(pluginName);
        if (expectingVersion == null) {
            PLUGIN_VERSION_MAP.put(pluginName, givingVersion);
        } else if (!expectingVersion.equals(givingVersion)) {
            throw new SchemaException(SchemaException.UNEXPECTED_VERSION, pluginName, givingVersion, expectingVersion);
        }
        return true;
    }

    /**
     * 加载所有插件包
     *
     * @param pluginName      插件名称
     * @param pluginDir       插件包目录
     * @param instrumentation Instrumentation对象
     */
    private static void loadPlugins(String pluginName, File pluginDir, Instrumentation instrumentation) {
        for (File jar : listJars(pluginDir)) {
            final JarFile jarFile = toJarFile(pluginName, jar, false);
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
            reader = new InputStreamReader(new FileInputStream(BootArgsIndexer.getPluginSettingFile()),
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
