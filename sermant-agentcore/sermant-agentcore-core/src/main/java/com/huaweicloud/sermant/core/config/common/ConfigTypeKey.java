/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.config.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * General configuration object prefix
 * <p>This annotation can be used if the configuration key for all properties in the configuration object contains
 * the same prefix
 * <p>Build the configuration key with {@link ConfigFieldKey}, and use the attribute name when {@link ConfigFieldKey}
 * does not exist
 * <p>see ConfigKeyUtil#getTypeKey(Class)
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConfigTypeKey {
    /**
     * type name
     *
     * @return type name
     */
    String value();
}
