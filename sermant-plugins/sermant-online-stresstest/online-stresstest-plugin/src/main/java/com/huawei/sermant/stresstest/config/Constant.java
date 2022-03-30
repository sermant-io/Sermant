/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.config;

/**
 * 常用变量信息
 *
 * @author yiwei
 * @since 2021-10-25
 */
public class Constant {
    /**
     * db
     */
    public static final String DB = "db";

    /**
     * topic
     */
    public static final String TEST_TOPIC = "topic";

    /**
     * redis.key
     */
    public static final String REDIS_KEY = "redis.key";

    /**
     * redis.repository
     */
    public static final String REDIS_REPOSITORY = "redis.repository";

    /**
     * mongo.key
     */
    public static final String MONGO_KEY = "mongo.key";

    /**
     * mongo.repository
     */
    public static final String MONGO_REPOSITORY = "mongo.repository";

    /**
     * shadow
     */
    public static final String SHADOW = "shadow_";

    /**
     * 压测标记key
     */
    public static final String TEST_FLAG = "x-test";
    /**
     * 压测标记值
     */
    public static final String TEST_VALUE = "true";

    private Constant() {
    }
}
