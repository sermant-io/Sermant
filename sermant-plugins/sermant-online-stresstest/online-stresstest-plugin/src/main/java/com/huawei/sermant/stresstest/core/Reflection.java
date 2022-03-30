/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.core;

import com.huawei.sermant.core.common.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 反射工具类
 *
 * @author yiwei
 * @since 2021-10-21
 */
@SuppressWarnings({"checkstyle:IllegalCatch", "checkstyle:RegexpSingleline", "checkstyle:RegexpSinglelineJava"})
public class Reflection {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private Reflection() {
    }

    /**
     * 方法执行。
     *
     * @param methodName 方法名
     * @param instance 实例
     * @param values values
     * @return 返回值。
     */
    public static Optional<Object> invokeDeclared(String methodName, Object instance, Object... values) {
        return getDeclaredMethod(methodName, instance.getClass(), values)
            .map(method -> invokeDeclared(method, instance, values));
    }

    private static Object invokeDeclared(Method method, Object instance, Object... values) {
        accessible(method);
        try {
            return method.invoke(instance, values);
        } catch (Throwable e) {
            LOGGER.severe(String.format("Cannot execute method %s for reason %s.", method.getName(), e.getMessage()));
            return null;
        }
    }

    /**
     * static方法执行。
     *
     * @param methodName 方法名
     * @param className 实例
     * @param values values
     * @return 返回值。
     */
    public static Optional<Object> invokeStaticDeclared(String methodName, String className, Object... values) {
        return getDeclaredMethod(methodName, className, values).map(method -> invokeDeclared(method, null, values));
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
                setDeclaredValue(field, instance, prefix + value, false);
            }
        });
    }

    /**
     * getValue of the specific private field.
     *
     * @param fieldName field name
     * @param instance instance of clazzName
     * @param <T> 泛型
     * @return value
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getDeclaredValue(String fieldName, Object instance) {
        return getDeclaredField(fieldName, instance).map(field -> {
            accessible(field);
            try {
                return (T)field.get(instance);
            } catch (IllegalAccessException e) {
                LOGGER.severe(
                    String.format("Cannot get field %s of class %s.", fieldName, instance.getClass().getName()));
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
     * @param enableFinal 支持修改final字段
     */
    public static void setDeclaredValue(String fieldName, Object instance, Object value, boolean enableFinal) {
        getDeclaredField(fieldName, instance).ifPresent(field -> setDeclaredValue(field, instance, value, enableFinal));
    }

    /**
     * setValue for the specific private field.
     *
     * @param fieldName field name
     * @param instance instance of clazzName
     * @param value the value to be set
     */
    public static void setDeclaredValue(String fieldName, Object instance, Object value) {
        setDeclaredValue(fieldName, instance, value, false);
    }

    private static void setDeclaredValue(Field field, Object instance, Object value, boolean enableFinal) {
        String fieldName = field.getName();
        accessible(field);
        try {
            if (enableFinal) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.set(instance, value);
            LOGGER.fine(String.format("Set value = %s to field %s.", value, fieldName));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.severe(String.format("Cannot set field %s of class %s.", fieldName, instance.getClass().getName()));
        }
    }

    private static String getDeclaredValueOfString(Field field, Object instance) {
        String fieldName = field.getName();
        accessible(field);
        try {
            Object value = field.get(instance);
            if (value instanceof String) {
                return (String)value;
            }
        } catch (IllegalAccessException e) {
            LOGGER.severe(String.format("Cannot set field %s of class %s.", fieldName, instance.getClass().getName()));
        }
        return "";
    }

    private static Optional<Method> getDeclaredMethod(String methodName, String className, Object... values) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            return getDeclaredMethod(methodName, clazz, values);
        } catch (ClassNotFoundException e) {
            LOGGER.severe(String.format("Cannot load %s.", className));
        }
        return Optional.empty();
    }

    private static Optional<Method> getDeclaredMethod(String methodName, Class<?> clazz, Object... value) {
        List<Class<?>> params = getParameterTypes(value);
        Method method = null;
        try {
            method = getDeclaredMethodOneTime(methodName, clazz, params);
        } catch (NoSuchMethodException e) {
            while (clazz.getSuperclass() != Object.class) {
                try {
                    method = getDeclaredMethodOneTime(methodName, clazz.getSuperclass(), params);
                } catch (NoSuchMethodException ignored) {
                    // 故意留白
                }
            }
        }
        return Optional.ofNullable(method);
    }

    private static Method getDeclaredMethodOneTime(String methodName, Class<?> clazz, List<Class<?>> params)
        throws NoSuchMethodException {
        if (params.isEmpty()) {
            return clazz.getDeclaredMethod(methodName);
        } else {
            return clazz.getDeclaredMethod(methodName, params.toArray(new Class[0]));
        }
    }

    /**
     * 获取参数列表的类型。
     *
     * @param values 参数列表
     * @return 类型队列。
     */
    private static List<Class<?>> getParameterTypes(Object... values) {
        List<Class<?>> list = new ArrayList<>();
        for (Object object : values) {
            if (object == null) {
                return Collections.emptyList();
            } else {
                list.add(object.getClass());
            }
        }
        return list;
    }

    private static Optional<Field> getDeclaredField(String fieldName, Object instance) {
        Class<?> clazz = instance.getClass();
        try {
            return Optional.of(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == Object.class) {
                    break;
                }
                try {
                    return Optional.of(clazz.getDeclaredField(fieldName));
                } catch (NoSuchFieldException ignored) {
                    // 故意留白
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 使改对象可访问
     *
     * @param object 待修改对象
     */
    public static void accessible(AccessibleObject object) {
        if (!object.isAccessible()) {
            object.setAccessible(true);
        }
    }
}
