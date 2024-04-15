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
import com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserScheduler;
import com.huaweicloud.sermant.core.plugin.agent.info.EnhancementManager;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;
import com.huaweicloud.sermant.core.plugin.agent.template.BaseAdviseHandler;
import com.huaweicloud.sermant.core.plugin.classloader.PluginClassLoader;
import com.huaweicloud.sermant.core.plugin.classloader.ServiceClassLoader;
import com.huaweicloud.sermant.core.plugin.common.PluginConstant;
import com.huaweicloud.sermant.core.plugin.common.PluginSchemaValidator;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.CollectionUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin manager, where plugin-related resources or operations are managed
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginManager {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<String, Plugin> PLUGIN_MAP = new HashMap<>();

    private PluginManager() {
    }

    /**
     * Install plugins
     *
     * @param pluginNames plugin name set, the plugin products must exist in the pluginPackage, otherwise it will not be
     * installed successfully
     */
    public static void install(Set<String> pluginNames) {
        initPlugins(pluginNames, true);
    }

    /**
     * uninstall plugin
     *
     * @param pluginNames plugin name set
     */
    public static void uninstall(Set<String> pluginNames) {
        if (CollectionUtils.isEmpty(pluginNames)) {
            LOGGER.log(Level.WARNING, "No plugin is configured to be uninstall.");
            return;
        }
        for (String name : pluginNames) {
            Plugin plugin = PLUGIN_MAP.get(name);
            if (plugin == null) {
                LOGGER.log(Level.INFO, "Plugin {0} has not been installed.", name);
                continue;
            }
            if (!plugin.isDynamic()) {
                LOGGER.log(Level.INFO, "Plugin {0} is static-support-plugin,can not be uninstalled.", name);
                continue;
            }

            // Release all locks occupied by plugins
            for (String adviceKey : plugin.getAdviceLocks()) {
                AdviserScheduler.unLock(adviceKey);
            }

            // Cancel bytecode enhancement
            ByteEnhanceManager.unEnhanceDynamicPlugin(plugin);

            // Clear enhancement information
            EnhancementManager.removePluginEnhancements(plugin);

            // Stop plugin services
            PluginServiceManager.shutdownPluginServices(plugin);

            // Remove the PluginClassLoader corresponding to plugin from the PluginClassLoaderFinder
            ClassLoaderManager.getPluginClassFinder().removePluginClassLoader(plugin);

            // Clean up the Interceptors created by the plugin
            Map<String, List<Interceptor>> interceptorListMap = BaseAdviseHandler.getInterceptorListMap();
            for (List<Interceptor> interceptors : interceptorListMap.values()) {
                interceptors.removeIf(
                        interceptor -> plugin.getPluginClassLoader().equals(interceptor.getClass().getClassLoader()));
            }

            // Delete the plugin configuration in the cache
            PluginConfigManager.cleanPluginConfigs(plugin);

            // Close the classLoader of the plugin
            closePluginLoaders(plugin);

            // Clear the plugin from the plugin Map
            PLUGIN_MAP.remove(name);

            // Clear the plugin information cache
            PluginSchemaValidator.removePluginVersionCache(name);
        }
    }

    /**
     * Uninstall all plugins
     */
    public static void uninstallAll() {
        // Create a new Set to prevent the plugin name set from being changed when the PLUGIN_MAP cache is deleted
        uninstall(new HashSet<>(PLUGIN_MAP.keySet()));
    }

    /**
     * Initialize plugin packages, configurations, and plugin service packages
     *
     * @param pluginNames plugin name set
     * @param isDynamic Whether the plugin is dynamic
     */
    public static void initPlugins(Set<String> pluginNames, boolean isDynamic) {
        if (pluginNames == null || pluginNames.isEmpty()) {
            LOGGER.log(Level.WARNING, "Non plugin is configured to be initialized.");
            return;
        }
        final String pluginPackage;
        try {
            pluginPackage = BootArgsIndexer.getPluginPackageDir().getCanonicalPath();
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Resolve plugin package failed.", ioException);
            return;
        }
        for (String pluginName : pluginNames) {
            if (PLUGIN_MAP.containsKey(pluginName)) {
                LOGGER.log(Level.WARNING, "Plugin: {0} has bean installed. It cannot be installed repeatedly.",
                        pluginName);
                continue;
            }
            executeInit(isDynamic, pluginPackage, pluginName);
        }
    }

    private static void executeInit(boolean isDynamic, String pluginPackage, String pluginName) {
        try {
            // Remove the copy tag of the plugin name to obtain the actual resource directory
            final String pluginPath = pluginPackage + File.separatorChar + getRealPluginName(pluginName);
            if (!new File(pluginPath).exists()) {
                LOGGER.log(Level.WARNING, "Plugin directory {0} does not exist, so skip initializing {1}. ",
                        new String[]{pluginPath, pluginName});
                return;
            }
            doInitPlugin(
                    new Plugin(pluginName, pluginPath, isDynamic, ClassLoaderManager.createPluginClassLoader()));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Load plugin failed, plugin name: " + pluginName, ex);
        }
    }

    public static Map<String, Plugin> getPluginMap() {
        return PLUGIN_MAP;
    }

    private static void doInitPlugin(Plugin plugin) {
        loadPluginLibs(plugin);
        loadServiceLibs(plugin);
        PluginConfigManager.loadPluginConfigs(plugin);
        PluginServiceManager.initPluginServices(plugin);

        // For adaptation logic, the classloader needs to be added to the plugin class finder before bytecode
        // enhancement, otherwise the interceptor may not be found during bytecode enhancement
        ClassLoaderManager.getPluginClassFinder().addPluginClassLoader(plugin);

        // Select different bytecode enhancement installation types based on the plugin type
        if (plugin.isDynamic()) {
            ByteEnhanceManager.enhanceDynamicPlugin(plugin);
        } else {
            ByteEnhanceManager.enhanceStaticPlugin(plugin);
        }

        // Steps after the plugin is successfully loaded
        PLUGIN_MAP.put(plugin.getName(), plugin);
        PluginSchemaValidator.setDefaultVersion(plugin.getName());
        FrameworkEventCollector.getInstance().collectPluginsLoadEvent(plugin.getName());
        LOGGER.log(Level.INFO, "Load plugin:{0} successful.", plugin.getName());
    }

    /**
     * Construct ServiceClassLoader of plugin
     *
     * @param plugin plugin
     */
    private static void loadServiceLibs(Plugin plugin) {
        URL[] urls = toUrls(plugin.getName(), listJars(getServiceDir(plugin.getPath())));
        if (urls.length > 0) {
            plugin.setServiceClassLoader(new ServiceClassLoader(urls, plugin.getPluginClassLoader()));
        }
    }

    /**
     * Load all plugin packages
     *
     * @param plugin plugin
     */
    private static void loadPluginLibs(Plugin plugin) {
        for (File jar : listJars(getPluginDir(plugin.getPath()))) {
            processByJarFile(plugin.getName(), jar, true, getJarFileConsumer(plugin));
        }
    }

    private static JarFileConsumer getJarFileConsumer(Plugin plugin) {
        return new JarFileConsumer() {
            @Override
            public void consume(JarFile jarFile) {
                try {
                    plugin.getPluginClassLoader().appendUrl(new File(jarFile.getName()).toURI().toURL());
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.SEVERE, "Add plugin path to pluginClassLoader fail, exception: ", e);
                }
            }
        };
    }

    /**
     * Obtain the URL of all jar packages of the plugin, and verify the jar package and version
     *
     * @param pluginName plugin name
     * @param jars jars
     * @return jar package URL set
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
     * Get the url from a file
     *
     * @param file file
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
     * Convert the plugin package file to a jar package and then do the processing
     *
     * @param pluginName plugin name
     * @param jar plugin package file
     * @param ifCheckSchema whether to check the jar package schema
     * @param consumer jar consumer
     * @return process result
     */
    private static boolean processByJarFile(String pluginName, File jar, boolean ifCheckSchema,
            JarFileConsumer consumer) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jar);
            if (ifCheckSchema && !PluginSchemaValidator.checkSchema(pluginName, getRealPluginName(pluginName),
                    jarFile)) {
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
     * plugin-name#1 plugin-name#2 marks the copy of the plugin, shares the resource file, and obtains the actual plugin
     * name by separating the plugin name, which corresponds to the plugin directory name and the plugin identification
     * in the Manifest
     *
     * @param pluginName plugin name
     * @return actual plugin name
     */
    private static String getRealPluginName(String pluginName) {
        return pluginName.split("#")[0];
    }

    /**
     * Iterate through all jar packages in the directory, sorted by file name dictionary
     *
     * @param dir target directory
     * @return all jars
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
     * Gets the plugin package directory
     *
     * @param pluginPath plugin root directory
     * @return plugin package directory
     */
    private static File getPluginDir(String pluginPath) {
        return new File(pluginPath + File.separatorChar + PluginConstant.PLUGIN_DIR_NAME);
    }

    /**
     * Gets the plugin service package directory
     *
     * @param pluginPath plugin root directory
     * @return plugin service package directory
     */
    private static File getServiceDir(String pluginPath) {
        return new File(pluginPath + File.separatorChar + PluginConstant.SERVICE_DIR_NAME);
    }

    private static void closePluginLoaders(Plugin plugin) {
        try {
            ServiceClassLoader serviceClassLoader = plugin.getServiceClassLoader();
            if (serviceClassLoader != null) {
                serviceClassLoader.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to close ServiceClassLoader for plugin:{0}", plugin.getName());
        }
        try {
            PluginClassLoader pluginClassLoader = plugin.getPluginClassLoader();
            if (pluginClassLoader != null) {
                pluginClassLoader.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to close PluginClassLoader for plugin:{0}", plugin.getName());
        }
    }

    /**
     * Jar file consumer
     *
     * @since 2021-11-12
     */
    private interface JarFileConsumer {
        /**
         * Consume Jar file
         *
         * @param jarFile Jar file object
         */
        void consume(JarFile jarFile);
    }
}
