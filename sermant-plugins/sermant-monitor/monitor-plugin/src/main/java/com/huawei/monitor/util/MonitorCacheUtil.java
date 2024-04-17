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

package com.huawei.monitor.util;

import com.huawei.monitor.common.MetricCalEntity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * monitoring data saving tools
 *
 * @author zhp
 * @since 2022-11-01
 */
public class MonitorCacheUtil {
    /**
     * monitor data cache
     */
    private static final ConcurrentMap<String, MetricCalEntity> CONCURRENT_MAP = new ConcurrentHashMap<>();

    private MonitorCacheUtil() {
    }

    /**
     * obtain monitoring data
     *
     * @param key monitoring tag name
     * @return MetricCalEntity monitoringData
     */
    public static MetricCalEntity getMetricCalEntity(String key) {
        return CONCURRENT_MAP.computeIfAbsent(key, str -> new MetricCalEntity());
    }

    /**
     * obtain monitoring data map
     *
     * @return ConcurrentMap monitoring data map
     */
    public static ConcurrentMap<String, MetricCalEntity> getMetric() {
        return CONCURRENT_MAP;
    }
}
