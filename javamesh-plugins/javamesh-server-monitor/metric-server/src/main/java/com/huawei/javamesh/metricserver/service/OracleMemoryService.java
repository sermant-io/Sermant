/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.service;

import com.huawei.javamesh.metricserver.dao.influxdb.InfluxDao;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.MemoryPoolInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.HeapMemoryInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.NonHeapMemoryInfluxEntity;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.OracleMemoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Oracle Memory 服务
 */
@Service
public class OracleMemoryService extends InfluxService {

    @Autowired
    public OracleMemoryService(InfluxDao influxDao) {
        super(influxDao);
    }

    /**
     * 添加{@link OracleMemoryDTO}实体
     *
     * @param memoryDto 待添加的{@link OracleMemoryDTO}实体
     */
    public void addMemoryMetric(OracleMemoryDTO memoryDto) {
        insert(() -> newEntityOfType(memoryDto.getType()), memoryDto);
    }

    /**
     * 查询{@link OracleMemoryDTO}实体列表
     *
     * @param type  Memory类型
     * @param start 开始时间
     * @param end   结束时间
     * @return @link OracleMemoryDTO}实体列表
     */
    public List<OracleMemoryDTO> getMemoryMetrics(OracleMemoryDTO.OracleMemoryType type, String start, String end) {
        return query(start, end, getClassOfType(type)).stream()
            .map(entity -> OracleMemoryDTO.builder()
                .service(entity.getService())
                .serviceInstance(entity.getServiceInstance())
                .time(entity.getTime())
                .type(type)
                .init(entity.getInit())
                .max(entity.getMax())
                .used(entity.getUsed())
                .committed(entity.getCommitted()).build())
            .collect(Collectors.toList());
    }

    private MemoryPoolInfluxEntity newEntityOfType(OracleMemoryDTO.OracleMemoryType type) {
        switch (type) {
            case HEAP:
                return new HeapMemoryInfluxEntity();
            case NON_HEAP:
                return new NonHeapMemoryInfluxEntity();
            default:
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }

    private Class<? extends MemoryPoolInfluxEntity> getClassOfType(OracleMemoryDTO.OracleMemoryType type) {
        switch (type) {
            case HEAP:
                return HeapMemoryInfluxEntity.class;
            case NON_HEAP:
                return NonHeapMemoryInfluxEntity.class;
            default:
                // null
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }


}
