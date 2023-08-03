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

package com.huaweicloud.sermant.core.plugin;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.event.collector.FrameworkEventCollector;
import com.huaweicloud.sermant.core.exception.SchemaException;
import com.huaweicloud.sermant.core.plugin.agent.ByteEnhanceManager;
import com.huaweicloud.sermant.core.plugin.common.PluginConstant;
import com.huaweicloud.sermant.core.plugin.common.PluginSchemaValidator;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
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

    private static final Map<String, Plugin> PLUGIN_MAP = new HashMap<>();

    private PluginManager() {
    }

    /**
     * 初始化插件包、配置、插件服务包等插件相关的内容
     *
     * @param pluginNames 插件名称集
     * @return boolean
     */
    public static boolean initPlugins(Set<String> pluginNames) {
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
            if (PLUGIN_MAP.containsKey(pluginName)) {
                LOGGER.log(Level.WARNING, "Plugin: {0} hsa bean installed. It cannot be loaded repeatedly.",
                        pluginName);
                continue;
            }
            try {
                initPlugin(pluginName, pluginPackage);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE,
                        String.format(Locale.ENGLISH, "Load plugin failed, plugin name: %s", pluginName), ex);
            }
        }
        return true;
    }

    /**
     * 初始化一个插件，检查必要参数，并调用{@link #doInitPlugin}
     *
     * @param pluginName 插件名称
     * @param pluginPackage 插件包路径
     */
    private static void initPlugin(String pluginName, String pluginPackage) {
        final String pluginPath = pluginPackage + File.separatorChar + pluginName;
        if (!new File(pluginPath).exists()) {
            LOGGER.warning(String.format(Locale.ROOT, "Plugin directory %s does not exist, so skip initializing %s. ",
                    pluginPath, pluginName));
            return;
        }
        doInitPlugin(new Plugin(pluginName, pluginPath, ClassLoaderManager.createPluginClassLoader()));
    }

    private static void doInitPlugin(Plugin plugin) {
        loadPlugins(plugin);
        plugin.createServiceClassLoader(toUrls(plugin.getName(), listJars(getServiceDir(plugin.getPath()))));
        PluginConfigManager.loadPluginConfig(plugin);
        PluginServiceManager.initPluginService(plugin);
        ByteEnhanceManager.enhanceStaticPlugin(plugin);

        // 插件成功加载后步骤
        PLUGIN_MAP.put(plugin.getName(), plugin);
        PluginSchemaValidator.setDefaultVersion(plugin.getName());
        ClassLoaderManager.getPluginClassFinder().addPluginClassLoader(plugin);
        FrameworkEventCollector.getInstance().collectPluginsLoadEvent(plugin.getName());
        LOGGER.log(Level.INFO, "Load plugin:{0} successful.", plugin.getName());
    }

    /**
     * 加载所有插件包
     *
     * @param plugin 插件
     */
    private static void loadPlugins(Plugin plugin) {
        for (File jar : listJars(getPluginDir(plugin.getPath()))) {
            processByJarFile(plugin.getName(), jar, true, new JarFileConsumer() {
                @Override
                public void consume(JarFile jarFile) {
                    try {
                        plugin.getPluginClassLoader().appendUrl(new File(jarFile.getName()).toURI().toURL());
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, "Add plugin path to pluginClassLoader fail, exception: ", e);
                    }
                }
            });
        }
    }

    /**
     * 获取插件所有jar包的URL，将进行jar包的校验和版本的校验
     *
     * @param pluginName 插件名称
     * @param jars jar包集
     * @return jar包的URL集
     */
    private static URL[] toUrls(String pluginName, File[] jars) {
        final List<URL> urls = new ArrayList<>();
        for (File jar : jars) {
            if (processByJarFile(pluginName, jar, false, null)) {
                final Optional<URL> url = toUrl(jar);
                url.ifPresent(urls::add);
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
    private static Optional<URL> toUrl(File file) {
        try {
            return Optional.of(file.toURI().toURL());
        } catch (MalformedURLException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Get URL of %s failed. ", file.getName()));
        }
        return Optional.empty();
    }

    /**
     * 将插件包文件转换为jar包，再做处理
     *
     * @param pluginName 插件名称
     * @param jar 插件包文件
     * @param ifCheckSchema 是否做jar包元数据检查
     * @param consumer jar包消费者
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
                } catch (IOException ioException) {
                    LOGGER.severe("Occurred ioException when close jar.");
                }
            }
        }
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
     * JarFile消费者
     *
     * @since 2021-11-12
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
