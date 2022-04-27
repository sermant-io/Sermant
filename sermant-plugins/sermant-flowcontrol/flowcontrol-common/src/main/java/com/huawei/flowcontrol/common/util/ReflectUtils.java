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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.ClassLoaderUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射工具类
 *
 * @author zhouss
 * @since 2022-03-04
 */
public class ReflectUtils {
    /**
     * 反射调用方法缓存
     */
    private static final Map<String, Optional<Method>> REFLECT_METHOD_CACHE = new ConcurrentHashMap<>();

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

    /**
     * 反射调用目标方法
     *
     * @param target         目标类
     * @param methodName     方法名
     * @param parameterTypes 方法参数类型
     * @param args           方法参数
     * @return 调用结果
     */
    public static Optional<Object> invokeTargetMethod(Object target, String methodName, Class<?>[] parameterTypes,
        Object[] args) {
        if (target == null || methodName == null) {
            return Optional.empty();
        }
        final Optional<Method> method = REFLECT_METHOD_CACHE
            .computeIfAbsent(formatReflectKey(target, methodName), fn -> {
                try {
                    return Optional.of(target.getClass().getDeclaredMethod(methodName, parameterTypes));
                } catch (NoSuchMethodException ex) {
                    LoggerFactory.getLogger().warning(String.format(Locale.ENGLISH, "No such method [%s] at class [%s]",
                        methodName, target.getClass().getName()));
                }
                return Optional.empty();
            });
        if (method.isPresent()) {
            try {
                return Optional.ofNullable(method.get().invoke(target, args));
            } catch (InvocationTargetException | IllegalAccessException ex) {
                LoggerFactory.getLogger().warning(String.format(Locale.ENGLISH,
                    "Can not invoker method [%s], reason: %s", methodName, ex.getMessage()));
            }
        }
        return Optional.empty();
    }

    private static String formatReflectKey(Object target, String methodName) {
        return String.format(Locale.ENGLISH, "%s_%s", target.getClass().getName(), methodName);
    }
}
