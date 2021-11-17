/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 统一配置系统的字段工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
 */
public class ConfigFieldUtil {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

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
            if (setter == null) {
                field.set(obj, value);
            } else {
                setter.invoke(obj, value);
            }
        } catch (IllegalAccessException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Cannot access field [%s] of [%s].", field.getName(), field.getDeclaringClass().getName()));
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

    public static Object getField(Object obj, Field field) {
        try {
            final Method getter = getGetter(field.getDeclaringClass(), field.getName(), field.getType());
            if (getter == null) {
                return field.get(obj);
            } else {
                return getter.invoke(obj);
            }
        } catch (IllegalAccessException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Cannot access field [%s] of [%s].", field.getName(), field.getDeclaringClass().getName()));
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
}
