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
import com.huawei.sermant.plugin.servermonitor.collector.CpuMetricCollector;
import com.huawei.sermant.plugin.servermonitor.collector.DiskMetricCollector;
import com.huawei.sermant.plugin.servermonitor.collector.MemoryMetricCollector;
import com.huawei.sermant.plugin.servermonitor.collector.NetworkMetricCollector;
import com.huawei.sermant.plugin.servermonitor.entity.ServerMetric;
import com.huawei.sermant.plugin.servermonitor.entity.ServerMetricCollection;

import java.util.List;
import java.util.logging.Logger;

/**
 * CPU、磁盘、网络、磁盘 Metric Provider
 */
public class ServerMonitorMetricProvider implements MetricProvider<ServerMetric> {
    private static final Logger LOGGER = LoggerFactory.getLogger();
    private static final int GATEWAY_DATA_TYPE = 4;

    private final CpuMetricCollector cpuMetricCollector;
    private final DiskMetricCollector diskMetricCollector;
    private final NetworkMetricCollector networkMetricCollector;
    private final MemoryMetricCollector memoryMetricCollector;

    private final GatewayClient gatewayClient;
    private final String service;
    private  final String serviceInstance;

    public ServerMonitorMetricProvider(long collectInterval) {
        ServiceConfig config = PluginConfigManager.getPluginConfig(ServiceConfig.class);
        service = config.getService();
        serviceInstance = config.getServiceInstance();
        gatewayClient = ServiceManager.getService(GatewayClient.class);
        cpuMetricCollector = new CpuMetricCollector();
        diskMetricCollector = new DiskMetricCollector(collectInterval);
        networkMetricCollector = new NetworkMetricCollector(collectInterval);
        memoryMetricCollector = new MemoryMetricCollector();
    }

    @Override
    public ServerMetric collect() {
        return ServerMetric.newBuilder()
            .setTime(System.currentTimeMillis())
            .setCpu(cpuMetricCollector.getCpuMetric())
            .addAllDisks(diskMetricCollector.getDiskMetrics())
            .setNetwork(networkMetricCollector.getNetworkMetric())
            .setMemory(memoryMetricCollector.getMemoryMetric())
            .build();
    }

    @Override
    public void consume(List<ServerMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No server monitor metric was collected.");
            return;
        }
        ServerMetricCollection collection = ServerMetricCollection.newBuilder()
            .setService(service)
            .setServiceInstance(serviceInstance)
            .addAllMetrics(metrics)
            .build();
        gatewayClient.send(collection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
