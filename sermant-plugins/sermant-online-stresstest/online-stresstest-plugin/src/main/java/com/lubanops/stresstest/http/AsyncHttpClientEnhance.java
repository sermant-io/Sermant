/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.http;


import static com.lubanops.stresstest.config.Constant.HTTP_INTERCEPTOR;

/**
 * AsyncHttpClient 增强
 *
 * @author yiwei
 * @since 2021/10/25
 */
public class AsyncHttpClientEnhance extends AbstractHttpClientEnhance {
    private static final String ENHANCE_CLASS = "org.apache.http.impl.nio.client.CloseableHttpAsyncClient";

    public AsyncHttpClientEnhance() {
        super(ENHANCE_CLASS, HTTP_INTERCEPTOR);
    }
}
