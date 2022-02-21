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

package com.huawei.sermant.metricserver.controller;

import com.huawei.sermant.metricserver.dto.connectionpool.DataSourceDTO;
import com.huawei.sermant.metricserver.service.ConnectionPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Connection Pool Controller
 */
@RestController
@RequestMapping("/connection-pool")
public class ConnectionPoolController {

    private final ConnectionPoolService connectionPoolService;

    @Autowired
    public ConnectionPoolController(ConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    /**
     * 查询指定时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param peer  database peer
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    @GetMapping("/data-sources/druid")
    public List<DataSourceDTO> getDataSources(
            @RequestParam String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) String peer) {
        if (StringUtils.hasText(peer)) {
            return connectionPoolService.getDataSourcesByPeer(start, end, peer);
        } else {
            return connectionPoolService.getDataSources(start, end);
        }
    }

    /**
     * 查询指定服务和时间段内的{@link DataSourceDTO}实体列表
     *
     * @param start   开始时间
     * @param end     结束时间
     * @param service 服务名
     * @return 满足条件的@link DataSourceDTO}实体列表
     */
    @GetMapping("/data-sources/druid/{service}")
    public List<DataSourceDTO> getDataSourceOfService(
            @RequestParam String start,
            @RequestParam(required = false) String end,
            @PathVariable("service") String service) {
        return connectionPoolService.getDataSourcesOfService(start, end, service);
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
    @GetMapping("/data-sources/druid/{service}/{instance}")
    public List<DataSourceDTO> getDataSourceOfServiceInstance(
            @RequestParam String start,
            @RequestParam(required = false) String end,
            @PathVariable("service") String service,
            @PathVariable("instance") String serviceInstance) {
        return connectionPoolService.getDataSourcesOfServiceInstance(start, end, service, serviceInstance);
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
    @GetMapping("/data-sources/druid/{service}/{instance}/{name}")
    public List<DataSourceDTO> getDataSourcesByNameInServiceInstance(
            @RequestParam String start,
            @RequestParam(required = false) String end,
            @PathVariable("service") String service,
            @PathVariable("instance") String serviceInstance,
            @PathVariable("name") String name) {
        return connectionPoolService.getDataSourcesByNameInServiceInstance(start, end, service, serviceInstance, name);
    }
}
