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

package com.huawei.discovery.config;

/**
 * 负载均衡相关常量
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class LbConstants {
    /**
     * ZK连接超时时间
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 2000;

    /**
     * ZK响应超时时间
     */
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    /**
     * ZK连接重试时间
     */
    public static final int DEFAULT_RETRY_INTERVAL_MS = 3000;

    /**
     * 缓存获取时间, 单位秒, 默认0, 表示永远不过期
     */
    public static final long DEFAULT_CACHE_EXPIRE_SEC = 0L;

    /**
     * 定时器执行间隔, 默认5秒
     */
    public static final long DEFAULT_REFRESH_TIMER_INTERVAL_SEC = 5L;

    /**
     * 缓存并发度, 影响从缓存获取实例的效率
     */
    public static final int DEFAULT_CACHE_CONCURRENCY_LEVEL = 16;

    /**
     * 服务指标数据缓存, 默认60分钟
     */
    public static final long DEFAULT_STATS_CACHE_EXPIRE_TIME = 60L;

    /**
     * 统计数据定时聚合统计刷新时间, 若设置<=0, 则不会开启聚合统计, 关联聚合统计的负载均衡将会失效
     */
    public static final long DEFAULT_LB_STATS_REFRESH_INTERVAL_MS = 30000L;

    /**
     * 实例状态统计时间窗口, 默认10分钟, 每一个时间窗口的开始, 统计都会清0
     */
    public static final long DEFAULT_INSTANCE_STATE_TIME_WINDOW_MS = 600000L;

    /**
     * 服务超时后最大重试次数
     */
    public static final int DEFAULT_MAX_RETRY = 3;

    /**
     * 最大相同实例的重试次数
     */
    public static final int DEFAULT_MAX_SAME_RETRY = 3;

    /**
     * 重试等待时间, 默认一秒
     */
    public static final long DEFAULT_RETRY_WAIT_MS = 1000L;

    /**
     * 最大的重试配置缓存数
     */
    public static final int DEFAULT_MAX_RETRY_CONFIG_CACHE = 9999;

    /**
     * 当zk状态存在问题时, 使用异步尝试重试, 此处为重试时间间隔
     */
    public static final long DEFAULT_WAIT_REGISTRY_INTERVAL_MS = 1000L;

    /**
     * 当zk状态存在问题时, 使用异步尝试重试, 此处为最大从事次数
     */
    public static final int DEFAULT_REGISTRY_MAX_RETRY_NUM = 60;

    private LbConstants() {
    }
}
