/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.monitor.service.collector;

import com.huawei.monitor.common.MetricCalEntity;
import com.huawei.monitor.common.MetricEnum;
import com.huawei.monitor.common.MetricFamilyBuild;
import com.huawei.monitor.util.MonitorCacheUtil;

import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * collect service data
 *
 * @author zhp
 * @since 2022-11-02
 */
public class ServiceCollectorService extends SwitchService implements PluginService {
    private static Map<String, MetricCalEntity> lastCurrents = null;

    private static final String DEFAULT_LABEL_NAME = "name";

    private static long lastTime;

    private static final int PROPORTION = 1000;

    @Override
    public List<MetricFamilySamples> collect() {
        ConcurrentMap<String, MetricCalEntity> metricMap = MonitorCacheUtil.getMetric();
        List<MetricFamilySamples> metricFamilySamplesList = new ArrayList<>();
        if (metricMap.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        long currentTime = System.currentTimeMillis();
        Map<String, MetricCalEntity> currentMap = new HashMap<>();
        for (Map.Entry<String, MetricCalEntity> entry : metricMap.entrySet()) {
            if (entry.getValue() == null || StringUtils.isBlank(entry.getKey())) {
                continue;
            }
            MetricCalEntity metricCalEntity = new MetricCalEntity();
            metricCalEntity.getConsumeReqTimeNum().set(entry.getValue().getConsumeReqTimeNum().get());
            metricCalEntity.getFailedReqNum().set(entry.getValue().getFailedReqNum().get());
            metricCalEntity.getSuccessFulReqNum().set(entry.getValue().getSuccessFulReqNum().get());
            metricCalEntity.getReqNum().set(entry.getValue().getReqNum().get());
            addMetricFamilySamples(metricFamilySamplesList, entry.getKey(), metricCalEntity, currentTime);
            currentMap.put(entry.getKey(), metricCalEntity);
        }
        lastTime = currentTime;
        lastCurrents = currentMap;
        return metricFamilySamplesList;
    }

    /**
     * add monitoring metric data
     *
     * @param metricFamilySamplesList metric collection
     * @param key label of the metric to be collected
     * @param metricCalEntity this collection of metric data
     * @param currentTime current time
     */
    private static void addMetricFamilySamples(List<MetricFamilySamples> metricFamilySamplesList,
            String key, MetricCalEntity metricCalEntity, long currentTime) {
        if (metricCalEntity == null || StringUtils.isBlank(key)) {
            return;
        }
        double qps = 0d;
        double avgTime = 0d;
        double tps = 0d;
        if (lastCurrents != null && lastTime != 0) {
            MetricCalEntity lastMetric = lastCurrents.get(key);
            if (lastMetric != null
                    && lastMetric.getSuccessFulReqNum().get() != metricCalEntity.getSuccessFulReqNum().get()
                    && lastMetric.getConsumeReqTimeNum().get() != metricCalEntity.getConsumeReqTimeNum().get()) {
                double reqNum = metricCalEntity.getSuccessFulReqNum().get() - lastMetric.getSuccessFulReqNum().get();
                double time = metricCalEntity.getConsumeReqTimeNum().get() - lastMetric.getConsumeReqTimeNum().get();
                long interval = currentTime - lastTime;
                qps = interval == 0 ? 0 : (reqNum / interval) * PROPORTION;
                avgTime = time / reqNum;
                tps = (qps / avgTime) * PROPORTION;
            }
        }
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.QPS, qps,
                DEFAULT_LABEL_NAME, key));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.TPS, tps,
                DEFAULT_LABEL_NAME, key));
        metricFamilySamplesList.add(MetricFamilyBuild.buildGaugeMetric(MetricEnum.AVG_RESPONSE_TIME, avgTime,
                DEFAULT_LABEL_NAME, key));
    }
}
