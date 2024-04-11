/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.classloader;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.Plugin;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to find the corresponding class and resource from multiple PluginClassLoaders
 *
 * @author luanwenfei
 * @since 2023-05-30
 */
public class PluginClassFinder {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Map<String, PluginClassLoader> pluginClassLoaderMap = new HashMap<>();

    /**
     * Cache pluginClassLoader
     *
     * @param plugin plugin
     */
    public void addPluginClassLoader(Plugin plugin) {
        pluginClassLoaderMap.put(plugin.getName(), plugin.getPluginClassLoader());
    }

    /**
     * Remove pluginClassLoader
     *
     * @param plugin plugin
     */
    public void removePluginClassLoader(Plugin plugin) {
        pluginClassLoaderMap.remove(plugin.getName());
    }

    /**
     * Load the corresponding class name under the Sermant search path
     *
     * @param name class name
     * @return Class<?>
     * @throws ClassNotFoundException If the class is not found in any of pluginClassLoaders, an exception is thrown
     * that the class is not found
     */
    public Class<?> loadSermantClass(String name) throws ClassNotFoundException {
        for (PluginClassLoader pluginClassLoader : pluginClassLoaderMap.values()) {
            try {
                Class<?> clazz = pluginClassLoader.loadSermantClass(name);
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.WARNING, "load sermant class failed, msg is {0}", e.getMessage());
            }
        }
        throw new ClassNotFoundException("Can not load class in pluginClassLoaders: " + name);
    }

    /**
     * Find the resource corresponding to the resource path under the Sermant search path
     *
     * @param path resource path
     * @return URL of the resource
     */
    public Optional<URL> findSermantResource(String path) {
        for (PluginClassLoader pluginClassLoader : pluginClassLoaderMap.values()) {
            URL url = pluginClassLoader.findResource(path);
            if (url != null) {
                return Optional.of(url);
            }
        }
        return Optional.empty();
    }
}
