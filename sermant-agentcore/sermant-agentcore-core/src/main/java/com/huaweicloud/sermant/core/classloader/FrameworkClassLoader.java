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

package com.huaweicloud.sermant.core.classloader;

import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.CommonConstant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 框架核心能力实现的类加载器
 *
 * @author luanwenfei
 * @since 2022-06-18
 */
public class FrameworkClassLoader extends URLClassLoader {
    /**
     * 对FrameClassLoader已经加载的类进行管理
     */
    private final Map<String, Class<?>> frameworkClassMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param urls   Url of sermant-agentcore-implement
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

            // 对于core中已经加载的类则遵循双亲委派原则,其他类则破坏双亲委派机制
            if (name != null && !name.startsWith("com.huaweicloud.sermant.core")) {
                clazz = findFrameworkClass(name);
            }
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);

                // 通过FrameworkClassLoader的super.loadClass方法把从自身加载的类放入缓存
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

        // 针对日志配置文件，定制化getResource方法，获取FrameworkClassloader下资源文件中的logback.xml
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
        // 由于类隔离的原因针对StaticLoggerBinder不再通过父类加载器获取重复资源，只返回加载器内的资源
        if ("org/slf4j/impl/StaticLoggerBinder.class".equals(name)) {
            return findResources(name);
        }
        return super.getResources(name);
    }
}
