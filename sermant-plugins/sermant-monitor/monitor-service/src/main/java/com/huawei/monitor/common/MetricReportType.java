/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.monitor.common;

import com.huawei.monitor.service.MetricReportService;
import com.huawei.monitor.service.report.PrometheusMetricServiceImpl;

import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Optional;

/**
 * monitoring report type
 *
 * @author zhp
 * @since 2022-10-08
 */
public enum MetricReportType {

    /**
     * PROMETHEUS
     */
    PROMETHEUS("PROMETHEUS", new PrometheusMetricServiceImpl());

    /**
     * type
     */
    private final String type;

    /**
     * service
     */
    private final MetricReportService metricReportService;

    MetricReportType(String type, MetricReportService metricReportService) {
        this.type = type;
        this.metricReportService = metricReportService;
    }

    public String getType() {
        return type;
    }

    public MetricReportService getMetricReportService() {
        return metricReportService;
    }

    /**
     * Obtain the report instance based on the report type
     *
     * @param type report type
     * @return report type enumeration
     */
    public static Optional<MetricReportType> getMetricReportType(String type) {
        if (StringUtils.isEmpty(type)) {
            return Optional.empty();
        }
        for (MetricReportType metricReportType : MetricReportType.values()) {
            if (StringUtils.equals(type, metricReportType.getType())) {
                return Optional.of(metricReportType);
            }
        }
        return Optional.empty();
    }
}
