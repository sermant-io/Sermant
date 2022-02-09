/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.config.utils;

import com.huawei.sermant.core.common.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一配置系统的字段工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class ConfigFieldUtil {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 设置值，优先查找{@code setter}调用，不存在时尝试直接赋值
     * <p>因此，要求配置对象的属性值需要拥有相应的{@code setter}，或者要求改属性值是公有的
     *
     * @param obj   被设置值的对象
     * @param field 被设置的字段
     * @param value 被设置的字段值
     */
    public static void setField(Object obj, Field field, Object value) {
        try {
            final Method setter = getSetter(field.getDeclaringClass(), field.getName(), field.getType());
            if (checkMethod(setter, field)) {
                setter.invoke(obj, value);
            } else {
                field.set(obj, value);
            }
        } catch (IllegalAccessException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Cannot access field [%s] of [%s], try forced set.",
                    field.getName(), field.getDeclaringClass().getName()));
            forceSet(obj, field, value);
        } catch (InvocationTargetException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Failed to set field [%s] of [%s].", field.getName(), field.getDeclaringClass().getName()));
        }
    }

    /**
     * 通过属性名称获取{@code setter}
     *
     * @param cls       配置对象类
     * @param fieldName 属性名称
     * @param type      属性类型
     * @return setter方法
     */
    private static Method getSetter(Class<?> cls, String fieldName, Class<?> type) {
        final String setterName;
        if ((type == boolean.class || type == Boolean.class) &&
                fieldName.length() >= 3 && (fieldName.startsWith("is") || fieldName.startsWith("Is")) &&
                fieldName.charAt(2) >= 'A' && fieldName.charAt(2) <= 'Z') {
            setterName = "set" + fieldName.substring(2);
        } else {
            final char head = fieldName.charAt(0);
            setterName = "set" + (
                    (head >= 'a' && head <= 'z') ? ((char) (head + 'A' - 'a')) + fieldName.substring(1) : fieldName);
        }
        try {
            return cls.getMethod(setterName, type);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void forceSet(Object obj, Field field, Object value) {
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            final int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers | Modifier.PUBLIC & ~Modifier.FINAL);
            field.set(obj, value);
            modifiersField.setInt(field, modifiers);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Missing modifiers field [%s] of [%s]. ", field.getName(), field.getDeclaringClass().getName()));
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Forced set field [%s] of [%s] failed.", field.getName(), field.getDeclaringClass().getName()));
        }
    }

    public static Object getField(Object obj, Field field) {
        try {
            final Method getter = getGetter(field.getDeclaringClass(), field.getName(), field.getType());
            if (checkMethod(getter, field)) {
                return getter.invoke(obj);
            } else {
                return field.get(obj);
            }
        } catch (IllegalAccessException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Cannot access field [%s] of [%s].", field.getName(), field.getDeclaringClass().getName()));
            return forceGet(obj, field);
        } catch (InvocationTargetException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Failed to get field [%s] of [%s].", field.getName(), field.getDeclaringClass().getName()));
        }
        return null;
    }

    private static Method getGetter(Class<?> cls, String fieldName, Class<?> type) {
        final String getterName;
        if ((type == boolean.class || type == Boolean.class) &&
                fieldName.length() >= 3 && (fieldName.startsWith("is") || fieldName.startsWith("Is")) &&
                fieldName.charAt(2) >= 'A' && fieldName.charAt(2) <= 'Z') {
            getterName = "is" + fieldName.substring(2);
        } else {
            final char head = fieldName.charAt(0);
            getterName = "get" + (
                    (head >= 'a' && head <= 'z') ? ((char) (head + 'A' - 'a')) + fieldName.substring(1) : fieldName);
        }
        try {
            return cls.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Object forceGet(Object obj, Field field) {
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            final int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers | Modifier.PUBLIC & ~Modifier.FINAL);
            final Object res = field.get(obj);
            modifiersField.setInt(field, modifiers);
            return res;
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Missing modifiers field [%s] of [%s]. ", field.getName(), field.getDeclaringClass().getName()));
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Forced get field [%s] of [%s] failed.", field.getName(), field.getDeclaringClass().getName()));
        }
        return null;
    }

    private static boolean checkMethod(Method method, Field field) {
        return method != null && Modifier.isStatic(method.getModifiers()) == Modifier.isStatic(field.getModifiers());
    }
}
