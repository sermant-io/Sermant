/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.declarer;

import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;

/**
 * HttpServerHandle拦截点，只引入spring-boot-starter-webflux进行响应式编程时，需要在后置方法移除线程变量
 * <p>spring cloud Greenwich.RELEASE+
 *
 * @author provenceee
 * @since 2023-06-09
 */
public class HttpServerHandleDeclarer extends AbstractDeclarer {
    private static final String[] ENHANCE_CLASS
            = {"reactor.netty.http.server.HttpServerHandle", "reactor.netty.http.server.HttpServer$HttpServerHandle"};

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.spring.interceptor.HttpServerHandleInterceptor";

    private static final String METHOD_NAME = "onStateChange";

    /**
     * 构造方法
     */
    public HttpServerHandleDeclarer() {
        super(null, INTERCEPT_CLASS, METHOD_NAME);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameContains(ENHANCE_CLASS);
    }
}