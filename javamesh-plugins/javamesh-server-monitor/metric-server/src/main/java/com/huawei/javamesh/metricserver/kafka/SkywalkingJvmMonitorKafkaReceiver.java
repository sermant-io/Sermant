/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.CpuDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.GcDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.OracleMemoryDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.OracleMemoryPoolDTO;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.ThreadDTO;
import com.huawei.javamesh.metricserver.service.OracleMemoryPoolService;
import com.huawei.javamesh.metricserver.service.OracleMemoryService;
import com.huawei.javamesh.metricserver.service.SkywalkingJvmMetricService;
import org.apache.skywalking.apm.network.common.v3.CPU;
import org.apache.skywalking.apm.network.language.agent.v3.GC;
import org.apache.skywalking.apm.network.language.agent.v3.GCPhrase;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetric;
import org.apache.skywalking.apm.network.language.agent.v3.JVMMetricCollection;
import org.apache.skywalking.apm.network.language.agent.v3.Memory;
import org.apache.skywalking.apm.network.language.agent.v3.MemoryPool;
import org.apache.skywalking.apm.network.language.agent.v3.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Skywalking JVMMetric kafka接收处理类
 */
@Component
public class SkywalkingJvmMonitorKafkaReceiver {

    private final OracleMemoryService memoryService;

    private final OracleMemoryPoolService memoryPoolService;

    private final SkywalkingJvmMetricService jvmMetricService;

    @Autowired
    public SkywalkingJvmMonitorKafkaReceiver(OracleMemoryService memoryService,
                                             OracleMemoryPoolService memoryPoolService,
                                             SkywalkingJvmMetricService jvmMetricService) {
        this.memoryService = memoryService;
        this.memoryPoolService = memoryPoolService;
        this.jvmMetricService = jvmMetricService;
    }

    @KafkaListener(topics = "topic-oracle-jvm-monitor", groupId = "monitor-server")
    public void onMessage(byte[] record) {
        JVMMetricCollection collection;
        try {
            collection = JVMMetricCollection.parseFrom(record);
        } catch (InvalidProtocolBufferException e) {
            return;
        }
        final String service = collection.getService();
        final String serviceInstance = collection.getServiceInstance();
        for (JVMMetric metric : collection.getMetricsList()) {
            final Instant time = Instant.ofEpochMilli(metric.getTime());
            addCpuMetric(service, serviceInstance, time, metric);
            addThreadMetric(service, serviceInstance, time, metric);
            addGcMetric(service, serviceInstance, time, metric);
            addMemoryMetric(service, serviceInstance, time, metric);
            addMemoryPoolMetric(service, serviceInstance, time, metric);
        }

    }

    private void addMemoryPoolMetric(String service, String serviceInstance, Instant time, JVMMetric metric) {
        for (MemoryPool memoryPool : metric.getMemoryPoolList()) {
            OracleMemoryPoolDTO.OraclePoolType type;
            switch (memoryPool.getType()) {
                case CODE_CACHE_USAGE:
                    type = OracleMemoryPoolDTO.OraclePoolType.CODE_CACHE;
                    break;
                case NEWGEN_USAGE:
                    type = OracleMemoryPoolDTO.OraclePoolType.NEW_GEN;
                    break;
                case OLDGEN_USAGE:
                    type = OracleMemoryPoolDTO.OraclePoolType.OLD_GEN;
                    break;
                case SURVIVOR_USAGE:
                    type = OracleMemoryPoolDTO.OraclePoolType.SURVIVOR;
                    break;
                case PERMGEN_USAGE:
                    type = OracleMemoryPoolDTO.OraclePoolType.PERM_GEN;
                    break;
                case METASPACE_USAGE:
                    type = OracleMemoryPoolDTO.OraclePoolType.METASPACE;
                    break;
                default:
                    continue;
            }
            memoryPoolService.addMemoryPoolMetric(OracleMemoryPoolDTO.builder()
                .service(service)
                .serviceInstance(serviceInstance)
                .time(time)
                .type(type)
                .committed(memoryPool.getCommitted())
                .init(memoryPool.getInit())
                .max(memoryPool.getMax())
                .used(memoryPool.getUsed())
                .build());
        }
    }

    private void addMemoryMetric(String service, String serviceInstance, Instant time, JVMMetric metric) {
        for (Memory memory : metric.getMemoryList()) {
            memoryService.addMemoryMetric(OracleMemoryDTO.builder()
                .service(service)
                .serviceInstance(serviceInstance)
                .time(time)
                .type(memory.getIsHeap() ? OracleMemoryDTO.OracleMemoryType.HEAP
                    : OracleMemoryDTO.OracleMemoryType.NON_HEAP)
                .committed(memory.getCommitted())
                .init(memory.getInit())
                .max(memory.getMax())
                .used(memory.getUsed())
                .build());
        }
    }

    private void addGcMetric(String service, String serviceInstance, Instant time, JVMMetric metric) {
        for (GC gc : metric.getGcList()) {
            jvmMetricService.addGcMetric(GcDTO.builder()
                .service(service)
                .serviceInstance(serviceInstance)
                .time(time)
                .gcType(gc.getPhrase() == GCPhrase.OLD ? GcDTO.GcType.OLD : GcDTO.GcType.YOUNG)
                .gcTime(gc.getTime())
                .gcCount(gc.getCount())
                .build());
        }
    }

    private void addThreadMetric(String service, String serviceInstance, Instant time, JVMMetric metric) {
        Thread thread = metric.getThread();
        jvmMetricService.addThreadMetric(ThreadDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .peakCount(thread.getPeakCount())
            .liveCount(thread.getLiveCount())
            .daemonCount(thread.getDaemonCount())
            .build());
    }

    private void addCpuMetric(String service, String serviceInstance, Instant time, JVMMetric metric) {
        CPU cpu = metric.getCpu();
        jvmMetricService.addCpuMetric(CpuDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .usagePercent(cpu.getUsagePercent())
            .build());
    }

}
