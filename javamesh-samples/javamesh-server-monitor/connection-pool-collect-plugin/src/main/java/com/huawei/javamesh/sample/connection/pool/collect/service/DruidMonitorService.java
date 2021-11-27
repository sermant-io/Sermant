/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.apm.core.config.ConfigManager;
import com.huawei.javamesh.sample.connection.pool.collect.collector.DruidMetricCollector;
import com.huawei.javamesh.sample.connection.pool.collect.collector.DruidMetricProvider;
import com.huawei.javamesh.sample.connection.pool.collect.config.DruidMonitorConfig;
import com.huawei.javamesh.sample.monitor.common.collect.CollectTask;
import com.huawei.javamesh.sample.servermonitor.entity.ConnectionPool;

import java.util.concurrent.TimeUnit;

/**
 * Druid监控服务
 */
public class DruidMonitorService {
    private static final DruidMonitorService INSTANCE = new DruidMonitorService();

    private final CollectTask<ConnectionPool> collectTask;

    private final DruidMetricCollector collector;

    private DruidMonitorService() {
        final DruidMonitorConfig config = ConfigManager.getConfig(DruidMonitorConfig.class);
        collector = DruidMetricCollector.getInstance();
        collectTask = CollectTask.create(new DruidMetricProvider(collector),
            config.getCollectInterval(), config.getConsumeInterval(), TimeUnit.valueOf(config.getTimeunit()));
    }

    public static DruidMonitorService getInstance() {
        return INSTANCE;
    }

    public void start() {
        collectTask.start();
    }

    public void stop() {
        collectTask.stop();
    }

    /**
     * 添加用于采集{@link ConnectionPool}指标的{@link DruidDataSource}实例
     *
     * @param dataSource 待添加的{@link DruidDataSource}实例
     */
    public void addDataSource(DruidDataSource dataSource) {
        collector.addDataSource(dataSource);
    }

    /**
     * 移除用于采集{@link ConnectionPool}指标的{@link DruidDataSource}实例
     *
     * @param dataSource 待移除的{@link DruidDataSource}实例
     */
    public void removeDataSource(DruidDataSource dataSource) {
        collector.removeDataSource(dataSource);
    }
}
