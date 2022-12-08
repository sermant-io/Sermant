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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 公共类加载器
 *
 * @author lilai
 * @since 2022-11-25
 */
public class CommonClassLoader extends URLClassLoader {

    /**
     * 对CommonClassLoader已经加载的类进行管理
     */
    private final Map<String, Class<?>> commonClassMap = new HashMap<>();

    /**
     * 构造方法，CommonClassLoader默认以AppClassloader为父类加载器
     *
     * @param urls Url of sermant-common
     */
    public CommonClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 破坏双亲委派，先从自身加载，再从父类加载，保持Sermant插件服务的第三方依赖和宿主依赖隔离
            Class<?> clazz = loadCommonClass(name);

            if (clazz == null) {
                clazz = getParent().loadClass(name);
            }

            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    /**
     * 加载公共第三方依赖目录下的类
     *
     * @param name 类名
     * @return Class<?>
     */
    private Class<?> loadCommonClass(String name) {
        if (!commonClassMap.containsKey(name)) {
            try {
                commonClassMap.put(name, findClass(name));
            } catch (ClassNotFoundException ignored) {
                // 若自身无法加载则把类名放入缓存，后续不再尝试加载
                commonClassMap.put(name, null);
            }
        }
        return commonClassMap.get(name);
    }
}
