/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.demo.spring.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * controller
 *
 * @author daizhenyu
 * @since 2024-07-02
 **/
@RestController
public class SpringClientController {
    private static final String HELLO_METHOD_PATH = "/hello";

    private static final int SUCCESS_CODE = 200;

    private static final int MAX_TOTAL = 200;

    private static final int MAX_PER_ROUTE = 20;

    private static final int CONNECT_TIMEOUT = 200;

    private static final int SOCKET_TIMEOUT = 200;

    private static final int RETRY_COUNT = 3;

    private static CloseableHttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_COUNT, true))
                .build();
    }

    /**
     * call upstream service
     *
     * @param address address
     * @return result
     */
    @RequestMapping("hello")
    public String hello(String address) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://");
        urlBuilder.append(address);
        urlBuilder.append(HELLO_METHOD_PATH);
        return httpGet(urlBuilder.toString());
    }

    private String httpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == SUCCESS_CODE) {
                return EntityUtils.toString(response.getEntity());
            }
            return "";
        } catch (IOException e) {
            return "";
        }
    }
}
