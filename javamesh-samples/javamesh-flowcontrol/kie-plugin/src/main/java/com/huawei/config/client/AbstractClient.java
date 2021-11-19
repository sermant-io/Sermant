/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.client;

import com.huawei.config.http.DefaultHttpClient;
import com.huawei.config.http.HttpClient;

/**
 * 抽象客户端
 *
 * @author zhouss
 * @since 2021-11-17
 */
public abstract class AbstractClient implements Client {
    protected final ClientUrlManager clientUrlManager;

    protected final HttpClient httpClient;

    protected AbstractClient(ClientUrlManager clientUrlManager) {
        this.clientUrlManager = clientUrlManager;
        httpClient = new DefaultHttpClient();
    }

    protected AbstractClient(ClientUrlManager clientUrlManager, HttpClient httpClient) {
        this.clientUrlManager = clientUrlManager;
        this.httpClient = httpClient;
    }
}
