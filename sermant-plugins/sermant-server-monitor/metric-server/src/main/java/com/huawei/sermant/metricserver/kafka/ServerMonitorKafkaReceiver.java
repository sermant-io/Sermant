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
import com.huawei.sermant.metricserver.dto.servermonitor.CpuDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.DiskDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.MemoryDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.NetworkDTO;
import com.huawei.sermant.metricserver.service.ServerMetricService;
import com.huawei.sermant.plugin.servermonitor.entity.CpuMetric;
import com.huawei.sermant.plugin.servermonitor.entity.DiskMetric;
import com.huawei.sermant.plugin.servermonitor.entity.MemoryMetric;
import com.huawei.sermant.plugin.servermonitor.entity.NetworkMetric;
import com.huawei.sermant.plugin.servermonitor.entity.ServerMetric;
import com.huawei.sermant.plugin.servermonitor.entity.ServerMetricCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServerMetric kafka接收处理类
 */
@Component
public class ServerMonitorKafkaReceiver {

    private final ServerMetricService serverMetricService;

    @Autowired
    public ServerMonitorKafkaReceiver(ServerMetricService serverMetricService) {
        this.serverMetricService = serverMetricService;
    }

    @KafkaListener(topics = "topic-server-monitor", groupId = "monitor-server")
    public void onMessage(byte[] record) {
        final ServerMetricCollection collection;
        try {
            collection = ServerMetricCollection.parseFrom(record);
        } catch (InvalidProtocolBufferException e) {
            return;
        }
        final String service = collection.getService();
        final String serviceInstance = collection.getServiceInstance();
        for (ServerMetric metric : collection.getMetricsList()) {
            final Instant time = Instant.ofEpochMilli(metric.getTime());
            addCpuMetric(service, serviceInstance, time, metric.getCpu());
            addMemoryMetric(service, serviceInstance, time, metric.getMemory());
            addNetworkMetric(service, serviceInstance, time, metric.getNetwork());
            addDisksMetric(service, serviceInstance, time, metric.getDisksList());
        }
    }

    private void addCpuMetric(String service, String serviceInstance, Instant time, CpuMetric cpu) {
        serverMetricService.addCpuMetric(CpuDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .userPercentage((long) cpu.getUserPercentage())
            .sysPercentage((long) cpu.getSysPercentage())
            .ioWaitPercentage((long) cpu.getIoWaitPercentage())
            .idlePercentage((long) cpu.getIdlePercentage())
            .build());
    }

    private void addMemoryMetric(String service, String serviceInstance, Instant time, MemoryMetric memory) {
        serverMetricService.addMemoryMetric(MemoryDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .memoryTotal(memory.getMemoryTotal())
            .swapCached(memory.getSwapCached())
            .memoryUsed(memory.getMemoryUsed())
            .cached(memory.getCached())
            .buffers(memory.getBuffers())
            .build());
    }

    private void addNetworkMetric(String service, String serviceInstance, Instant time, NetworkMetric network) {
        serverMetricService.addNetworkMetric(NetworkDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .readBytesPerSec(network.getReadBytesPerSec())
            .writeBytesPerSec(network.getWriteBytesPerSec())
            .readPackagesPerSec(network.getReadPackagesPerSec())
            .writePackagesPerSec(network.getWritePackagesPerSec())
            .build());
    }

    private void addDisksMetric(String service, String serviceInstance, Instant time, List<DiskMetric> disks) {
        Map<String, Object> readRateOfEachDevice = new HashMap<>();
        Map<String, Object> writeRateOfEachDevice = new HashMap<>();
        Map<String, Object> ioSpentPercentageOfEachDevice = new HashMap<>();
        for (DiskMetric disk : disks) {
            String deviceName = disk.getDeviceName();
            readRateOfEachDevice.put(deviceName, disk.getReadBytesPerSec());
            writeRateOfEachDevice.put(deviceName, disk.getWriteBytesPerSec());
            ioSpentPercentageOfEachDevice.put(deviceName, disk.getIoSpentPercentage());
        }
        serverMetricService.addDiskMetric(DiskDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .type(DiskDTO.ValueType.READ_RATE)
            .deviceAndValueMap(readRateOfEachDevice).build());
        serverMetricService.addDiskMetric(DiskDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .type(DiskDTO.ValueType.WRITE_RATE)
            .deviceAndValueMap(writeRateOfEachDevice).build());
        serverMetricService.addDiskMetric(DiskDTO.builder()
            .service(service)
            .serviceInstance(serviceInstance)
            .time(time)
            .type(DiskDTO.ValueType.IO_SPENT_PERCENTAGE)
            .deviceAndValueMap(ioSpentPercentageOfEachDevice).build());
    }
}
