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
import com.huawei.sermant.metricserver.dto.openjdk.CpuDTO;
import com.huawei.sermant.metricserver.dto.openjdk.GcDTO;
import com.huawei.sermant.metricserver.dto.openjdk.OpenJdkMemoryDTO;
import com.huawei.sermant.metricserver.dto.openjdk.OpenJdkMemoryPoolDTO;
import com.huawei.sermant.metricserver.dto.openjdk.ThreadDTO;
import com.huawei.sermant.metricserver.service.OpenJdkMemoryPoolService;
import com.huawei.sermant.metricserver.service.OpenJdkMemoryService;
import com.huawei.sermant.metricserver.service.OpenJdkJvmMetricService;
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
 * OpenJdk JVMMetric kafka接收处理类
 */
@Component
public class OpenJdkJvmMonitorKafkaReceiver {

    private final OpenJdkMemoryService memoryService;

    private final OpenJdkMemoryPoolService memoryPoolService;

    private final OpenJdkJvmMetricService jvmMetricService;

    @Autowired
    public OpenJdkJvmMonitorKafkaReceiver(
            OpenJdkMemoryService memoryService,
            OpenJdkMemoryPoolService memoryPoolService,
            OpenJdkJvmMetricService jvmMetricService) {
        this.memoryService = memoryService;
        this.memoryPoolService = memoryPoolService;
        this.jvmMetricService = jvmMetricService;
    }

    @KafkaListener(topics = "topic-open-jdk-jvm-monitor", groupId = "monitor-server")
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
            OpenJdkMemoryPoolDTO.OraclePoolType type;
            switch (memoryPool.getType()) {
                case CODE_CACHE_USAGE:
                    type = OpenJdkMemoryPoolDTO.OraclePoolType.CODE_CACHE;
                    break;
                case NEWGEN_USAGE:
                    type = OpenJdkMemoryPoolDTO.OraclePoolType.NEW_GEN;
                    break;
                case OLDGEN_USAGE:
                    type = OpenJdkMemoryPoolDTO.OraclePoolType.OLD_GEN;
                    break;
                case SURVIVOR_USAGE:
                    type = OpenJdkMemoryPoolDTO.OraclePoolType.SURVIVOR;
                    break;
                case PERMGEN_USAGE:
                    type = OpenJdkMemoryPoolDTO.OraclePoolType.PERM_GEN;
                    break;
                case METASPACE_USAGE:
                    type = OpenJdkMemoryPoolDTO.OraclePoolType.METASPACE;
                    break;
                default:
                    continue;
            }
            memoryPoolService.addMemoryPoolMetric(OpenJdkMemoryPoolDTO.builder()
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
            memoryService.addMemoryMetric(OpenJdkMemoryDTO.builder()
                    .service(service)
                    .serviceInstance(serviceInstance)
                    .time(time)
                    .type(memory.getIsHeap() ? OpenJdkMemoryDTO.OracleMemoryType.HEAP
                            : OpenJdkMemoryDTO.OracleMemoryType.NON_HEAP)
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
