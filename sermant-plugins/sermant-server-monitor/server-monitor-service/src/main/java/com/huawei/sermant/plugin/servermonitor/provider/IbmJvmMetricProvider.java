/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.plugin.servermonitor.provider;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.send.GatewayClient;
import com.huawei.sermant.plugin.monitor.common.collect.MetricProvider;
import com.huawei.sermant.plugin.monitor.common.config.ServiceConfig;
import com.huawei.sermant.plugin.servermonitor.collector.IbmJvmMetricCollector;
import com.huawei.sermant.plugin.servermonitor.entity.IbmJvmMetric;
import com.huawei.sermant.plugin.servermonitor.entity.IbmJvmMetricCollection;

import java.util.List;
import java.util.logging.Logger;

/**
 * IBM JVM Metric Provider
 */
public class IbmJvmMetricProvider implements MetricProvider<IbmJvmMetric> {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final int GATEWAY_DATA_TYPE = 6;
    private static final IbmJvmMetricProvider INSTANCE = new IbmJvmMetricProvider();

    private final GatewayClient gatewayClient;
    private final IbmJvmMetricCollector ibmJvmMetricCollector;
    private final String service;
    private final String serviceInstance;

    private IbmJvmMetricProvider() {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        this.service = config.getService();
        this.serviceInstance = config.getServiceInstance();
        ibmJvmMetricCollector = new IbmJvmMetricCollector();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
    }

    public static IbmJvmMetricProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public IbmJvmMetric collect() {
        return IbmJvmMetric.newBuilder().setTime(System.currentTimeMillis())
            .addAllIbmPoolMetrics(ibmJvmMetricCollector.getIbmJvmMetrics()).build();
    }

    @Override
    public void consume(List<IbmJvmMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No IBM jvm metric was collected.");
            return;
        }
        IbmJvmMetricCollection metricCollection = IbmJvmMetricCollection.newBuilder()
            .setService(service)
            .setServiceInstance(serviceInstance)
            .addAllMetrics(metrics).build();
        gatewayClient.send(metricCollection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
