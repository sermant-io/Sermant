/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

/**
 * Webclient interception point, springboot 2.0.0. RELEASE+, injection request filter
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class WebClientBuilderDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "org.springframework.web.reactive.function.client.WebClient$Builder";

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.spring.interceptor.WebClientBuilderInterceptor";

    private static final String METHOD_NAME = "build";

    /**
     * Constructor
     */
    public WebClientBuilderDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}