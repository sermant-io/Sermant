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

package com.huawei.flowre.flowreplay.invocation;

import com.huawei.flowre.flowreplay.domain.content.HttpInvokeContent;
import com.huawei.flowre.flowreplay.domain.message.HttpInvokeMessage;
import com.huawei.flowre.flowreplay.domain.result.HttpRequestResult;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * http调用封装
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-06-28
 */
@Component
public class HttpReplayInvocationImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpReplayInvocationImpl.class);

    private static final String CODE = "UTF-8";

    /**
     * Content-Length头
     */
    private static final String CONTENT_LENGTH = "Content-Length";

    @Autowired
    CloseableHttpClient httpClient;

    /**
     * 根据method构造不同的请求
     *
     * @param invokeMessage
     * @return 封装状态码和返回体
     */
    public HttpRequestResult invoke(HttpInvokeMessage invokeMessage) {
        HttpInvokeContent httpInvokeContent = invokeMessage.getHttpInvokeContent();
        HttpRequestResult httpRequestResult = new HttpRequestResult();
        switch (httpInvokeContent.getMethod()) {
            case "GET": {
                httpRequestResult = sendGet(httpInvokeContent.getUrl(), httpInvokeContent.getParams(),
                    httpInvokeContent.getHeaders());
                break;
            }
            case "POST": {
                if (httpInvokeContent.getParams() == null) {
                    httpRequestResult = sendPostJson(httpInvokeContent.getUrl(),
                        httpInvokeContent.getData(), httpInvokeContent.getHeaders());
                } else {
                    httpRequestResult = sendPostForm(httpInvokeContent.getUrl(),
                        httpInvokeContent.getParams(), httpInvokeContent.getHeaders());
                }
                break;
            }
            default:
        }
        return httpRequestResult;
    }

    /**
     * 填充请求头参数至get请求中
     *
     * @param httpGet 构造的httpPost
     * @param headers 请求头
     * @return 返回添加请求的httpGet
     */
    private HttpGet setHeadersToGet(HttpGet httpGet, Map<String, String> headers) {
        if (headers != null && headers.size() != 0) {
            for (String key : headers.keySet()) {
                if (CONTENT_LENGTH.equals(key)) {
                    continue;
                }
                httpGet.addHeader(key, headers.get(key));
            }
        }
        return httpGet;
    }

    /**
     * 填充请求头参数至Post请求中
     *
     * @param httpPost 构造的httpPost
     * @param headers  请求头
     * @return 返回添加请求头的httpPost
     */
    private HttpPost setHeadersToPost(HttpPost httpPost, Map<String, String> headers) {
        if (headers != null && headers.size() != 0) {
            for (String key : headers.keySet()) {
                if (CONTENT_LENGTH.equals(key)) {
                    continue;
                }
                httpPost.addHeader(key, headers.get(key));
            }
        }
        return httpPost;
    }

    /**
     * 填充请求体参数至Post请求中
     *
     * @param httpPost 构造的httpPost
     * @param params   form参数
     * @return 返回添加参数的 httpPost
     */
    private HttpPost setParamsToPost(HttpPost httpPost, Map<String, String> params) {
        List<NameValuePair> pairList = new ArrayList<>(params.size());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            NameValuePair pair;
            if (entry.getValue() == null) {
                pair = new BasicNameValuePair(entry.getKey(), null);
            } else {
                pair = new BasicNameValuePair(entry.getKey(), entry
                    .getValue());
            }
            pairList.add(pair);
        }
        httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName(CODE)));
        return httpPost;
    }

    /**
     * 添加url路径参数
     *
     * @param url    接口路径
     * @param params 路径参数
     * @return 返回包含路径参数的接口路径
     */
    public String addParamToUrl(String url, Map<String, String> params) {
        StringBuffer param = new StringBuffer();
        int paramIndex = 0;
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                if (paramIndex == 0) {
                    param.append("?");
                } else {
                    param.append("&");
                }
                param.append(key).append("=").append(params.get(key));
                paramIndex++;
            }
        }
        return url + param;
    }

    /**
     * 发送 GET 请求（HTTP），K-V形式，有请求头参数
     *
     * @param url     API接口URL
     * @param params  参数map
     * @param headers 请求头参数
     * @return http返回状态码和返回体
     */
    public HttpRequestResult sendGet(String url, Map<String, String> params, Map<String, String> headers) {
        HttpRequestResult httpRequestResult = new HttpRequestResult();
        String urlWithParam = addParamToUrl(url, params);
        HttpGet httpGet = new HttpGet(urlWithParam);
        httpGet = setHeadersToGet(httpGet, headers);
        try {
            Date start = new Date();
            CloseableHttpResponse response = httpClient.execute(httpGet);
            Date end = new Date();
            httpRequestResult.setStatusCode(response.getStatusLine().getStatusCode());
            httpRequestResult.setResponseBody(EntityUtils.toString(response.getEntity()));
            httpRequestResult.setResponseTime(end.getTime() - start.getTime());
            response.close();
        } catch (IOException ioException) {
            LOGGER.error("Do get error:{}", ioException.getMessage());
        }
        return httpRequestResult;
    }

    /**
     * 发送 POST 请求（HTTP），K-V形式 ，有请求头参数
     *
     * @param url     API接口URL
     * @param params  参数map
     * @param headers 请求头参数
     * @return http返回状态码和返回体
     */
    public HttpRequestResult sendPostForm(String url, Map<String, String> params, Map<String, String> headers) {
        HttpRequestResult httpRequestResult = new HttpRequestResult();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost = setHeadersToPost(httpPost, headers);
            httpPost = setParamsToPost(httpPost, params);
            Date start = new Date();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            Date end = new Date();
            httpRequestResult.setStatusCode(response.getStatusLine().getStatusCode());
            httpRequestResult.setResponseBody(EntityUtils.toString(response.getEntity(), CODE));
            httpRequestResult.setResponseTime(end.getTime() - start.getTime());
            response.close();
        } catch (IOException ioException) {
            LOGGER.error("Do post error:{}", ioException.getMessage());
        }
        return httpRequestResult;
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式，有请求头参数
     *
     * @param url      API接口URL
     * @param dataJson 请求体json字符串
     * @param headers  请求头参数
     * @return http返回状态码和返回体
     */
    public HttpRequestResult sendPostJson(String url, String dataJson, Map<String, String> headers) {
        HttpRequestResult httpRequestResult = new HttpRequestResult();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost = setHeadersToPost(httpPost, headers);
            httpPost.setEntity(new StringEntity(dataJson, CODE));
            Date start = new Date();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            Date end = new Date();
            httpRequestResult.setStatusCode(response.getStatusLine().getStatusCode());
            httpRequestResult.setResponseBody(EntityUtils.toString(response.getEntity(), CODE));
            httpRequestResult.setResponseTime(end.getTime() - start.getTime());
            response.close();
        } catch (IOException ioException) {
            LOGGER.error("Do post json body error:{}", ioException.getMessage());
        }
        return httpRequestResult;
    }
}