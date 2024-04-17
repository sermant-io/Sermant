/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.database.prohibition.integration.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HTTP Request Tool Class
 *
 * @author daizhenyu
 * @since 2024-03-12
 **/
public class HttpRequestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtils.class);

    private static final int SUCCESS_CODE = 200;

    private HttpRequestUtils() {
    }

    /**
     * send get request
     *
     * @param url hTTP request URL
     * @return response Body
     */
    public static String doGet(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == SUCCESS_CODE) {
                    return EntityUtils.toString(response.getEntity());
                }
                LOGGER.info("Request error, the message is: {}", EntityUtils.toString(response.getEntity()));
                return "";
            }
        } catch (IOException e) {
            LOGGER.info("Request exception, the message is: {}", e.getMessage());
            return "";
        }
    }
}
