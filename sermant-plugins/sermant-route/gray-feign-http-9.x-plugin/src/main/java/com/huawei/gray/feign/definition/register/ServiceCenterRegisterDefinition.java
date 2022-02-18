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

/**
 * 拦截MicroserviceFactory 创建微服务的方法，获取当前服务名
 *
 * @author lilai
 * @since 2021-11-03
 */
public class ServiceCenterRegisterDefinition extends AbstractInstDefinition {
    private static final String[] ENHANCE_CLASS = {"org.apache.servicecomb.registry.api.registry.MicroserviceFactory"};

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.ServiceCenterRegisterInterceptor";

    public ServiceCenterRegisterDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, "createMicroserviceFromConfiguration");
    }
}