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

package com.huawei.test.configelement.service;

import com.huawei.test.configelement.config.HttpClientConfig;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Set;

/**
 * 功能描述：
 *
 * @author hjw
 * @since 2022-01-25
 */
public interface HttpClientServiceInterface {
    /**
     * 发送get请求
     *
     * @param config httpclient配置类对象
     * @return 返回结果
     */
    HttpResponse executeGet(HttpClientConfig config);

    /**
     * 发送表单类型的post请求
     *
     * @param config 请求配置类参数
     * @param param 参数列表
     * @return 返回结果
     */
    HttpResponse postForm(HttpClientConfig config, List<NameValuePair> param);

    /**
     * 发送json类型的post请求
     *
     * @param config 请求配置类参数
     * @param jsonBody 参数列表
     * @return 返回结果
     */
    HttpResponse postJson(HttpClientConfig config, String jsonBody);

    /**
     * 发送json类型的put请求
     *
     * @param config 请求配置类参数
     * @param jsonBody 参数列表
     * @return 返回结果
     */
    HttpResponse putJson(HttpClientConfig config, String jsonBody);

    /**
     * 发送json类型的delete请求
     *
     * @param config 请求配置类参数
     * @param jsonBody 参数列表
     * @return 返回结果
     */
    HttpResponse executeDelete(HttpClientConfig config, String jsonBody);

    /**
     * 发送 head请求，不会返回消息体
     *
     * @param config 请求配置类参数
     * @return 返回结果
     */
    HttpResponse executeHead(HttpClientConfig config);

    /**
     * 发送 options请求用于获取服务器支持的HTTP请求方法
     *
     * @param config 请求配置类参数
     * @return 返回结果
     */
    Set<String> executeOptions(HttpClientConfig config);
}
