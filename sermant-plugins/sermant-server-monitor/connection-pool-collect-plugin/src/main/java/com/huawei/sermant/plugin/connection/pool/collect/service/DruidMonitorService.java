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

package com.huawei.sermant.plugin.connection.pool.collect.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.plugin.connection.pool.collect.collector.DruidMetricCollector;
import com.huawei.sermant.plugin.connection.pool.collect.collector.DruidMetricProvider;
import com.huawei.sermant.plugin.connection.pool.collect.config.DruidMonitorConfig;
import com.huawei.sermant.plugin.monitor.common.collect.CollectTask;
import com.huawei.sermant.plugin.servermonitor.entity.ConnectionPool;

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
