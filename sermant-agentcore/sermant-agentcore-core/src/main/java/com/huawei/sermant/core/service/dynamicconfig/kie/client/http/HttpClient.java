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

package com.huawei.sermant.core.service.dynamicconfig.kie.client.http;

import com.huawei.sermant.core.service.dynamicconfig.kie.client.Client;
import org.apache.http.client.config.RequestConfig;

import java.util.Map;

/**
 * http请求
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface HttpClient extends Client {

    /**
     * get请求
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param requestConfig 请求配置
     * @return HttpResult
     */
    HttpResult doGet(String url, Map<String, String> headers, RequestConfig requestConfig);

    /**
     * get请求
     *
     * @param url 请求地址
     * @return HttpResult
     */
    HttpResult doGet(String url);

    /**
     * get请求
     *
     * @param url 请求地址
     * @param requestConfig 请求配置
     * @return HttpResult
     */
    HttpResult doGet(String url, RequestConfig requestConfig);

    /**
     * post请求
     *
     * @param url 请求地址
     * @param params 请求参数
     * @return HttpResult
     */
    HttpResult doPost(String url, Map<String, Object> params);

    /**
     * post请求
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param requestConfig 请求配置
     * @return HttpResult
     */
    HttpResult doPost(String url, Map<String, Object> params,  RequestConfig requestConfig);

    /**
     * post请求
     *
     * @param url 请求地址
     * @param params 请求参数
     * @param headers 请求头
     * @param requestConfig 请求配置
     * @return HttpResult
     */
    HttpResult doPost(String url, Map<String, Object> params, RequestConfig requestConfig, Map<String, String> headers);

    /**
     * put请求
     *
     * @param url 请求地址
     * @param params 请求参数
     * @return HttpResult
     */
    HttpResult doPut(String url, Map<String, Object> params);

    /**
     * delete请求
     *
     * @param url 请求地址
     * @return HttpResult
     */
    HttpResult doDelete(String url);
}
