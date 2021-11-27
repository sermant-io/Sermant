/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.collector;

import com.alibaba.druid.pool.DruidDataSource;
import com.huawei.apm.core.util.StringUtils;
import com.huawei.javamesh.sample.servermonitor.entity.ConnectionPool;
import com.huawei.javamesh.sample.servermonitor.entity.DataSourceBean;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

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

    private DruidMetricCollector() {
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
        String url = dataSource.getUrl();
        if (StringUtils.isBlank(url)) {
            return null;
        }
        ConnectionInfo connectionInfo;
        try {
            connectionInfo = URLParser.parser(url);
        } catch (Exception e) {
            // URLParser 有空指针异常BUG
            return null;
        }
        return connectionInfo == null ? null : connectionInfo.getDatabasePeer();
    }
}
