/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.dispatcher;

import com.huawei.apm.core.source.IBMMemoryPoolType;
import com.huawei.apm.core.source.ServerMonitorCPUMetric;
import com.huawei.apm.core.source.ServerMonitorDiskMetric;
import com.huawei.apm.core.source.ServerMonitorMemoryMetric;
import com.huawei.apm.core.source.ServerMonitorNetWorkMetric;
import com.huawei.apm.core.source.ServiceInstanceIBMJVMMemoryPool;
import com.huawei.apm.network.language.agent.v3.IBMMemoryPool;
import com.huawei.apm.network.language.agent.v3.ServerCPU;
import com.huawei.apm.network.language.agent.v3.ServerDisk;
import com.huawei.apm.network.language.agent.v3.ServerMemory;
import com.huawei.apm.network.language.agent.v3.ServerMonitoringMetric;
import com.huawei.apm.network.language.agent.v3.ServerNetWork;

import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.NodeType;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器监控指标接收处理类
 *
 * @author zhengbin zhao
 * @since 2021-02-25
 */
public class ServerMonitorMetricDispatcher {
    /**
     * 服务ID-key
     */
    private static final String SERVICE_ID = "serviceId";

    /**
     * 实例ID-key
     */
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";

    /**
     * 服务名-key
     */
    private static final String SERVICE = "service";

    /**
     * 实例名-key
     */
    private static final String SERVICE_INSTANCE = "serviceInstance";

    private final SourceReceiver sourceReceiver;

    public ServerMonitorMetricDispatcher(ModuleManager moduleManager) {
        this.sourceReceiver = moduleManager.find(CoreModule.NAME).provider().getService(SourceReceiver.class);
    }

    /**
     * 将信息分发到各个source，便于后续聚合统计
     *
     * @param service         服务名
     * @param serviceInstance 实例名
     * @param metrics         采集对象
     * @param copy         复制数据标记
     */
    public void sendMetric(String service, String serviceInstance, ServerMonitoringMetric metrics, int copy) {
        final String serviceId = IDManager.ServiceID.buildId(service, NodeType.Normal);
        final String serviceInstanceId = IDManager.ServiceInstanceID.buildId(serviceId, serviceInstance);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(SERVICE, service);
        paramMap.put(SERVICE_ID, serviceId);
        paramMap.put(SERVICE_INSTANCE, serviceInstance);
        paramMap.put(SERVICE_INSTANCE_ID, serviceInstanceId);
        this.sendToCpuMetricProcess(paramMap, metrics.getCpu(), copy);
        this.sendToDiskMetricProcess(paramMap, metrics.getDiskList(), copy);
        this.sendToNetWorkMetricProcess(paramMap, metrics.getNetWork(), copy);
        this.sendToMemoryMetricProcess(paramMap, metrics.getMemory(), copy);
        long minuteTimeBucket = TimeBucket.getMinuteTimeBucket(metrics.getTime());
        this.sendToMemoryPoolMetricProcess(paramMap, metrics.getIbmMemoryPoolList(), minuteTimeBucket, copy);
    }

    private void sendToMemoryPoolMetricProcess(Map<String, String> paramMap, List<IBMMemoryPool> memoryPools,
                                               long timeBucket, int copy) {
        memoryPools.forEach(memoryPool -> {
            ServiceInstanceIBMJVMMemoryPool serviceInstanceIbmJvmMemoryPool = new ServiceInstanceIBMJVMMemoryPool();
            serviceInstanceIbmJvmMemoryPool.setId(paramMap.get(SERVICE_INSTANCE_ID));
            serviceInstanceIbmJvmMemoryPool.setName(paramMap.get(SERVICE));
            serviceInstanceIbmJvmMemoryPool.setServiceId(paramMap.get(SERVICE_ID));
            serviceInstanceIbmJvmMemoryPool.setServiceName(paramMap.get(SERVICE_INSTANCE));

            switch (memoryPool.getType()) {
                case IBM_CLASS_STORAGE_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_CLASS_STORAGE_USAGE);
                    break;
                case IBM_CODE_CACHE_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_CODE_CACHE_USAGE);
                    break;
                case IBM_DATA_CACHE_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_DATA_CACHE_USAGE);
                    break;
                case IBM_MISCELLANEOUS_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_MISCELLANEOUS_USAGE);
                    break;
                case IBM_NURSERY_ALLOCATE_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_NURSERY_ALLOCATE_USAGE);
                    break;
                case IBM_NURSERY_SURVIVOR_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_NURSERY_SURVIVOR_USAGE);
                    break;
                case IBM_TENURED_LOA_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_TENURED_LOA_USAGE);
                    break;
                case IBM_TENURED_SOA_USAGE:
                    serviceInstanceIbmJvmMemoryPool.setPoolType(IBMMemoryPoolType.IBM_TENURED_SOA_USAGE);
                    break;
                default:
                    break;
            }

            serviceInstanceIbmJvmMemoryPool.setInit(memoryPool.getInit());
            serviceInstanceIbmJvmMemoryPool.setMax(memoryPool.getMax());
            serviceInstanceIbmJvmMemoryPool.setUsed(memoryPool.getUsed());
            serviceInstanceIbmJvmMemoryPool.setCommitted(memoryPool.getCommitted());
            serviceInstanceIbmJvmMemoryPool.setTimeBucket(timeBucket);
            serviceInstanceIbmJvmMemoryPool.setCopy(copy);// huawei update.无损演练：添加复制标签
            sourceReceiver.receive(serviceInstanceIbmJvmMemoryPool);
        });
    }

    private void sendToMemoryMetricProcess(Map<String, String> paramMap, ServerMemory memory, int copy) {
        if (memory.getTimeBucket() < 1) {
            return;
        }
        ServerMonitorMemoryMetric serverMonitorMemory = new ServerMonitorMemoryMetric();
        serverMonitorMemory.setId(paramMap.get(SERVICE_INSTANCE_ID));
        serverMonitorMemory.setName(paramMap.get(SERVICE));
        serverMonitorMemory.setServiceId(paramMap.get(SERVICE_ID));
        serverMonitorMemory.setServiceName(paramMap.get(SERVICE_INSTANCE));
        serverMonitorMemory.setBuffers(memory.getBuffers());
        serverMonitorMemory.setCached(memory.getCached());
        serverMonitorMemory.setMemoryTotal(memory.getMemoryTotal());
        serverMonitorMemory.setSwapCached(memory.getSwapCached());
        serverMonitorMemory.setMemoryUsed(memory.getMemoryUsed());
        serverMonitorMemory.setTimeBucket(TimeBucket.getMinuteTimeBucket(memory.getTimeBucket()));
        serverMonitorMemory.setCopy(copy);// huawei update.无损演练：添加复制标签
        sourceReceiver.receive(serverMonitorMemory);
    }

    private void sendToCpuMetricProcess(Map<String, String> paramMap, ServerCPU cpu, int copy) {
        if (cpu.getTimeBucket() < 1) {
            return;
        }
        ServerMonitorCPUMetric serverMonitorCpu = new ServerMonitorCPUMetric();
        serverMonitorCpu.setId(paramMap.get(SERVICE_INSTANCE_ID));
        serverMonitorCpu.setName(paramMap.get(SERVICE));
        serverMonitorCpu.setServiceId(paramMap.get(SERVICE_ID));
        serverMonitorCpu.setServiceName(paramMap.get(SERVICE_INSTANCE));
        serverMonitorCpu.setIdle(cpu.getIdle());
        serverMonitorCpu.setSys(cpu.getSys());
        serverMonitorCpu.setWait(cpu.getWait());
        serverMonitorCpu.setUser(cpu.getUser());
        serverMonitorCpu.setTimeBucket(TimeBucket.getMinuteTimeBucket(cpu.getTimeBucket()));
        serverMonitorCpu.setCopy(copy);// huawei update.无损演练：添加复制标签
        sourceReceiver.receive(serverMonitorCpu);
    }

    private void sendToDiskMetricProcess(Map<String, String> paramMap, List<ServerDisk> disk, int copy) {
        disk.forEach(serverDisk -> {
            ServerMonitorDiskMetric serverMonitorDisk = new ServerMonitorDiskMetric();
            serverMonitorDisk.setId(paramMap.get(SERVICE_INSTANCE_ID) + "_" + serverDisk.getDiskName());
            serverMonitorDisk.setName(paramMap.get(SERVICE));
            serverMonitorDisk.setServiceId(paramMap.get(SERVICE_ID));
            serverMonitorDisk.setServiceName(paramMap.get(SERVICE_INSTANCE));
            serverMonitorDisk.setIoBusy(serverDisk.getIoBusy());
            serverMonitorDisk.setIoRead(serverDisk.getIoRead());
            serverMonitorDisk.setIoWrite(serverDisk.getIoWrite());
            serverMonitorDisk.setDiskName(serverDisk.getDiskName());
            serverMonitorDisk.setQueryId(paramMap.get(SERVICE_INSTANCE_ID));
            serverMonitorDisk.setTimeBucket(TimeBucket.getMinuteTimeBucket(serverDisk.getTimeBucket()));
            serverMonitorDisk.setCopy(copy);// huawei update.无损演练：添加复制标签
            sourceReceiver.receive(serverMonitorDisk);
        });
    }

    private void sendToNetWorkMetricProcess(Map<String, String> paramMap, ServerNetWork netWork, int copy) {
        if (netWork.getTimeBucket() < 1) {
            return;
        }
        ServerMonitorNetWorkMetric serverMonitorNetWork = new ServerMonitorNetWorkMetric();
        serverMonitorNetWork.setId(paramMap.get(SERVICE_INSTANCE_ID));
        serverMonitorNetWork.setName(paramMap.get(SERVICE));
        serverMonitorNetWork.setServiceId(paramMap.get(SERVICE_ID));
        serverMonitorNetWork.setServiceName(paramMap.get(SERVICE_INSTANCE));
        serverMonitorNetWork.setReadByte(netWork.getTotalReadBytes());
        serverMonitorNetWork.setSendByte(netWork.getTotalWriteBytes());
        serverMonitorNetWork.setReadPacket(netWork.getTotalReadPackage());
        serverMonitorNetWork.setSendPacket(netWork.getTotalWritePackage());
        serverMonitorNetWork.setTimeBucket(TimeBucket.getMinuteTimeBucket(netWork.getTimeBucket()));
        serverMonitorNetWork.setCopy(copy);// huawei update.无损演练：添加复制标签
        sourceReceiver.receive(serverMonitorNetWork);
    }
}
