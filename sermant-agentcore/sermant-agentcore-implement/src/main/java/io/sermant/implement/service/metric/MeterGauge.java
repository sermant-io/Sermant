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

package io.sermant.implement.service.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.sermant.core.service.metric.api.Gauge;

import java.util.function.ToDoubleFunction;

/**
 * metric gauge implementation
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public class MeterGauge implements Gauge {
    private final MeterRegistry registry;

    private final String metricName;

    private final Iterable<Tag> tags;

    private final String description;

    /**
     * Constructs a MeterGauge instance to measure and record data for a specified metric name.
     *
     * @param registry The MeterRegistry instance used for registering and managing metrics.
     * @param metricName The name of the metric, used to uniquely identify this metric.
     * @param tags A set of tags associated with the metric, used for categorization and filtering.
     * @param description The description of the metric, used to explain its purpose and meaning.
     */
    public MeterGauge(MeterRegistry registry, String metricName, Iterable<Tag> tags, String description) {
        this.registry = registry;
        this.metricName = metricName;
        this.tags = tags;
        this.description = description;
    }

    @Override
    public <T> T gaugeState(T stateObject, ToDoubleFunction<T> valueFunction) {
        io.micrometer.core.instrument.Gauge.builder(metricName, stateObject, valueFunction)
                .tags(tags).description(description).register(registry);
        return stateObject;
    }
}
