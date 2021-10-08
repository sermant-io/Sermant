/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowreplay.mockclient.httpclient;

import com.lubanops.apm.plugin.flowreplay.mockclient.config.PluginConfig;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 分装http请求 用于mock client向mock server发送请求获取mock结果
 *
 * @author luanwenfei
 * @version 1.0 2021-02-08
 * @since 2021-02-08
 */
public class MockHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockHttpClient.class);

    private static volatile HttpClient httpClient = HttpClientBuilder.create().build();

    private MockHttpClient() {
    }

    public static HttpClient getInstance() {
        if (httpClient == null) {
            synchronized (HttpClient.class) {
                if (httpClient == null) {
                    httpClient = HttpClientBuilder.create().build();
                }
            }
        }
        return httpClient;
    }

    /**
     * 封装post请求
     *
     * @param url  http请求的url
     * @param data post的数据
     * @return String
     */
    public static String post(String url, String data) {
        String result = PluginConfig.RETURN_BLANK;
        httpClient = getInstance();
        HttpPost httpPost = new HttpPost(url);

        // 连接配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(PluginConfig.httpTimeout).setConnectTimeout(PluginConfig.httpTimeout).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(data, "UTF-8"));
        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == PluginConfig.httpSuccessStatus) {
                result = EntityUtils.toString(response.getEntity());
            } else {
                LOGGER.error("Post server error：{}", statusCode);
                return PluginConfig.RETURN_BLANK;
            }
        } catch (IOException ioException) {
            LOGGER.error("Caught IOException:{}", ioException.getMessage());
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.error("Caught illegalArgumentException:{}", illegalArgumentException.getMessage());
        }
        return result;
    }
}
