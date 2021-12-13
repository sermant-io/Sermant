/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowrecordreplay.console.rtc.common.utils;

/**
 * 用于核心功能的常量配置类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
public class RtcCoreConstants {
    /**
     * 线程池总线程数
     */
    public static final int KAFKA_CONSUMER_THREAD_POOL_THREADS = 10;

    /**
     * 线程池核心线程数
     */
    public static final int KAFKA_CONSUMER_THREAD_POOL_CORE_THREADS = 5;
    /**
     * 线程池等待队列大小，个数
     */
    public static final int KAFKA_CONSUMER_THREAD_POOL_BLOCKING_QUEUE_SIZE = 100;

    /**
     * consumer消费异常情况下，重试机制里的重试次数
     */
    public static final int RETRIES = 5;

    /**
     * 在存入redis中的metric，设置的过期时间
     */
    public static final int METRIC_EXPIRE_TIME = 24 * 60 * 60;

    private RtcCoreConstants() {
    }
}
