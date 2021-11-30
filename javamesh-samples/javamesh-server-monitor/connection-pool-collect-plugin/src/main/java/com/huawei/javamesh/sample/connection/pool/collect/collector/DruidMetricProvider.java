/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.collector;

import com.huawei.apm.core.plugin.config.PluginConfigManager;
import com.huawei.apm.core.service.ServiceManager;
import com.huawei.apm.core.service.send.GatewayClient;
import com.huawei.javamesh.sample.monitor.common.collect.MetricProvider;
import com.huawei.javamesh.sample.monitor.common.config.ServiceConfig;
import com.huawei.javamesh.sample.servermonitor.entity.ConnectionPool;
import com.huawei.javamesh.sample.servermonitor.entity.ConnectionPoolCollection;

import java.util.List;

/**
 * Druid Connection Pool Metric Provider
 */
public class DruidMetricProvider implements MetricProvider<ConnectionPool> {

    private static final int GATEWAY_DATA_TYPE = 10;

    private final DruidMetricCollector metricCollector;
    private final GatewayClient gatewayClient;
    private final String service;
    private final String serviceInstance;

    public DruidMetricProvider(DruidMetricCollector collector) {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        service = config.getService();
        serviceInstance = config.getServiceInstance();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
        metricCollector = collector;
    }

    @Override
    public ConnectionPool collect() {
        return metricCollector.getConnectionPool();
    }

    @Override
    public void consume(List<ConnectionPool> metrics) {
        if (metrics.isEmpty()) {
            return;
        }
        ConnectionPoolCollection collection = ConnectionPoolCollection.newBuilder()
            .setService(service)
            .setServiceInstance(serviceInstance)
            .addAllMetrics(metrics).build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
