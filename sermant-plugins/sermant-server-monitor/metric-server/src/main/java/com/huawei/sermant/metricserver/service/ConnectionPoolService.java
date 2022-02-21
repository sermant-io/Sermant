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
import com.huawei.sermant.metricserver.dao.influxdb.entity.connectionpool.DataSourceInfluxEntity;
import com.huawei.sermant.metricserver.dto.connectionpool.DataSourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Connection Pool服务
 */
@Service
public class ConnectionPoolService extends InfluxService {

    @Autowired
    public ConnectionPoolService(InfluxDao influxDao) {
        super(influxDao);
    }

    /**
     * 添加{@link DataSourceDTO}实体
     *
     * @param dataSource 待添加的{@link DataSourceDTO}实体
     */
    public void addDataSource(DataSourceDTO dataSource) {
        insert(DataSourceInfluxEntity::new, dataSource);
    }

    /**
     * 查询指定时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    public List<DataSourceDTO> getDataSources(String start, String end) {
        return query(start, end, DataSourceInfluxEntity.class)
                .stream().map(this::entity2DTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定服务和时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start   开始时间
     * @param end     结束时间
     * @param service 服务名
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    public List<DataSourceDTO> getDataSourcesOfService(String start, String end, String service) {
        return queryByService(start, end, service, DataSourceInfluxEntity.class)
                .stream().map(this::entity2DTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定服务、服务实例和时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start           开始时间
     * @param end             结束时间
     * @param service         服务名
     * @param serviceInstance 服务实例名
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    public List<DataSourceDTO> getDataSourcesOfServiceInstance(
            String start, String end, String service, String serviceInstance) {
        return queryByServiceInstance(start, end, service, serviceInstance, DataSourceInfluxEntity.class)
                .stream().map(this::entity2DTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定服务、服务实例、数据源名称和时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start           开始时间
     * @param end             结束时间
     * @param service         服务名
     * @param serviceInstance 服务实例名
     * @param name            数据源名称
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    public List<DataSourceDTO> getDataSourcesByNameInServiceInstance(
            String start, String end, String service, String serviceInstance, String name) {
        HashMap<String, String> tags = new HashMap<>();
        tags.put(TAG_SERVICE, service);
        tags.put(TAG_SERVICE_INSTANCE, serviceInstance);
        tags.put("name", name);
        return query(start, end, tags, DataSourceInfluxEntity.class)
                .stream().map(this::entity2DTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定数据库peer和时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param peer  database peer
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    public List<DataSourceDTO> getDataSourcesByPeer(String start, String end, String peer) {
        return query(start, end, Collections.singletonMap("database_peer", peer), DataSourceInfluxEntity.class)
                .stream().map(this::entity2DTO)
                .collect(Collectors.toList());
    }

    private DataSourceDTO entity2DTO(DataSourceInfluxEntity e) {
        return DataSourceDTO.builder()
                .service(e.getService())
                .serviceInstance(e.getServiceInstance())
                .time(e.getTime())
                .databasePeer(e.getDatabasePeer())
                .name(e.getName())
                .initialSize(e.getInitialSize())
                .activeCount(e.getActiveCount())
                .maxActive(e.getMaxActive())
                .poolingCount(e.getPoolingCount()).build();
    }
}
