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

package com.huawei.sermant.core.plugin.agent.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java Bean标记，被该注解修饰的接口，将根据该注解定义的属性在被增强类中实现get、set方法
 * <p>见{@link com.huawei.sermant.core.plugin.agent.declarer.SuperTypeDeclarer.ForBeanProperty}
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
     * 声明Java Bean属性名
     *
     * @return Java Bean属性名
     */
    String value();

    /**
     * 声明Java Bean属性类型
     *
     * @return Java Bean属性类型
     */
    Class<?> type();
}
