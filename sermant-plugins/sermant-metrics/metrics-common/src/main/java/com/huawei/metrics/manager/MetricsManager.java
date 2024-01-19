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

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsInfo;
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
    private static final Map<String, MetricsRpcInfo> METRICS_RPC_INFO_MAP = new ConcurrentHashMap<>();

    private MetricsManager() {
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
     * 保存RPC信息
     *
     * @param metricsRpcInfo RPC信息
     */
    public static void saveRpcInfo(MetricsRpcInfo metricsRpcInfo) {
        String key = getKey(metricsRpcInfo);
        MetricsRpcInfo rpcInfo = METRICS_RPC_INFO_MAP.computeIfAbsent(key, s -> metricsRpcInfo);
        if (rpcInfo != metricsRpcInfo) {
            rpcInfo.getReqCount().getAndAdd(metricsRpcInfo.getReqCount().get());
            rpcInfo.getResponseCount().getAndAdd(metricsRpcInfo.getResponseCount().get());
            rpcInfo.getSumLatency().getAndAdd(metricsRpcInfo.getSumLatency().get());
            rpcInfo.getLatencyList().addAll(metricsRpcInfo.getLatencyList());
            rpcInfo.getReqErrorCount().getAndAdd(metricsRpcInfo.getReqErrorCount().get());
        }
    }

    /**
     * 获取Map的key
     *
     * @param metricsInfo 指标信息
     * @return key
     */
    public static String getKey(MetricsInfo metricsInfo) {
        return metricsInfo.getClientIp() + Constants.CONNECT + metricsInfo.getServerIp() + Constants.CONNECT
                + metricsInfo.getServerPort() + Constants.CONNECT + metricsInfo.getProtocol() + Constants.CONNECT
                + metricsInfo.getL4Role() + Constants.CONNECT + metricsInfo.isEnableSsl() + Constants.CONNECT
                + metricsInfo.getUrl();
    }

    /**
     * 获取Map的key
     *
     * @param metricsInfo 指标信息
     * @return key
     */
    public static String getRpcKey(MetricsInfo metricsInfo) {
        return metricsInfo.getClientIp() + Constants.CONNECT + metricsInfo.getServerIp() + Constants.CONNECT
                + metricsInfo.getServerPort() + Constants.CONNECT + metricsInfo.getProtocol() + Constants.CONNECT
                + metricsInfo.getL4Role() + Constants.CONNECT + metricsInfo.isEnableSsl();
    }
}
