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

import com.huawei.sermant.core.config.common.ConfigFieldKey;
import com.huawei.sermant.core.config.common.ConfigTypeKey;

import java.lang.reflect.Field;

/**
 * 用于处理统一配置系统键的工具
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class ConfigKeyUtil {
    /**
     * 获取配置对象的键
     * <p>如果配置对象被{@link ConfigTypeKey}修饰，取其值
     * <p>如果不被{@link ConfigTypeKey}修饰，则取类的全限定名
     *
     * @param cls 配置对象类
     * @return 前缀字符串
     */
    public static String getTypeKey(Class<?> cls) {
        final ConfigTypeKey configTypeKey = cls.getAnnotation(ConfigTypeKey.class);
        if (configTypeKey == null) {
            return cls.getName();
        } else {
            return configTypeKey.value();
        }
    }

    /**
     * 获取配置信息键
     * <p>通过{@link ConfigFieldKey}注解获取成员属性对应配置信息键
     * <p>不存在注解时，直接取字段名
     *
     * @param field 字段
     * @return 配置信息键
     */
    public static String getFieldKey(Field field) {
        final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
        if (configFieldKey == null) {
            return field.getName();
        } else {
            return configFieldKey.value();
        }
    }

    /**
     * 附带ClassLoader的类型键
     *
     * @param typeKey     类型键
     * @param classLoader ClassLoader
     * @return 附带ClassLoader的类型键
     */
    public static String getCLTypeKey(String typeKey, ClassLoader classLoader) {
        return typeKey + "@" + Integer.toHexString(classLoader.hashCode());
    }
}
