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

package com.huaweicloud.sermant.core.config.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Field tools of configuration system
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class ConfigFieldUtil {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * The minimal length of a boolean property name in a get or set method
     */
    private static final int FIELD_NAME_MIN_LENGTH = 3;

    /**
     * The fist letter subscript of a boolean property name in a get or set method
     */
    private static final int FIELD_NAME_CHECK_INDEX = 2;

    /**
     * BOOLEAN_FUNCTION_PREFIX_LOWERCASE
     */
    private static final String BOOLEAN_FUNCTION_PREFIX_LOWERCASE = "is";

    /**
     * BOOLEAN_FUNCTION_PREFIX_UPPERCASE
     */
    private static final String BOOLEAN_FUNCTION_PREFIX_UPPERCASE = "Is";

    private ConfigFieldUtil() {
    }

    /**
     * Set the value, look for {@code setter} calls first, try direct assignment if none exists
     * <p>Therefore, the property value of the configuration object is required to have the corresponding {@code
     * setter}, or the property value is required to be public
     *
     * @param obj The object to which the value is set
     * @param field The field to be set
     * @param value The value of the field to be set
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
     * Get by property name {@code setter}
     *
     * @param cls Configuration object class
     * @param fieldName field name
     * @param type class type of attribute
     * @return setter method
     */
    private static Method getSetter(Class<?> cls, String fieldName, Class<?> type) {
        final String setterName;
        if ((type == boolean.class || type == Boolean.class) && fieldName.length() >= FIELD_NAME_MIN_LENGTH
                && checkFieldName(fieldName)) {
            setterName = "set" + fieldName.substring(FIELD_NAME_CHECK_INDEX);
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

    /**
     * Get attribute object
     *
     * @param obj Target object
     * @param field field
     * @return attribute object
     */
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
        if ((type == boolean.class || type == Boolean.class) && fieldName.length() >= FIELD_NAME_MIN_LENGTH
                && checkFieldName(fieldName)) {
            getterName = BOOLEAN_FUNCTION_PREFIX_LOWERCASE + fieldName.substring(FIELD_NAME_CHECK_INDEX);
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

    private static boolean checkFieldName(String fieldName) {
        return (fieldName.startsWith(BOOLEAN_FUNCTION_PREFIX_LOWERCASE) || fieldName.startsWith(
                BOOLEAN_FUNCTION_PREFIX_UPPERCASE))
                && fieldName.charAt(FIELD_NAME_CHECK_INDEX) >= 'A'
                && fieldName.charAt(FIELD_NAME_CHECK_INDEX) <= 'Z';
    }
}
