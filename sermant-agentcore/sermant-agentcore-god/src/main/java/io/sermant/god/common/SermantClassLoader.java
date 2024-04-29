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

package io.sermant.god.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * SermantClassLoader, each Sermant has a separate classloader
 *
 * @author luanwenfei
 * @since 2023-05-22
 */
public class SermantClassLoader extends URLClassLoader {
    Collection<String> godClassList =
            new ArrayList<>(Arrays.asList("io.sermant.core.plugin.agent.interceptor.Interceptor",
                    "io.sermant.core.plugin.agent.entity.ExecuteContext",
                    "io.sermant.core.plugin.agent.adviser.AdviserInterface",
                    "io.sermant.core.plugin.agent.adviser.AdviserScheduler"));

    /**
     * Manages classes that have been loaded by the FrameworkClassLoader
     */
    private final Map<String, Class<?>> sermantClassMap = new HashMap<>();

    /**
     * parent cannot be null, which facilitates service governance scenarios
     *
     * @param urls urls
     */
    public SermantClassLoader(URL[] urls) {
        super(urls);
    }

    private Class<?> findSermantClass(String name) {
        if (!sermantClassMap.containsKey(name)) {
            try {
                sermantClassMap.put(name, findClass(name));
            } catch (ClassNotFoundException ignored) {
                sermantClassMap.put(name, null);
            }
        }
        return sermantClassMap.get(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = null;
            if (!name.startsWith("io.sermant.god") && !godClassList.contains(name)) {
                clazz = findSermantClass(name);
            }
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);

                // The classes loaded from itself are placed in the cache using the super.loadClass method of the
                // SermantClassLoader
                if (clazz != null && clazz.getClassLoader() == this) {
                    sermantClassMap.put(name, clazz);
                }
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    /**
     * Add a URL to SermantClassLoader
     *
     * @param url url
     */
    public void appendUrl(URL url) {
        this.addURL(url);
    }

    /**
     * Add URLs to SermantClassLoader
     *
     * @param urls urls
     */
    public void appendUrls(URL[] urls) {
        for (URL url : urls) {
            this.addURL(url);
        }
    }
}
