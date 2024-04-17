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

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.listener.SubscriberManager;

import com.alibaba.fastjson.JSONObject;

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
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * HTTP client
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class DefaultHttpClient
        implements com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.http.HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Maximum number of connections. The value is recommended to be greater than maximum number of threads
     * MAX_THREAD_SIZE in {@link SubscriberManager}
     */
    private static final int MAX_TOTAL = SubscriberManager.MAX_THREAD_SIZE * 2;

    /**
     * Maximum number of connections per route
     */
    private static final int DEFAULT_MAX_PER_ROUTE = 10;

    private final HttpClient httpClient;

    /**
     * Constructor
     *
     * @param timeout timeout
     */
    public DefaultHttpClient(int timeout) {
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(
                RequestConfig.custom().setConnectTimeout(timeout)
                        .setSocketTimeout(timeout)
                        .setConnectionRequestTimeout(timeout)
                        .build()
        ).setConnectionManager(buildConnectionManager()).build();
    }

    /**
     * Constructor
     *
     * @param requestConfig request config
     */
    public DefaultHttpClient(RequestConfig requestConfig) {
        this.httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(buildConnectionManager())
                .build();
    }

    /**
     * The primary purpose of configuring connection pools is to prevent requests from being sent without enough
     * connections, especially for subscribers
     *
     * @return PoolingHttpClientConnectionManager
     */
    private PoolingHttpClientConnectionManager buildConnectionManager() {
        RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();
        builder.register("http", PlainConnectionSocketFactory.INSTANCE);
        registerHttps(builder);

        // if need to configure SSL, register https here
        Registry<ConnectionSocketFactory> connectionSocketFactoryRegistry = builder.build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                connectionSocketFactoryRegistry);
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        return connectionManager;
    }

    private void registerHttps(RegistryBuilder<ConnectionSocketFactory> builder) {
        try {
            HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new SslTrustStrategy()).build();
            builder.register("https", new SSLConnectionSocketFactory(sslContext, hostnameVerifier));
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
            LOGGER.log(Level.WARNING, "Failed to get SSLContext, reason: ", ex);
        }
    }

    /**
     * get request
     *
     * @param url request address
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
     * get request
     *
     * @param url request address
     * @param headers request headers
     * @param requestConfig request config
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
    public HttpResult doPost(String url, Map<String, Object> params, RequestConfig requestConfig,
            Map<String, String> headers) {
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
     * Execute request
     *
     * @param request HttpUriRequest
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
            LOGGER.log(Level.WARNING, "Execute request failed.", ex);
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
            LOGGER.log(Level.WARNING, "Consumed http entity failed.", ex);
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
     * Add default request headers
     *
     * @param base HttpRequestBase
     */
    private void addDefaultHeaders(HttpRequestBase base) {
        base.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        base.addHeader(HttpHeaders.USER_AGENT, "sermant/client");
    }

    /**
     * SSL trust strategy
     *
     * @author zhouss
     * @since 2021-11-17
     */
    static class SslTrustStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) {
            return true;
        }
    }
}
