/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.test.configelement.service.impl;

import com.huawei.test.configelement.ConfigElement;
import com.huawei.test.configelement.config.HttpClientConfig;
import com.huawei.test.configelement.service.HttpClientServiceInterface;
import com.huawei.test.configelement.service.HttpDeleteWithBody;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 功能描述：
 *
 * @author hjw
 * @since 2022-01-25
 */
public class HttpClientService extends ConfigElement<HttpClientConfig> implements HttpClientServiceInterface {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    private static final String CONNECTION = "Connection";
    private static final String KEEP_ALIVE = "Keep-Alive";

    /**
     * 请求参数配置
     */
    private static CloseableHttpClient closeableHttpClient;
    private RequestConfig requestConfig;

    public HttpClientService() {
    }

    @Override
    public void initConfig(HttpClientConfig config) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();

        // 设置请求的默认配置
        requestConfig = setRequestConfig(config);
        /*
         * 设置默认保持长连接
         */
        List<Header> defaultHeaders = new ArrayList<>();
        BasicHeader basicHeader = new BasicHeader(CONNECTION, KEEP_ALIVE);
        defaultHeaders.add(basicHeader);
        httpClientBuilder.setDefaultHeaders(defaultHeaders);

        // 如果关闭连接池复用
        if (!config.getUseConnectPool()) {
            LOGGER.info("The Connect Pool of HttpClient is Closed.");
        } else {
            LOGGER.info("The Connect Pool of HttpClient is Opened.");

            // 绕过不安全的https请求的证书验证
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", trustHttpCertificates())
                .build();

            // 创建连接池对象
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);

            // 连接池最大连接
            connectionManager.setMaxTotal(config.getMaxTotal());
            httpClientBuilder.setConnectionManager(connectionManager);
        }
        closeableHttpClient = httpClientBuilder.setDefaultRequestConfig(requestConfig).build();
    }

    @Override
    public boolean isConfigValid() {
        return false;
    }

    /**
     *  设置请求头参数
     *
     * @param requestBase 请求对象基类
     * @param headers 请求头参数
     */
    private void setHeader(HttpRequestBase requestBase, Map<String, String> headers) {
        if (headers != null) {
            Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry:entries) {
                requestBase.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * 获取 请求的响应结果
     *
     * @param requestBase 请求对象基类
     * @return 返回响应结果
     */
    private HttpResponse getHttpResponse(HttpRequestBase requestBase) {
        try {
            CloseableHttpResponse response = closeableHttpClient.execute(requestBase);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("The response is ok: {}", statusLine.getStatusCode());
                EntityUtils.consume(response.getEntity());
                return response;
            } else {
                LOGGER.error("The response is error: {}", statusLine.getStatusCode());
                throw new ClientProtocolException("Unexpected response status");
            }
        } catch (IOException e) {
            LOGGER.error("The response is error");
        }
        return null;
    }

    /**
     * 发送get请求
     *
     * @param config httpclient配置类对象
     * @return 返回结果
     */
    @Override
    public HttpResponse executeGet(HttpClientConfig config) {
        HttpRequestBase httpGet = new HttpGet(config.getUrl());
        setHeader(httpGet, config.getHeaders());
        return getHttpResponse(httpGet);
    }

    /**
     * 发送表单类型的post请求
     *
     * @param config 请求配置类参数
     * @param param 参数列表
     * @return 返回结果x
     */
    @Override
    public HttpResponse postForm(HttpClientConfig config, List<NameValuePair> param) {
        HttpEntityEnclosingRequestBase httpPost = new HttpPost(config.getUrl());
        setHeader(httpPost, config.getHeaders());
        httpPost.addHeader(CONTENT_TYPE, CONTENT_TYPE_FORM);
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(param, Consts.UTF_8);
        httpPost.setEntity(formEntity);

        return getHttpResponse(httpPost);
    }

    /**
     * 发送json类型的post请求
     *
     * @param config 请求配置类参数
     * @param jsonBody 参数列表
     * @return 返回结果
     */
    @Override
    public HttpResponse postJson(HttpClientConfig config, String jsonBody) {
        HttpEntityEnclosingRequestBase httpPost = new HttpPost(config.getUrl());
        return setJsonObject(config, jsonBody, httpPost);
    }

    /**
     * 发送json类型的put请求
     *
     * @param config 请求配置类参数
     * @param jsonBody 参数列表
     * @return 返回结果
     */
    @Override
    public HttpResponse putJson(HttpClientConfig config, String jsonBody) {
        HttpEntityEnclosingRequestBase httpPut = new HttpPut(config.getUrl());
        return setJsonObject(config, jsonBody, httpPut);
    }

    /**
     * 发送json类型的delete请求
     *
     * @param config 请求配置类参数
     * @param jsonBody 参数列表
     * @return 返回结果
     */
    @Override
    public HttpResponse executeDelete(HttpClientConfig config, String jsonBody) {
        HttpEntityEnclosingRequestBase httpDelete = new HttpDeleteWithBody(config.getUrl());
        return setJsonObject(config, jsonBody, httpDelete);
    }

    /**
     * 发送 head请求，不会返回消息体
     *
     * @param config 请求配置类参数
     * @return 返回结果
     */
    @Override
    public HttpResponse executeHead(HttpClientConfig config) {
        HttpRequestBase httpHead = new HttpHead(config.getUrl());
        setHeader(httpHead, config.getHeaders());
        return getHttpResponse(httpHead);
    }

    /**
     * 发送 options请求用于获取服务器支持的HTTP请求方法
     *
     * @param config 请求配置类参数
     * @return 返回结果
     */
    @Override
    public Set<String> executeOptions(HttpClientConfig config) {
        HttpOptions httpOptions = new HttpOptions(config.getUrl());
        setHeader(httpOptions, config.getHeaders());

        CloseableHttpResponse response;
        try {
            response = closeableHttpClient.execute(httpOptions);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                LOGGER.info("The http response is ok: {}", statusLine.getStatusCode());
                return httpOptions.getAllowedMethods(response);
            } else {
                LOGGER.error("The http response is error: {}", statusLine.getStatusCode());
            }
        } catch (IOException e) {
            LOGGER.error("The http response is error");
        }
        return null;
    }

    /**
     * 设置 json数据
     *
     * @param config 配置类对象
     * @param jsonBody json类型字符串数据
     * @param requestBase 请求对象基类
     * @return 返回响应结果
     */
    private HttpResponse setJsonObject(HttpClientConfig config, String jsonBody,
                                       HttpEntityEnclosingRequestBase requestBase) {
        setHeader(requestBase, config.getHeaders());
        requestBase.addHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        StringEntity jsonEntity = new StringEntity(jsonBody, Consts.UTF_8);
        jsonEntity.setContentEncoding(Consts.UTF_8.name());
        requestBase.setEntity(jsonEntity);

        return getHttpResponse(requestBase);
    }

    /**
     * 设置请求参数
     *
     * @param config httpclient配置对象
     * @return 返回请求配置
     */
    private RequestConfig setRequestConfig(HttpClientConfig config) {
        return RequestConfig.custom()
            // 连接超时，完成tcp 3次握手的时间上限
            .setConnectTimeout(config.getConnectTimeout())
            // 读取超时，表示从请求的网址处获得响应数据的时间间隔
            .setSocketTimeout(config.getSocketTimeout())
            .build();
    }

    /**
     * 创建支持安全协议的工程
     *
     * @return ConnectionSocketFactory
     */
    private ConnectionSocketFactory trustHttpCertificates() {
        X509TrustManager x509m = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) {
            }
        };
        try {
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{x509m}, null);

            return new SSLConnectionSocketFactory(ctx,
                new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null,
                NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to construct secure connection factory");
        }
    }
}
