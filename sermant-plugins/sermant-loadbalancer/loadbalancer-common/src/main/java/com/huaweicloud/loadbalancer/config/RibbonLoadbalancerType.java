/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.config;

/**
 * ribbon负载均衡策略
 *
 * @author provenceee
 * @since 2022-01-21
 */
public enum RibbonLoadbalancerType {
    /**
     * 随机
     */
    RANDOM,

    /**
     * 轮询
     */
    ROUND_ROBIN,

    /**
     * 重试策略
     */
    RETRY,

    /**
     * 最低并发策略
     */
    BEST_AVAILABLE,

    /**
     * 可用过滤策略
     */
    AVAILABILITY_FILTERING,

    /**
     * 响应时间加权重策略
     */
    @Deprecated
    RESPONSE_TIME_WEIGHTED,

    /**
     * 区域权重策略
     */
    ZONE_AVOIDANCE,

    /**
     * 响应时间加权重策略
     */
    WEIGHTED_RESPONSE_TIME
}
