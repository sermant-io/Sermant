/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.plugin.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 插件类加载器，用于加载插件服务包
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class PluginClassLoader extends URLClassLoader {
    public PluginClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz == null) {
                try {
                    clazz = findClass(name); // 优先使用PluginClassLoader加载未知依赖
                } catch (ClassNotFoundException e) {
                    clazz = super.loadClass(name, resolve);
                }
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }
}
