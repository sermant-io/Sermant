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

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.sermant.core.plugin.service.PluginServiceManager;
import com.huawei.sermant.plugin.monitor.common.service.DatabasePeerParseService;
import com.huawei.sermant.plugin.servermonitor.entity.ConnectionPool;
import com.huawei.sermant.plugin.servermonitor.entity.DataSourceBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Druid指标采集器
 */
public class DruidMetricCollector {

    private static final DruidMetricCollector INSTANCE = new DruidMetricCollector();

    /**
     * value: 由{@link #getDatabasePeer(DruidDataSource)}生成的databasePeer
     */
    private final Map<DruidDataSource, String> dataSources = new HashMap<DruidDataSource, String>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private final DatabasePeerParseService databasePeerParseService;

    private DruidMetricCollector() {
        databasePeerParseService = PluginServiceManager.getPluginService(DatabasePeerParseService.class);
    }

    public static DruidMetricCollector getInstance() {
        return INSTANCE;
    }

    /**
     * 添加用于采集{@link ConnectionPool}指标的{@link DruidDataSource}实例
     *
     * @param dataSource 待添加的{@link DruidDataSource}实例
     */
    public void addDataSource(DruidDataSource dataSource) {
        String databasePeer = getDatabasePeer(dataSource);
        if (StringUtils.isBlank(databasePeer)) {
            return;
        }
        writeLock.lock();
        try {
            dataSources.put(dataSource, databasePeer);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 移除用于采集{@link ConnectionPool}指标的{@link DruidDataSource}实例
     *
     * @param dataSource 待移除的{@link DruidDataSource}实例
     */
    public void removeDataSource(DruidDataSource dataSource) {
        writeLock.lock();
        try {
            dataSources.remove(dataSource);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 从已添加的{@link ConnectionPool}实例采集{@link ConnectionPool}指标
     *
     * @return {@link ConnectionPool}指标
     */
    public ConnectionPool getConnectionPool() {
        readLock.lock();
        try {
            if (dataSources.isEmpty()) {
                return null;
            }
            ConnectionPool.Builder builder = ConnectionPool.newBuilder()
                .setTimestamp(System.currentTimeMillis());
            for (Map.Entry<DruidDataSource, String> entry : dataSources.entrySet()) {
                DruidDataSource druidDataSource = entry.getKey();
                builder.addDataSourceBeans(DataSourceBean.newBuilder()
                    .setName(druidDataSource.getName())
                    .setActiveCount(druidDataSource.getActiveCount())
                    .setInitialSize(druidDataSource.getInitialSize())
                    .setMaxActive(druidDataSource.getMaxActive())
                    .setPoolingCount(druidDataSource.getPoolingCount())
                    .setDatabasePeer(entry.getValue()));
            }
            return builder.build();
        } finally {
            readLock.unlock();
        }
    }

    private String getDatabasePeer(DruidDataSource dataSource) {
        return databasePeerParseService.parse(dataSource.getUrl());
    }
}
