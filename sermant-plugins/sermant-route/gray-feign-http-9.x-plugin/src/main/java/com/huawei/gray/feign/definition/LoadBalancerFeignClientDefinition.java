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
 * 拦截FeignBlockingLoadBalancerClient#execute方法 获取下游服务名称，该方法会在client调用之前，且request域名还未进行解析，因此可通过该参数拿到下游的服务名称
 *
 * @author lilai
 * @since 2021-11-03
 */
public class LoadBalancerFeignClientDefinition extends AbstractInstDefinition {
    private static final String[] ENHANCE_CLASS = {
        "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient",
        "org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient",
        "org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient"};

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "com.huawei.gray.feign.interceptor.LoadBalancerClientInterceptor";

    public LoadBalancerFeignClientDefinition() {
        super(ENHANCE_CLASS, INTERCEPT_CLASS, "execute");
    }
}