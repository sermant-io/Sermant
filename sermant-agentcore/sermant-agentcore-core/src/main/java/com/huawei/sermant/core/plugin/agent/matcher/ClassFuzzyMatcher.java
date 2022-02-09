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

package com.huawei.sermant.core.plugin.agent.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 类的模糊匹配器，提供相关的逻辑操作
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class ClassFuzzyMatcher extends ClassMatcher {
    /**
     * 逻辑操作{@code not}，原为假时返回真，否则返回假
     *
     * @return 类的模糊匹配器
     */
    public ClassFuzzyMatcher not() {
        final ClassFuzzyMatcher thisMatcher = this;
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return !thisMatcher.matches(typeDescription);
            }
        };
    }

    /**
     * 逻辑操作{@code and}，同为真时返回真，否则返回假
     *
     * @param matcher 另一个类的模糊匹配器
     * @return 类的模糊匹配器
     */
    public ClassFuzzyMatcher and(ElementMatcher<TypeDescription> matcher) {
        final ClassFuzzyMatcher thisMatcher = this;
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return thisMatcher.matches(typeDescription) && matcher.matches(typeDescription);
            }
        };
    }

    /**
     * 逻辑操作{@code or}，两者其一为真时返回真，否则返回假
     *
     * @param matcher 另一个类的模糊匹配器
     * @return 类的模糊匹配器
     */
    public ClassFuzzyMatcher or(ElementMatcher<TypeDescription> matcher) {
        final ClassFuzzyMatcher thisMatcher = this;
        return new ClassFuzzyMatcher() {
            @Override
            public boolean matches(TypeDescription typeDescription) {
                return thisMatcher.matches(typeDescription) || matcher.matches(typeDescription);
            }
        };
    }
}
