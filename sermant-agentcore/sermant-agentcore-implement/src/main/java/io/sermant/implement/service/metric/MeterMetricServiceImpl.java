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

import io.micrometer.core.instrument.Metrics;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.metric.api.Counter;
import io.sermant.core.service.metric.api.DistributionStatisticConfig;
import io.sermant.core.service.metric.api.Gauge;
import io.sermant.core.service.metric.api.Metric;
import io.sermant.core.service.metric.api.MetricService;
import io.sermant.core.service.metric.api.Summary;
import io.sermant.core.service.metric.api.Tags;
import io.sermant.core.service.metric.api.Timer;
import io.sermant.core.service.metric.config.MetricConfig;
import io.sermant.core.utils.SpiLoadUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The MeterMetricService class manages Micrometer metrics and provides initialization and data retrieval
 * functionalities.
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public class MeterMetricServiceImpl implements MetricService {
    private static MeterRegistryProvider meterRegistryProvider;
    private Metric metric;

    /**
     * Initializes the metric manager. If the metric manager has not been initialized, it creates and configures a
     * PrometheusMeterRegistry, and sets it as the global metric manager.
     */
    @Override
    public void start() {
        if (meterRegistryProvider != null) {
            return;
        }
        Map<String, MeterRegistryProvider> providerMap = SpiLoadUtils.loadAll(MeterRegistryProvider.class,
                        this.getClass().getClassLoader()).stream()
                .collect(Collectors.toMap(MeterRegistryProvider::getType, provider -> provider));
        MetricConfig metricConfig = ConfigManager.getConfig(MetricConfig.class);
        String type = metricConfig.getType();
        meterRegistryProvider = providerMap.get(type);
        if (meterRegistryProvider == null) {
            throw new IllegalArgumentException("can not find metricRegistry provider by " + type);
        }
        Metrics.addRegistry(meterRegistryProvider.getRegistry());
        metric = new MeterMetric(meterRegistryProvider, metricConfig);
    }

    /**
     * Retrieves the current metric data from the metric manager.
     *
     * @return The metric data of the current metric manager, represented in Prometheus format.
     */
    public static String getScrape() {
        return meterRegistryProvider.getScrape();
    }

    @Override
    public Counter counter(String metricName) {
        return counter(metricName, null);
    }

    @Override
    public Counter counter(String metricName, String tagKey, String tagValue) {
        return counter(metricName, Tags.of(tagKey, tagValue));
    }

    @Override
    public Counter counter(String metricName, Tags tags) {
        return counter(metricName, tags, null);
    }

    @Override
    public Counter counter(String metricName, Tags tags, String description) {
        return metric.counter(metricName, tags, description);
    }

    @Override
    public Gauge gauge(String metricName) {
        return gauge(metricName, null);
    }

    @Override
    public Gauge gauge(String metricName, String tagKey, String tagValue) {
        return gauge(metricName, Tags.of(tagKey, tagValue));
    }

    @Override
    public Gauge gauge(String metricName, Tags tags) {
        return gauge(metricName, tags, null);
    }

    @Override
    public Gauge gauge(String metricName, Tags tags, String description) {
        return metric.gauge(metricName, tags, description);
    }

    @Override
    public Timer timer(String metricName) {
        return timer(metricName, null);
    }

    @Override
    public Timer timer(String metricName, String tagKey, String tagValue) {
        return timer(metricName, Tags.of(tagKey, tagValue));
    }

    @Override
    public Timer timer(String metricName, Tags tags) {
        return timer(metricName, tags, null);
    }

    @Override
    public Timer timer(String metricName, Tags tags, String description) {
        return metric.timer(metricName, tags, description);
    }

    @Override
    public Summary summary(String metricName) {
        return summary(metricName, null);
    }

    @Override
    public Summary summary(String metricName, String tagKey, String tagValue) {
        return summary(metricName, Tags.of(tagKey, tagValue));
    }

    @Override
    public Summary summary(String metricName, Tags tags) {
        return summary(metricName, tags, null, null);
    }

    @Override
    public Summary summary(String metricName, Tags tags, String description,
                           DistributionStatisticConfig distributionStatisticConfig) {
        return metric.summary(metricName, tags, description, distributionStatisticConfig);
    }
}
