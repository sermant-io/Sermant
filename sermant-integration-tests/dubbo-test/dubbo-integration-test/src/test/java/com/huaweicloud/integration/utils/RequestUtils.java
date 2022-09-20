/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration.utils;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 请求工具类
 *
 * @author zhouss
 * @since 2022-07-30
 */
public class RequestUtils {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final ResponseExtractor DEFAULT = response -> response;

    private static final DefaultResponseErrorHandler DEFAULT_RESPONSE_ERROR_HANDLER = new DefaultResponseErrorHandler();

    private RequestUtils() {
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param responseClass 响应类型
     * @param <T> 响应类型
     * @return 响应结果
     */
    public static <T> T get(String url, Map<String, Object> params, Class<T> responseClass) {
        return get(url, params, responseClass, (ResponseExtractor<T>) DEFAULT);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param responseClass 响应类型
     * @param responseExtractor 结果转换器
     * @param <T> 响应类型
     * @return 响应结果
     */
    public static <T> T get(String url, Map<String, Object> params, Class<T> responseClass,
            ResponseExtractor<T> responseExtractor) {
        ResponseExtractor<T> cur = responseExtractor;
        if (cur == DEFAULT || cur == null) {
            cur = new HttpMessageConverterExtractor<T>(responseClass, REST_TEMPLATE.getMessageConverters());
        }
        return REST_TEMPLATE.execute(url, HttpMethod.GET, null, cur, params);
    }

    /**
     * 含回调的get方法
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param responseClass 响应类型
     * @param responseCallback 回调
     * @param <T>  响应类型
     * @return 请求结果
     */
    public static <T> T get(String url, Map<String, Object> params, Class<T> responseClass,
            BiFunction<ClientHttpResponse, T, T> responseCallback) {
        final HttpMessageConverterExtractor<T> httpMessageConverterExtractor = new HttpMessageConverterExtractor<T>(
                responseClass, REST_TEMPLATE.getMessageConverters()) {
            @Override
            public T extractData(ClientHttpResponse response) throws IOException {
                final T result = super.extractData(response);
                return responseCallback.apply(response, result);
            }
        };
        return get(url, params, responseClass, httpMessageConverterExtractor);
    }

    /**
     * 含回调的get方法
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param responseClass 响应类型
     * @param responseCallback 回调
     * @param <T>  响应类型
     * @return 请求结果
     */
    public static <T> T get(String url, Map<String, Object> params, Class<T> responseClass,
            BiFunction<ClientHttpResponse, T, T> responseCallback, ResponseErrorHandler errorHandler) {
        try {
            if (errorHandler != null) {
                REST_TEMPLATE.setErrorHandler(errorHandler);
            }
           return get(url, params, responseClass, responseCallback);
        } finally {
            REST_TEMPLATE.setErrorHandler(DEFAULT_RESPONSE_ERROR_HANDLER);
        }
    }

}
