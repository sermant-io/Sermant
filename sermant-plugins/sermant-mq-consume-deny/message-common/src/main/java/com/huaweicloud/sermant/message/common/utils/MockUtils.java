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

package com.huaweicloud.sermant.message.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于缓存增强类的元数据，并且反射调用mock实例的方法的一个工具类<br>
 *
 * @author yuzl 俞真龙
 * @since 2022-10-11
 */
public final class MockUtils {
    private static final Map<Method, Optional<Method>> METHODS = new ConcurrentHashMap<>();

    private MockUtils() {
    }

    /**
     * 调用方法
     *
     * @param mockObj   模拟obj
     * @param method    方法
     * @param arguments 参数
     * @return {@link Object}
     */
    public static Optional<Object> invokeMethod(Object mockObj, Method method, Object[] arguments) {
        Optional<Method> methodOptional = METHODS.computeIfAbsent(method, methodsMapKey -> getMethod(mockObj,
                methodsMapKey));
        if (!methodOptional.isPresent()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(methodOptional.get().invoke(mockObj, arguments));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return Optional.empty();
        }
    }

    private static Optional<Method> getMethod(Object mockObj, Method method) {
        try {
            return Optional
                .ofNullable(mockObj.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
