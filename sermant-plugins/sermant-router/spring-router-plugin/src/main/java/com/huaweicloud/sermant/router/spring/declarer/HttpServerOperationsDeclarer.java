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
 * HttpServerOperations拦截点，只引入spring-boot-starter-webflux进行响应式编程时，需要在后置方法移除线程变量
 * <p>spring cloud Finchley.x
 *
 * @author provenceee
 * @since 2023-06-09
 */
public class HttpServerOperationsDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "reactor.ipc.netty.http.server.HttpServerOperations";

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.spring.interceptor.HttpServerOperationsInterceptor";

    private static final String METHOD_NAME = "onHandlerStart";

    /**
     * 构造方法
     */
    public HttpServerOperationsDeclarer() {
        super(null, INTERCEPT_CLASS, METHOD_NAME);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }
}