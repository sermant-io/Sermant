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

import com.google.common.collect.Lists;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.DistributionSummary.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.sermant.core.service.metric.api.Counter;
import io.sermant.core.service.metric.api.DistributionStatisticConfig;
import io.sermant.core.service.metric.api.Gauge;
import io.sermant.core.service.metric.api.Metric;
import io.sermant.core.service.metric.api.Summary;
import io.sermant.core.service.metric.api.Tags;
import io.sermant.core.service.metric.api.Timer;
import io.sermant.core.service.metric.config.MetricConfig;
import io.sermant.core.service.metric.entity.MetricCommonTagEnum;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * metric
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public class MeterMetric implements Metric {
    private static final Iterable<Tag> EMPTY = Collections.emptyList();

    private final MeterRegistry registry;

    /**
     * Constructor that initializes a MeterMetric instance
     *
     * @param registryProvider An instance of MeterRegistryProvider used for creating and managing metrics.
     * @param metricConfig MetricConfig
     */
    public MeterMetric(MeterRegistryProvider registryProvider, MetricConfig metricConfig) {
        this.registry = registryProvider.getRegistry();
        MeterRegistry.Config config = this.registry.config();
        Set<String> commonTagKeys = metricConfig.getCommonTagKeySet();
        if (CollectionUtils.isEmpty(commonTagKeys)) {
            config.meterFilter(MeterFilter.maximumAllowableMetrics(metricConfig.getMaxTimeSeries()));
            return;
        }

        List<io.micrometer.core.instrument.Tag> tags = new ArrayList<>();
        for (String tagKey : commonTagKeys) {
            if (StringUtils.isEmpty(tagKey)) {
                continue;
            }
            String tagValue = MetricCommonTagEnum.of(tagKey);
            if (StringUtils.isEmpty(tagValue)) {
                continue;
            }
            tags.add(io.micrometer.core.instrument.Tag.of(tagKey, tagValue));
        }
        if (!CollectionUtils.isEmpty(tags)) {
            config.commonTags(tags);
        }
        config.meterFilter(MeterFilter.maximumAllowableMetrics(metricConfig.getMaxTimeSeries()));
    }

    @Override
    public Counter counter(String metricName, Tags tags, String description) {
        io.micrometer.core.instrument.Counter counter = io.micrometer.core.instrument.Counter.builder(metricName)
                .tags(getMeterTags(tags))
                .description(description)
                .register(registry);
        return new MeterCounter(counter);
    }

    @Override
    public Gauge gauge(String metricName, Tags tags, String description) {
        return new MeterGauge(registry, metricName, getMeterTags(tags), description);
    }

    @Override
    public Timer timer(String metricName, Tags tags, String description) {
        io.micrometer.core.instrument.Timer timer = io.micrometer.core.instrument.Timer.builder(metricName)
                .tags(getMeterTags(tags)).description(description).register(registry);
        return new MeterTimer(registry, timer);
    }

    @Override
    public Summary summary(String metricName, Tags tags, String description,
                           DistributionStatisticConfig distributionStatisticConfig) {
        Builder summaryBuilder = DistributionSummary.builder(metricName)
                .tags(getMeterTags(tags))
                .description(description);

        if (distributionStatisticConfig == null) {
            DistributionSummary summary = summaryBuilder.register(registry);
            return new MeterSummary(summary);
        }
        Optional.ofNullable(distributionStatisticConfig.getPercentileHistogram())
                .ifPresent(summaryBuilder::publishPercentileHistogram);

        Optional.ofNullable(distributionStatisticConfig.getPercentiles())
                .ifPresent(summaryBuilder::publishPercentiles);

        Optional.ofNullable(distributionStatisticConfig.getPercentilePrecision())
                .ifPresent(summaryBuilder::percentilePrecision);

        Optional.ofNullable(distributionStatisticConfig.getMinimumExpectedValue())
                .ifPresent(summaryBuilder::minimumExpectedValue);

        Optional.ofNullable(distributionStatisticConfig.getMaximumExpectedValue())
                .ifPresent(summaryBuilder::maximumExpectedValue);

        Optional.ofNullable(distributionStatisticConfig.getExpiry())
                .ifPresent(summaryBuilder::distributionStatisticExpiry);

        Optional.ofNullable(distributionStatisticConfig.getBufferLength())
                .ifPresent(summaryBuilder::distributionStatisticBufferLength);
        DistributionSummary summary = summaryBuilder.register(registry);
        return new MeterSummary(summary);
    }

    /**
     * Converts a Tags object into an iterable collection of Tag objects.
     *
     * @param tags The Tags object to convert.
     * @return An iterable collection of Tag objects. Returns an empty collection if there are no tags or if tags is
     * null.
     */
    private Iterable<Tag> getMeterTags(Tags tags) {
        if (tags == null || tags.getSize() == 0) {
            return EMPTY;
        }

        List<Tag> result = Lists.newArrayListWithCapacity(tags.getSize());
        for (Map.Entry<String, String> entry : tags.getTags().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isEmpty(key) || value == null) {
                continue;
            }
            result.add(Tag.of(entry.getKey(), value));
        }
        return result;
    }
}
