/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.router.common.metric;

import io.sermant.core.utils.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Routing Metrics information
 *
 * @author zhp
 * @since 2024-10-16
 */
public class MetricInfo {
    /**
     * Metric Name
     */
    private String metricName;

    /**
     * Tag Information
     */
    private Map<String, String> tags;

    /**
     * Constructor
     *
     * @param metricName metric name
     * @param tags tag information
     */
    public MetricInfo(String metricName, Map<String, String> tags) {
        this.metricName = metricName;
        this.tags = tags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricName, tags);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MetricInfo metricInfo = (MetricInfo) object;
        if (!StringUtils.equals(this.metricName, metricInfo.getMetricName())) {
            return false;
        }
        return Objects.equals(this.tags, metricInfo.getTags());
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
