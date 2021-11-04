/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.config;

/**
 * 常用变量信息
 *
 * @author yiwei
 * @since 2021/10/25
 */
public interface Constant {
    String DB = "db";

    String TEST_TOPIC = "test.topic";

    String SHADOW = "shadow_";

    String HTTP_INTERCEPTOR = "com.lubanops.stresstest.http.HttpClientInterceptor";
}
