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

package com.huaweicloud.sermant.core.plugin.agent.matcher;

import net.bytebuddy.description.method.MethodDescription;

/**
 * Method Type
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public enum MethodType {
    /**
     * static method
     */
    STATIC() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return methodDescription.isStatic();
        }
    },
    /**
     * constructor
     */
    CONSTRUCTOR() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return methodDescription.isConstructor();
        }
    },
    /**
     * member method
     */
    MEMBER() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return !methodDescription.isStatic() && !methodDescription.isConstructor();
        }
    },
    /**
     * public method
     */
    PUBLIC() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return methodDescription.isPublic();
        }
    };

    /**
     * Check whether the method description matches
     *
     * @param methodDescription method description
     * @return match result
     */
    public abstract boolean match(MethodDescription methodDescription);
}
