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

package io.sermant.router.common.utils;

import io.sermant.core.common.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reflection tool class
 *
 * @author provenceee
 * @since 2022-02-07
 */
public class ReflectUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<String, AccessibleObject> ACCESSIBLE_OBJECT_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Optional<Method>> METHOD_MAP = new ConcurrentHashMap<>();

    private static final int EXTRA_LENGTH_FOR_METHOD_KEY = 3;

    private ReflectUtils() {
    }

    /**
     * Get private field values
     *
     * @param obj Object
     * @param fieldName The name of the field
     * @return Private field values
     */
    public static Optional<Object> getFieldValue(Object obj, String fieldName) {
        return io.sermant.core.utils.ReflectUtils.getFieldValue(obj, fieldName);
    }

    /**
     * Get the permission check class
     *
     * @param object Permission check object
     * @param <T> Permission check object
     * @return Returns the object after setAccessible(true).
     */
    public static <T extends AccessibleObject> T getAccessibleObject(T object) {
        return (T) ACCESSIBLE_OBJECT_MAP.computeIfAbsent(object.toString(), key ->
                AccessController.doPrivileged((PrivilegedAction<T>) () -> {
                    if (!object.isAccessible()) {
                        object.setAccessible(true);
                    }
                    return object;
                }));
    }

    /**
     * Reflection calls a parameterless method and returns a string
     *
     * @param obj Object
     * @param name Method Name
     * @return The return value of the method execution
     */
    public static String invokeWithNoneParameterAndReturnString(Object obj, String name) {
        Object result = invokeWithNoneParameter(obj, name);
        return result == null ? null : String.valueOf(result);
    }

    /**
     * Reflection calls the parameter-free method
     *
     * @param obj Object
     * @param name Method Name
     * @return The return value of the method execution
     */
    public static Object invokeWithNoneParameter(Object obj, String name) {
        return invoke(obj.getClass(), obj, name, null, null).orElse(null);
    }

    /**
     * Reflection calls a method that has a parameter
     *
     * @param obj Object
     * @param name method name
     * @param parameter Parameter
     * @param parameterClass Parameter type
     * @return Value
     */
    public static Object invokeWithParameter(Object obj, String name, Object parameter, Class<?> parameterClass) {
        return invoke(obj.getClass(), obj, name, parameter, parameterClass).orElse(null);
    }

    private static Optional<Object> invoke(Class<?> invokeClass, Object obj, String name, Object parameter,
            Class<?> parameterClass) {
        Optional<Method> method = METHOD_MAP.computeIfAbsent(buildMethodKey(invokeClass, name, parameterClass), key -> {
            try {
                if (parameterClass == null) {
                    return Optional.of(getAccessibleObject(invokeClass.getMethod(name)));
                }
                return Optional.of(getAccessibleObject(invokeClass.getMethod(name, parameterClass)));
            } catch (NoSuchMethodException noSuchMethodException) {
                // Due to version limitations, it is possible that methods may not be found,
                // so these errors can be ignored
                LOGGER.log(Level.WARNING, "Method {0} for class {1} is not found.",
                        new Object[]{name, invokeClass.getCanonicalName()});
            }
            return Optional.empty();
        });
        if (method.isPresent()) {
            try {
                if (parameterClass == null) {
                    return Optional.ofNullable(method.get().invoke(obj));
                }
                return Optional.ofNullable(method.get().invoke(obj, parameter));
            } catch (IllegalAccessException | InvocationTargetException operationException) {
                // Due to version limitations, it is possible that methods may not be found,
                // so these errors can be ignored
                LOGGER.log(Level.WARNING, "Method {0} for class {1} is not found.",
                        new Object[]{name, invokeClass.getCanonicalName()});
            }
        }
        return Optional.empty();
    }

    private static String buildMethodKey(Class<?> clazz, String methodName, Class<?> parameterClass) {
        String parameterClassName = "";
        if (parameterClass != null) {
            parameterClassName = parameterClass.getName();
        }
        String className = clazz.getName();

        // Initialize the length of the StringBuilder for performance purposes
        StringBuilder sb = new StringBuilder(
                className.length() + methodName.length() + parameterClassName.length() + EXTRA_LENGTH_FOR_METHOD_KEY);
        sb.append(className).append("#").append(methodName).append("(").append(parameterClassName).append(")");
        return sb.toString();
    }
}
