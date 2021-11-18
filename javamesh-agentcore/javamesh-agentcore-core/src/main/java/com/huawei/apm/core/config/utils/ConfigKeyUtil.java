/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config.utils;

import java.lang.reflect.Field;

import com.huawei.apm.core.config.common.ConfigFieldKey;
import com.huawei.apm.core.config.common.ConfigTypeKey;

/**
 * 用于处理统一配置系统键的工具
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/16
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
