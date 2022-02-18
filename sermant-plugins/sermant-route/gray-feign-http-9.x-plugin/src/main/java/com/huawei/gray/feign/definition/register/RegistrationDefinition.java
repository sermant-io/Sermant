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

package com.huawei.gray.feign.definition.register;

import com.huawei.gray.feign.definition.AbstractInstDefinition;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;

/**
 * 注册增强
 *
 * @author fuziye
 * @since 2021-12-29
 */
public class RegistrationDefinition extends AbstractInstDefinition {
    /**
     * 增强类的全限定名
     */
    private static final String ENHANCE_CLASS = "org.springframework.cloud.client.serviceregistry.ServiceRegistry";

    /**
     * 拦截类的全限定名
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.RegistrationInterceptor";

    public RegistrationDefinition() {
        super(null, INTERCEPT_CLASS, "register");
    }

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.hasSuperTypes(ENHANCE_CLASS);
    }
}