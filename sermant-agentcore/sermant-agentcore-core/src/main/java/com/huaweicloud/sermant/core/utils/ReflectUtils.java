/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 反射工具类
 *
 * @author zhouss
 * @since 2022-05-20
 */
public class ReflectUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 字段缓存
     */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 方法缓存 key: class#method(params) value: method
     */
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private ReflectUtils() {
    }

    /**
     * 反射调用无参方法
     *
     * @param target 目标方法
     * @param methodName 方法名
     * @return 结果
     */
    public static Optional<Object> invokeMethodWithNoneParameter(Object target, String methodName) {
        return invokeMethod(target, methodName, null, null);
    }

    /**
     * 反射调用方法
     *
     * @param target 目标方法
     * @param methodName 方法名
     * @param paramsType 参数类型
     * @param params 参数
     * @return 结果
     */
    public static Optional<Object> invokeMethod(Object target, String methodName, Class<?>[] paramsType,
        Object[] params) {
        if (methodName == null || target == null) {
            return Optional.empty();
        }
        final Optional<Method> method = findMethod(target.getClass(), methodName, paramsType);
        if (method.isPresent()) {
            return invokeMethod(target, method.get(), params);
        }
        return Optional.empty();
    }

    /**
     * 反射调用方法
     *
     * @param method 方法
     * @param target 目标对象
     * @param params 方法参数
     * @return 结果
     */
    public static Optional<Object> invokeMethod(Object target, Method method, Object[] params) {
        setAccessible(method);
        try {
            if (params == null) {
                return Optional.ofNullable(method.invoke(target));
            }
            return Optional.ofNullable(method.invoke(target, params));
        } catch (InvocationTargetException | IllegalAccessException ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Can not invoke method [%s] in class [%s], reason: %s",
                method.getName(), target.getClass().getName(), ex.getMessage()));
        }
        return Optional.empty();
    }

    private static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>[] paramsType) {
        if (clazz == null) {
            return Optional.empty();
        }
        final String methodKey = buildMethodKey(clazz, methodName, paramsType);
        try {
            Method method = METHOD_CACHE.get(methodKey);
            if (method != null) {
                return Optional.of(method);
            }
            method = clazz.getDeclaredMethod(methodName, paramsType);
            METHOD_CACHE.put(methodKey, method);
            return Optional.of(method);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null || clazz.getInterfaces().length > 0) {
                Optional<Method> method = findMethod(clazz.getSuperclass(), methodName, paramsType);
                if (method.isPresent()) {
                    return method;
                }
                for (Class<?> interfaceClass : clazz.getInterfaces()) {
                    method = findMethod(interfaceClass, methodName, paramsType);
                    if (method.isPresent()) {
                        return method;
                    }
                }
            } else {
                LOGGER.warning(String.format(Locale.ENGLISH, "Can not find method named [%s] from class [%s]",
                    methodName, clazz.getName()));
            }
        }
        return Optional.empty();
    }

    private static String buildMethodKey(Class<?> clazz, String methodName, Class<?>[] paramsType) {
        final String name = clazz.getName();
        final StringBuilder sb = new StringBuilder(name);
        sb.append("#").append(methodName).append("(");
        if (paramsType != null) {
            for (Class<?> paramType : paramsType) {
                sb.append(paramType.getName()).append(",");
            }
        }
        return sb.append(")").toString();
    }

    /**
     * 设置字段值
     *
     * @param target 对象
     * @param fieldName 字段
     * @param value 值
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        final Optional<Field> fieldOption = getField(target, fieldName);
        if (!fieldOption.isPresent()) {
            return;
        }
        final Field field = fieldOption.get();
        if (isFinalField(field)) {
            updateFinalModifierField(field);
        }
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                field.set(target, value);
            } catch (IllegalAccessException ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Set value for field [%s] failed! %s", fieldName,
                    ex.getMessage()));
            }
            return value;
        });
    }

    /**
     * 更新final 字段
     *
     * @param field 目标字段
     */
    public static void updateFinalModifierField(Field field) {
        final Field modifiersField = getField(Field.class, "modifiers");
        if (modifiersField != null) {
            setAccessible(field);
            try {
                modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Could not update final field named %s", field.getName()));
            }
        }
    }

    /**
     * 通过反射获取字段值
     *
     * @param target 目标对象
     * @param fieldName 字段名称
     * @return value
     */
    public static Optional<Object> getFieldValue(Object target, String fieldName) {
        if (target == null || fieldName == null) {
            return Optional.empty();
        }
        try {
            final Optional<Field> field = getField(target, fieldName);
            if (field.isPresent()) {
                return Optional.ofNullable(field.get().get(target));
            }
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                "Could not acquire the value of field %s", fieldName));
        }
        return Optional.empty();
    }

    /**
     * 判断当前字段是否为final
     *
     * @param field 字段
     * @return true 为final
     */
    private static boolean isFinalField(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    private static Optional<Field> getField(Object target, String fieldName) {
        return Optional.ofNullable(getField(target.getClass(), fieldName));
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        final Map<String, Field> cache = FIELD_CACHE.getOrDefault(clazz, new ConcurrentHashMap<>());
        Field field = cache.get(fieldName);
        try {
            if (field == null) {
                field = clazz.getDeclaredField(fieldName);
                cache.putIfAbsent(fieldName, setAccessible(field));
            }
        } catch (IllegalArgumentException | NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null) {
                return getField(clazz.getSuperclass(), fieldName);
            } else {
                LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                    "Could not find field named %s", fieldName));
            }
        } finally {
            FIELD_CACHE.put(clazz, cache);
        }
        return field;
    }

    private static <T extends AccessibleObject> T setAccessible(T object) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            object.setAccessible(true);
            return object;
        });
        return object;
    }
}
