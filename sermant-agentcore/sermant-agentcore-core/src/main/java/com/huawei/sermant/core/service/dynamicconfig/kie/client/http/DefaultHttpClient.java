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

import com.alibaba.fastjson.JSONObject;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HTTP客户端
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class DefaultHttpClient implements com.huawei.sermant.core.service.dynamicconfig.kie.client.http.HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    /**
     * 默认超时时间
     */
    private static final int DEFAULT_TIMEOUT_MS = 5000;

    /**
     * 最大的连接数
     * 该值建议 > {@link SubscriberManager}最大线程数MAX_THREAD_SIZE
     */
    private static final int MAX_TOTAL = SubscriberManager.MAX_THREAD_SIZE * 2;

    /**
     * 没分支最大连接数
     */
    private static final int DEFAULT_MAX_PER_ROUTE = 10;

    private final HttpClient httpClient;

    public DefaultHttpClient() {
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(
                RequestConfig.custom().setConnectTimeout(DEFAULT_TIMEOUT_MS)
                        .setSocketTimeout(DEFAULT_TIMEOUT_MS)
                        .setConnectionRequestTimeout(DEFAULT_TIMEOUT_MS)
                        .build()
        ).setConnectionManager(buildConnectionManager()).build();
    }

    public DefaultHttpClient(RequestConfig requestConfig) {
        this.httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(buildConnectionManager())
                .build();
    }


    /**
     * 配置连接池的主要目的是防止在发送请求时连接不够导致无法请求
     * 特别是针对订阅者
     *
     * @return PoolingHttpClientConnectionManager
     */
    private PoolingHttpClientConnectionManager buildConnectionManager() {
        RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();
        builder.register("http", PlainConnectionSocketFactory.INSTANCE);
        // 如果需配置SSL 在此处注册https
        Registry<ConnectionSocketFactory> connectionSocketFactoryRegistry = builder.build();

        //connection pool management
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                connectionSocketFactoryRegistry);
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        return connectionManager;
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @return HttpResult
     */
    @Override
    public HttpResult doGet(String url) {
        return doGet(url, null, null);
    }

    @Override
    public HttpResult doGet(String url, RequestConfig requestConfig) {
        return doGet(url, null, requestConfig);
    }

    /**
     * get请求
     *
     * @param url 请求地址
     * @param headers 请求头
     * @return HttpResult
     */
    @Override
    public HttpResult doGet(String url, Map<String, String> headers, RequestConfig requestConfig) {
        final HttpGet httpGet = new HttpGet(url);
        beforeRequest(httpGet, requestConfig, headers);
        return execute(httpGet);
    }

    @Override
    public HttpResult doPost(String url, Map<String, Object> params) {
        return doPost(url, params, null);
    }

    @Override
    public HttpResult doPost(String url, Map<String, Object> params, RequestConfig requestConfig) {
        return doPost(url, params, requestConfig, new HashMap<>());
    }

    @Override
    public HttpResult doPost(String url, Map<String, Object> params, RequestConfig requestConfig, Map<String, String> headers) {
        HttpPost httpPost = new HttpPost(url);
        beforeRequest(httpPost, requestConfig, headers);
        addParams(httpPost, params);
        return execute(httpPost);
    }

    @Override
    public HttpResult doPut(String url, Map<String, Object> params) {
        final HttpPut httpPut = new HttpPut(url);
        beforeRequest(httpPut, null, new HashMap<>());
        addParams(httpPut, params);
        return execute(httpPut);
    }

    @Override
    public HttpResult doDelete(String url) {
        final HttpDelete httpDelete = new HttpDelete(url);
        beforeRequest(httpDelete, null, null);
        return execute(httpDelete);
    }

    /**
     * 执行请求
     *
     * @param request 请求参数
     * @return HttpResult
     */
    private HttpResult execute(HttpUriRequest request) {
        HttpEntity entity = null;
        HttpResponse response = null;
        String result = null;
        try {
            response = this.httpClient.execute(request);
            if (response == null) {
                return HttpResult.error();
            }
            entity = response.getEntity();
            if (entity == null) {
                return new HttpResult(response.getStatusLine().getStatusCode(), "", response.getAllHeaders());
            }
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException ex) {
            LOGGER.warning(String.format("Execute request failed, %s", ex.getMessage()));
        } finally {
            consumeEntity(entity);
        }
        if (response == null) {
            return HttpResult.error();
        }
        return new HttpResult(response.getStatusLine().getStatusCode(), result, response.getAllHeaders());
    }

    private void consumeEntity(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException ex) {
            LOGGER.warning(String.format("Consumed http entity failed, %s", ex.getMessage()));
        }
    }

    private void addParams(HttpEntityEnclosingRequestBase httpPost, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        httpPost.setEntity(new StringEntity(JSONObject.toJSONString(params), ContentType.APPLICATION_JSON));
    }

    private void beforeRequest(HttpRequestBase httpRequest, RequestConfig requestConfig, Map<String, String> headers) {
        addDefaultHeaders(httpRequest);
        addHeaders(httpRequest, headers);
        if (requestConfig != null) {
            httpRequest.setConfig(requestConfig);
        }
    }

    private void addHeaders(HttpRequestBase base, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                base.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 添加默认的请求头
     */
    private void addDefaultHeaders(HttpRequestBase base) {
        base.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        base.addHeader(HttpHeaders.USER_AGENT, "sermant/client");
    }
}
