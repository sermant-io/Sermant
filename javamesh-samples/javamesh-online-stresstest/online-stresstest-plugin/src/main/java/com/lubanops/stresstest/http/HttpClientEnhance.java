/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.http;


import static com.lubanops.stresstest.config.Constant.HTTP_INTERCEPTOR;

/**
 * HttpClient 增强
 *
 * @author yiwei
 * @since 2021/10/25
 */
public class HttpClientEnhance extends AbstractHttpClientEnhance {
    private static final String ENHANCE_CLASS = "javax.servlet.http.HttpServlet";

    public HttpClientEnhance() {
        super(ENHANCE_CLASS, HTTP_INTERCEPTOR);
    }
}
