/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.javamesh.metricserver.dto.ibmpool.IbmMemoryPoolDTO;
import com.huawei.javamesh.metricserver.dto.ibmpool.IbmPoolType;
import com.huawei.javamesh.metricserver.service.IbmMemoryPoolService;
import com.huawei.javamesh.sample.servermonitor.entity.IbmJvmMetric;
import com.huawei.javamesh.sample.servermonitor.entity.IbmJvmMetricCollection;
import com.huawei.javamesh.sample.servermonitor.entity.IbmPoolMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 *  IbmJvmMetric kafka接收处理类
 */
@Component
public class IbmJvmMonitorKafkaReceiver {

    private final IbmMemoryPoolService ibmMemoryPoolService;

    @Autowired
    public IbmJvmMonitorKafkaReceiver(IbmMemoryPoolService ibmMemoryPoolService) {
        this.ibmMemoryPoolService = ibmMemoryPoolService;
    }

    @KafkaListener(topics = "topic-ibm-jvm-monitor", groupId = "monitor-server")
    public void onMessage(byte[] record) {
        final IbmJvmMetricCollection collection;
        try {
            collection = IbmJvmMetricCollection.parseFrom(record);
        } catch (InvalidProtocolBufferException e) {
            return;
        }
        final List<IbmMemoryPoolDTO> metricsToBeStored = buildDTOs(collection);
        ibmMemoryPoolService.batchAddMemoryPools(metricsToBeStored);
    }

    private LinkedList<IbmMemoryPoolDTO> buildDTOs(IbmJvmMetricCollection collection) {
        final LinkedList<IbmMemoryPoolDTO> metrics = new LinkedList<>();
        final String service = collection.getService();
        final String serviceInstance = collection.getServiceInstance();
        for (IbmJvmMetric ibmJvmMetric : collection.getMetricsList()) {
            final Instant time = Instant.ofEpochMilli(ibmJvmMetric.getTime());
            for (IbmPoolMetric ibmPoolMetric : ibmJvmMetric.getIbmPoolMetricsList()) {
                metrics.add(IbmMemoryPoolDTO.builder()
                    .service(service)
                    .serviceInstance(serviceInstance)
                    .time(time)
                    .type(IbmPoolType.ofNumber(ibmPoolMetric.getType().getNumber()))
                    .init(ibmPoolMetric.getInit())
                    .max(ibmPoolMetric.getMax())
                    .used(ibmPoolMetric.getUsed())
                    .committed(ibmPoolMetric.getCommitted())
                    .build());
            }
        }
        return metrics;
    }
}
