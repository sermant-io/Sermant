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

package com.huawei.gray.feign.service;

import com.huawei.gray.feign.context.HostContext;
import com.huawei.sermant.core.agent.common.BeforeResult;

import feign.Request;

import java.lang.reflect.Method;
import java.net.URI;

/**
 * LoadBalancerClientInterceptor的service
 *
 * @author provenceee
 * @since 2021/11/26
 */
public class LoadBalancerClientServiceImpl implements LoadBalancerClientService {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
        final Object argument = arguments[0];
        if (argument instanceof Request) {
            Request request = (Request) argument;
            URI uri = URI.create(request.url());

            // 将下游服务名存入线程变量中
            HostContext.set(uri.getHost());
        }
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
        // 移除线程变量
        HostContext.remove();
    }
}
