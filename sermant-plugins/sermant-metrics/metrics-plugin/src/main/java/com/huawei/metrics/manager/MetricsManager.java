/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.manager;

import com.huawei.metrics.entity.MetricsLinkInfo;
import com.huawei.metrics.entity.MetricsRpcInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metrics管理类
 *
 * @author zhp
 * @since 2023-10-17
 */
public class MetricsManager {
    private static final Map<String, MetricsLinkInfo> METRICS_LINK_INFO_MAP = new ConcurrentHashMap<>();

    private static final Map<String, MetricsRpcInfo> METRICS_RPC_INFO_MAP = new ConcurrentHashMap<>();

    private MetricsManager() {
    }

    /**
     * 获取全部连接信息
     *
     * @return 连接信息
     */
    public static Map<String, MetricsLinkInfo> getLinkInfoMap() {
        return METRICS_LINK_INFO_MAP;
    }

    /**
     * 获取全部RPC信息
     *
     * @return 连接信息
     */
    public static Map<String, MetricsRpcInfo> getRpcInfoMap() {
        return METRICS_RPC_INFO_MAP;
    }

    /**
     * 获取连接信息
     *
     * @param key MAP的密钥信息
     * @return 连接信息
     */
    public static MetricsLinkInfo getLinkInfo(String key) {
        return METRICS_LINK_INFO_MAP.computeIfAbsent(key, s -> new MetricsLinkInfo());
    }

    /**
     * 获取RPC信息
     *
     * @param key MAP的密钥信息
     * @return RPC信息
     */
    public static MetricsRpcInfo getRpcInfo(String key) {
        return METRICS_RPC_INFO_MAP.computeIfAbsent(key, s -> new MetricsRpcInfo());
    }
}
