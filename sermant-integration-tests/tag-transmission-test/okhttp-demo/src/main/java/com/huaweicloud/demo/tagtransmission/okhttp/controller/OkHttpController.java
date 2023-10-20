/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.demo.tagtransmission.okhttp.controller;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * controller，使用okhttp调用http 服务端
 *
 * @author daizhenyu
 * @since 2023-10-14
 **/
@RestController
@RequestMapping(value = "okHttp")
public class OkHttpController {
    @Value("${common.server.url}")
    private String commonServerUrl;

    /**
     * 验证okhttp透传流量标签
     *
     * @return 流量标签值
     * @throws IOException
     */
    @RequestMapping(value = "testOkHttp", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testOkHttp() throws IOException {
        return doOkHttpGet(commonServerUrl);
    }

    private String doOkHttpGet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 创建 HTTP 请求
        Request request = new Request.Builder()
                .url(url)
                .build();

        // 执行请求
        Response response = client.newCall(request).execute();
        String responseContext = response.body().string();
        response.body().close();

        return responseContext;
    }
}
