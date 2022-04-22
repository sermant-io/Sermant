/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config.utils;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.utils.ClassLoaderUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;

/**
 * 类工具类
 *
 * @author zhouss
 * @since 2022-04-08
 */
public class ClassUtils {
    private ClassUtils() {
    }

    /**
     * 通过指定类加载器加载类
     *
     * @param className   类全限定名
     * @param classLoader 被增强类的类加载器
     * @return 重定义后的类
     */
    public static Optional<Class<?>> defineClass(String className, ClassLoader classLoader) {
        if (classLoader == null || className == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(ClassLoaderUtils.defineClass(className, classLoader, ClassLoaderUtils
                .getClassResource(ClassLoader.getSystemClassLoader(), className)));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | IOException ex) {
            LoggerFactory.getLogger()
                .warning(String.format(Locale.ENGLISH, "Can not define class [%s], reason: [%s], try to load it!",
                    className, ex.getMessage()));

            // 有可能已经加载过了，直接用contextClassLoader.loadClass加载
            return loadClass(className, classLoader);
        }
    }

    /**
     * 加载类
     *
     * @param className   类全限定名
     * @param classLoader 类加载器
     * @return 已加载的类
     */
    public static Optional<Class<?>> loadClass(String className, ClassLoader classLoader) {
        if (classLoader == null || className == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(classLoader.loadClass(className));
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            LoggerFactory.getLogger()
                .warning(String.format(Locale.ENGLISH, "Can not define and load class [%s]!", className));
        }
        return Optional.empty();
    }
}
