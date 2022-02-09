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

package com.huawei.sermant.core.config.common;

import com.huawei.sermant.core.config.utils.ConfigKeyUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用配置对象前缀
 * <p>如果配置对象中的所有属性对应的配置键都包含相同的前缀，那么可以使用该注解声明
 * <p>与{@link ConfigFieldKey}一同构建配置键，{@link ConfigFieldKey}不存在时，直接使用属性名
 * <p>见{@link ConfigKeyUtil#getTypeKey(Class)}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConfigTypeKey {
    String value();
}
