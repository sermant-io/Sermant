/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.declarer;

/**
 * LoadBalancerClientFactory enhancement Class
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class ClientFactoryDeclarer extends AbstractDeclarer {
    private static final String ENHANCE_CLASS =
            "org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory";

    private static final String INTERCEPT_CLASS = "com.huaweicloud.loadbalancer.interceptor.ClientFactoryInterceptor";

    private static final String METHOD_NAME = "getInstance";

    /**
     * 构造方法
     */
    public ClientFactoryDeclarer() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, METHOD_NAME);
    }
}
