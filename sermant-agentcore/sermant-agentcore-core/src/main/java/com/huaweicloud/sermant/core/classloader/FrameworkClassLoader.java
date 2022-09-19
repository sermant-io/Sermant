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

import com.huaweicloud.sermant.core.common.CommonConstant;

import java.net.URL;
import java.net.URLClassLoader;
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
     * @param urls Url of sermant-agentcore-implement
     */
    public FrameworkClassLoader(URL[] urls) {
        super(urls);
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
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = null;

            // 对于core中已经加载的类则遵循双亲委派原则,其他类则破坏双亲委派机制
            if (name != null && !name.startsWith("com.huaweicloud.sermant.core")) {
                clazz = findFrameworkClass(name);
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
    public URL getResource(String name) {
        URL url = null;

        // 针对日志配置文件，定制化getResource方法，获取FrameworkClassloader下资源文件中的logback.xml
        if (CommonConstant.LOG_SETTING_FILE_NAME.equals(name)) {
            url = findResource(name);
        }
        if (url == null) {
            url = super.getResource(name);
        }
        return url;
    }
}
