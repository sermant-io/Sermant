/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.core;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 反射工具类
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class Reflection {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 方法执行。
     *
     * @param methodName 方法名
     * @param instance 实例
     * @return 返回值。
     */
    public static Optional<Object> invokeDeclared(String methodName, Object instance) {
        if (instance == null) {
            return Optional.empty();
        }
        Class<?> clazz = instance.getClass();
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            LOGGER.severe(String.format("Cannot find method %s.", methodName));
            return Optional.empty();
        }
        return invokeDeclared(method, instance);
    }

    private static Optional<Object> invokeDeclared(Method method, Object instance) {
        accessible(method);
        try {
            return Optional.ofNullable(method.invoke(instance));
        } catch (Throwable e) {
            LOGGER.severe(String.format("Cannot execute method %s for reason %s.", method.getName(),
                    e.getMessage()));
            return Optional.empty();
        }
    }

    /**
     * add prefix for the specific private field.
     *
     * @param fieldName field name
     * @param instance instance of clazzName
     * @param prefix the prefix value to be add
     */
    public static void addPrefixOnDeclaredField(String fieldName, Object instance, String prefix) {
        getDeclaredField(fieldName, instance).ifPresent(field -> {
            String value = getDeclaredValueOfString(field, instance);
            if (!Tester.isTestPrefix(value, prefix)) {
                setDeclaredValue(field, instance, prefix + value);
            }
        });
    }

    /**
     * getValue of the specific private field.
     *
     * @param fieldName field name
     * @param instance instance of clazzName
     * @return value
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getDeclaredValue(String fieldName, Object instance) {
        return getDeclaredField(fieldName, instance).map(field -> {
            accessible(field);
            try {
                return (T) field.get(instance);
            } catch (IllegalAccessException e) {
                LOGGER.severe(String.format("Cannot get field %s of class %s.", fieldName,
                        instance.getClass().getName()));
                return null;
            }
        });
    }

    /**
     * setValue for the specific private field.
     *
     * @param fieldName field name
     * @param instance instance of clazzName
     * @param value the value to be set
     */
    public static void setDeclaredValue(String fieldName, Object instance, Object value) {
        getDeclaredField(fieldName, instance).ifPresent(field -> setDeclaredValue(field, instance, value));
    }

    private static void setDeclaredValue(Field field, Object instance, Object value) {
        String fieldName = field.getName();
        accessible(field);
        try {
            field.set(instance, value);
            LOGGER.fine(String.format("Set value = %s to field %s.", value, fieldName));
        } catch (IllegalAccessException e) {
            LOGGER.severe(String.format("Cannot set field %s of class %s.", fieldName,
                    instance.getClass().getName()));
        }
    }

    private static String getDeclaredValueOfString(Field field, Object instance) {
        String fieldName = field.getName();
        accessible(field);
        try {
            Object value = field.get(instance);
            if (value instanceof String) {
                return (String) value;
            }
        } catch (IllegalAccessException e) {
            LOGGER.severe(String.format("Cannot set field %s of class %s.", fieldName,
                    instance.getClass().getName()));
        }
        return "";
    }

    private static Optional<Field> getDeclaredField(String fieldName, Object instance) {
        Class<?> clazz = instance.getClass();
        try {
            return Optional.of(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            while (clazz != null) {
                try {
                    return Optional.of(clazz.getDeclaredField(fieldName));
                } catch (NoSuchFieldException ignored) {
                    // 故意留白
                }
                clazz = clazz.getSuperclass();
            }
        }
        return Optional.empty();
    }

    private static void accessible(AccessibleObject object) {
        if (!object.isAccessible()) {
            object.setAccessible(true);
        }
    }
}
