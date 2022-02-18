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

package com.huawei.gray.feign.definition;

/**
 * 获取服务下游列表增强
 *
 * @author fuziye
 * @since 2021-12-29
 */
public class ServiceInstanceListSupplierDefinition extends AbstractInstDefinition {
    private static final String[] ENHANCE_CLASS = {
        "org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier",
        "org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier"};

    private static final String INTERCEPT_CLASS =
        "com.huawei.gray.feign.interceptor.ServiceInstanceListSupplierInterceptor";

    private static final String ENHANCE_METHOD = "get";

    public ServiceInstanceListSupplierDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, ENHANCE_METHOD);
    }
}