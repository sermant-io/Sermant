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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http;

import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.Client;

import org.apache.http.client.config.RequestConfig;

import java.util.Map;

/**
 * http request
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface HttpClient extends Client {
    /**
     * get request
     *
     * @param url request address
     * @param headers request headers
     * @param requestConfig request config
     * @return HttpResult
     */
    HttpResult doGet(String url, Map<String, String> headers, RequestConfig requestConfig);

    /**
     * get request
     *
     * @param url request address
     * @return HttpResult
     */
    HttpResult doGet(String url);

    /**
     * get request
     *
     * @param url request address
     * @param requestConfig request config
     * @return HttpResult
     */
    HttpResult doGet(String url, RequestConfig requestConfig);

    /**
     * post request
     *
     * @param url request address
     * @param params request params
     * @return HttpResult
     */
    HttpResult doPost(String url, Map<String, Object> params);

    /**
     * post request
     *
     * @param url request address
     * @param params request params
     * @param requestConfig request config
     * @return HttpResult
     */
    HttpResult doPost(String url, Map<String, Object> params, RequestConfig requestConfig);

    /**
     * post request
     *
     * @param url request address
     * @param params request params
     * @param headers request headers
     * @param requestConfig request config
     * @return HttpResult
     */
    HttpResult doPost(String url, Map<String, Object> params, RequestConfig requestConfig, Map<String, String> headers);

    /**
     * put request
     *
     * @param url request address
     * @param params request params
     * @return HttpResult
     */
    HttpResult doPut(String url, Map<String, Object> params);

    /**
     * delete request
     *
     * @param url request address
     * @return HttpResult
     */
    HttpResult doDelete(String url);
}
