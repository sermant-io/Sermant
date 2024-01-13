/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.mq.prohibition.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author daizhenyu
 * @since 2024-01-09
 **/
public class ReflectUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtils.class);

    private ReflectUtils() {
    }

    /**
     * 获取类的私有属性
     *
     * @param clazz 类对象
     * @param obj 类实例
     * @param fieldName 属性名
     * @return 属性值
     */
    public static Object getField(Class clazz, Object obj, String fieldName) {
        Object fieldValue = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Get field error, the message is: {}", e.getMessage());
        }
        return fieldValue;
    }

    /**
     * 调用类的public无参方法
     *
     * @param clazz 类对象
     * @param obj 类实例
     * @param methodName 方法名
     * @return 方法返回值
     */
    public static Object invokeMethod(Class clazz, Object obj, String methodName) {
        Object result = null;
        try {
            Method method = clazz.getMethod(methodName);
            result = method.invoke(obj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Invoke method error, the message is: {}", e.getMessage());
        }
        return result;
    }

    /**
     * 判断类是否具有某个方法
     *
     * @param clazz 类对象
     * @param methodName 方法名
     * @return 是否含有方法
     */
    public static boolean isHasMethod(Class clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }
}
