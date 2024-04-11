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

package com.huaweicloud.sermant.core.plugin.agent.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * ClassFuzzyMatcher, provides related logical operations
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public abstract class ClassFuzzyMatcher extends ClassMatcher {
    /**
     * Logical operation {@code not}, which returns true if false, false otherwise
     *
     * @return ClassFuzzyMatcher
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
     * Logical operation {@code and} returns true if both is true, false otherwise
     *
     * @param matcher Another ClassFuzzyMatcher
     * @return ClassFuzzyMatcher
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
     * Logical operation {@code or}, returns true if either is true, false otherwise
     *
     * @param matcher Another ClassFuzzyMatcher
     * @return ClassFuzzyMatcher
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
