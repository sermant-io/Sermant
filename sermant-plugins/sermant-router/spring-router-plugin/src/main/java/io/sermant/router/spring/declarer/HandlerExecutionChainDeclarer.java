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

package io.sermant.router.spring.declarer;

import io.sermant.core.plugin.agent.matcher.ClassMatcher;

/**
 * Add an injection interceptor by intercepting and inject a spring web interceptor
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class HandlerExecutionChainDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.springframework.web.servlet.HandlerExecutionChain";

    private static final String INTERCEPT_CLASS
            = "io.sermant.router.spring.interceptor.HandlerExecutionChainInterceptor";

    private static final String METHOD_NAME = "applyPreHandle";

    /**
     * Constructor
     */
    public HandlerExecutionChainDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }
}
