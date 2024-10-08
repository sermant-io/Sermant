/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
 * When the HttpServerHandle interception point only introduces spring-boot-starter-webflux for reactive programming,
 * you need to remove the thread variable in the post-method
 * <p>spring cloud Greenwich.RELEASE+
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class HttpServerHandleDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS
            = {"reactor.netty.http.server.HttpServerHandle", "reactor.netty.http.server.HttpServer$HttpServerHandle"};

    private static final String INTERCEPT_CLASS
            = "io.sermant.router.spring.interceptor.HttpServerHandleInterceptor";

    private static final String METHOD_NAME = "onStateChange";

    /**
     * Constructor
     */
    public HttpServerHandleDeclarer() {
        super(null, INTERCEPT_CLASS, METHOD_NAME);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASS);
    }
}
