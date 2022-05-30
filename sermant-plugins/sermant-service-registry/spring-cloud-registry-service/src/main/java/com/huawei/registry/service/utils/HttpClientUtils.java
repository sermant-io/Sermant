/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * httpClient工具类
 *
 * @author provenceee
 * @since 2022-05-26
 */
public enum HttpClientUtils {
    /**
     * 单例
     */
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 设置连接超时时间，单位毫秒。指三次握手的超时时间
     */
    private static final int CONNECT_TIMEOUT = 2000;

    /**
     * 请求获取数据的超时时间(即响应时间)，单位毫秒。
     */
    private static final int SOCKET_TIMEOUT = 2000;

    private static final int MAX_TOTAL_POOL = 100;

    private static final int REQUEST_TIMEOUT = 3000;

    private final CloseableHttpClient client;

    HttpClientUtils() {
        this.client = createClient();
    }

    private CloseableHttpClient createClient() {
        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE).build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
            socketFactoryRegistry);
        connManager.setMaxTotal(MAX_TOTAL_POOL);
        connManager.setDefaultMaxPerRoute(MAX_TOTAL_POOL);
        RequestConfig.Builder configBuilder = RequestConfig.custom();

        // 设置连接超时
        configBuilder.setConnectTimeout(CONNECT_TIMEOUT);

        // 设置读取超时
        configBuilder.setSocketTimeout(SOCKET_TIMEOUT);

        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(REQUEST_TIMEOUT);

        // 创建自定义的httpclient对象
        return HttpClients.custom()
            .setConnectionManager(connManager)
            .setConnectionManagerShared(true)
            .setDefaultRequestConfig(configBuilder.build())
            .build();
    }

    /**
     * 发送post请求；带请求头和请求参数
     *
     * @param url 请求地址
     * @param json 请求参数
     * @param header 请求头
     * @return HttpClientResult结果包装类
     */
    public HttpClientResult doPost(String url, String json, Map<String, String> header) {
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);

        // 创建httpResponse对象
        try (CloseableHttpResponse httpResponse = client.execute(packageParam(httpPost, json, header))) {
            // 执行请求并获得响应结果
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
        Map<String, String> header) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(json, Consts.UTF_8);
        entity.setContentType(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
        entity.setContentEncoding(Consts.UTF_8.name());
        httpMethod.setEntity(entity);
        if (header != null) {
            Set<Entry<String, String>> entrySet = header.entrySet();
            for (Entry<String, String> entry : entrySet) {
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return httpMethod;
    }
}