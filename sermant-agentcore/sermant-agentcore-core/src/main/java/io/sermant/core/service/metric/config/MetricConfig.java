/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.service.metric.config;

import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.common.ConfigTypeKey;

/**
 * Metric Configuration
 *
 * @author zwmagic
 * @since 2024-08-19
 */
@ConfigTypeKey("metric")
public class MetricConfig implements BaseConfig {
    private static final int MAXIMUM_TIME_SERIES_VALUE = 1000;

    /**
     * The metric type, currently supports prometheus.
     */
    @ConfigFieldKey("type")
    private String type = "prometheus";

    /**
     * The maximum number of metrics.
     */
    @ConfigFieldKey("maxTimeSeries")
    private Integer maxTimeSeries = MAXIMUM_TIME_SERIES_VALUE;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMaxTimeSeries(Integer maximumTimeSeries) {
        this.maxTimeSeries = maximumTimeSeries;
    }

    public Integer getMaxTimeSeries() {
        return maxTimeSeries;
    }
}
