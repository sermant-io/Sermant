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

package io.sermant.monitor.common;

import io.prometheus.client.GaugeMetricFamily;

import java.util.Collections;

/**
 * MetricFamily builder
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class MetricFamilyBuild {
    /**
     * construction method
     */
    private MetricFamilyBuild() {
    }

    /**
     * build GaugeMetric
     *
     * @param metricEnum metric enumeration information
     * @param value metric value
     * @return created a collection metric
     */
    public static GaugeMetricFamily buildGaugeMetric(MetricEnum metricEnum, double value) {
        return new GaugeMetricFamily(metricEnum.getName(), metricEnum.getDisc(), value);
    }

    /**
     * build GaugeMetric
     *
     * @param metricEnum metric enumeration information
     * @param value metric value
     * @param labelName label name
     * @param labelValue label value
     * @return created a collection metric
     */
    public static GaugeMetricFamily buildGaugeMetric(MetricEnum metricEnum, double value, String labelName,
            String labelValue) {
        GaugeMetricFamily gaugeMetricFamily = new GaugeMetricFamily(metricEnum.getName(), metricEnum.getDisc(),
                Collections.singletonList(labelName));
        return gaugeMetricFamily.addMetric(Collections.singletonList(labelValue), value);
    }
}
