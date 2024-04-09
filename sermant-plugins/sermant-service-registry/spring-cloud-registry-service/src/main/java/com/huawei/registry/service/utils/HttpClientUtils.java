/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.service.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * http client utility class
 *
 * @author provenceee
 * @since 2022-05-26
 */
public enum HttpClientUtils {
    /**
     * Singleton
     */
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Set the connection timeout period in milliseconds. Refers to the timeout period for a three-way handshake
     */
    private static final int CONNECT_TIMEOUT = 2000;

    /**
     * The timeout period (that is, the response time) of the request to get data, in milliseconds.
     */
    private static final int SOCKET_TIMEOUT = 2000;

    private static final int MAX_TOTAL_POOL = 100;

    private static final int REQUEST_TIMEOUT = 3000;

    private final CloseableHttpClient client;

    HttpClientUtils() {
        this.client = createClient();
    }

    private CloseableHttpClient createClient() {
        // Set the protocols HTTP and HTTPS to handle the objects of the socket link factory
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE).build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
            socketFactoryRegistry);
        connManager.setMaxTotal(MAX_TOTAL_POOL);
        connManager.setDefaultMaxPerRoute(MAX_TOTAL_POOL);
        RequestConfig.Builder configBuilder = RequestConfig.custom();

        // Set the connection timeout
        configBuilder.setConnectTimeout(CONNECT_TIMEOUT);

        // Set the read timeout
        configBuilder.setSocketTimeout(SOCKET_TIMEOUT);

        // Set the timeout for getting connected instances from the connection pool
        configBuilder.setConnectionRequestTimeout(REQUEST_TIMEOUT);

        // Create custom httpclient object
        return HttpClients.custom()
            .setConnectionManager(connManager)
            .setConnectionManagerShared(true)
            .setDefaultRequestConfig(configBuilder.build())
            .build();
    }

    /**
     * Send POST request with request headers and request parameters
     *
     * @param url The address of the request
     * @param json Request parameters
     * @param header Request header
     * @return HttpClientResult result wrapper class
     */
    public HttpClientResult doPost(String url, String json, Map<String, Collection<String>> header) {
        // Create an HTTP object
        HttpPost httpPost = new HttpPost(url);

        // Create an httpResponse object
        try (CloseableHttpResponse httpResponse = client.execute(packageParam(httpPost, json, header))) {
            // Execute the request and get a response
            HttpClientResult result = new HttpClientResult(httpResponse.getStatusLine().getStatusCode(),
                EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
            LOGGER
                .info(String.format(Locale.ROOT, "Post successfully, result is %s.", JSONObject.toJSONString(result)));
            return result;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Request submit error.Error info: ", e);
            return new HttpClientResult(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static HttpEntityEnclosingRequestBase packageParam(HttpEntityEnclosingRequestBase httpMethod, String json,
        Map<String, Collection<String>> header) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(json, Consts.UTF_8);
        entity.setContentType(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
        entity.setContentEncoding(Consts.UTF_8.name());
        httpMethod.setEntity(entity);
        if (header != null) {
            Set<Entry<String, Collection<String>>> entrySet = header.entrySet();
            for (Entry<String, Collection<String>> entry : entrySet) {
                if (entry.getValue() == null) {
                    continue;
                }
                entry.getValue().forEach(value -> httpMethod.addHeader(entry.getKey(), value));
            }
        }
        return httpMethod;
    }
}