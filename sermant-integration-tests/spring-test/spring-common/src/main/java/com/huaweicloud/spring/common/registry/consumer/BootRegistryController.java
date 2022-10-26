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

package com.huaweicloud.spring.common.registry.consumer;

import com.huaweicloud.spring.common.registry.common.RegistryConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 注册控制器
 *
 * @author zhouss
 * @since 2022-10-25
 */
@Controller
@ResponseBody
@RequestMapping(RegistryConstants.REGISTRY_REQUEST_PREFIX)
public class BootRegistryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootRegistryController.class);

    private final String getMethod = "GET";

    private final String emptyJson = "{}";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${config.domain:www.domain.com}")
    private String domain;

    @Value("${config.downStream:rest-provider}")
    private String downStream;

    @Value("${config.connectTimeout:1000}")
    private int connectTimeout;

    @Value("${config.readTimeout:1000}")
    private int readTimeout;

    /**
     * 测试rest调用
     *
     * @return ok
     */
    @RequestMapping("restRegistry")
    public String restRegistry() {
        return get("restRegistry");
    }

    /**
     * 测试rest调用
     *
     * @return ok
     */
    @RequestMapping("restRegistryPost")
    public String restRegistryPost() {
        return post("restRegistryPost");
    }

    /**
     * post测试 HttpURLConnection
     *
     * @return ok
     * @throws Exception 请求异常时抛出
     */
    @RequestMapping("urlConnectionPostNoEntity")
    public String urlConnectionPostNoEntity() throws Exception {
        String params = "";
        String url = buildUrl("post");
        final Map<String, String> headers = new HashMap<>();
        return urlConnection(url, params, headers, "POST");
    }

    /**
     * get测试 HttpURLConnection
     *
     * @return ok
     * @throws Exception 请求异常时抛出
     */
    @RequestMapping("urlConnectionGet")
    public String urlConnectionGet() throws Exception {
        String url = buildUrl("get");
        final Map<String, String> headers = new HashMap<>();
        return urlConnection(url, emptyJson, headers, getMethod);
    }

    /**
     * 超时重试测试 HttpURLConnection
     *
     * @return ok
     * @throws Exception 请求异常时抛出
     */
    @RequestMapping("urlConnectionRetry")
    public String urlConnectionRetry() throws Exception {
        String url = buildUrl("retry");
        final Map<String, String> headers = new HashMap<>();
        return urlConnection(url, emptyJson, headers, getMethod);
    }

    private String urlConnection(String url, String params, Map<String, String> headers, String method)
            throws Exception {
        String result;
        HttpURLConnection connection = null;
        try {
            URL uri = new URL(url);
            connection = (HttpURLConnection) uri.openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (getMethod.equalsIgnoreCase(method)) {
                connection.connect();
            } else {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(params.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
            }
            int responseCode = connection.getResponseCode();
            if (HttpStatus.OK.value() == responseCode) {
                result = getResult(connection.getInputStream());
                LOGGER.info("success=======");
            } else {
                final String error = getResult(connection.getErrorStream());
                LOGGER.info("Failed:======={}", error);
                throw new Exception("Url connection request error!");
            }
            LOGGER.debug("postJSON request finish, url:{}, request:{}, response：{}", url, params, result);
            return result;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getResult(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private String get(String apiPath) {
        return restTemplate.getForObject(buildUrl(apiPath), String.class);
    }

    private String post(String apiPath) {
        return restTemplate.postForObject(buildUrl(apiPath), "{}", String.class);
    }

    private String buildUrl(String apiPath) {
        return String.format(Locale.ENGLISH, "http://%s/%s/%s/%s", domain, downStream,
                RegistryConstants.REGISTRY_REQUEST_PREFIX, apiPath);
    }
}
