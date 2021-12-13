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
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.CSInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.MemoryPoolInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.JCCInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.JDCInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.MNHSInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.NAInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.NSInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.TLInfluxEntity;
import com.huawei.sermant.metricserver.dao.influxdb.entity.ibmpool.TSInfluxEntity;
import com.huawei.sermant.metricserver.dto.ibmpool.IbmMemoryPoolDTO;
import com.huawei.sermant.metricserver.dto.ibmpool.IbmPoolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IBM Pool服务
 */
@Service
public class IbmMemoryPoolService extends InfluxService {

    @Autowired
    public IbmMemoryPoolService(InfluxDao influxDao) {
        super(influxDao);
    }

    /**
     * 批量添加IBM Pool
     *
     * @param memoryPools 待添加{@link IbmMemoryPoolDTO}列表
     */
    public void batchAddMemoryPools(List<IbmMemoryPoolDTO> memoryPools) {
        for (IbmMemoryPoolDTO pool : memoryPools) {
            insert(() -> newEntityOfType(pool.getType()), pool);
        }
    }

    /**
     * 查询指定时间段内的{@link IbmMemoryPoolDTO}
     *
     * @param type  类型
     * @param start 开始时间
     * @param end   结束时间
     * @return {@link IbmMemoryPoolDTO}实体List
     */
    public List<IbmMemoryPoolDTO> getMemoryPools(IbmPoolType type, String start, String end) {
        return query(start, end, getClassOfType(type)).stream()
            .map(entity -> IbmMemoryPoolDTO.builder()
                .service(entity.getService())
                .serviceInstance(entity.getServiceInstance())
                .time(entity.getTime())
                .type(type)
                .typeDescription(type.getDescription())
                .init(entity.getInit())
                .max(entity.getMax())
                .used(entity.getUsed())
                .committed(entity.getCommitted()).build())
            .collect(Collectors.toList());
    }

    private MemoryPoolInfluxEntity newEntityOfType(IbmPoolType type) {
        switch (type) {
            case JCC:
                return new JCCInfluxEntity();
            case JDC:
                return new JDCInfluxEntity();
            case TS:
                return new TSInfluxEntity();
            case TL:
                return new TLInfluxEntity();
            case NA:
                return new NAInfluxEntity();
            case NS:
                return new NSInfluxEntity();
            case CS:
                return new CSInfluxEntity();
            case MNHS:
                return new MNHSInfluxEntity();
            default:
                // null
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }

    private Class<? extends MemoryPoolInfluxEntity> getClassOfType(IbmPoolType type) {
        switch (type) {
            case JCC:
                return JCCInfluxEntity.class;
            case JDC:
                return JDCInfluxEntity.class;
            case TS:
                return TSInfluxEntity.class;
            case TL:
                return TLInfluxEntity.class;
            case NA:
                return NAInfluxEntity.class;
            case NS:
                return NSInfluxEntity.class;
            case CS:
                return CSInfluxEntity.class;
            case MNHS:
                return MNHSInfluxEntity.class;
            default:
                // null
                throw new IllegalArgumentException("Illegal pool type.");
        }
    }
}
