/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.service;

import com.huawei.apm.bootstrap.boot.CoreServiceManager;
import com.huawei.apm.bootstrap.boot.PluginService;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.service.send.GatewayClient;
import com.huawei.javamesh.sample.servermonitor.collector.IbmJvmMetricCollector;
import com.huawei.javamesh.sample.servermonitor.common.Consumer;
import com.huawei.javamesh.sample.servermonitor.common.Supplier;
import com.huawei.javamesh.sample.servermonitor.entity.IbmJvmMetric;
import com.huawei.javamesh.sample.servermonitor.entity.IbmJvmMetricCollection;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * IBM JVM采集服务
 */
public class IbmJvmCollectService implements PluginService {

    private static final Logger LOGGER = LogFactory.getLogger();

    private final static int GATEWAY_DATA_TYPE = 6;

    private CollectTask<IbmJvmMetric> collectTask;

    private GatewayClient gatewayClient;

    private IbmJvmMetricCollector ibmJvmMetricCollector;

    @Override
    public void init() {
        boolean needCollect = System.getProperty("java.vm.vendor").contains("IBM");
        if (!needCollect) {
            LOGGER.info("The IBM jvm metric collect task does not need to start in current jvm arch.");
            return;
        }

        ibmJvmMetricCollector = new IbmJvmMetricCollector();
        gatewayClient = CoreServiceManager.INSTANCE.getService(GatewayClient.class);

        // Get from config
        final long collectInterval = 1;
        final long consumeInterval = 60;
        collectTask = CollectTask.create(
            new Supplier<IbmJvmMetric>() {
                @Override
                public IbmJvmMetric get() {
                    return collect();
                }
            }, collectInterval,
            new Consumer<List<IbmJvmMetric>>() {
                @Override
                public void accept(List<IbmJvmMetric> serverMonitoringMetrics) {
                    send(serverMonitoringMetrics);
                }
            }, consumeInterval,
            TimeUnit.SECONDS);
        collectTask.start();
        LOGGER.info("IBM jvm metric collect task started.");
    }

    @Override
    public void stop() {
        collectTask.stop();
    }

    private IbmJvmMetric collect() {
        return IbmJvmMetric.newBuilder().setTime(System.currentTimeMillis())
            .addAllIbmPoolMetrics(ibmJvmMetricCollector.getIbmJvmMetrics()).build();
    }

    private void send(List<IbmJvmMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            LOGGER.warning("No IBM jvm metric was collected.");
            return;
        }
        IbmJvmMetricCollection metricCollection = IbmJvmMetricCollection.newBuilder()
            .addAllMetrics(metrics).build();
        gatewayClient.send(metricCollection.toByteArray(), GATEWAY_DATA_TYPE);
    }
}
