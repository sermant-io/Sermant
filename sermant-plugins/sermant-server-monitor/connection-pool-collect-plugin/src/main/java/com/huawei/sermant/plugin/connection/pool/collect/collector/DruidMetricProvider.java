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

package com.huawei.sermant.plugin.connection.pool.collect.collector;

import com.huawei.sermant.plugin.monitor.common.collect.MetricProvider;
import com.huawei.sermant.plugin.monitor.common.config.ServiceConfig;
import com.huawei.sermant.plugin.servermonitor.entity.ConnectionPool;
import com.huawei.sermant.plugin.servermonitor.entity.ConnectionPoolCollection;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.send.api.GatewayClient;

import java.util.List;

/**
 * Druid Connection Pool Metric Provider
 *
 * @since 2021-12-31
 */
public class DruidMetricProvider implements MetricProvider<ConnectionPool> {

    private static final int GATEWAY_DATA_TYPE = 10;

    private final DruidMetricCollector metricCollector;
    private final GatewayClient gatewayClient;
    private final String service;
    private final String serviceInstance;

    /**
     * 构造函数
     *
     * @param collector collector
     */
    public DruidMetricProvider(DruidMetricCollector collector) {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        service = config.getService();
        serviceInstance = config.getServiceInstance();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
        metricCollector = collector;
    }

    @Override
    public ConnectionPool collect() {
        return metricCollector.getConnectionPool().orElse(null);
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
