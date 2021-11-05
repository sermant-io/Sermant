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

    String TEST_TOPIC = "topic";

    String REDIS_KEY = "redis.key";

    String REDIS_REPOSITORY = "redis.repository";

    String MONGO_KEY = "mongo.key";

    String MONGO_REPOSITORY = "mongo.repository";

    String SHADOW = "shadow_";

    String HTTP_INTERCEPTOR = "com.lubanops.stresstest.http.HttpClientInterceptor";
    /**
     * 压测标记key
     */
    String TEST_FLAG = "x-test";
    /**
     * 压测标记值
     */
    String TEST_VALUE = "true";
}
