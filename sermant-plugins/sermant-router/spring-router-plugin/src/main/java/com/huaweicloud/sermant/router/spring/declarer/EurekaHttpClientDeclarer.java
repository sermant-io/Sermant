/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
 * EurekaHttpClient Enhancement Class, EurekaRegistration Method
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class EurekaHttpClientDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS = "com.netflix.discovery.shared.transport.EurekaHttpClient";

    private static final String INTERCEPT_CLASS
            = "com.huaweicloud.sermant.router.spring.interceptor.EurekaHttpClientInterceptor";

    private static final String METHOD_NAME = "register";

    /**
     * Constructor
     */
    public EurekaHttpClientDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}