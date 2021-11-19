/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.collecttask;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.send.GatewayClient;
import com.huawei.javamesh.sample.servermonitor.collector.IbmJvmMetricCollector;
import com.huawei.javamesh.sample.servermonitor.config.ServiceConfig;
import com.huawei.javamesh.sample.servermonitor.entity.IbmJvmMetric;
import com.huawei.javamesh.sample.servermonitor.entity.IbmJvmMetricCollection;

import java.util.List;
import java.util.logging.Logger;

/**
 * IBM JVM Metric Provider
 */
public class IbmJvmMetricProvider implements MetricProvider<IbmJvmMetric> {
    private static final Logger LOGGER = LogFactory.getLogger();
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
