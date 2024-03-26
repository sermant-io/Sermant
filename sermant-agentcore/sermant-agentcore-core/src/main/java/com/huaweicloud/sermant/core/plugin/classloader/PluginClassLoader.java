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
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.agent.config.AgentConfig;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 加载插件主模块的类加载器
 *
 * @author luanwenfei
 * @since 2023-04-27
 */
public class PluginClassLoader extends URLClassLoader {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final HashMap<Long, ClassLoader> localLoader = new HashMap<>();

    /**
     * 是否允许使用线程上下文类加载器
     */
    private final boolean useContextLoader;

    /**
     * 对ClassLoader内部已加载的Class的管理
     */
    private final Map<String, Class<?>> pluginClassMap = new HashMap<>();

    /**
     * 构造方法
     *
     * @param urls 需要被该类加载器加载类所在lib的URL
     * @param parent 双亲类加载器
     */
    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        useContextLoader = ConfigManager.getConfig(AgentConfig.class).isUseContextLoader();
    }

    /**
     * 加载插件类并缓存
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

    /**
     * 向类加载器中添加类的搜索路径
     *
     * @param url 搜索路径
     */
    public void appendUrl(URL url) {
        this.addURL(url);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = loadPluginClass(name);

            // 自身无法加载类，则通过Sermant搜索路径中加载
            if (clazz == null) {
                try {
                    clazz = super.loadClass(name, resolve);
                } catch (ClassNotFoundException e) {
                    // 捕获类找不到的异常，下一步会进入localLoader中去加载类
                    // ignored
                    LOGGER.log(Level.FINE, "Load class failed, msg is {0}", e.getMessage());
                }
            }

            // 无法从Sermant搜索路径中找到类，则尝试通过线程绑定的局部类加载器加载
            if (clazz == null) {
                ClassLoader loader = localLoader.get(Thread.currentThread().getId());

                if (loader == null && useContextLoader) {
                    loader = Thread.currentThread().getContextClassLoader();
                }

                // 确保局部类加载器不是当前类加载器，否则会stackoverflow
                if (loader != null && !this.equals(loader) && !(loader instanceof ServiceClassLoader)) {
                    try {
                        clazz = loader.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // 无法找到类，忽略，后续抛出异常
                        LOGGER.log(Level.FINE, "Load class failed, msg is {0}", e.getMessage());
                    }
                }
            }

            // 如果无法找到类，则抛出异常
            if (clazz == null) {
                throw new ClassNotFoundException("Sermant pluginClassLoader can not load class: " + name);
            }

            // 如果有需要则解析该类
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    /**
     * 只通过Sermant自身的搜索路径加载类，不利用局部类加载器，否则会stackoverflow
     *
     * @param name 类名
     * @return Class<?>
     * @throws ClassNotFoundException 无法通过类加载
     */
    public Class<?> loadSermantClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = loadPluginClass(name);

            if (clazz == null) {
                try {
                    clazz = super.loadClass(name, false);
                } catch (ClassNotFoundException e) {
                    // 无法找到类，忽略，后续抛出异常
                    LOGGER.log(Level.WARNING, "load sermant class failed, msg is {0}", e.getMessage());
                }
            }

            // 如果无法找到类，则抛出异常
            if (clazz == null) {
                throw new ClassNotFoundException("Sermant pluginClassLoader can not load class: " + name);
            }
            return clazz;
        }
    }

    /**
     * 设置局部类加载器
     *
     * @param loader 类加载器
     */
    public void setLocalLoader(ClassLoader loader) {
        localLoader.put(Thread.currentThread().getId(), loader);
    }

    /**
     * 清除局部类加载器
     */
    public void removeLocalLoader() {
        localLoader.remove(Thread.currentThread().getId());
    }
}