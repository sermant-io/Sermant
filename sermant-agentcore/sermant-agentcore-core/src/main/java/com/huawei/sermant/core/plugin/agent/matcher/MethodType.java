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

import net.bytebuddy.description.method.MethodDescription;

/**
 * 方法类型
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public enum MethodType {
    /**
     * 静态方法
     */
    STATIC() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return methodDescription.isStatic();
        }
    },
    /**
     * 构造函数
     */
    CONSTRUCTOR() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return methodDescription.isConstructor();
        }
    },
    /**
     * 成员方法
     */
    MEMBER() {
        @Override
        public boolean match(MethodDescription methodDescription) {
            return !methodDescription.isStatic() && !methodDescription.isConstructor();
        }
    };

    /**
     * 判断方法描述是否匹配
     *
     * @param methodDescription 方法描述
     * @return 是否匹配
     */
    public abstract boolean match(MethodDescription methodDescription);
}
