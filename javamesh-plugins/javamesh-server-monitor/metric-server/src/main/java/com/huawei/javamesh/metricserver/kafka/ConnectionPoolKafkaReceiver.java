/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.javamesh.metricserver.dto.connectionpool.DataSourceDTO;
import com.huawei.javamesh.metricserver.service.ConnectionPoolService;
import com.huawei.javamesh.sample.servermonitor.entity.ConnectionPool;
import com.huawei.javamesh.sample.servermonitor.entity.ConnectionPoolCollection;
import com.huawei.javamesh.sample.servermonitor.entity.DataSourceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Druid Connection Pool kafka接收处理类
 */
@Component
public class ConnectionPoolKafkaReceiver {

    private final ConnectionPoolService connectionPoolService;

    @Autowired
    public ConnectionPoolKafkaReceiver(ConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    @KafkaListener(topics = "topic-druid-monitor", groupId = "monitor-server")
    public void onMessage(byte[] record) {
        ConnectionPoolCollection collection;
        try {
            collection = ConnectionPoolCollection.parseFrom(record);
        } catch (InvalidProtocolBufferException e) {
            return;
        }
        final String service = collection.getService();
        final String serviceInstance = collection.getServiceInstance();
        for (ConnectionPool connectionPool : collection.getMetricsList()) {
            final Instant time = Instant.ofEpochMilli(connectionPool.getTimestamp());
            for (DataSourceBean dataSourceBean : connectionPool.getDataSourceBeansList()) {
                connectionPoolService.addDataSource(buildDataSourceDTO(service, serviceInstance, time, dataSourceBean));
            }
        }
    }

    private DataSourceDTO buildDataSourceDTO(String service, String serviceInstance, Instant time, DataSourceBean dataSourceBean) {
        return DataSourceDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .databasePeer(dataSourceBean.getDatabasePeer())
            .name(dataSourceBean.getName())
            .poolingCount((long) dataSourceBean.getPoolingCount())
            .maxActive((long) dataSourceBean.getMaxActive())
            .activeCount((long) dataSourceBean.getActiveCount())
            .initialSize((long) dataSourceBean.getInitialSize())
            .build();
    }
}
