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

package com.huaweicloud.sermant.god.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Sermant类加载器,每一个Sermant有一个独立的类加载器
 *
 * @author luanwenfei
 * @since 2023-05-22
 */
public class SermantClassLoader extends URLClassLoader {
    Collection<String> godClassList =
            new ArrayList<>(Arrays.asList("com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor",
                    "com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext",
                    "com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserInterface",
                    "com.huaweicloud.sermant.core.plugin.agent.adviser.AdviserScheduler"));

    /**
     * 对FrameClassLoader已经加载的类进行管理
     */
    private final Map<String, Class<?>> sermantClassMap = new HashMap<>();

    /**
     * parent 不可为 null，有利于服务治理场景
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
            if (!name.startsWith("com.huaweicloud.sermant.god") && !godClassList.contains(name)) {
                clazz = findSermantClass(name);
            }
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);

                // 通过SermantClassLoader的super.loadClass方法把从自身加载的类放入缓存
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
     * 向SermantClassLoader添加一个URL
     *
     * @param url url
     */
    public void appendUrl(URL url) {
        this.addURL(url);
    }

    /**
     * 向SermantClassLoader添加多个URL
     *
     * @param urls urls
     */
    public void appendUrls(URL[] urls) {
        for (URL url : urls) {
            this.addURL(url);
        }
    }
}
