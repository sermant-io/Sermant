/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register.definition;

import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;

/**
 * ApplicationConfig增强类
 *
 * @author provenceee
 * @date 2021/11/24
 */
public class ApplicationConfigDefinition extends AbstractDefinition {
    private static final String[] ENHANCE_CLASS = {"org.apache.dubbo.config.ApplicationConfig",
            "com.alibaba.dubbo.config.ApplicationConfig"};

    private static final String INTERCEPT_CLASS = "com.huawei.dubbo.register.interceptor.ApplicationConfigInterceptor";

    private static final String METHOD_NAME = "setName";

    public ApplicationConfigDefinition() {
        super(null, INTERCEPT_CLASS, METHOD_NAME);
    }

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(ENHANCE_CLASS);
    }
}
