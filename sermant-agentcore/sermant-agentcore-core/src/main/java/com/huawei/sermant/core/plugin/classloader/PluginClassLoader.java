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

package com.huawei.sermant.core.plugin.classloader;

import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.plugin.agent.config.AgentConfig;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 插件类加载器，用于加载插件服务包
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class PluginClassLoader extends URLClassLoader {
    /**
     * 不优先使用PluginClassLoader加载的全限定名前缀
     */
    private final Set<String> ignoredPrefixes = ConfigManager.getConfig(AgentConfig.class).getIgnoredPrefixes();

    /**
     * 对ClassLoader内部已加载的Class的管理
     */
    private final Map<String, Class<?>> pluginClassMap = new HashMap<>();

    public PluginClassLoader(URL[] urls) {
        super(urls);
    }

    /**
     * 加载插件服务包中的类并维护
     *
     * @param name 全限定名
     * @return Class对象
     */
    private Class<?> loadPluginClass(String name) {
        if (!pluginClassMap.containsKey(name)) {
            try {
                pluginClassMap.put(name, findClass(name));
            } catch (ClassNotFoundException ignored) {
                pluginClassMap.put(name, null);
            }
        }
        return pluginClassMap.get(name);
    }

    private boolean ifExclude(String name) {
        for (String excludePrefix : ignoredPrefixes) {
            if (name.startsWith(excludePrefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = null;
            if (!ifExclude(name)) {
                clazz = loadPluginClass(name);
            }
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
