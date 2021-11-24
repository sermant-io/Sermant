/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.client.http;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.kie.listener.SubscriberManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HTTP客户端
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class DefaultHttpClient implements com.huawei.apm.core.service.dynamicconfig.kie.client.http.HttpClient {
    private static final Logger LOGGER = LogFactory.getLogger();
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
     * @param headers 请求头
     * @return HttpResult
     */
    @Override
    public HttpResult doGet(String url, Map<String, String> headers, RequestConfig requestConfig) {
        final HttpGet httpGet = new HttpGet(url);
        addDefaultHeaders(httpGet);
        addHeaders(httpGet, headers);
        if (requestConfig != null) {
            httpGet.setConfig(requestConfig);
        }
        HttpEntity entity = null;
        HttpResponse response = null;
        String result = null;
        try {
            response = this.httpClient.execute(httpGet);
            if (response == null) {
                return HttpResult.error();
            }
            entity = response.getEntity();
            if (entity == null) {
                return new HttpResult(response.getStatusLine().getStatusCode(), "", response.getAllHeaders());
            }
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException ex) {
            LOGGER.warning(String.format("Execute get request failed, %s", ex.getMessage()));
        } finally {
            try {
                EntityUtils.consume(entity);
            } catch (IOException ex) {
                LOGGER.warning(String.format("Consumed http entity failed, %s", ex.getMessage()));
            }
        }
        if (response == null) {
            return HttpResult.error();
        }
        return new HttpResult(response.getStatusLine().getStatusCode(), result, response.getAllHeaders());
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
        base.addHeader(HttpHeaders.USER_AGENT, "java-mesh/client");
    }

}
