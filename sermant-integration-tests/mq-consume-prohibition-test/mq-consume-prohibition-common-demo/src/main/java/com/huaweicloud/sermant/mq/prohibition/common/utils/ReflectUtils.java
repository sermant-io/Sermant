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
}
