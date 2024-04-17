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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestTag;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientRequest.Builder;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

/**
 * webclient interceptor, get & inject request headers
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class RouterExchangeFilterFunction implements ExchangeFilterFunction {
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction exchangeFunction) {
        HttpHeaders readOnlyHttpHeaders = request.headers();
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        if (requestTag == null) {
            ThreadLocalUtils.setRequestData(new RequestData(readOnlyHttpHeaders, request.url().getPath(),
                    request.method().name()));
            return exchangeFunction.exchange(request);
        }
        Builder newRequestBuilder = ClientRequest.from(request);
        requestTag.getTag().forEach((key, value) -> newRequestBuilder.header(key, value.toArray(new String[0])));
        ClientRequest newRequest = newRequestBuilder.build();
        ThreadLocalUtils.setRequestData(new RequestData(newRequest.headers(), newRequest.url().getPath(),
                newRequest.method().name()));
        return exchangeFunction.exchange(newRequest);
    }
}