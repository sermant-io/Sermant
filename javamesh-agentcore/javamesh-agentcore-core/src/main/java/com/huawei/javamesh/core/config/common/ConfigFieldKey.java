/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.config.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.huawei.javamesh.core.config.utils.ConfigKeyUtil;

/**
 * 通用字段键注解
 * <p>用于修饰配置对象的属性，与{@link ConfigTypeKey}一并构建配置信息键
 * <p>主要作用是修正成员属性和配置键之间的差异
 * <p>见{@link ConfigKeyUtil#getFieldKey(java.lang.reflect.Field)}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConfigFieldKey {
    String value();
}
