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

package io.sermant.core.service.metric.api;

import io.sermant.core.service.BaseService;

/**
 * MetricService
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public interface MetricService extends BaseService {

    /**
     * Creates a counter to measure the number of occurrences of an event.
     *
     * @param metricName The name of the metric.
     * @return The created counter instance.
     */
    Counter counter(String metricName);

    /**
     * Creates a tagged counter to measure the number of occurrences of an event with specific tags.
     *
     * @param metricName The name of the metric.
     * @param tagKey The tag key.
     * @param tagValue The tag value.
     * @return The created tagged counter instance.
     */
    Counter counter(String metricName, String tagKey, String tagValue);

    /**
     * Creates a counter with a set of tags to measure the number of occurrences of an event with specific tags.
     *
     * @param metricName The name of the metric.
     * @param tags The set of tags.
     * @return The created counter instance with tags.
     */
    Counter counter(String metricName, Tags tags);

    /**
     * Creates a counter with a set of tags and a description to measure the number of occurrences of an event.
     *
     * @param metricName The name of the metric.
     * @param tags The set of tags.
     * @param description The description of the counter.
     * @return The created counter instance with tags and description.
     */
    Counter counter(String metricName, Tags tags, String description);

    /**
     * Creates a gauge to measure the instantaneous value of a metric.
     *
     * @param metricName The name of the metric.
     * @return The created gauge instance.
     */
    Gauge gauge(String metricName);

    /**
     * Creates a tagged gauge to measure the instantaneous value of a metric with specific tags.
     *
     * @param metricName The name of the metric.
     * @param tagKey The tag key.
     * @param tagValue The tag value.
     * @return The created tagged gauge instance.
     */
    Gauge gauge(String metricName, String tagKey, String tagValue);

    /**
     * Creates a gauge with a set of tags to measure the instantaneous value of a metric with specific tags.
     *
     * @param metricName The name of the metric.
     * @param tags The set of tags.
     * @return The created gauge instance with tags.
     */
    Gauge gauge(String metricName, Tags tags);

    /**
     * Creates a gauge with a set of tags and a description to measure the instantaneous value of a metric.
     *
     * @param metricName The name of the metric.
     * @param tags The set of tags.
     * @param description The description of the gauge.
     * @return The created gauge instance with tags and description.
     */
    Gauge gauge(String metricName, Tags tags, String description);

    /**
     * Creates a timer to measure the duration of an operation.
     *
     * @param metricName The name of the metric.
     * @return The created timer instance.
     */
    Timer timer(String metricName);

    /**
     * Creates a tagged timer to measure the duration of an operation with specific tags.
     *
     * @param metricName The name of the metric.
     * @param tagKey The tag key.
     * @param tagValue The tag value.
     * @return The created tagged timer instance.
     */
    Timer timer(String metricName, String tagKey, String tagValue);

    /**
     * Creates a timer with a set of tags to measure the duration of an operation with specific tags.
     *
     * @param metricName The name of the metric.
     * @param tags The set of tags.
     * @return The created timer instance with tags.
     */
    Timer timer(String metricName, Tags tags);

    /**
     * Creates a timer with a set of tags and a description to measure the duration of an operation.
     *
     * @param metricName The name of the metric.
     * @param tags The set of tags.
     * @param description The description of the timer.
     * @return The created timer instance with tags and description.
     */
    Timer timer(String metricName, Tags tags, String description);

    /**
     * Creates a Summary metric without any tags.
     *
     * @param metricName the name of the metric
     * @return the created Summary metric
     */
    Summary summary(String metricName);

    /**
     * Creates a Summary metric and adds a single tag to it.
     *
     * @param metricName the name of the metric
     * @param tagKey the key of the tag
     * @param tagValue the value of the tag
     * @return the created Summary metric
     */
    Summary summary(String metricName, String tagKey, String tagValue);

    /**
     * Creates a Summary metric and adds a set of tags to it.
     *
     * @param metricName the name of the metric
     * @param tags the set of tags
     * @return the created Summary metric
     */
    Summary summary(String metricName, Tags tags);

    /**
     * Creates a highly customizable Summary metric that can include tags, a description, and distribution statistics
     * configuration.
     *
     * @param metricName the name of the metric
     * @param tags the set of tags
     * @param description the description of the metric
     * @param distributionStatisticConfig the distribution statistics configuration
     * @return the created Summary metric
     */
    Summary summary(String metricName, Tags tags, String description,
                    DistributionStatisticConfig distributionStatisticConfig);

}
