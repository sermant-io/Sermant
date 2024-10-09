/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.demo.spring.client;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * SpringRouterController
 *
 * @author daizhenyu
 * @since 2024-10-08
 **/
@RequestMapping("router")
@RestController
public class SpringRouterController {
    private static final String VERSION = "version";

    private static final int SUCCEED_CODE = 200;

    private static final String ROUTER_METHOD_PATH = "/router";

    /**
     * test okhttp2 routing
     *
     * @param host host
     * @param version version
     * @return result
     */
    @RequestMapping("okHttp2")
    public String testOkHttp2Routing(String host, String version) {
        String url = buildUrl(host);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader(VERSION, version)
                .build();
        try {
            Response response = client.newCall(request).execute();
            int statusCode = response.code();
            if (statusCode == SUCCEED_CODE) {
                return response.body().string();
            } else {
                return "";
            }
        } catch (IOException e) {
            return "";
        }
    }

    private String buildUrl(String host) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://");
        urlBuilder.append(host);
        urlBuilder.append(ROUTER_METHOD_PATH);
        return urlBuilder.toString();
    }
}
