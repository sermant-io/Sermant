/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.apm.network.language.agent.v3.IBMMemoryPool;
import com.lubanops.apm.plugin.servermonitor.entity.CpuMetric;
import com.lubanops.apm.plugin.servermonitor.entity.DiskMetric;
import com.lubanops.apm.plugin.servermonitor.entity.IbmJvmMetric;
import com.lubanops.apm.plugin.servermonitor.entity.MemoryMetric;
import com.lubanops.apm.plugin.servermonitor.entity.NetworkMetric;
import com.lubanops.apm.plugin.servermonitor.entity.ServerMonitorCollection;
import com.lubanops.apm.plugin.servermonitor.entity.ServerMonitorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.huawei.apm.core.drill.MetricLabelValueType.DOUBLE;
import static com.huawei.apm.core.drill.MetricLabelValueType.INT;
import static com.huawei.apm.core.drill.MetricLabelValueType.LONG;

/**
 * ServerMonitoringMetric复制器
 *
 * @author qinfurong
 * @since 2021-06-29
 */
public class KafkaServerMonitoringMetricReplicator extends BaseReplicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaServerMonitoringMetricReplicator.class);
    /**
     * 复制ServerMonitoringMetric，并按照标签配置值修改
     *
     * @param builder 被复制的采集对象
     */
    public static void copyModification(ServerMonitorCollection.Builder builder) {
        List<ServerMonitorMetric> metrics = builder.getMetricList();
        List<ServerMonitorMetric> copyMetrics = new ArrayList<>(metrics.size());

        // 深拷贝对象
        metrics.forEach(metric -> {
            try {
                copyMetrics.add(ServerMonitorMetric.parseFrom(metric.toByteArray()));
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error("parse ServerMonitoringMetric to ServerMonitoringMetric failed!, message:{}", e);
            }
        });

        // 解析指标标签值
        Map<String, Object> metricValueMap = parseLabelMetricValueToMap(builder.getLabelMetricValue());

        // 修改数据
        copyMetrics.forEach(serverMonitoringMetric -> {
            toCopyModificationCPU(serverMonitoringMetric.getCpu(), metricValueMap);
            toCopyModificationDisk(serverMonitoringMetric.getDisksList(), metricValueMap);
            toCopyModificationMemory(serverMonitoringMetric.getMemory(), metricValueMap);
            toCopyModificationNetWork(serverMonitoringMetric.getNetwork(), metricValueMap);
            toCopyModificationIbmMemoryPool(serverMonitoringMetric.getIbmJvmMetricsList(), metricValueMap);
        });

        builder.clearMetric();
        builder.addAllMetric(copyMetrics);
    }

    private static void toCopyModificationCPU(CpuMetric cpu, Map<String, Object> metricValueMap) {
        updateNumericFiledValue(cpu, "userPercentage_", INT, metricValueMap.get("SERVER_MONITOR_CPU_USER"));
        updateNumericFiledValue(cpu, "sysPercentage_", INT, metricValueMap.get("SERVER_MONITOR_CPU_SYS"));
        updateNumericFiledValue(cpu, "ioWaitPercentage_", INT, metricValueMap.get("SERVER_MONITOR_CPU_WAIT"));
        updateNumericFiledValue(cpu, "idlePercentage_", INT, metricValueMap.get("SERVER_MONITOR_CPU_IDLE"));
    }

    private static void toCopyModificationDisk(List<DiskMetric> diskList, Map<String, Object> metricValueMap) {
        if (diskList == null || diskList.isEmpty()) {
            return;
        }
        diskList.forEach(serverDisk -> {
            updateNumericFiledValue(serverDisk, "readBytesPerSec_", LONG, metricValueMap.get("SERVER_MONITOR_DISK_IOREAD"));
            updateNumericFiledValue(serverDisk, "writeBytesPerSec_", LONG, metricValueMap.get("SERVER_MONITOR_DISK_IOWRITE"));
            updateNumericFiledValue(serverDisk, "ioSpentPercentage_", DOUBLE, metricValueMap.get("SERVER_MONITOR_DISK_IOBUSY"));
        });
    }

    private static void toCopyModificationMemory(MemoryMetric memory, Map<String, Object> metricValueMap) {
        updateNumericFiledValue(memory, "memoryTotal_", LONG, metricValueMap.get("SERVER_MONITOR_MEMORY_MEMORYTOTAL"));
        updateNumericFiledValue(memory, "swapCached_", LONG, metricValueMap.get("SERVER_MONITOR_MEMORY_SWAPCACHED"));
        updateNumericFiledValue(memory, "cached_", LONG, metricValueMap.get("SERVER_MONITOR_MEMORY_CACHED"));
        updateNumericFiledValue(memory, "buffers_", LONG, metricValueMap.get("SERVER_MONITOR_MEMORY_BUFFERS"));
        updateNumericFiledValue(memory, "memoryUsed_", LONG, metricValueMap.get("SERVER_MONITOR_MEMORY_MEMORYUSED"));
    }

    private static void toCopyModificationNetWork(NetworkMetric netWork, Map<String, Object> metricValueMap) {
        updateNumericFiledValue(netWork, "readBytesPerSec_", LONG, metricValueMap.get("SERVER_MONITOR_NETWORK_READBYTE"));
        updateNumericFiledValue(netWork, "writeBytesPerSec_", LONG, metricValueMap.get("SERVER_MONITOR_NETWORK_SENDBYTE"));
        updateNumericFiledValue(netWork, "readPackagesPerSec_", LONG, metricValueMap.get("SERVER_MONITOR_NETWORK_READPACKET"));
        updateNumericFiledValue(netWork, "writePackagesPerSec_", LONG, metricValueMap.get("SERVER_MONITOR_NETWORK_SENDPACKET"));
    }

    private static void toCopyModificationIbmMemoryPool(List<IbmJvmMetric> ibmMemoryPoolList, Map<String, Object> metricValueMap) {
        if (ibmMemoryPoolList == null || ibmMemoryPoolList.isEmpty()) {
            return;
        }
        ibmMemoryPoolList.forEach(memoryPool -> {
            switch (memoryPool.getType()) {
                case IBM_CLASS_STORAGE_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CLASS_STORAGE_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CLASS_STORAGE_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CLASS_STORAGE_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CLASS_STORAGE_COMMITTED"));
                    break;
                case IBM_CODE_CACHE_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CODE_CACHE_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CODE_CACHE_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CODE_CACHE_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_CODE_CACHE_COMMITTED"));
                    break;
                case IBM_DATA_CACHE_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_DATA_CACHE_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_DATA_CACHE_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_DATA_CACHE_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_DATA_CACHE_COMMITTED"));
                    break;
                case IBM_MISCELLANEOUS_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_MISCELLANEOUS_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_MISCELLANEOUS_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_MISCELLANEOUS_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_NONHEAP_MISCELLANEOUS_COMMITTED"));
                    break;
                case IBM_NURSERY_ALLOCATE_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_ALLOCATE_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_ALLOCATE_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_ALLOCATE_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_ALLOCATE_COMMITTED"));
                    break;
                case IBM_NURSERY_SURVIVOR_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_SURVIVOR_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_SURVIVOR_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_SURVIVOR_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_NURSERY_SURVIVOR_COMMITTED"));
                    break;
                case IBM_TENURED_LOA_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_LOA_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_LOA_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_LOA_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_LOA_COMMITTED"));
                    break;
                case IBM_TENURED_SOA_USAGE:
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_SOA_INIT"));
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_SOA_USED"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_SOA_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_IBMJVM_MEMORY_HEAP_TENURED_SOA_COMMITTED"));
                    break;
                default:
                    break;
            }
        });
    }
}
