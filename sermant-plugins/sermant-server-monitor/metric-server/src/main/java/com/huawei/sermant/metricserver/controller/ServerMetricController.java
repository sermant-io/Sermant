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

import com.huawei.sermant.metricserver.dto.servermonitor.CpuDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.DiskDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.MemoryDTO;
import com.huawei.sermant.metricserver.dto.servermonitor.NetworkDTO;
import com.huawei.sermant.metricserver.service.ServerMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Server monitor metric controller
 */
@RestController
@RequestMapping("/server-metrics")
public class ServerMetricController {

    private final ServerMetricService serverMetricService;

    @Autowired
    public ServerMetricController(ServerMetricService serverMetricService) {
        this.serverMetricService = serverMetricService;
    }

    @GetMapping("/cpu")
    public List<CpuDTO> getCpuMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return serverMetricService.getCpuMetrics(start, end);
    }

    @GetMapping("/memory")
    public List<MemoryDTO> getMemoryMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return serverMetricService.getMemoryMetrics(start, end);
    }

    @GetMapping("/network")
    public List<NetworkDTO> getNetworkMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return serverMetricService.getNetworkMetrics(start, end);
    }

    @GetMapping("/disk/read-rate")
    public List<DiskDTO> getDiskReadRateMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return serverMetricService.getDiskMetrics(DiskDTO.ValueType.READ_RATE, start, end);
    }

    @GetMapping("/disk/write-rate")
    public List<DiskDTO> getDiskWriteRateMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return serverMetricService.getDiskMetrics(DiskDTO.ValueType.WRITE_RATE, start, end);
    }

    @GetMapping("/disk/io-spent-percentage")
    public List<DiskDTO> getDiskIoSpentMetrics(
            @RequestParam String start,
            @RequestParam(required = false) String end) {
        return serverMetricService.getDiskMetrics(DiskDTO.ValueType.IO_SPENT_PERCENTAGE, start, end);
    }
}
