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

package com.huawei.sermant.metricserver.service;

import com.huawei.sermant.metricserver.dao.influxdb.InfluxDao;
import com.huawei.sermant.metricserver.dao.influxdb.common.FluxTableResolver;
import com.huawei.sermant.metricserver.dao.influxdb.entity.servermonitor.CpuInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.servermonitor.MemoryInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.servermonitor.NetworkInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.request.InfluxInsertRequest;
import com.huawei.sermant.metricserver.dao.influxdb.request.InfluxQueryRequest;
import com.huawei.sermant.metricserver.dto.servermonitor.CpuDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.DiskDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.MemoryDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.NetworkDTO;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ServerMetric服务
 */
@Service
public class ServerMetricService extends InfluxService {

    private static final String MEASUREMENT_DISK_READ_RATE = "server_monitor_disk_read_rate";
    private static final String MEASUREMENT_DISK_WRITE_RATE = "server_monitor_disk_write_rate";
    private static final String MEASUREMENT_DISK_IO_SPENT = "server_monitor_disk_io_spent";

    @Autowired
    public ServerMetricService(InfluxDao influxDao) {
        super((influxDao));
    }

    /**
     * 添加{@link CpuDTO}实体
     *
     * @param cpuDTO 待添加的{@link CpuDTO}实体
     */
    public void addCpuMetric(CpuDTO cpuDTO) {
        insert(CpuInfluxEntity::new, cpuDTO);
    }

    /**
     * 添加{@link MemoryDTO}实体
     *
     * @param memoryDTO 待添加的{@link MemoryDTO}实体
     */
    public void addMemoryMetric(MemoryDTO memoryDTO) {
        insert(MemoryInfluxEntity::new, memoryDTO);
    }

    /**
     * 添加{@link NetworkDTO}实体
     *
     * @param networkDTO 待添加的{@link NetworkDTO}实体
     */
    public void addNetworkMetric(NetworkDTO networkDTO) {
        insert(NetworkInfluxEntity::new, networkDTO);
    }

    /**
     * 添加{@link DiskDTO}实体
     *
     * @param diskDTO 待添加的{@link DiskDTO}实体
     */
    public void addDiskMetric(DiskDTO diskDTO) {
        String measurement = resolveDiskMeasurement(diskDTO.getType());
        InfluxInsertRequest.InfluxInsertRequestBuilder requestBuilder = InfluxInsertRequest.builder()
            .measurement(measurement)
            .time(diskDTO.getTime());
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_SERVICE, diskDTO.getService());
        tags.put(TAG_SERVICE_INSTANCE, diskDTO.getServiceInstance());
        InfluxInsertRequest request = requestBuilder.tags(tags)
            .fields(diskDTO.getDeviceAndValueMap()).build();
        getInfluxDao().asyncInsert(request);
    }

    /**
     * 查询指定时间段内的{@link CpuDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link CpuDTO}实体列表
     */
    public List<CpuDTO> getCpuMetrics(String start, String end) {
        return query(start, end, CpuInfluxEntity.class)
            .stream().map(e -> CpuDTO.builder()
                .service(e.getService())
                .serviceInstance(e.getServiceInstance())
                .time(e.getTime())
                .idlePercentage(e.getIdlePercentage())
                .ioWaitPercentage(e.getIoWaitPercentage())
                .sysPercentage(e.getSysPercentage())
                .userPercentage(e.getUserPercentage()).build()
            ).collect(Collectors.toList());
    }

    /**
     * 查询指定时间段内的{@link MemoryDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link MemoryDTO}实体列表
     */
    public List<MemoryDTO> getMemoryMetrics(String start, String end) {
        return query(start, end, MemoryInfluxEntity.class)
            .stream().map(e -> MemoryDTO.builder()
                .service(e.getService())
                .serviceInstance(e.getServiceInstance())
                .time(e.getTime())
                .memoryTotal(e.getMemoryTotal())
                .memoryUsed(e.getMemoryUsed())
                .buffers(e.getBuffers())
                .cached(e.getCached())
                .swapCached(e.getSwapCached()).build())
            .collect(Collectors.toList());
    }

    /**
     * 查询指定时间段内的{@link NetworkDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link NetworkDTO}实体列表
     */
    public List<NetworkDTO> getNetworkMetrics(String start, String end) {
        return query(start, end, NetworkInfluxEntity.class)
            .stream().map(e -> NetworkDTO.builder()
                .service(e.getService())
                .serviceInstance(e.getServiceInstance())
                .time(e.getTime())
                .readBytesPerSec(e.getReadBytesPerSec())
                .readPackagesPerSec(e.getReadPackagesPerSec())
                .writeBytesPerSec(e.getWriteBytesPerSec())
                .writePackagesPerSec(e.getWritePackagesPerSec())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * 查询指定类型和时间段内的{@link DiskDTO}实体列表
     *
     * @param type  {@link DiskDTO}类型
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link DiskDTO}实体列表
     */
    public List<DiskDTO> getDiskMetrics(DiskDTO.ValueType type, String start, String end) {
        InfluxQueryRequest request = InfluxQueryRequest.builder()
            .measurement(resolveDiskMeasurement(type))
            .start(start).end(end).build();
        return getInfluxDao().query(request, DiskMetricResolver.getInstance(), DiskDTO.class)
            .stream().peek(d -> d.setType(type)).collect(Collectors.toList());

    }

    private String resolveDiskMeasurement(DiskDTO.ValueType type) {
        switch (type) {
            case READ_RATE:
                return MEASUREMENT_DISK_READ_RATE;
            case WRITE_RATE:
                return MEASUREMENT_DISK_WRITE_RATE;
            case IO_SPENT_PERCENTAGE:
                return MEASUREMENT_DISK_IO_SPENT;
            default:
                throw new IllegalArgumentException("Illegal disk type");
        }
    }

    private static class DiskMetricResolver implements FluxTableResolver {

        private static final DiskMetricResolver INSTANCE = new DiskMetricResolver();

        public static DiskMetricResolver getInstance() {
            return INSTANCE;
        }

        @Override
        public <M> List<M> resolve(List<FluxTable> fluxTables, Class<M> targetClass) {
            if (targetClass != DiskDTO.class) {
                throw new IllegalArgumentException("Illegal type.");
            }
            final List<M> metrics = new LinkedList<>();
            Map<Instant, List<FluxRecord>> recordGroupByTime = fluxTables.stream()
                .map(FluxTable::getRecords)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(FluxRecord::getTime));
            for (Map.Entry<Instant, List<FluxRecord>> entry : recordGroupByTime.entrySet()) {
                String service = null;
                String serviceInstance = null;
                Map<String, Object> data = new HashMap<>();
                for (FluxRecord record : entry.getValue()) {
                    if (service == null) {
                        service = (String) record.getValueByKey(TAG_SERVICE);
                    }
                    if (serviceInstance == null) {
                        serviceInstance = (String) record.getValueByKey(TAG_SERVICE_INSTANCE);
                    }
                    data.put(record.getField(), record.getValue());
                }
                DiskDTO diskDTO = DiskDTO.builder()
                    .time(entry.getKey())
                    .service(service)
                    .serviceInstance(serviceInstance)
                    .deviceAndValueMap(data)
                    .build();
                metrics.add((M) diskDTO);
            }
            return metrics;
        }
    }
}
