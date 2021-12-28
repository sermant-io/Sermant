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

package com.huawei.sermant.metricserver.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.sermant.metricserver.dto.connectionpool.DataSourceDTO;
import com.huawei.sermant.metricserver.service.ConnectionPoolService;
import com.huawei.sermant.plugin.servermonitor.entity.ConnectionPool;
import com.huawei.sermant.plugin.servermonitor.entity.ConnectionPoolCollection;
import com.huawei.sermant.plugin.servermonitor.entity.DataSourceBean;
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
