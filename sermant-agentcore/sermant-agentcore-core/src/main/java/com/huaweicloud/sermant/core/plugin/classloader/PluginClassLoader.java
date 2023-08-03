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

import com.huaweicloud.sermant.core.common.CommonConstant;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

/**
 * 加载插件主模块的类加载器
 *
 * @author luanwenfei
 * @since 2023-04-27
 */
public class PluginClassLoader extends URLClassLoader {
    private final HashMap<Long, ClassLoader> tmpLoader = new HashMap<>();

    /**
     * 构造方法
     *
     * @param urls Url of sermant-xxx-plugin
     * @param parent parent classloader
     */
    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
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
        Class<?> clazz = null;

        try {
            clazz = super.loadClass(name, resolve);
        } catch (ClassNotFoundException ignored) {
            // ignored
        }

        // If not found in parent, try to load using the context class loader
        if (clazz == null && !isSermantClass(name)) {
            ClassLoader loader = tmpLoader.get(Thread.currentThread().getId());

            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
            }

            // Ensure the loader is not the same as this class loader to avoid StackOverflow
            if (loader != null && !this.equals(loader)) {
                try {
                    clazz = loader.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // Class not found in the context class loader
                }
            }
        }

        // If still not found, throw ClassNotFoundException
        if (clazz == null) {
            throw new ClassNotFoundException("Sermant pluginClassLoader can not load class: " + name);
        }

        // Resolve the class if needed
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    /**
     * 设置局部临时类加载器
     *
     * @param loader 类加载器
     */
    public void setTmpLoader(ClassLoader loader) {
        tmpLoader.put(Thread.currentThread().getId(), loader);
    }

    /**
     * 清楚局部临时类加载器
     *
     * @return 被移除的类加载器
     */
    public ClassLoader removeTmpLoader() {
        return tmpLoader.remove(Thread.currentThread().getId());
    }

    private boolean isSermantClass(String name) {
        for (String classPrefix : CommonConstant.LOAD_PREFIXES) {
            if (name.startsWith(classPrefix)) {
                return true;
            }
        }
        return false;
    }
}
