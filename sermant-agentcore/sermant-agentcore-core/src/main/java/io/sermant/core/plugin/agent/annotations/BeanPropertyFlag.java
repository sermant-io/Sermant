/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.plugin.agent.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Java Bean tag, the interface modified by the annotation, will implement get and set methods in the enhanced
 * class based on the properties defined by the annotation
 * <p>see SuperTypeDeclarer.ForBeanProperty
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(BeanPropertyFlags.class)
public @interface BeanPropertyFlag {
    /**
     * Declares the Java Bean property name
     *
     * @return Java Bean property name
     */
    String value();

    /**
     * Declares the Java Bean property type
     *
     * @return Java Bean property type
     */
    Class<?> type();
}
