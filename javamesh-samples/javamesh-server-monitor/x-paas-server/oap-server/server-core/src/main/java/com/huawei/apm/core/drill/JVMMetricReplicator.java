/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.skywalking.apm.network.common.v3.CPU;
import org.apache.skywalking.apm.network.language.agent.v3.Thread;
import org.apache.skywalking.apm.network.language.agent.v3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.huawei.apm.core.drill.MetricLabelValueType.*;

/**
 * SegmentObject复制器
 *
 * @author qinfurong
 * @since 2021-06-29
 */
public class JVMMetricReplicator extends BaseReplicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JVMMetricReplicator.class);
    /**
     * 复制JVMMetrics，并按照标签配置值修改
     *
     * @param builder 被复制的采集对象
     */
    public static void copyModification(JVMMetricCollection.Builder builder) {
        List<JVMMetric> metrics = builder.getMetricsList();
        List<JVMMetric> copyMetrics = new ArrayList<>(metrics.size());

        // 深拷贝对象
        metrics.forEach(jvmMetric -> {
            try {
                copyMetrics.add(JVMMetric.parseFrom(jvmMetric.toByteArray()));
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error("parse JVMMetric to JVMMetric failed!, message:{}", e);
            }
        });

        // 解析指标标签值
        Map<String, Object> metricValueMap = parseLabelMetricValueToMap(builder.getLabelMetricValue());

        // 修改数据
        copyMetrics.forEach(jvmMetric -> {
            toCopyModificationCPU(jvmMetric.getCpu(), metricValueMap);
            toCopyModificationMemory(jvmMetric.getMemoryList(), metricValueMap);
            toCopyModificationGC(jvmMetric.getGcList(), metricValueMap);
            toCopyModificationThread(jvmMetric.getThread(), metricValueMap);
            toCopyModificationMemoryPool(jvmMetric.getMemoryPoolList(), metricValueMap);
        });

        builder.clearMetrics();
        builder.addAllMetrics(copyMetrics);
    }

    private static void toCopyModificationCPU(CPU cpu, Map<String, Object> metricValueMap) {
        updateNumericFiledValue(cpu, "usagePercent_", DOUBLE, metricValueMap.get("INSTANCE_JVM_CPU"));
    }

    private static void toCopyModificationMemory(List<Memory> memoryList, Map<String, Object> metricValueMap) {
        if (memoryList == null || memoryList.isEmpty()) {
            return;
        }
        memoryList.forEach(memory -> {
            boolean isHeap = memory.getIsHeap();
            if (isHeap) {
                updateNumericFiledValue(memory, "used_", LONG, metricValueMap.get("INSTANCE_JVM_MEMORY_HEAP"));
                updateNumericFiledValue(memory, "max_", LONG, metricValueMap.get("INSTANCE_JVM_MEMORY_HEAP_MAX"));
            } else {
                updateNumericFiledValue(memory, "used_", LONG, metricValueMap.get("INSTANCE_JVM_MEMORY_NOHEAP"));
                updateNumericFiledValue(memory, "max_", LONG, metricValueMap.get("INSTANCE_JVM_MEMORY_NOHEAP_MAX"));
            }
        });

    }

    private static void toCopyModificationGC(List<GC> gcList, Map<String, Object> metricValueMap) {
        if (gcList == null || gcList.isEmpty()) {
            return;
        }
        gcList.forEach(gc -> {
            switch (gc.getPhrase()) {
                case NEW:
                    updateNumericFiledValue(gc, "time_", LONG, metricValueMap.get("INSTANCE_JVM_YOUNG_GC_TIME"));
                    updateNumericFiledValue(gc, "count_", LONG, metricValueMap.get("INSTANCE_JVM_YOUNG_GC_COUNT"));
                    break;
                case OLD:
                    updateNumericFiledValue(gc, "time_", LONG, metricValueMap.get("INSTANCE_JVM_OLD_GC_TIME"));
                    updateNumericFiledValue(gc, "count_", LONG, metricValueMap.get("INSTANCE_JVM_OLD_GC_COUNT"));
                    break;
                default:
                    break;
            }
        });
    }

    private static void toCopyModificationThread(Thread thread, Map<String, Object> metricValueMap) {
        updateNumericFiledValue(thread, "liveCount_", LONG, metricValueMap.get("INSTANCE_JVM_THREAD_LIVE_COUNT"));
        updateNumericFiledValue(thread, "daemonCount_", LONG, metricValueMap.get("INSTANCE_JVM_THREAD_DAEMON_COUNT"));
        updateNumericFiledValue(thread, "peakCount_", LONG, metricValueMap.get("INSTANCE_JVM_THREAD_PEAK_COUNT"));
    }

    private static void toCopyModificationMemoryPool(List<MemoryPool> memoryPoolList, Map<String, Object> metricValueMap) {
        if (memoryPoolList == null || memoryPoolList.isEmpty()) {
            return;
        }
        memoryPoolList.forEach(memoryPool -> {
            switch (memoryPool.getType()) {
                case NEWGEN_USAGE:
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_EDEN_USED"));
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_EDEN_INIT"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_EDEN_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_EDEN_COMMITTED"));
                    break;
                case OLDGEN_USAGE:
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_OLD_USED"));
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_OLD_INIT"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_OLD_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_OLD_COMMITTED"));
                    break;
                case PERMGEN_USAGE:
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_PERMGEN_USED"));
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_PERMGEN_INIT"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_PERMGEN_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_PERMGEN_COMMITTED"));
                    break;
                case SURVIVOR_USAGE:
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_SURVIVOR_USED"));
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_SURVIVOR_INIT"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_SURVIVOR_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_HEAP_SURVIVOR_COMMITTED"));
                    break;
                case METASPACE_USAGE:
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_METASPACE_USED"));
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_METASPACE_INIT"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_METASPACE_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_METASPACE_COMMITTED"));
                    break;
                case CODE_CACHE_USAGE:
                    updateNumericFiledValue(memoryPool, "used_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_CODE_CACHE_USED"));
                    updateNumericFiledValue(memoryPool, "init_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_CODE_CACHE_INIT"));
                    updateNumericFiledValue(memoryPool, "max_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_CODE_CACHE_MAX"));
                    updateNumericFiledValue(memoryPool, "committed_", LONG, metricValueMap.get("INSTANCE_ORACLEJVM_MEMORY_NOHEAP_CODE_CACHE_COMMITTED"));
                    break;
            }
        });
    }
}
