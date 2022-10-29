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

package com.huaweicloud.spring.rest.consumer;

import com.huaweicloud.spring.common.registry.common.RegistryConstants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

/**
 * HttpClient测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.REGISTRY_REQUEST_PREFIX)
public class BootRegistryHttpClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootRegistryHttpClientController.class);

    private static final int SLEEP_MS = 2000;

    private final String getMethod = "get";

    private final String postMethod = "post";

    private final IOException reqError = new IOException("req error");

    @Resource(name = "defaultHttpClient")
    private HttpClient defaultHttpClient;

    @Resource(name = "minimalHttpClient")
    private HttpClient minimalHttpClient;

    @Resource(name = "httpClient")
    private HttpClient httpClient;

    @Resource(name = "minimalHttpAsyncClient")
    private HttpAsyncClient minimalHttpAsyncClient;

    @Resource(name = "internalHttpAsyncClient")
    private HttpAsyncClient internalHttpAsyncClient;

    @Value("${config.domain:www.domain.com}")
    private String domain;

    @Value("${config.downStreamB:rest-provider}")
    private String downStreamB;

    @Value("${config.futureTimeout:4000}")
    private int futureTimeout;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10, r -> new Thread(r, "async"));

    /**
     * 默认get请求
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("defaultHttpClientGet")
    public String defaultHttpClientGet() throws IOException {
        return invokeHttpClient(defaultHttpClient, new HttpGet(buildUrl(getMethod)));
    }

    /**
     * 默认post请求
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("defaultHttpClientPost")
    public String defaultHttpClientPost() throws IOException {
        return invokeHttpClient(defaultHttpClient, new HttpPost(buildUrl(postMethod)));
    }

    /**
     * min get请求
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("minHttpClientGet")
    public String minHttpClientGet() throws IOException {
        return invokeHttpClient(minimalHttpClient, new HttpGet(buildUrl(getMethod)));
    }

    /**
     * min post请求
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("minHttpClientPost")
    public String minHttpClientPost() throws IOException {
        return invokeHttpClient(minimalHttpClient, new HttpPost(buildUrl(postMethod)));
    }

    /**
     * httpclient get
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("httpClientGet")
    public String httpClientGet() throws IOException {
        return invokeHttpClient(httpClient, new HttpGet(buildUrl(getMethod)));
    }

    /**
     * httpclient post
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("httpClientPost")
    public String httpClientPost() throws IOException {
        return invokeHttpClient(httpClient, new HttpPost(buildUrl(postMethod)));
    }

    /**
     * httpclient 重试
     *
     * @return 请求结果
     * @throws IOException 请求异常抛出
     */
    @GetMapping("httpClientRetry")
    public String httpClientRetry() throws IOException {
        return invokeHttpClient(httpClient, new HttpGet(buildUrl("retry")));
    }

    private String invokeHttpClient(HttpClient curClient, HttpRequestBase requestBase) throws IOException {
        HttpResponse response = null;
        try {
            response = curClient.execute(requestBase);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw reqError;
            }
            return EntityUtils.toString(response.getEntity());
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    /**
     * httpAsyncClient get
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("httpAsyncClientGet")
    public String httpAsyncClientGet() throws Exception {
        return asyncInvoke(internalHttpAsyncClient, new HttpGet(buildUrl(getMethod)));
    }

    /**
     * httpAsyncClient post
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("httpAsyncClientPost")
    public String httpAsyncClientPost() throws Exception {
        return asyncInvoke(internalHttpAsyncClient, new HttpPost(buildUrl(postMethod)));
    }

    /**
     * minimalHttpAsyncClient get
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("minimalHttpAsyncClientPost")
    public String minimalHttpAsyncClientPost() throws Exception {
        return asyncInvoke(minimalHttpAsyncClient, new HttpPost(buildUrl(postMethod)));
    }

    /**
     * minimalHttpAsyncClient post
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("minimalHttpAsyncClientGet")
    public String minimalHttpAsyncClientGet() throws Exception {
        return asyncInvoke(minimalHttpAsyncClient, new HttpGet(buildUrl(getMethod)));
    }

    private String asyncInvoke(HttpAsyncClient httpAsyncClient, HttpRequestBase requestBase)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        final Future<HttpResponse> execute = getFuture(httpAsyncClient, requestBase);

        HttpResponse response = null;
        try {
            response = execute.get(futureTimeout, TimeUnit.SECONDS);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw reqError;
            }
            return EntityUtils.toString(response.getEntity());
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    /**
     * minimalHttpAsyncClientThread get
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("minimalHttpAsyncClientThreadGet")
    public String minimalHttpAsyncClientThreadGet() throws Exception {
        return asyncInvokerThread(minimalHttpAsyncClient, new HttpGet(buildUrl(getMethod)));
    }

    /**
     * minimalHttpAsyncClientThread post
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("minimalHttpAsyncClientThreadPost")
    public String minimalHttpAsyncClientThreadPost() throws Exception {
        return asyncInvokerThread(minimalHttpAsyncClient, new HttpPost(buildUrl(postMethod)));
    }

    /**
     * httpAsyncClientThread get
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("httpAsyncClientThreadGet")
    public String httpAsyncClientThreadGet() throws Exception {
        return asyncInvokerThread(internalHttpAsyncClient, new HttpGet(buildUrl(getMethod)));
    }

    /**
     * httpAsyncClientThread post
     *
     * @return 请求结果
     * @throws Exception 请求异常抛出
     */
    @GetMapping("httpAsyncClientThreadPost")
    public String httpAsyncClientThreadPost() throws Exception {
        return asyncInvokerThread(internalHttpAsyncClient, new HttpPost(buildUrl(postMethod)));
    }

    private Future<HttpResponse> getFuture(HttpAsyncClient httpAsyncClient, HttpRequestBase requestBase) {
        return httpAsyncClient.execute(requestBase, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                LOGGER.info(String.valueOf(result));
            }

            @Override
            public void failed(Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }

            @Override
            public void cancelled() {
            }
        });
    }

    private String asyncInvokerThread(HttpAsyncClient httpAsyncClient, HttpRequestBase requestBase)
            throws IOException, InterruptedException {
        final Future<HttpResponse> execute = getFuture(httpAsyncClient, requestBase);
        AtomicReference<HttpResponse> response = new AtomicReference<>();
        try {
            final AtomicBoolean isError = new AtomicBoolean();
            executorService.execute(() -> {
                try {
                    response.set(execute.get(futureTimeout, TimeUnit.SECONDS));
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOGGER.error(e.getMessage(), e);
                    isError.set(true);
                }
            });
            Thread.sleep(SLEEP_MS);
            if (isError.get()) {
                throw reqError;
            }
            return EntityUtils.toString(response.get().getEntity());
        } finally {
            if (response.get() != null) {
                EntityUtils.consume(response.get().getEntity());
            }
        }
    }

    private String buildUrl(String path) {
        return String.format(Locale.ENGLISH, "http://%s/%s/%s/%s", domain, downStreamB,
                RegistryConstants.REGISTRY_REQUEST_PREFIX, path);
    }
}
