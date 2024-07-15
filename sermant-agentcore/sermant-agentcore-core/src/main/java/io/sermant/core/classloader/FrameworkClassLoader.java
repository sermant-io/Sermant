/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.classloader;

import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.common.CommonConstant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The classloader of the core capabilities of framework
 *
 * @author luanwenfei
 * @since 2022-06-18
 */
public class FrameworkClassLoader extends URLClassLoader {
    /**
     * Manages classes that have been loaded by FrameworkClassLoader
     */
    private final Map<String, Class<?>> frameworkClassMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param urls Url of sermant-agentcore-implement
     * @param parent parent classloader
     */
    public FrameworkClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    private Class<?> findFrameworkClass(String name) {
        if (!frameworkClassMap.containsKey(name)) {
            try {
                frameworkClassMap.put(name, findClass(name));
            } catch (ClassNotFoundException ignored) {
                frameworkClassMap.put(name, null);
            }
        }
        return frameworkClassMap.get(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = null;

            // For classes already loaded in core, the parent delegation principle is followed, and other classes
            // break the parent delegation principle
            if (name != null) {
                clazz = findFrameworkClass(name);
            }
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);

                // Use the super.loadClass method of the FrameworkClassLoader to load classes from itself into the cache
                if (clazz != null && clazz.getClassLoader() == this) {
                    frameworkClassMap.put(name, clazz);
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

        // Customize the getResource method to obtain logback.xml in the resource file of FrameworkClassloader
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
    public Enumeration<URL> getResources(String name) throws IOException {
        // Due to class isolation, the StaticLoggerBinder does not obtain duplicate resources from the parent
        // classloader, but returns only the resources in the classloader
        if ("org/slf4j/impl/StaticLoggerBinder.class".equals(name)) {
            return findResources(name);
        }

        // Due to class isolation, the service loader does not obtain the service provider from the parent
        // classloader, but returns only the resources in the classloader
        if (name.startsWith("META-INF/services/")) {
            return findResources(name);
        }

        return super.getResources(name);
    }
}
