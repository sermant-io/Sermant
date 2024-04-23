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

package io.sermant.discovery.config;

/**
 * Load-balancing-related constants
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class LbConstants {
    /**
     * The timeout period of the ZK connection
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 2000;

    /**
     * The timeout period for the ZK response
     */
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    /**
     * ZK connection retry time
     */
    public static final int DEFAULT_RETRY_INTERVAL_MS = 3000;

    /**
     * The cache fetch time, in seconds, is 0 by default, which means that it will never expire
     */
    public static final long DEFAULT_CACHE_EXPIRE_SEC = 0L;

    /**
     * The timer execution interval is 5 seconds by default
     */
    public static final long DEFAULT_REFRESH_TIMER_INTERVAL_SEC = 5L;

    /**
     * Cache concurrency, which affects the efficiency of getting instances from the cache
     */
    public static final int DEFAULT_CACHE_CONCURRENCY_LEVEL = 16;

    /**
     * Service metric data cache, 60 minutes by default
     */
    public static final long DEFAULT_STATS_CACHE_EXPIRE_TIME = 60L;

    /**
     * If the refresh time of statistics is set to <=0, the aggregation statistics will not be enabled, and the load
     * balancer associated with the aggregation statistics will become invalid
     */
    public static final long DEFAULT_LB_STATS_REFRESH_INTERVAL_MS = 30000L;

    /**
     * The default time window for instance status statistics is 10 minutes, and the statistics will be cleared to 0 at
     * the beginning of each time window
     */
    public static final long DEFAULT_INSTANCE_STATE_TIME_WINDOW_MS = 600000L;

    /**
     * The maximum number of retries after the service times out
     */
    public static final int DEFAULT_MAX_RETRY = 3;

    /**
     * The maximum number of retries for the same instance
     */
    public static final int DEFAULT_MAX_SAME_RETRY = 3;

    /**
     * Retry wait time, default of one second
     */
    public static final long DEFAULT_RETRY_WAIT_MS = 1000L;

    /**
     * The maximum number of retry configuration caches
     */
    public static final int DEFAULT_MAX_RETRY_CONFIG_CACHE = 9999;

    /**
     * When there is a problem with the zk state, use an asynchronous attempt to retry, in this case the retry interval
     */
    public static final long DEFAULT_WAIT_REGISTRY_INTERVAL_MS = 1000L;

    /**
     * When there is a problem with the zk state, use an asynchronous attempt to retry, here is the maximum number of
     * engagements
     */
    public static final int DEFAULT_REGISTRY_MAX_RETRY_NUM = 60;

    private LbConstants() {
    }
}
