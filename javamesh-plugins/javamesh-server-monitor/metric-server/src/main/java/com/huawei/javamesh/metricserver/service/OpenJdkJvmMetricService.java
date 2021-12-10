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

package com.huawei.javamesh.metricserver.service;

import com.huawei.javamesh.metricserver.dao.influxdb.InfluxDao;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.openjdk.CpuInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.openjdk.GCInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.openjdk.OldGCInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.openjdk.ThreadInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.openjdk.YoungGCInfluxEntity;
import com.huawei.javamesh.metricserver.dto.openjdk.CpuDTO;
import com.huawei.javamesh.metricserver.dto.openjdk.GcDTO;
import com.huawei.javamesh.metricserver.dto.openjdk.ThreadDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenJdkJvmMetricService extends InfluxService {

    @Autowired
    public OpenJdkJvmMetricService(InfluxDao influxDao) {
        super(influxDao);
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
     * 添加{@link ThreadDTO}实体
     *
     * @param threadDTO 待添加的{@link ThreadDTO}实体
     */
    public void addThreadMetric(ThreadDTO threadDTO) {
        insert(ThreadInfluxEntity::new, threadDTO);
    }

    /**
     * 添加{@link GcDTO}实体
     *
     * @param gcDTO 待添加的{@link GcDTO}实体
     */
    public void addGcMetric(GcDTO gcDTO) {
        insert(() -> newGcEntityOfType(gcDTO.getGcType()), gcDTO);
    }

    /**
     * 查询指定时间段内的{@link CpuDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link CpuDTO}实体列表
     */
    public List<CpuDTO> getCpuMetrics(String start, String end) {
        return query(start, end, CpuInfluxEntity.class).stream()
            .map(entity -> CpuDTO.builder()
                .service(entity.getService())
                .serviceInstance(entity.getServiceInstance())
                .time(entity.getTime())
                .usagePercent(entity.getUsagePercent()).build())
            .collect(Collectors.toList());
    }

    /**
     * 查询指定时间段内的{@link ThreadDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link ThreadDTO}实体列表
     */
    public List<ThreadDTO> getThreadMetrics(String start, String end) {
        return query(start, end, ThreadInfluxEntity.class).stream()
            .map(e -> ThreadDTO.builder()
                .service(e.getService())
                .serviceInstance(e.getServiceInstance())
                .time(e.getTime())
                .daemonCount(e.getDaemonCount())
                .liveCount(e.getLiveCount())
                .peakCount(e.getPeakCount()).build())
            .collect(Collectors.toList());
    }

    /**
     * 查询指定时间段内的{@link GcDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link GcDTO}实体列表
     */
    public List<GcDTO> getGcMetrics(String start, String end, GcDTO.GcType type) {
        return query(start, end, getGcClassOfType(type)).stream()
            .map(e -> GcDTO.builder()
                .service(e.getService())
                .serviceInstance(e.getServiceInstance())
                .time(e.getTime())
                .gcCount(e.getGcCount())
                .gcTime(e.getGcTime())
                .gcType(type).build())
            .collect(Collectors.toList());
    }

    private Class<? extends GCInfluxEntity> getGcClassOfType(GcDTO.GcType type) {
        switch (type) {
            case YOUNG:
                return YoungGCInfluxEntity.class;
            case OLD:
                return OldGCInfluxEntity.class;
            default:
                // null
                throw new IllegalArgumentException("Illegal GC type.");
        }
    }

    private GCInfluxEntity newGcEntityOfType(GcDTO.GcType type) {
        switch (type) {
            case YOUNG:
                return new YoungGCInfluxEntity();
            case OLD:
                return new OldGCInfluxEntity();
            default:
                // null
                throw new IllegalArgumentException("Illegal GC type.");
        }
    }
}
