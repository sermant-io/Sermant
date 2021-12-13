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
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.HeapMemoryInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.openjdk.NonHeapMemoryInfluxEntity;
import com.huawei.sermant.metricserver.dto.openjdk.OpenJdkMemoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenJdk Memory 服务
 */
@Service
public class OpenJdkMemoryService extends InfluxService {

    @Autowired
    public OpenJdkMemoryService(InfluxDao influxDao) {
        super(influxDao);
    }

    /**
     * 添加{@link OpenJdkMemoryDTO}实体
     *
     * @param memoryDto 待添加的{@link OpenJdkMemoryDTO}实体
     */
    public void addMemoryMetric(OpenJdkMemoryDTO memoryDto) {
        insert(() -> newEntityOfType(memoryDto.getType()), memoryDto);
    }

    /**
     * 查询{@link OpenJdkMemoryDTO}实体列表
     *
     * @param type  Memory类型
     * @param start 开始时间
     * @param end   结束时间
     * @return @link OracleMemoryDTO}实体列表
     */
    public List<OpenJdkMemoryDTO> getMemoryMetrics(OpenJdkMemoryDTO.OracleMemoryType type, String start, String end) {
        return query(start, end, getClassOfType(type)).stream()
            .map(entity -> OpenJdkMemoryDTO.builder()
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

    private MemoryPoolInfluxEntity newEntityOfType(OpenJdkMemoryDTO.OracleMemoryType type) {
        switch (type) {
            case HEAP:
                return new HeapMemoryInfluxEntity();
            case NON_HEAP:
                return new NonHeapMemoryInfluxEntity();
            default:
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }

    private Class<? extends MemoryPoolInfluxEntity> getClassOfType(OpenJdkMemoryDTO.OracleMemoryType type) {
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
