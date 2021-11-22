/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.huawei.apm.core.config.utils.ConfigKeyUtil;

/**
 * 通用配置对象前缀
 * <p>如果配置对象中的所有属性对应的配置键都包含相同的前缀，那么可以使用该注解声明
 * <p>与{@link ConfigFieldKey}一同构建配置键，{@link ConfigFieldKey}不存在时，直接使用属性名
 * <p>见{@link ConfigKeyUtil#getTypeKey(Class)}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConfigTypeKey {
    String value();
}
