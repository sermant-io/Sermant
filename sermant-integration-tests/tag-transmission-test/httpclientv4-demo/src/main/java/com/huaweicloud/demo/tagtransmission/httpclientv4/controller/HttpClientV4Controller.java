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

package com.huaweicloud.demo.tagtransmission.httpclientv4.controller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * controller，使用httpclient3.x调用http 服务端
 *
 * @author daizhenyu
 * @since 2023-10-14
 **/
@RestController
@RequestMapping(value = "httpClientV4")
public class HttpClientV4Controller {
    @Value("${common.server.url}")
    private String commonServerUrl;

    /**
     * 验证httpclient4.x透传流量标签
     *
     * @return 流量标签值
     * @throws IOException
     */
    @RequestMapping(value = "testHttpClientV4", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testHttpClientV4() throws IOException {
        return doHttpClientV4Get(commonServerUrl);
    }

    private String doHttpClientV4Get(String url) throws IOException {
        // 创建 CloseableHttpClient 实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        // 执行 GET 请求
        CloseableHttpResponse response = httpClient.execute(httpGet);
        String responseContext = EntityUtils.toString(response.getEntity());
        response.close();
        httpClient.close();
        return responseContext;
    }
}
