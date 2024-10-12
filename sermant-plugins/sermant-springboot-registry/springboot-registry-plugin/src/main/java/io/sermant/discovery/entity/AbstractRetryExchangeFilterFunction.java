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

package io.sermant.discovery.entity;

import io.sermant.discovery.utils.HttpConstants;
import io.sermant.discovery.utils.PlugEffectWhiteBlackUtils;
import io.sermant.discovery.utils.RequestInterceptorUtils;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import java.net.URI;
import java.util.Map;

/**
 * webclient Retry
 *
 * @author provenceee
 * @since 2023-05-06
 */
public abstract class AbstractRetryExchangeFilterFunction implements ExchangeFilterFunction {
    @Override
    public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction exchangeFunction) {
        Mono<ClientResponse> mono = exchangeFunction.exchange(clientRequest);
        URI uri = clientRequest.url();
        if (!PlugEffectWhiteBlackUtils.isHostEqualRealmName(uri.getHost())) {
            return mono;
        }
        Map<String, String> hostAndPath = RequestInterceptorUtils.recoverHostAndPath(uri.getPath());
        if (!PlugEffectWhiteBlackUtils.isPlugEffect(hostAndPath.get(HttpConstants.HTTP_URI_SERVICE))) {
            return mono;
        }
        return retry(mono);
    }

    /**
     * Retry
     *
     * @param mono mono
     * @return Retrying Mono
     */
    public abstract Mono<ClientResponse> retry(Mono<ClientResponse> mono);
}
