/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.client;

import com.huawei.apm.core.service.dynamicconfig.kie.client.http.DefaultHttpClient;
import com.huawei.apm.core.service.dynamicconfig.kie.client.http.HttpClient;

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
