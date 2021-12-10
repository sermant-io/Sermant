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
import com.huawei.sermant.metricserver.dao.influxdb.entity.MemoryPoolInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.memorypool.CodeCacheInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.memorypool.MetaspaceInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.memorypool.NewGenInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.memorypool.OldGenInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.memorypool.PermGenInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.memorypool.SurvivorInfluxEntity;
import com.huawei.sermant.metricserver.dto.openjdk.OpenJdkMemoryPoolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenJdk Memory Pool服务
 */
@Service
public class OpenJdkMemoryPoolService extends InfluxService {

    @Autowired
    public OpenJdkMemoryPoolService(InfluxDao influxDao) {
        super(influxDao);
    }

    /**
     * 添加{@link OpenJdkMemoryPoolDTO}实体
     *
     * @param memoryPool 待添加的{@link OpenJdkMemoryPoolDTO}实体
     */
    public void addMemoryPoolMetric(OpenJdkMemoryPoolDTO memoryPool) {
        insert(() -> newEntityOfType(memoryPool.getType()), memoryPool);
    }

    /**
     * 查询{@link OpenJdkMemoryPoolDTO}实体列表
     *
     * @param type  Memory Pool类型
     * @param start 开始时间
     * @param end   结束时间
     * @return @link OracleMemoryPoolDTO}实体列表
     */
    public List<OpenJdkMemoryPoolDTO> getMemoryPools(OpenJdkMemoryPoolDTO.OraclePoolType type, String start, String end) {
        return query(start, end, getClassOfType(type)).stream()
            .map(entity -> OpenJdkMemoryPoolDTO.builder()
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

    private MemoryPoolInfluxEntity newEntityOfType(OpenJdkMemoryPoolDTO.OraclePoolType type) {
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

    private Class<? extends MemoryPoolInfluxEntity> getClassOfType(OpenJdkMemoryPoolDTO.OraclePoolType type) {
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
