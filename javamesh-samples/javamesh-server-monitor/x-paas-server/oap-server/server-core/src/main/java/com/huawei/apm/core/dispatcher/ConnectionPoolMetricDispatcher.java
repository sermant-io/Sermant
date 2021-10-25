/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.dispatcher;

import com.huawei.apm.core.source.ConnectionPoolMetric;
import com.huawei.apm.core.source.ConnectionPoolType;
import com.huawei.apm.core.source.NodeRecordType;
import com.huawei.apm.network.language.agent.v3.DataSourceBean;
import com.huawei.apm.network.language.agent.v3.Instance;

import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.NodeType;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * 应用，实例以及数据源指标数据转发
 *
 * @author zhouss
 * @since 2020-11-28
 */
public class ConnectionPoolMetricDispatcher {
    private final SourceReceiver sourceReceiver;

    public ConnectionPoolMetricDispatcher(ModuleManager moduleManager) {
        this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
    }

    /**
     * 将信息分发到各个source，便于后续聚合统计
     *
     * @param metric   实例数据
     * @param poolType 连接池类型
     */
    public void send(com.huawei.apm.network.language.agent.v3.ConnectionPoolMetric metric,
        ConnectionPoolType poolType, int copy) {
        if (metric == null) {
            return;
        }
        long minuteTimeBucket = TimeBucket.getMinuteTimeBucket(metric.getTimestamp());
        List<DataSourceBean> dataSources = metric.getDataSourceBeansList();
        if (CollectionUtils.isNotEmpty(dataSources)) {
            dataSources.forEach(dataSourceBean -> {
                sendToDataSourceMetric(minuteTimeBucket, dataSourceBean, poolType, copy);
            });
        }
        this.sendToAppMetric(minuteTimeBucket, metric.getInstance(), poolType, copy);
        this.sendToInstanceMetric(minuteTimeBucket, metric.getInstance(), poolType, copy);
    }

    private void sendToAppMetric(long minuteTimeBucket, Instance metric, ConnectionPoolType poolType, int copy) {
        ConnectionPoolMetric appMetric = new ConnectionPoolMetric();
        appMetric.setName(metric.getAppName());
        appMetric.setDimensionType(NodeRecordType.APP);
        appMetric.setTimeBucket(minuteTimeBucket);
        String serviceId = IDManager.ServiceID.buildId(metric.getAppName(), NodeType.Normal);
        appMetric.setServiceId(serviceId);
        appMetric.setId(IDManager.ServiceInstanceID.buildId(serviceId, metric.getAppName()));
        appMetric.setConnectionPoolType(poolType);
        appMetric.setCopy(copy);// huawei update.无损演练：添加复制标签
        appMetric.setParentName(""); // 避免空指针报错（在grpc序列化时会判断是否为空）  修复bug
        appMetric.setDataBasePeers(Optional.ofNullable(metric.getCountStatistic()).orElse(""));
        sourceReceiver.receive(appMetric);
    }

    private void sendToInstanceMetric(long minuteTimeBucket, Instance metric, ConnectionPoolType poolType, int copy) {
        ConnectionPoolMetric instanceMetric = new ConnectionPoolMetric();
        instanceMetric.setName(metric.getInstanceName());
        instanceMetric.setDimensionType(NodeRecordType.IP_INSTANCE);
        instanceMetric.setTimeBucket(minuteTimeBucket);
        instanceMetric.setParentName(metric.getAppName());
        String serviceId = IDManager.ServiceID.buildId(metric.getAppName(), NodeType.Normal);
        instanceMetric.setServiceId(serviceId);
        instanceMetric.setId(IDManager.ServiceInstanceID.buildId(serviceId, metric.getInstanceName()));
        instanceMetric.setActiveCount(metric.getActiveCount());
        instanceMetric.setPoolingCount(metric.getPoolingCount());
        instanceMetric.setMaxActive(metric.getMaxActive());
        instanceMetric.setRatio(getRatio(instanceMetric.getActiveCount(), instanceMetric.getMaxActive()));
        instanceMetric.setConnectionPoolType(poolType);
        instanceMetric.setDataBasePeers(metric.getCountStatistic());
        instanceMetric.setCopy(copy);// huawei update.无损演练：添加复制标签
        sourceReceiver.receive(instanceMetric);
    }

    private void sendToDataSourceMetric(long minuteTimeBucket, DataSourceBean metric, ConnectionPoolType poolType, int copy) {
        ConnectionPoolMetric connectionPoolMetric = new ConnectionPoolMetric();
        connectionPoolMetric.setName(metric.getDataSourceName());
        connectionPoolMetric.setDimensionType(NodeRecordType.DATASOURCE);
        connectionPoolMetric.setTimeBucket(minuteTimeBucket);
        connectionPoolMetric.setParentName(metric.getInstanceName());
        String serviceId = IDManager.ServiceID.buildId(metric.getInstanceName(), NodeType.Normal);
        connectionPoolMetric.setServiceId(serviceId);
        connectionPoolMetric.setId(IDManager.ServiceInstanceID.buildId(serviceId, metric.getDataSourceName()));
        connectionPoolMetric.setActiveCount(metric.getActiveCount());
        connectionPoolMetric.setPoolingCount(metric.getPoolingCount());
        connectionPoolMetric.setMaxActive(metric.getMaxActive());
        connectionPoolMetric.setRatio(getRatio(connectionPoolMetric.getActiveCount(),
                connectionPoolMetric.getMaxActive()));
        connectionPoolMetric.setConnectionPoolType(poolType);
        connectionPoolMetric.setDataBasePeers(metric.getDatabasePeer());
        connectionPoolMetric.setCopy(copy);// huawei update.无损演练：添加复制标签
        sourceReceiver.receive(connectionPoolMetric);
    }

    /**
     * 计算 活动连接数 / 最大连接数
     *
     * @param activeCount 可用连接数
     * @param maxActive   最大连接数
     * @return 两者之比
     */
    private double getRatio(long activeCount, long maxActive) {
        if (maxActive == 0) {
            return 0D;
        }
        return 1.0D * activeCount / maxActive;
    }
}
