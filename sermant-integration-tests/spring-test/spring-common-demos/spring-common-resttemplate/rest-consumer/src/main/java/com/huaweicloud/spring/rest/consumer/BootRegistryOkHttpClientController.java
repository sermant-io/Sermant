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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * okHttpClient测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.REGISTRY_REQUEST_PREFIX)
public class BootRegistryOkHttpClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootRegistryOkHttpClientController.class);

    @Value("${config.domain:www.domain.com}")
    private String domain;

    @Value("${config.downStreamB:rest-provider}")
    private String downStreamB;

    /**
     * get请求测试
     *
     * @return String
     * @throws Exception 请求异常抛出
     */
    @GetMapping("/okHttpClientGet")
    public String okHttpClientGet() throws Exception {
        return sync(buildUrl("get"));
    }

    /**
     * post + 异步
     *
     * @return ok
     * @throws Exception 请求异常抛出
     */
    @GetMapping("/okHttpClientAsyncPost")
    public String okHttpClientAsyncPost() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Call call = client.newCall(createPostBody(buildUrl("post")));
        final AtomicReference<String> result = new AtomicReference<String>();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        String errorFlag = "error";
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException ex) {
                result.set(errorFlag + " " + ex.getMessage());
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.code() != HttpStatus.SC_OK || response.body() == null) {
                        String msg = "failed: " + response.code() + " " + response.message();
                        result.set(msg);
                        return;
                    }
                    result.set(new String(response.body().bytes(), StandardCharsets.UTF_8));
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
        if (result.get().contains(errorFlag)) {
            throw new Exception("ok client req error!");
        }
        return result.get();
    }

    private String sync(String url) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request rq = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(rq);
        Response execute;
        execute = call.execute();
        if (execute.code() != HttpStatus.SC_OK || execute.body() == null) {
            throw new Exception("ok http client request error!");
        }
        return new String(execute.body().bytes(), StandardCharsets.UTF_8);
    }

    private Request createPostBody(String url) {
        String json = "{}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json);
        Request.Builder builder = new Request.Builder();
        return builder
                .url(url)
                .post(requestBody)
                .build();
    }

    private String buildUrl(String path) {
        return String.format(Locale.ENGLISH, "http://%s/%s/%s/%s", domain, downStreamB,
                RegistryConstants.REGISTRY_REQUEST_PREFIX, path);
    }
}
