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
import io.sermant.core.service.metric.entity.MetricCommonTagEnum;
import io.sermant.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Metric Configuration
 *
 * @author zwmagic
 * @since 2024-08-19
 */
@ConfigTypeKey("metric")
public class MetricConfig implements BaseConfig {
    private static final int MAXIMUM_TIME_SERIES_VALUE = 1000;

    private static final String COMMA = ",";

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

    /**
     * Defines the common tag keys for metrics, with multiple keys separated by commas.
     * The default values include "agent", "agent.app.name", and "agent.ip".
     * For a complete list of available tag keys, refer to
     * {@link io.sermant.core.service.metric.entity.MetricCommonTagEnum}.
     */
    @ConfigFieldKey("common.tag.keys")
    private String commonTagKeys = String.join(COMMA, MetricCommonTagEnum.getDefaultKeys());

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

    public String getCommonTagKeys() {
        return commonTagKeys;
    }

    public void setCommonTagKeys(String commonTagKeys) {
        this.commonTagKeys = commonTagKeys;
    }

    /**
     * Gets the list of custom default tags.
     * <p>
     * If the custom default tags string is blank or null, an empty list is returned to avoid null pointer exceptions.
     * The custom default tags string is split by commas and converted into a list.
     *
     * @return A set of custom default tags, or an empty set if not set or blank.
     */
    public Set<String> getCommonTagKeySet() {
        if (StringUtils.isBlank(commonTagKeys)) {
            return Collections.emptySet();
        }
        String[] array = commonTagKeys.split(COMMA);
        return new HashSet<>(Arrays.asList(array));
    }

}
