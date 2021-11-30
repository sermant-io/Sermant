/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.service;

import com.huawei.javamesh.metricserver.dao.influxdb.InfluxDao;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.MemoryPoolInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.oraclepool.CodeCacheInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.oraclepool.MetaspaceInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.oraclepool.NewGenInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.oraclepool.OldGenInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.oraclepool.PermGenInfluxEntity;
import com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm.oraclepool.SurvivorInfluxEntity;
import com.huawei.javamesh.metricserver.dto.skywalkingjvm.OracleMemoryPoolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Oracle Memory Pool服务
 */
@Service
public class OracleMemoryPoolService extends InfluxService {

    @Autowired
    public OracleMemoryPoolService(InfluxDao influxDao) {
        super(influxDao);
    }

    /**
     * 添加{@link OracleMemoryPoolDTO}实体
     *
     * @param memoryPool 待添加的{@link OracleMemoryPoolDTO}实体
     */
    public void addMemoryPoolMetric(OracleMemoryPoolDTO memoryPool) {
        insert(() -> newEntityOfType(memoryPool.getType()), memoryPool);
    }

    /**
     * 查询{@link OracleMemoryPoolDTO}实体列表
     *
     * @param type  Memory Pool类型
     * @param start 开始时间
     * @param end   结束时间
     * @return @link OracleMemoryPoolDTO}实体列表
     */
    public List<OracleMemoryPoolDTO> getMemoryPools(OracleMemoryPoolDTO.OraclePoolType type, String start, String end) {
        return query(start, end, getClassOfType(type)).stream()
            .map(entity -> OracleMemoryPoolDTO.builder()
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

    private MemoryPoolInfluxEntity newEntityOfType(OracleMemoryPoolDTO.OraclePoolType type) {
        switch (type) {
            case CODE_CACHE:
                return new CodeCacheInfluxEntity();
            case NEW_GEN:
                return new NewGenInfluxEntity();
            case SURVIVOR:
                return new SurvivorInfluxEntity();
            case OLD_GEN:
                return new OldGenInfluxEntity();
            case METASPACE:
                return new MetaspaceInfluxEntity();
            case PERM_GEN:
                return new PermGenInfluxEntity();
            default:
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }

    private Class<? extends MemoryPoolInfluxEntity> getClassOfType(OracleMemoryPoolDTO.OraclePoolType type) {
        switch (type) {
            case CODE_CACHE:
                return CodeCacheInfluxEntity.class;
            case NEW_GEN:
                return NewGenInfluxEntity.class;
            case SURVIVOR:
                return SurvivorInfluxEntity.class;
            case OLD_GEN:
                return OldGenInfluxEntity.class;
            case METASPACE:
                return MetaspaceInfluxEntity.class;
            case PERM_GEN:
                return PermGenInfluxEntity.class;
            default:
                // null
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }
}
