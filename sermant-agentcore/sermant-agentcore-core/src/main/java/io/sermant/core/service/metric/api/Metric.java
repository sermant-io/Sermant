/*
 * Copyright 2017 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on io/micrometer/core/instrument/MeterRegistry.java
 * from the Micrometer project.
 */

package io.sermant.core.service.metric.api;

/**
 * The Metric interface defines methods for creating different types of metrics objects, used for monitoring and
 * measuring application performance metrics. It supports Counter, Gauge, and Timer metric types.
 *
 * @author zwmagic
 * @since 2024-08-16
 */
public interface Metric {
    /**
     * Creates a tagged counter with a description to record the count of a specific metric.
     *
     * @param metricName the name of the metric
     * @param tags tags that further refine the metric data
     * @param description a description of the counter's purpose
     * @return the created counter object
     */
    Counter counter(String metricName, Tags tags, String description);

    /**
     * Creates a tagged gauge with a description to represent the current value of a specific metric.
     *
     * @param metricName the name of the metric
     * @param tags tags that further refine the metric data
     * @param description a description of the gauge's purpose
     * @return the created gauge object
     */
    Gauge gauge(String metricName, Tags tags, String description);

    /**
     * Creates a tagged timer with a description to measure the execution time of a specific metric.
     *
     * @param metricName the name of the metric
     * @param tags tags that further refine the metric data
     * @param description a description of the timer's purpose
     * @return the created timer object
     */
    Timer timer(String metricName, Tags tags, String description);

    /**
     * Creates a Summary metric to track distribution statistics.
     *
     * @param metricName the name of the metric to identify the monitoring metric
     * @param tags a set of tags for categorizing and filtering the metric
     * @param description a description to explain the purpose and meaning of the metric
     * @param distributionStatisticConfig the configuration defining what statistics to track
     * @return the created Summary metric instance
     */
    Summary summary(String metricName, Tags tags, String description,
            DistributionStatisticConfig distributionStatisticConfig);
}

