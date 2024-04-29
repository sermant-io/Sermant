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

package io.sermant.core.plugin.classloader;

import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.common.CommonConstant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * ServiceClassLoader, used to load the plugin service package
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class ServiceClassLoader extends URLClassLoader {
    /**
     * Manages the loaded classes in the ServiceClassLoader
     */
    private final Map<String, Class<?>> serviceClassMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param urls Url of plugin service package
     * @param parent parent classloader
     */
    public ServiceClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Load and maintain the classes in the plugin service package
     *
     * @param name class full qualified name
     * @return Class object
     */
    private Class<?> loadServiceClass(String name) {
        if (!serviceClassMap.containsKey(name)) {
            try {
                serviceClassMap.put(name, findClass(name));
            } catch (ClassNotFoundException ignored) {
                serviceClassMap.put(name, null);
            }
        }
        return serviceClassMap.get(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = loadServiceClass(name);
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);

                // Put the classes loaded from itself into the cache
                if (clazz != null && clazz.getClassLoader() == this) {
                    serviceClassMap.put(name, clazz);
                }
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    @Override
    public URL getResource(String name) {
        URL url = null;

        // Customize the getResource method for the log configuration file. First get agent/config/logback.xml,
        // then logback.xml in the resource file under the PluginClassloader
        if (CommonConstant.LOG_SETTING_FILE_NAME.equals(name)) {
            File logSettingFile = BootArgsIndexer.getLogSettingFile();
            if (logSettingFile.exists() && logSettingFile.isFile()) {
                try {
                    url = logSettingFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    url = findResource(name);
                }
            } else {
                url = findResource(name);
            }
        }
        if (url == null) {
            url = super.getResource(name);
        }
        return url;
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
