/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.agentcore.test.request.utils;

import com.huaweicloud.agentcore.test.request.dynamicconfig.DynamicConfigTest;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * http请求工具类
 *
 * @author tangle
 * @since 2023-09-26
 */
public class RequestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);

    /**
     * 对测试请求结果进行断言判断
     *
     * @param url 测试请求接口的url
     */
    public static void testRequest(String url) throws IOException {
        String response = doGet(url);
        Assertions.assertNotEquals("", response, url + " Request Error. ");
        Map<String, Boolean> resultMap = convertHttpEntityToMap(response);
        for (Map.Entry<String, Boolean> entry : resultMap.entrySet()) {
            Assertions.assertTrue(entry.getValue(), entry.getKey());
        }
    }

    /**
     * http的get请求
     *
     * @param url http请求url
     * @return 响应体body
     */
    private static String doGet(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return EntityUtils.toString(response.getEntity());
                } else {
                    LOGGER.info("Request error, the message is: {}", EntityUtils.toString(response.getEntity()));
                    return "";
                }
            }
        } catch (IOException e) {
            LOGGER.info("Request exception, the message is: {}", e.getMessage());
            return "";
        }
    }

    /**
     * JSON数据转换为Map
     *
     * @param response JSON数据
     * @return map数据
     */
    private static Map<String, Boolean> convertHttpEntityToMap(String response) throws IOException {
        Map<String, Boolean> result = new HashMap<>();
        JSONObject jsonObject = JSONObject.parseObject(response);
        for (String key : jsonObject.keySet()) {
            result.put(key, jsonObject.getBooleanValue(key));
        }
        return result;
    }
}
