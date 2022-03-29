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

package com.huawei.flowcontrol.common.util;

import com.huawei.sermant.core.utils.ClassLoaderUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * 反射工具类
 *
 * @author zhouss
 * @since 2022-03-04
 */
public class ReflectUtils {
    private ReflectUtils() {
    }

    /**
     * 加载宿主类
     *
     * @param className 宿主全限定类名
     * @return 宿主类
     */
    public static Optional<Class<?>> defineClass(String className) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            return Optional.ofNullable(ClassLoaderUtils.defineClass(className, contextClassLoader,
                ClassLoaderUtils.getClassResource(ClassLoader.getSystemClassLoader(), className)));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | IOException e) {
            // 有可能已经加载过了，直接用contextClassLoader.loadClass加载
            try {
                return Optional.ofNullable(contextClassLoader.loadClass(className));
            } catch (ClassNotFoundException ex) {
                return Optional.empty();
            }
        }
    }
}
