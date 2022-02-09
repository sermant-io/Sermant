/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.exception.SchemaException;
import com.huawei.sermant.core.plugin.classloader.PluginClassLoader;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.common.PluginSchemaValidator;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.plugin.service.PluginServiceManager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 插件管理器，在这里将对插件相关的资源或操作进行管理
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginManager {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 初始化插件包、配置、插件服务包等插件相关的内容
     *
     * @param pluginNames     插件名称集
     * @param instrumentation Instrumentation对象
     * @return 是否有加载任何插件
     */
    public static boolean initPlugins(List<String> pluginNames, Instrumentation instrumentation) {
        if (pluginNames == null || pluginNames.isEmpty()) {
            return false;
        }
        final String pluginPackage;
        try {
            pluginPackage = BootArgsIndexer.getPluginPackageDir().getCanonicalPath();
        } catch (IOException ignored) {
            LOGGER.warning("Resolve plugin package failed. ");
            return false;
        }
        for (String pluginName : pluginNames) {
            initPlugin(pluginName, pluginPackage, instrumentation);
        }
        return true;
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
     * 初始化一个插件，检查必要参数，并调用{@link #doInitPlugin}
     *
     * @param pluginName      插件名称
     * @param pluginPackage   插件包路径
     * @param instrumentation Instrumentation对象
     */
    private static void initPlugin(String pluginName, String pluginPackage, Instrumentation instrumentation) {
        final String pluginPath = pluginPackage + File.separatorChar + pluginName;
        if (!new File(pluginPath).exists()) {
            LOGGER.warning(String.format(Locale.ROOT,
                    "Plugin directory %s does not exist, so skip initializing %s. ", pluginPath, pluginName));
            return;
        }
        doInitPlugin(pluginName, pluginPath, instrumentation);
    }

    /**
     * 初始化一个插件的插件包、配置、插件服务包等相关内容，主要包含以下流程：
     * <pre>
     *     1.加载插件包
     *     2.创建自定义类加载器加载插件服务包
     *     3.加载插件配置信息
     *     4.初始化插件服务
     *     5.设置默认插件版本
     * </pre>
     *
     * @param pluginName      插件名称
     * @param pluginPath      插件路径
     * @param instrumentation Instrumentation对象
     */
    private static void doInitPlugin(String pluginName, String pluginPath, Instrumentation instrumentation) {
        loadPlugins(pluginName, getPluginDir(pluginPath), instrumentation);
        final ClassLoader classLoader = loadServices(pluginName, getServiceDir(pluginPath));
        loadConfig(PluginConstant.getPluginConfigFile(pluginPath), classLoader);
        initService(classLoader);
        setDefaultVersion(pluginName);
    }

    /**
     * 设置默认的插件版本
     *
     * @param pluginName 插件名称
     */
    private static void setDefaultVersion(String pluginName) {
        PluginSchemaValidator.setDefaultVersion(pluginName);
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
        PluginConfigManager.loadServiceConfig(configFile, classLoader);
    }

    /**
     * 遍历目录下所有jar包，按文件名字典序排列
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
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
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
        final URL[] urls = toUrls(pluginName, listJars(serviceDir));
        if (urls.length > 0) {
            return new PluginClassLoader(urls);
        }
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * 获取插件所有jar包的URL，将进行jar包的校验和版本的校验
     *
     * @param pluginName 插件名称
     * @param jars       jar包集
     * @return jar包的URL集
     */
    private static URL[] toUrls(String pluginName, File[] jars) {
        final List<URL> urls = new ArrayList<URL>();
        for (File jar : jars) {
            if (processByJarFile(pluginName, jar, false, null)) {
                final URL url = toUrl(jar);
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
    private static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Get URL of %s failed. ", file.getName()));
        }
        return null;
    }

    /**
     * 将插件包文件转换为jar包，再做处理
     *
     * @param pluginName    插件名称
     * @param jar           插件包文件
     * @param ifCheckSchema 是否做jar包元数据检查
     * @param consumer      jar包消费者
     * @return 是否无异常处理完毕
     */
    private static boolean processByJarFile(String pluginName, File jar, boolean ifCheckSchema,
            JarFileConsumer consumer) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jar);
            if (ifCheckSchema && !PluginSchemaValidator.checkSchema(pluginName, jarFile)) {
                throw new SchemaException(SchemaException.UNEXPECTED_EXT_JAR, jar.getPath());
            }
            if (consumer != null) {
                consumer.consume(jarFile);
            }
            return true;
        } catch (IOException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Check schema of %s failed. ", jar.getPath()));
            return false;
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignored) {
                }
            }
        }
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
            processByJarFile(pluginName, jar, true, new JarFileConsumer() {
                @Override
                public void consume(JarFile jarFile) {
                    instrumentation.appendToSystemClassLoaderSearch(jarFile);
                }
            });
        }
    }

    /**
     * JarFile消费者
     */
    private interface JarFileConsumer {
        /**
         * 消费JarFile
         *
         * @param jarFile JarFile对象
         */
        void consume(JarFile jarFile);
    }
}
