/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.core.service.dynamicconfig.kie.client.http;

import com.huawei.javamesh.core.service.dynamicconfig.kie.client.Client;
import org.apache.http.client.config.RequestConfig;

import java.util.Map;

/**
 * http请求
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface HttpClient extends Client {

    /**
     * get请求
     *
     * @param url 请求地址
     * @param headers 请求头
     * @param requestConfig 请求配置
     * @return HttpResult
     */
    HttpResult doGet(String url, Map<String, String> headers, RequestConfig requestConfig);

    /**
     * get请求
     *
     * @param url 请求地址
     * @return HttpResult
     */
    HttpResult doGet(String url);

    /**
     * get请求
     *
     * @param url 请求地址
     * @param requestConfig 请求配置
     * @return HttpResult
     */
    HttpResult doGet(String url, RequestConfig requestConfig);

}
