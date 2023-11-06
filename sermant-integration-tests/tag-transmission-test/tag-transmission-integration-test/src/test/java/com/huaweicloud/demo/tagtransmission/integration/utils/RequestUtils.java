/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.demo.tagtransmission.integration.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 请求工具类
 *
 * @author daizhenyu
 * @since 2023-10-16
 */
public class RequestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);

    private RequestUtils() {
    }

    /**
     * httpclient get方法
     *
     * @param url 请求地址
     * @param headers 需要添加的header
     * @return Optional<String> 包含response的optional
     */
    public static Optional<String> get(String url, Map<String, String> headers) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            for (String key : headers.keySet()) {
                httpGet.addHeader(key, headers.get(key));
            }
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return Optional.ofNullable(EntityUtils.toString(response.getEntity()));
                } else {
                    LOGGER.error("Request error, the message is: {}", EntityUtils.toString(response.getEntity()));
                    return Optional.empty();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Request exception, the message is: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
